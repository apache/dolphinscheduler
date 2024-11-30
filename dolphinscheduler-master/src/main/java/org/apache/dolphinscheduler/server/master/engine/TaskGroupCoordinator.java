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
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.utils.TaskGroupUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The TaskGroupCoordinator use to manage the task group slot. The task group slot is used to limit the number of {@link TaskInstance} that can be run at the same time.
 * <p>
 * The {@link TaskGroupQueue} is used to represent the task group slot. When a {@link TaskGroupQueue} which inQueue is YES means the {@link TaskGroupQueue} is using by a {@link TaskInstance}.
 * <p>
 * When the {@link TaskInstance} need to use task group, we should use @{@link TaskGroupCoordinator#acquireTaskGroupSlot(TaskInstance)} to acquire the task group slot,
 * this method doesn't block should always acquire successfully, and you should directly stop dispatch the task instance.
 * When the task group slot is available, the TaskGroupCoordinator will wake up the waiting {@link TaskInstance} to dispatch.
 * <pre>
 *     if(needAcquireTaskGroupSlot(taskInstance)) {
 *         taskGroupCoordinator.acquireTaskGroupSlot(taskInstance);
 *         return;
 *     }
 * </pre>
 * <p>
 * When the {@link TaskInstance} is finished, we should use @{@link TaskGroupCoordinator#releaseTaskGroupSlot(TaskInstance)} to release the task group slot.
 * <pre>
 *     if(needToReleaseTaskGroupSlot(taskInstance)) {
 *         taskGroupCoordinator.releaseTaskGroupSlot(taskInstance);
 *     }
 * </pre>
 */
@Slf4j
@Component
public class TaskGroupCoordinator extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private TaskGroupDao taskGroupDao;

    @Autowired
    private TaskGroupQueueDao taskGroupQueueDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    private boolean flag = true;

    private static final int DEFAULT_LIMIT = 1000;

    public TaskGroupCoordinator() {
        super("TaskGroupCoordinator");
    }

    @Override
    public synchronized void start() {
        log.info("TaskGroupCoordinator starting...");
        flag = true;
        super.start();
        log.info("TaskGroupCoordinator started...");
    }

    @Override
    public void run() {
        while (flag) {
            try {
                registryClient.getLock(RegistryNodeType.MASTER_TASK_GROUP_COORDINATOR_LOCK.getRegistryPath());
                try {
                    StopWatch taskGroupCoordinatorRoundCost = StopWatch.createStarted();

                    amendTaskGroupUseSize();
                    amendTaskGroupQueueStatus();
                    dealWithForceStartTaskGroupQueue();
                    dealWithWaitingTaskGroupQueue();

                    taskGroupCoordinatorRoundCost.stop();
                    log.debug("TaskGroupCoordinator round cost: {}/ms", taskGroupCoordinatorRoundCost.getTime());
                } finally {
                    registryClient.releaseLock(RegistryNodeType.MASTER_TASK_GROUP_COORDINATOR_LOCK.getRegistryPath());
                }
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
                continue;
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
            List<TaskGroupQueue> taskGroupQueues =
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
        for (TaskGroupQueue taskGroupQueue : taskGroupQueues) {
            try {
                LogUtils.setTaskInstanceIdMDC(taskGroupQueue.getTaskId());
                // notify the waiting task instance
                // We notify first, it notify failed, the taskGroupQueue will be in queue, and then we will retry it
                // next time.
                notifyWaitingTaskInstance(taskGroupQueue);
                log.info("Notify the ForceStart waiting TaskInstance: {} for taskGroupQueue: {} success",
                        taskGroupQueue.getTaskName(),
                        taskGroupQueue.getId());

                deleteTaskGroupQueueSlot(taskGroupQueue);
                log.info("Release the force start TaskGroupQueue {}", taskGroupQueue);
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
                    // Reduce the taskGroupSize
                    boolean acquireResult = taskGroupDao.acquireTaskGroupSlot(taskGroup.getId());
                    if (!acquireResult) {
                        log.error("Failed to acquire task group slot for task group {}", taskGroup);
                        continue;
                    }
                    // Notify the waiting task instance
                    // We notify first, it notify failed, the taskGroupQueue will be in queue, and then we will retry it
                    // next time.
                    notifyWaitingTaskInstance(taskGroupQueue);

                    // Set the taskGroupQueue status to RUNNING and remove from queue
                    taskGroupQueue.setInQueue(Flag.YES.getCode());
                    taskGroupQueue.setStatus(TaskGroupQueueStatus.ACQUIRE_SUCCESS);
                    taskGroupQueue.setUpdateTime(new Date());
                    taskGroupQueueDao.updateById(taskGroupQueue);
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

    /**
     * If the {@link TaskInstance#getTaskGroupId()} > 0, and the TaskGroup flag is {@link Flag#YES} then the task instance need to use task group.
     *
     * @param taskInstance task instance
     * @return true if the TaskInstance need to acquireTaskGroupSlot
     */
    public boolean needAcquireTaskGroupSlot(TaskInstance taskInstance) {
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

    /**
     * Acquire the task group slot for the given {@link TaskInstance}.
     * <p>
     * When taskInstance want to acquire a TaskGroup slot, should call this method. If acquire successfully, will create a TaskGroupQueue in db which is in queue and status is {@link TaskGroupQueueStatus#WAIT_QUEUE}.
     * The TaskInstance shouldn't dispatch until there exist available slot, the taskGroupCoordinator notify it.
     *
     * @param taskInstance the task instance which want to acquire task group slot.
     * @throws IllegalArgumentException if the taskInstance is null or the used taskGroup doesn't exist.
     */
    public void acquireTaskGroupSlot(TaskInstance taskInstance) {
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
                .priority(taskInstance.getTaskGroupPriority())
                .inQueue(Flag.YES.getCode())
                .forceStart(Flag.NO.getCode())
                .status(TaskGroupQueueStatus.WAIT_QUEUE)
                .createTime(now)
                .updateTime(now)
                .build();
        log.info("Success insert TaskGroupQueue: {} for TaskInstance: {}", taskGroupQueue, taskInstance.getName());
        taskGroupQueueDao.insert(taskGroupQueue);
    }

    /**
     * If the TaskInstance is using TaskGroup then it need to release TaskGroupSlot.
     *
     * @param taskInstance taskInsatnce
     * @return true if the TaskInstance need to release TaskGroupSlot
     */
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

    /**
     * Release the task group slot for the given {@link TaskInstance}.
     * <p>
     * When taskInstance want to release a TaskGroup slot, should call this method. The release method will delete the taskGroupQueue.
     * This method is idempotent, this means that if the task group slot is already released, this method will do nothing.
     *
     * @param taskInstance the task instance which want to release task group slot.
     * @throws IllegalArgumentException If the taskInstance is null or the task doesn't use task group.
     */
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
            throw new UnsupportedOperationException(
                    "The WorkflowInstance: " + workflowInstance.getId() + " state is " + workflowInstance.getState()
                            + ", no need to notify");
        }
        if (workflowInstance.getHost() == null || Constants.NULL.equals(workflowInstance.getHost())) {
            throw new UnsupportedOperationException(
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
            throw new UnsupportedOperationException(
                    "Notify TaskInstance: " + taskInstance.getId() + " failed: "
                            + taskGroupSlotAcquireSuccessNotifyResponse);
        }
        log.info("Wake up TaskInstance: {} success", taskInstance.getName());
    }

    private void deleteTaskGroupQueueSlot(TaskGroupQueue taskGroupQueue) {
        taskGroupQueueDao.deleteById(taskGroupQueue);
        log.info("Success release TaskGroupQueue: {}", taskGroupQueue);
    }

    @Override
    public void close() throws Exception {
        flag = false;
        log.info("TaskGroupCoordinator closed");
    }
}
