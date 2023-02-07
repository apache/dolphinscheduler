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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.utils.TaskCacheUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskDispatchCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.WorkerGroupNotFoundException;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TaskUpdateQueue consumer
 */
@Component
@Slf4j
public class TaskPriorityQueueConsumer extends BaseDaemonThread {

    /**
     * taskUpdateQueue
     */
    @Autowired
    private TaskPriorityQueue<TaskPriority> taskPriorityQueue;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

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
     * storage operator
     */
    @Autowired(required = false)
    private StorageOperate storageOperate;

    /**
     * consumer thread pool
     */
    private ThreadPoolExecutor consumerThreadPoolExecutor;

    protected TaskPriorityQueueConsumer() {
        super("TaskPriorityQueueConsumeThread");
    }

    @PostConstruct
    public void init() {
        this.consumerThreadPoolExecutor = (ThreadPoolExecutor) ThreadUtils
                .newDaemonFixedThreadExecutor("TaskUpdateQueueConsumerThread", masterConfig.getDispatchTaskNumber());
        log.info("Task priority queue consume thread staring");
        super.start();
        log.info("Task priority queue consume thread started");
    }

    @Override
    public void run() {
        int fetchTaskNum = masterConfig.getDispatchTaskNumber();
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                List<TaskPriority> failedDispatchTasks = this.batchDispatch(fetchTaskNum);

                if (CollectionUtils.isNotEmpty(failedDispatchTasks)) {
                    log.info("{} tasks dispatch failed, will retry to dispatch", failedDispatchTasks.size());
                    TaskMetrics.incTaskDispatchFailed(failedDispatchTasks.size());
                    for (TaskPriority dispatchFailedTask : failedDispatchTasks) {
                        taskPriorityQueue.put(dispatchFailedTask);
                    }
                    // If the all task dispatch failed, will sleep for 1s to avoid the master cpu higher.
                    if (fetchTaskNum == failedDispatchTasks.size()) {
                        log.info("All tasks dispatch failed, will sleep a while to avoid the master cpu higher");
                        TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS);
                    }
                }
            } catch (Exception e) {
                TaskMetrics.incTaskDispatchError();
                log.error("dispatcher task error", e);
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
                    try {
                        this.dispatchTask(taskPriority);
                    } catch (WorkerGroupNotFoundException e) {
                        // If the worker group not found, will not try to dispatch again.
                        // The task instance will be failed
                        // todo:
                        addDispatchFailedEvent(taskPriority);
                    } catch (ExecuteException e) {
                        failedDispatchTasks.add(taskPriority);
                    } catch (Exception e) {
                        log.error("Dispatch task error, meet an unknown exception", e);
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
    protected void dispatchTask(TaskPriority taskPriority) throws ExecuteException {
        TaskMetrics.incTaskDispatch();
        WorkflowExecuteRunnable workflowExecuteRunnable =
                processInstanceExecCacheManager.getByProcessInstanceId(taskPriority.getProcessInstanceId());
        if (workflowExecuteRunnable == null) {
            log.error("Cannot find the related processInstance of the task, taskPriority: {}", taskPriority);
            return;
        }
        Optional<TaskInstance> taskInstanceOptional =
                workflowExecuteRunnable.getTaskInstance(taskPriority.getTaskId());
        if (!taskInstanceOptional.isPresent()) {
            log.error("Cannot find the task instance from related processInstance, taskPriority: {}",
                    taskPriority);
            // we return true, so that we will drop this task.
            return;
        }
        TaskInstance taskInstance = taskInstanceOptional.get();
        TaskExecutionContext context = taskPriority.getTaskExecutionContext();
        ExecutionContext executionContext = ExecutionContext.builder()
                .taskInstance(taskInstance)
                .workerGroup(context.getWorkerGroup())
                .executorType(ExecutorType.WORKER)
                .command(toCommand(context))
                .build();

        if (isTaskNeedToCheck(taskPriority)) {
            if (taskInstanceIsFinalState(taskPriority.getTaskId())) {
                // when task finish, ignore this task, there is no need to dispatch anymore
                log.info("Task {} is already finished, no need to dispatch, task instance id: {}",
                        taskInstance.getName(), taskInstance.getId());
                return;
            }
        }

        // check task is cache execution, and decide whether to dispatch
        if (checkIsCacheExecution(taskInstance, context)) {
            return;
        }

        dispatcher.dispatch(executionContext);
        log.info("Master success dispatch task to worker, taskInstanceId: {}, worker: {}",
                taskPriority.getTaskId(),
                executionContext.getHost());
        addDispatchEvent(context, executionContext);
    }

    /**
     * add dispatch event
     */
    private void addDispatchEvent(TaskExecutionContext context, ExecutionContext executionContext) {
        TaskEvent taskEvent = TaskEvent.newDispatchEvent(context.getProcessInstanceId(), context.getTaskInstanceId(),
                executionContext.getHost().getAddress());
        taskEventService.addEvent(taskEvent);
    }

    private void addDispatchFailedEvent(TaskPriority taskPriority) {
        TaskExecutionContext taskExecutionContext = taskPriority.getTaskExecutionContext();
        TaskEvent taskEvent = TaskEvent.builder()
                .processInstanceId(taskPriority.getProcessInstanceId())
                .taskInstanceId(taskPriority.getTaskId())
                .state(TaskExecutionStatus.FAILURE)
                .logPath(taskExecutionContext.getLogPath())
                .executePath(taskExecutionContext.getExecutePath())
                .appIds(taskExecutionContext.getAppIds())
                .processId(taskExecutionContext.getProcessId())
                .varPool(taskExecutionContext.getVarPool())
                .startTime(DateUtils.timeStampToDate(taskExecutionContext.getStartTime()))
                .endTime(new Date())
                .event(TaskEventType.RESULT)
                .build();
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
        TaskInstance taskInstance = taskInstanceDao.findTaskInstanceById(taskInstanceId);
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

    /**
     * check if task is cache execution
     * if the task is defined as cache execution, and we find the cache task instance is finished yet, we will not dispatch this task
     * @param taskInstance taskInstance
     * @param context context
     * @return true if we will not dispatch this task, false if we will dispatch this task
     */
    private boolean checkIsCacheExecution(TaskInstance taskInstance, TaskExecutionContext context) {
        try {
            // check if task is defined as a cache task
            if (taskInstance.getIsCache().equals(Flag.NO)) {
                return false;
            }
            // check if task is cache execution
            String cacheKey = TaskCacheUtils.generateCacheKey(taskInstance, context, storageOperate);
            TaskInstance cacheTaskInstance = taskInstanceDao.findTaskInstanceByCacheKey(cacheKey);
            // if we can find the cache task instance, we will add cache event, and return true.
            if (cacheTaskInstance != null) {
                log.info("Task {} is cache, no need to dispatch, task instance id: {}",
                        taskInstance.getName(), taskInstance.getId());
                addCacheEvent(taskInstance, cacheTaskInstance);
                taskInstance.setCacheKey(TaskCacheUtils.generateTagCacheKey(cacheTaskInstance.getId(), cacheKey));
                return true;
            } else {
                // if we can not find cache task, update cache key, and return false. the task will be dispatched
                taskInstance.setCacheKey(TaskCacheUtils.generateTagCacheKey(taskInstance.getId(), cacheKey));
            }
        } catch (Exception e) {
            log.error("checkIsCacheExecution error", e);
        }
        return false;
    }

    private void addCacheEvent(TaskInstance taskInstance, TaskInstance cacheTaskInstance) {
        if (cacheTaskInstance == null) {
            return;
        }
        TaskEvent taskEvent = TaskEvent.newCacheEvent(taskInstance.getProcessInstanceId(), taskInstance.getId(),
                cacheTaskInstance.getId());
        taskEventService.addEvent(taskEvent);
    }
}
