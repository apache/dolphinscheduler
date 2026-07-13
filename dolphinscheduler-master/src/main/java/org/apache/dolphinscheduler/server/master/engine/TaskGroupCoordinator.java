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

package org.apache.dolphinscheduler.server.master.engine;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupQueueDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.ITaskInstanceController;
import org.apache.dolphinscheduler.extract.master.transportor.TaskGroupSlotAcquireSuccessNotifyRequest;
import org.apache.dolphinscheduler.extract.master.transportor.TaskGroupSlotAcquireSuccessNotifyResponse;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.utils.TaskGroupUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;

@Slf4j
@Component
public class TaskGroupCoordinator implements ITaskGroupCoordinator, AutoCloseable {

    @Autowired
    private TaskGroupDao taskGroupDao;

    @Autowired
    private TaskGroupQueueDao taskGroupQueueDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private boolean flag = false;

    private Thread internalThread;

    private static final int DEFAULT_LIMIT = 1000;

    public synchronized void start() {
        log.info("TaskGroupCoordinator starting...");
        if (flag) {
            throw new IllegalStateException("TaskGroupCoordinator is already started");
        }
        if (internalThread != null) {
            throw new IllegalStateException("InternalThread is already started");
        }
        flag = true;
        internalThread = new BaseDaemonThread(this::doStart) {
        };
        internalThread.setName("TaskGroupCoordinator-Thread");
        internalThread.start();
        log.info("TaskGroupCoordinator started...");
    }

    @VisibleForTesting
    boolean isStarted() {
        return flag;
    }

    private void doStart() {
        // Sleep 1 minutes here to make sure the previous task group slot has been released.
        // This step is not necessary, since the wakeup operation is idempotent, but we can avoid confusion warning.
        ThreadUtils.sleep(TimeUnit.MINUTES.toMillis(1));

        while (flag) {
            try {
                final StopWatch taskGroupCoordinatorRoundCost = StopWatch.createStarted();

                amendTaskGroupUseSize();
                amendTaskGroupQueueStatus();
                dealWithForceStartTaskGroupQueue();
                dealWithWaitingTaskGroupQueue();

                taskGroupCoordinatorRoundCost.stop();
                log.debug("TaskGroupCoordinator round cost: {}/ms", taskGroupCoordinatorRoundCost.getTime());
            } catch (Throwable e) {
                log.error("TaskGroupCoordinator error", e);
            } finally {
                // sleep 5s
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS * 5);
            }
        }
    }

    /**
     * Make sure the TaskGroup useSize is equal to the TaskGroupQueue which status is {@link TaskGroupQueueStatus#ACQUIRE_SUCCESS} and forceStart is {@link org.apache.dolphinscheduler.common.enums.Flag#NO}.
     */
    private void amendTaskGroupUseSize() {
        // The TaskGroup useSize should equal to the TaskGroupQueue which inQueue is YES and forceStart is NO
        List<TaskGroup> taskGroups = taskGroupDao.queryAllTaskGroups();
        if (CollectionUtils.isEmpty(taskGroups)) {
            return;
        }
        StopWatch taskGroupCoordinatorRoundTimeCost = StopWatch.createStarted();

        for (TaskGroup taskGroup : taskGroups) {
            int actualUseSize = taskGroupQueueDao.countUsingTaskGroupQueueByGroupId(taskGroup.getId());
            if (taskGroup.getUseSize() == actualUseSize) {
                continue;
            }
            log.warn("The TaskGroup: {} useSize is {}, but the actual use size is {}, will amend it",
                    taskGroup.getName(),
                    taskGroup.getUseSize(), actualUseSize);
            taskGroup.setUseSize(actualUseSize);
            taskGroupDao.updateById(taskGroup);
        }
        log.info("Success amend TaskGroup useSize cost: {}/ms", taskGroupCoordinatorRoundTimeCost.getTime());
    }

    /**
     * Clear the TaskGroupQueue when the related {@link TaskInstance} is not exist or status is finished.
     */
    private void amendTaskGroupQueueStatus() {
        int minTaskGroupQueueId = -1;
        int limit = DEFAULT_LIMIT;
        StopWatch taskGroupCoordinatorRoundTimeCost = StopWatch.createStarted();
        while (true) {
            List<TaskGroupQueue> taskGroupQueues =
                    taskGroupQueueDao.queryInQueueTaskGroupQueue(minTaskGroupQueueId, limit);
            if (CollectionUtils.isEmpty(taskGroupQueues)) {
                break;
            }
            amendTaskGroupQueueStatus(taskGroupQueues);
            if (taskGroupQueues.size() < limit) {
                break;
            }
            minTaskGroupQueueId = taskGroupQueues.get(taskGroupQueues.size() - 1).getId();
        }
        log.debug("Success amend TaskGroupQueue status cost: {}/ms", taskGroupCoordinatorRoundTimeCost.getTime());
    }

    /**
     * Clear the TaskGroupQueue when the related {@link TaskInstance} is not exist or status is finished.
     */
    private void amendTaskGroupQueueStatus(List<TaskGroupQueue> taskGroupQueues) {
        final List<Integer> taskInstanceIds = taskGroupQueues.stream()
                .map(TaskGroupQueue::getTaskId)
                .collect(Collectors.toList());
        final Map<Integer, TaskInstance> taskInstanceMap = taskInstanceDao.queryByIds(taskInstanceIds)
                .stream()
                .collect(Collectors.toMap(TaskInstance::getId, Function.identity()));

        for (TaskGroupQueue taskGroupQueue : taskGroupQueues) {
            int taskId = taskGroupQueue.getTaskId();
            final TaskInstance taskInstance = taskInstanceMap.get(taskId);

            if (taskInstance == null) {
                log.warn("The TaskInstance: {} is not exist, will release the TaskGroupQueue: {}", taskId,
                        taskGroupQueue);
                deleteTaskGroupQueueSlot(taskGroupQueue);
                continue;
            }

            if (taskInstance.getState().isFinished()) {
                log.warn("The TaskInstance: {} state: {} finished, will release the TaskGroupQueue: {}",
                        taskInstance.getName(), taskInstance.getState(), taskGroupQueue);
                deleteTaskGroupQueueSlot(taskGroupQueue);
            }
        }
    }

    private void dealWithForceStartTaskGroupQueue() {
        // Find the force start task group queue(Which is inQueue and forceStart is YES)
        // Notify the related waiting task instance
        // Set the taskGroupQueue status to RELEASE and remove it from queue
        // We use limit here to avoid OOM, and we will retry to notify force start queue at next time
        int minTaskGroupQueueId = -1;
        int limit = DEFAULT_LIMIT;
        StopWatch taskGroupCoordinatorRoundTimeCost = StopWatch.createStarted();
        while (true) {
            final List<TaskGroupQueue> taskGroupQueues =
                    taskGroupQueueDao.queryWaitNotifyForceStartTaskGroupQueue(minTaskGroupQueueId, limit);
            if (CollectionUtils.isEmpty(taskGroupQueues)) {
                break;
            }
            dealWithForceStartTaskGroupQueue(taskGroupQueues);
            if (taskGroupQueues.size() < limit) {
                break;
            }
            minTaskGroupQueueId = taskGroupQueues.get(taskGroupQueues.size() - 1).getId();
        }
        log.debug("Success deal with force start TaskGroupQueue cost: {}/ms",
                taskGroupCoordinatorRoundTimeCost.getTime());
    }

    private void dealWithForceStartTaskGroupQueue(List<TaskGroupQueue> taskGroupQueues) {
        // Find the force start task group queue(Which is inQueue and forceStart is YES)
        // Notify the related waiting task instance
        // Set the taskGroupQueue status to RELEASE and remove it from queue
        for (final TaskGroupQueue taskGroupQueue : taskGroupQueues) {
            try {
                LogUtils.setTaskInstanceIdMDC(taskGroupQueue.getTaskId());
                if (!notifyForceStartTaskGroupQueue(taskGroupQueue)) {
                    log.debug("Skip stale ForceStart TaskGroupQueue: {}", taskGroupQueue.getId());
                    continue;
                }
                log.info("Notify the ForceStart waiting TaskInstance: {} for taskGroupQueue: {} success",
                        taskGroupQueue.getTaskName(),
                        taskGroupQueue.getId());
                log.info("Release the force start TaskGroupQueue {}", taskGroupQueue);
            } catch (RetryableTaskGroupNotificationException retryableException) {
                log.info("Notify the ForceStart TaskGroupQueue: {} is temporarily unavailable, will retry",
                        taskGroupQueue.getId(), retryableException);
            } catch (UnsupportedOperationException unsupportedOperationException) {
                deleteTaskGroupQueueSlot(taskGroupQueue);
                log.info(
                        "Notify the ForceStart TaskInstance: {} for taskGroupQueue: {} failed, will release the taskGroupQueue",
                        taskGroupQueue.getTaskName(), taskGroupQueue.getId(), unsupportedOperationException);
            } catch (Throwable throwable) {
                log.info("Notify the force start TaskGroupQueue {} failed", taskGroupQueue, throwable);
            } finally {
                LogUtils.removeTaskInstanceIdMDC();
            }
        }
    }

    private boolean notifyForceStartTaskGroupQueue(TaskGroupQueue taskGroupQueue) {
        // Keep the row lock through notification so cancellation and dispatch cannot both succeed.
        final Boolean notified = transactionTemplate.execute(transactionStatus -> {
            final TaskGroupQueue lockedTaskGroupQueue =
                    taskGroupQueueDao.queryByIdForUpdate(taskGroupQueue.getId());
            if (lockedTaskGroupQueue == null
                    || lockedTaskGroupQueue.getInQueue() != Flag.YES.getCode()
                    || lockedTaskGroupQueue.getForceStart() != Flag.YES.getCode()) {
                return false;
            }
            notifyWaitingTaskInstance(lockedTaskGroupQueue);
            if (!taskGroupQueueDao.deleteById(lockedTaskGroupQueue.getId())) {
                throw new IllegalStateException(
                        "Failed to delete ForceStart TaskGroupQueue: " + lockedTaskGroupQueue.getId());
            }
            return true;
        });
        if (notified == null) {
            throw new IllegalStateException("Notify ForceStart TaskGroupQueue transaction returned null");
        }
        return notified;
    }

    private void dealWithWaitingTaskGroupQueue() {
        // Find the TaskGroup which usage < maxSize.
        // Find the highest priority inQueue task group queue(Which is inQueue and status is Waiting and force start is
        // NO) belong to the
        // task group.
        List<TaskGroup> taskGroups = taskGroupDao.queryAvailableTaskGroups();
        if (CollectionUtils.isEmpty(taskGroups)) {
            log.debug("There is no available task group");
            return;
        }
        for (TaskGroup taskGroup : taskGroups) {
            int availableSize = taskGroup.getGroupSize() - taskGroup.getUseSize();
            if (availableSize <= 0) {
                log.info("TaskGroup {} is full, available size is {}", taskGroup, availableSize);
                continue;
            }
            List<TaskGroupQueue> taskGroupQueues =
                    taskGroupQueueDao.queryAllInQueueTaskGroupQueueByGroupId(taskGroup.getId())
                            .stream()
                            .filter(taskGroupQueue -> Flag.NO.getCode() == taskGroupQueue.getForceStart())
                            .filter(taskGroupQueue -> TaskGroupQueueStatus.WAIT_QUEUE == taskGroupQueue.getStatus())
                            .limit(availableSize)
                            .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(taskGroupQueues)) {
                log.debug("There is no waiting task group queue for task group {}", taskGroup.getName());
                continue;
            }
            for (TaskGroupQueue taskGroupQueue : taskGroupQueues) {
                try {
                    LogUtils.setTaskInstanceIdMDC(taskGroupQueue.getTaskId());
                    if (!acquireTaskGroupSlotAndNotify(taskGroupQueue)) {
                        log.debug("Skip stale or unavailable TaskGroupQueue: {} for TaskGroup: {}",
                                taskGroupQueue.getId(), taskGroup.getId());
                        continue;
                    }
                    log.info("Success acquire TaskGroupSlot for TaskGroupQueue: {}", taskGroupQueue);
                } catch (RetryableTaskGroupNotificationException retryableException) {
                    log.info("Notify Waiting TaskGroupQueue: {} is temporarily unavailable, will retry",
                            taskGroupQueue.getId(), retryableException);
                } catch (UnsupportedOperationException unsupportedOperationException) {
                    deleteTaskGroupQueueSlot(taskGroupQueue);
                    log.info(
                            "Notify the Waiting TaskInstance: {} for taskGroupQueue: {} failed, will release the taskGroupQueue",
                            taskGroupQueue.getTaskName(), taskGroupQueue.getId(), unsupportedOperationException);
                } catch (Throwable throwable) {
                    log.error("Notify Waiting TaskGroupQueue: {} failed", taskGroupQueue, throwable);
                } finally {
                    LogUtils.removeTaskInstanceIdMDC();
                }
            }
        }
    }

    private boolean acquireTaskGroupSlotAndNotify(TaskGroupQueue taskGroupQueue) {
        // The queue claim, slot acquisition and notification are one transaction. A notification failure rolls back
        // both database changes and leaves the queue available for pause/kill cancellation.
        final Boolean acquired = transactionTemplate.execute(transactionStatus -> {
            final TaskGroupQueue lockedTaskGroupQueue =
                    taskGroupQueueDao.queryByIdForUpdate(taskGroupQueue.getId());
            if (lockedTaskGroupQueue == null
                    || lockedTaskGroupQueue.getInQueue() != Flag.YES.getCode()
                    || lockedTaskGroupQueue.getForceStart() != Flag.NO.getCode()
                    || lockedTaskGroupQueue.getStatus() != TaskGroupQueueStatus.WAIT_QUEUE) {
                return false;
            }
            if (!taskGroupDao.acquireTaskGroupSlot(lockedTaskGroupQueue.getGroupId())) {
                return false;
            }
            notifyWaitingTaskInstance(lockedTaskGroupQueue);
            lockedTaskGroupQueue.setStatus(TaskGroupQueueStatus.ACQUIRE_SUCCESS);
            lockedTaskGroupQueue.setUpdateTime(new Date());
            if (!taskGroupQueueDao.updateById(lockedTaskGroupQueue)) {
                throw new IllegalStateException(
                        "Failed to mark TaskGroupQueue as acquired: " + lockedTaskGroupQueue.getId());
            }
            return true;
        });
        if (acquired == null) {
            throw new IllegalStateException("Acquire TaskGroupSlot transaction returned null");
        }
        return acquired;
    }

    @Override
    public boolean needAcquireTaskGroupSlot(final TaskInstance taskInstance) {
        if (taskInstance == null) {
            throw new IllegalArgumentException("The TaskInstance is null");
        }
        if (!TaskGroupUtils.isUsingTaskGroup(taskInstance)) {
            log.debug("The current TaskInstance doesn't use TaskGroup, no need to acquire TaskGroupSlot");
            return false;
        }
        TaskGroup taskGroup = taskGroupDao.queryById(taskInstance.getTaskGroupId());
        if (taskGroup == null) {
            log.warn("The current TaskGroup: {} does not exist, will not acquire TaskGroupSlot",
                    taskInstance.getTaskGroupId());
            return false;
        }
        return Flag.YES.equals(taskGroup.getStatus());
    }

    @Override
    public void acquireTaskGroupSlot(TaskInstance taskInstance, TaskDefinition taskDefinition) {
        if (taskInstance == null || taskInstance.getTaskGroupId() <= 0) {
            throw new IllegalArgumentException("The current TaskInstance does not use task group");
        }
        TaskGroup taskGroup = taskGroupDao.queryById(taskInstance.getTaskGroupId());
        if (taskGroup == null) {
            throw new IllegalArgumentException(
                    "The current TaskGroup: " + taskInstance.getTaskGroupId() + " does not exist");
        }
        // Write TaskGroupQueue in db, and then return wait TaskGroupCoordinator to notify it
        // Set the taskGroupQueue status to WAIT_QUEUE and add to queue
        // The queue only contains the taskGroupQueue which status is WAIT_QUEUE or ACQUIRE_SUCCESS
        Date now = new Date();
        TaskGroupQueue taskGroupQueue = TaskGroupQueue
                .builder()
                .taskId(taskInstance.getId())
                .taskName(taskInstance.getName())
                .groupId(taskInstance.getTaskGroupId())
                .workflowInstanceId(taskInstance.getWorkflowInstanceId())
                .priority(taskDefinition.getTaskGroupPriority())
                .inQueue(Flag.YES.getCode())
                .forceStart(Flag.NO.getCode())
                .status(TaskGroupQueueStatus.WAIT_QUEUE)
                .createTime(now)
                .updateTime(now)
                .build();
        log.info("Success insert TaskGroupQueue: {} for TaskInstance: {}", taskGroupQueue, taskInstance.getName());
        taskGroupQueueDao.insert(taskGroupQueue);
    }

    @Override
    public boolean needToReleaseTaskGroupSlot(TaskInstance taskInstance) {
        if (taskInstance == null) {
            throw new IllegalArgumentException("The TaskInstance is null");
        }
        if (taskInstance.getTaskGroupId() <= 0) {
            log.debug("The current TaskInstance doesn't use TaskGroup, no need to release TaskGroupSlot");
            return false;
        }
        return true;
    }

    @Override
    public void releaseTaskGroupSlot(TaskInstance taskInstance) {
        if (taskInstance == null) {
            throw new IllegalArgumentException("The TaskInstance is null");
        }
        if (taskInstance.getTaskGroupId() <= 0) {
            log.warn("The task: {} is no need to release TaskGroupSlot", taskInstance.getName());
            return;
        }
        List<TaskGroupQueue> taskGroupQueues = taskGroupQueueDao.queryByTaskInstanceId(taskInstance.getId());
        for (TaskGroupQueue taskGroupQueue : taskGroupQueues) {
            deleteTaskGroupQueueSlot(taskGroupQueue);
        }
    }

    @Override
    public boolean removeTaskFromWaitingTaskGroupQueue(TaskInstance taskInstance) {
        if (taskInstance == null) {
            throw new IllegalArgumentException("The TaskInstance is null");
        }
        if (taskInstance.getId() == null) {
            throw new IllegalArgumentException("The TaskInstance id is null");
        }
        if (!TaskGroupUtils.isUsingTaskGroup(taskInstance)) {
            return false;
        }
        final boolean removed = taskGroupQueueDao.deleteByTaskInstanceIdAndStatus(
                taskInstance.getId(), TaskGroupQueueStatus.WAIT_QUEUE);
        if (removed) {
            log.info("Removed TaskInstance: {} from waiting TaskGroupQueue, taskGroupId: {}",
                    taskInstance.getId(), taskInstance.getTaskGroupId());
        }
        return removed;
    }

    private void notifyWaitingTaskInstance(TaskGroupQueue taskGroupQueue) {
        // Find the related waiting task instance
        // send RPC to notify the waiting task instance
        TaskInstance taskInstance = taskInstanceDao.queryById(taskGroupQueue.getTaskId());
        if (taskInstance == null) {
            throw new UnsupportedOperationException(
                    "The TaskInstance: " + taskGroupQueue.getTaskId() + " is not exist, no need to notify");
        }
        // todo: We may need to add a new status to represent the task instance is waiting for task group slot
        if (taskInstance.getState() != TaskExecutionStatus.SUBMITTED_SUCCESS) {
            throw new UnsupportedOperationException(
                    "The TaskInstance: " + taskInstance.getId() + " state is " + taskInstance.getState()
                            + ", no need to notify");
        }
        WorkflowInstance workflowInstance = workflowInstanceDao.queryById(taskInstance.getWorkflowInstanceId());
        if (workflowInstance == null) {
            throw new UnsupportedOperationException(
                    "The WorkflowInstance: " + taskInstance.getWorkflowInstanceId()
                            + " is not exist, no need to notify");
        }
        if (workflowInstance.getState() != WorkflowExecutionStatus.RUNNING_EXECUTION) {
            if (workflowInstance.getState() == WorkflowExecutionStatus.READY_PAUSE
                    || workflowInstance.getState() == WorkflowExecutionStatus.READY_STOP) {
                throw new RetryableTaskGroupNotificationException(
                        "The WorkflowInstance: " + workflowInstance.getId() + " state is "
                                + workflowInstance.getState());
            }
            throw new UnsupportedOperationException(
                    "The WorkflowInstance: " + workflowInstance.getId() + " state is " + workflowInstance.getState()
                            + ", no need to notify");
        }
        if (workflowInstance.getHost() == null || Constants.NULL.equals(workflowInstance.getHost())) {
            throw new RetryableTaskGroupNotificationException(
                    "WorkflowInstance host is null, maybe it is in failover: " + workflowInstance);
        }

        TaskGroupSlotAcquireSuccessNotifyRequest taskGroupSlotAcquireSuccessNotifyRequest =
                TaskGroupSlotAcquireSuccessNotifyRequest.builder()
                        .workflowInstanceId(workflowInstance.getId())
                        .taskInstanceId(taskInstance.getId())
                        .build();

        TaskGroupSlotAcquireSuccessNotifyResponse taskGroupSlotAcquireSuccessNotifyResponse =
                Clients
                        .withService(ITaskInstanceController.class)
                        .withHost(workflowInstance.getHost())
                        .notifyTaskGroupSlotAcquireSuccess(taskGroupSlotAcquireSuccessNotifyRequest);
        if (!taskGroupSlotAcquireSuccessNotifyResponse.isSuccess()) {
            throw new RetryableTaskGroupNotificationException(
                    "Notify TaskInstance: " + taskInstance.getId() + " failed: "
                            + taskGroupSlotAcquireSuccessNotifyResponse);
        }
        log.info("Wake up TaskInstance: {} success", taskInstance.getName());
    }

    private static final class RetryableTaskGroupNotificationException extends RuntimeException {

        private RetryableTaskGroupNotificationException(String message) {
            super(message);
        }
    }

    private void deleteTaskGroupQueueSlot(TaskGroupQueue taskGroupQueue) {
        if (taskGroupQueueDao.deleteById(taskGroupQueue)) {
            log.info("Success release TaskGroupQueue: {}", taskGroupQueue);
            return;
        }
        log.debug("TaskGroupQueue has already been released: {}", taskGroupQueue.getId());
    }

    @Override
    public synchronized void close() {
        if (!flag) {
            log.warn("TaskGroupCoordinator is already closed");
            return;
        }
        flag = false;
        try {
            if (internalThread != null) {
                internalThread.interrupt();
            }
        } catch (Exception ex) {
            log.error("Close internalThread failed", ex);
        }
        internalThread = null;
        log.info("TaskGroupCoordinator closed");
    }
}
