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
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskDispatchCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
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
public class TaskPriorityQueueConsumer extends BaseDaemonThread {

    /**
     * logger of TaskUpdateQueueConsumer
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskPriorityQueueConsumer.class);

    /**
     * taskUpdateQueue
     */
    @Autowired
    private TaskPriorityQueue<TaskPriority> taskPriorityQueue;

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
     * task dispatch failed queue
     */
    private final PriorityBlockingQueue<TaskPriority> taskDispatchFailedQueue = new PriorityBlockingQueue<>(1000);

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

    protected TaskPriorityQueueConsumer() {
        super("TaskPriorityQueueConsumeThread");
    }

    @PostConstruct
    public void init() {
        this.consumerThreadPoolExecutor = (ThreadPoolExecutor) ThreadUtils
                .newDaemonFixedThreadExecutor("TaskUpdateQueueConsumerThread", masterConfig.getDispatchTaskNumber());
        logger.info("Task priority queue consume thread staring");
        super.start();
        logger.info("Task priority queue consume thread started");
    }

    @Override
    public void run() {
        int fetchTaskNum = masterConfig.getDispatchTaskNumber();
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                List<TaskPriority> failedDispatchTasks = this.batchDispatch(fetchTaskNum);

                if (CollectionUtils.isNotEmpty(failedDispatchTasks)) {
                    TaskMetrics.incTaskDispatchFailed(failedDispatchTasks.size());
                    for (TaskPriority dispatchFailedTask : failedDispatchTasks) {
                        // put into failure queue after failure
                        taskDispatchFailedQueue.put(dispatchFailedTask);
                    }
                }
            } catch (Exception e) {
                TaskMetrics.incTaskDispatchError();
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

        if (!taskDispatchFailedQueue.isEmpty()) {
            consumerThreadPoolExecutor.submit(() -> {
                try {
                    dispatchFailedBackToTaskPriorityQueue(fetchTaskNum);
                } catch (Exception e) {
                    logger.warn("dispatch failed back to task priority queue error", e);
                }
            });
        }

        for (int i = 0; i < fetchTaskNum; i++) {
            TaskPriority taskPriority = taskPriorityQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS);
            if (Objects.isNull(taskPriority)) {
                latch.countDown();
                continue;
            }

            consumerThreadPoolExecutor.submit(() -> {
                try {
                    boolean dispatchResult = this.dispatchTask(taskPriority);
                    if (!dispatchResult) {
                        taskPriority.setLastDispatchTime(System.currentTimeMillis());
                        failedDispatchTasks.add(taskPriority);
                    }
                } finally {
                    // make sure the latch countDown
                    latch.countDown();
                }
            });
        }

        latch.await();

        return failedDispatchTasks;
    }

    /**
     * put the failed dispatch task into the dispatch queue again
     */
    private void dispatchFailedBackToTaskPriorityQueue(int fetchTaskNum) throws InterruptedException {
        for (int i = 0; i < fetchTaskNum; i++) {
            TaskPriority dispatchFailedTaskPriority = taskDispatchFailedQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS);
            if (Objects.isNull(dispatchFailedTaskPriority)){
                continue;
            }
            if (canRetry(dispatchFailedTaskPriority)){
                dispatchFailedTaskPriority.setDispatchFailedRetryTimes(dispatchFailedTaskPriority.getDispatchFailedRetryTimes() + 1);
                taskPriorityQueue.put(dispatchFailedTaskPriority);
            } else {
                taskDispatchFailedQueue.put(dispatchFailedTaskPriority);
            }
        }
    }

    /**
     * the time interval is adjusted according to the number of retries
     */
    private boolean canRetry (TaskPriority taskPriority){
        int dispatchFailedRetryTimes = taskPriority.getDispatchFailedRetryTimes();
        long now = System.currentTimeMillis();
        // retry more than 100 times with 100 seconds delay each time
        if (dispatchFailedRetryTimes >= Constants.DEFAULT_MAX_RETRY_COUNT){
            return now - taskPriority.getLastDispatchTime() >= TIME_DELAY[Constants.DEFAULT_MAX_RETRY_COUNT];
        }
        return now - taskPriority.getLastDispatchTime() >= TIME_DELAY[dispatchFailedRetryTimes];
    }

    /**
     * Dispatch task to worker.
     *
     * @param taskPriority taskPriority
     * @return dispatch result, return true if dispatch success, return false if dispatch failed.
     */
    protected boolean dispatchTask(TaskPriority taskPriority) {
        TaskMetrics.incTaskDispatch();
        boolean result = false;
        try {
            WorkflowExecuteRunnable workflowExecuteRunnable =
                    processInstanceExecCacheManager.getByProcessInstanceId(taskPriority.getProcessInstanceId());
            if (workflowExecuteRunnable == null) {
                logger.error("Cannot find the related processInstance of the task, taskPriority: {}", taskPriority);
                return true;
            }
            Optional<TaskInstance> taskInstanceOptional =
                    workflowExecuteRunnable.getTaskInstance(taskPriority.getTaskId());
            if (!taskInstanceOptional.isPresent()) {
                logger.error("Cannot find the task instance from related processInstance, taskPriority: {}",
                        taskPriority);
                // we return true, so that we will drop this task.
                return true;
            }
            TaskInstance taskInstance = taskInstanceOptional.get();
            TaskExecutionContext context = taskPriority.getTaskExecutionContext();
            ExecutionContext executionContext =
                    new ExecutionContext(toCommand(context), ExecutorType.WORKER, context.getWorkerGroup(),
                            taskInstance);

            if (isTaskNeedToCheck(taskPriority)) {
                if (taskInstanceIsFinalState(taskPriority.getTaskId())) {
                    // when task finish, ignore this task, there is no need to dispatch anymore
                    return true;
                }
            }

            result = dispatcher.dispatch(executionContext);

            if (result) {
                logger.info("Master success dispatch task to worker, taskInstanceId: {}, worker: {}",
                        taskPriority.getTaskId(),
                        executionContext.getHost());
                addDispatchEvent(context, executionContext);
            } else {
                logger.info("Master failed to dispatch task to worker, taskInstanceId: {}, worker: {}",
                        taskPriority.getTaskId(),
                        executionContext.getHost());
            }
        } catch (RuntimeException | ExecuteException e) {
            logger.error("Master dispatch task to worker error, taskPriority: {}", taskPriority, e);
        }
        return result;
    }

    /**
     * add dispatch event
     */
    private void addDispatchEvent(TaskExecutionContext context, ExecutionContext executionContext) {
        TaskEvent taskEvent = TaskEvent.newDispatchEvent(context.getProcessInstanceId(), context.getTaskInstanceId(),
                executionContext.getHost().getAddress());
        taskEventService.addEvent(taskEvent);
    }

    private Command toCommand(TaskExecutionContext taskExecutionContext) {
        // todo: we didn't set the host here, since right now we didn't need to retry this message.
        TaskDispatchCommand requestCommand = new TaskDispatchCommand(taskExecutionContext,
                masterConfig.getMasterAddress(),
                taskExecutionContext.getHost(),
                System.currentTimeMillis());
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
        return taskInstance.getState().isFinished();
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
