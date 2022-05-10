/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.consumer;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TaskUpdateQueue consumer
 */
@Component
public class TaskPriorityQueueConsumer extends Thread {

    /**
     * logger of TaskUpdateQueueConsumer
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskPriorityQueueConsumer.class);

    /**
     * taskUpdateQueue
     */
    @Autowired
    private TaskPriorityQueue<TaskPriority> taskPriorityQueue;

    @Autowired
    private TaskPriorityQueue<TaskPriority> taskPriorityDispatchFailedQueue;

    /**
     * processService
     */
    @Autowired
    private ProcessService processService;

    /**
     * executor dispatcher
     */
    @Autowired
    private ExecutorDispatcher dispatcher;

    /**
     * processInstance cache manager
     */
    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    /**
     * master config
     */
    @Autowired
    private MasterConfig masterConfig;

    /**
     * task response service
     */
    @Autowired
    private TaskEventService taskEventService;

    /**
     * consumer thread pool
     */
    private ThreadPoolExecutor consumerThreadPoolExecutor;

    /**
     * delay time for retries
     */
    private static final Long[] TIME_DELAY;

    /**
     * initialization failure retry delay rule
     */
    static {
        TIME_DELAY = new Long[Constants.DEFAULT_MAX_RETRY_COUNT];
        for (int i = 0; i < Constants.DEFAULT_MAX_RETRY_COUNT; i++) {
            int delayTime = (i + 1) * 1000;
            TIME_DELAY[i] = (long) delayTime;
        }
    }

    @PostConstruct
    public void init() {
        this.consumerThreadPoolExecutor = (ThreadPoolExecutor) ThreadUtils.newDaemonFixedThreadExecutor("TaskUpdateQueueConsumerThread", masterConfig.getDispatchTaskNumber());
        super.start();
    }

    @Override
    public void run() {
        int fetchTaskNum = masterConfig.getDispatchTaskNumber();
        while (Stopper.isRunning()) {
            try {
                List<TaskPriority> failedDispatchTasks = this.batchDispatch(fetchTaskNum);

                if (!failedDispatchTasks.isEmpty()) {
                    for (TaskPriority dispatchFailedTask : failedDispatchTasks) {
                        if (dispatchFailedTask.getDispatchFailedRetryTimes() >= Constants.DEFAULT_MAX_RETRY_COUNT){
                            logger.error("the number of retries for dispatch failure has exceeded the maximum limit, taskId: {} processInstanceId: {}", dispatchFailedTask.getTaskId(), dispatchFailedTask.getProcessInstanceId());
                            // business alarm
                            continue;
                        }
                        // differentiate the queue to prevent high priority from affecting the execution of other tasks
                        taskPriorityDispatchFailedQueue.put(dispatchFailedTask);
                    }
                }
            } catch (Exception e) {
                logger.error("dispatcher task error", e);
            }
        }
    }

    /**
     * batch dispatch with thread pool
     */
    public List<TaskPriority> batchDispatch(int fetchTaskNum) throws TaskPriorityQueueException, InterruptedException {
        List<TaskPriority> failedDispatchTasks = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(fetchTaskNum);

        // put the failed dispatch task into the dispatch queue again
        for (int i = 0; i < fetchTaskNum; i++) {
            TaskPriority dispatchFailedTaskPriority = taskPriorityDispatchFailedQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS);
            if (Objects.isNull(dispatchFailedTaskPriority)){
                continue;
            }
            if (canRetry(dispatchFailedTaskPriority)){
                dispatchFailedTaskPriority.setDispatchFailedRetryTimes(dispatchFailedTaskPriority.getDispatchFailedRetryTimes() + 1);
                taskPriorityQueue.put(dispatchFailedTaskPriority);
            } else {
                taskPriorityDispatchFailedQueue.put(dispatchFailedTaskPriority);
            }
        }

        for (int i = 0; i < fetchTaskNum; i++) {
            TaskPriority taskPriority = taskPriorityQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS);
            if (Objects.isNull(taskPriority)) {
                latch.countDown();
                continue;
            }

            consumerThreadPoolExecutor.submit(() -> {
                boolean dispatchResult = this.dispatchTask(taskPriority);
                if (!dispatchResult) {
                    taskPriority.setLastDispatchTime(System.currentTimeMillis());
                    failedDispatchTasks.add(taskPriority);
                }
                latch.countDown();
            });
        }

        latch.await();

        return failedDispatchTasks;
    }

    /**
     * the time interval is adjusted according to the number of retries
     * @param taskPriority
     * @return
     */
    private boolean canRetry (TaskPriority taskPriority){
        int dispatchFailedRetryTimes = taskPriority.getDispatchFailedRetryTimes();
        long now = System.currentTimeMillis();
        return now - taskPriority.getLastDispatchTime() >= TIME_DELAY[dispatchFailedRetryTimes];
    }

    /**
     * dispatch task
     *
     * @param taskPriority taskPriority
     * @return result
     */
    protected boolean dispatchTask(TaskPriority taskPriority) {
        boolean result = false;
        try {
            TaskExecutionContext context = taskPriority.getTaskExecutionContext();
            ExecutionContext executionContext = new ExecutionContext(toCommand(context), ExecutorType.WORKER, context.getWorkerGroup());

            if (isTaskNeedToCheck(taskPriority)) {
                if (taskInstanceIsFinalState(taskPriority.getTaskId())) {
                    // when task finish, ignore this task, there is no need to dispatch anymore
                    return true;
                }
            }

            result = dispatcher.dispatch(executionContext);

            if (result) {
                addDispatchEvent(context, executionContext);
            } else {
                dispatchFailedTaskInstanceState2Pending(context, executionContext);
            }
        } catch (RuntimeException e) {
            logger.error("dispatch error: ", e);
        } catch (ExecuteException e) {
            logger.error("dispatch error: {}", e.getMessage());
        }
        return result;
    }

    /**
     * add dispatch event
     */
    private void addDispatchEvent(TaskExecutionContext context, ExecutionContext executionContext) {
        TaskEvent taskEvent = TaskEvent.newDispatchEvent(context.getProcessInstanceId(), context.getTaskInstanceId(), executionContext.getHost().getAddress());
        taskEventService.addEvent(taskEvent);
    }

    /**
     * dispatch failed task instance state to pending
     */
    public void dispatchFailedTaskInstanceState2Pending(TaskExecutionContext context, ExecutionContext executionContext) {
        int taskInstanceId = context.getTaskInstanceId();
        TaskInstance taskInstance = processService.findTaskInstanceById(taskInstanceId);
        if (taskInstance == null) {
            logger.error("taskInstance is null");
            return;
        }
        if (taskInstance.getState() != ExecutionStatus.PENDING && taskInstance.getState() != ExecutionStatus.SUBMITTED_SUCCESS){
            logger.error("taskInstance status illegal");
            return;
        }
        if (taskInstance.getState() == ExecutionStatus.SUBMITTED_SUCCESS){
            String firstDispatchFailedErrorLog = String.format("fail to execute : %s due to no suitable worker, current task needs worker group %s to execute", executionContext.getCommand(), executionContext.getWorkerGroup());
            logger.error(firstDispatchFailedErrorLog);
        }
        taskInstance.setState(ExecutionStatus.PENDING);
        if (executionContext.getHost() != null){
            taskInstance.setHost(executionContext.getHost().getAddress());
        }
        processService.saveTaskInstance(taskInstance);
    }

    private Command toCommand(TaskExecutionContext taskExecutionContext) {
        TaskExecuteRequestCommand requestCommand = new TaskExecuteRequestCommand();
        requestCommand.setTaskExecutionContext(JSONUtils.toJsonString(taskExecutionContext));
        return requestCommand.convert2Command();
    }

    /**
     * taskInstance is final state
     * success，failure，kill，stop，pause，threadwaiting is final state
     *
     * @param taskInstanceId taskInstanceId
     * @return taskInstance is final state
     */
    public boolean taskInstanceIsFinalState(int taskInstanceId) {
        TaskInstance taskInstance = processService.findTaskInstanceById(taskInstanceId);
        return taskInstance.getState().typeIsFinished();
    }

    /**
     * check if task need to check state, if true, refresh the checkpoint
     */
    private boolean isTaskNeedToCheck(TaskPriority taskPriority) {
        long now = System.currentTimeMillis();
        if (now - taskPriority.getCheckpoint() > Constants.SECOND_TIME_MILLIS) {
            taskPriority.setCheckpoint(now);
            return true;
        }
        return false;
    }
}
