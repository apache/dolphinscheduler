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
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
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

    protected TaskPriorityQueueConsumer() {
        super("TaskPriorityQueueConsumeThread");
    }

    @PostConstruct
    public void init() {
        this.consumerThreadPoolExecutor = (ThreadPoolExecutor) ThreadUtils.newDaemonFixedThreadExecutor("TaskUpdateQueueConsumerThread", masterConfig.getDispatchTaskNumber());
        logger.info("Task priority queue consume thread staring");
        super.start();
        logger.info("Task priority queue consume thread started");
    }

    @Override
    public void run() {
        int fetchTaskNum = masterConfig.getDispatchTaskNumber();
        while (Stopper.isRunning()) {
            try {
                List<TaskPriority> failedDispatchTasks = this.batchDispatch(fetchTaskNum);

                if (CollectionUtils.isNotEmpty(failedDispatchTasks)) {
                    TaskMetrics.incTaskDispatchFailed(failedDispatchTasks.size());
                    for (TaskPriority dispatchFailedTask : failedDispatchTasks) {
                        taskPriorityQueue.put(dispatchFailedTask);
                    }
                    // If the all task dispatch failed, will sleep for 1s to avoid the master cpu higher.
                    if (fetchTaskNum == failedDispatchTasks.size()) {
                        TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS);
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
                new ExecutionContext(toCommand(context), ExecutorType.WORKER, context.getWorkerGroup(), taskInstance);

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
        TaskEvent taskEvent = TaskEvent.newDispatchEvent(context.getProcessInstanceId(), context.getTaskInstanceId(), executionContext.getHost().getAddress());
        taskEventService.addEvent(taskEvent);
    }

    private Command toCommand(TaskExecutionContext taskExecutionContext) {
        TaskExecuteRequestCommand requestCommand = new TaskExecuteRequestCommand(taskExecutionContext);
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
