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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowStateEvent;
import org.apache.dolphinscheduler.server.master.runner.task.TaskInstanceKey;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Check thread
 * 1. timeout task check
 * 2. dependent task state check
 * 3. retry task check
 * 4. timeout process check
 */
@Component
@Slf4j
public class StateWheelExecuteThread extends BaseDaemonThread {

    /**
     * ProcessInstance timeout check list, element is the processInstanceId.
     */
    private final ConcurrentLinkedQueue<Integer> processInstanceTimeoutCheckList = new ConcurrentLinkedQueue<>();

    /**
     * task time out check list
     */
    private final ConcurrentLinkedQueue<TaskInstanceKey> taskInstanceTimeoutCheckList = new ConcurrentLinkedQueue<>();

    /**
     * task retry check list
     */
    private final ConcurrentLinkedQueue<TaskInstanceKey> taskInstanceRetryCheckList = new ConcurrentLinkedQueue<>();

    @Autowired
    private MasterConfig masterConfig;

    @Lazy
    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    protected StateWheelExecuteThread() {
        super("StateWheelExecuteThread");
    }

    @PostConstruct
    public void startWheelThread() {
        super.start();
    }

    @Override
    public void run() {
        final long checkInterval = masterConfig.getStateWheelInterval().toMillis();
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                checkTask4Timeout();
                checkTask4Retry();
                checkProcess4Timeout();
            } catch (Exception e) {
                log.error("state wheel thread check error:", e);
            }
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                log.error("state wheel thread sleep error, will close the loop", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void addProcess4TimeoutCheck(ProcessInstance processInstance) {
        processInstanceTimeoutCheckList.add(processInstance.getId());
        log.info("Success add workflow instance {} into timeout check list", processInstance.getId());
    }

    public void removeProcess4TimeoutCheck(int processInstanceId) {
        boolean removeFlag = processInstanceTimeoutCheckList.remove(processInstanceId);
        if (removeFlag) {
            log.info("Success remove workflow instance {} from timeout check list", processInstanceId);
        }
    }

    private void checkProcess4Timeout() {
        if (processInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (Integer processInstanceId : processInstanceTimeoutCheckList) {
            try {
                LogUtils.setWorkflowInstanceIdMDC(processInstanceId);
                WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(
                        processInstanceId);
                if (workflowExecuteThread == null) {
                    log.warn(
                            "Check workflow timeout failed, can not find workflowExecuteThread from cache manager, will remove this workflowInstance from check list");
                    processInstanceTimeoutCheckList.remove(processInstanceId);
                    continue;
                }
                ProcessInstance processInstance =
                        workflowExecuteThread.getWorkflowExecuteContext().getWorkflowInstance();
                if (processInstance == null) {
                    log.warn("Check workflow timeout failed, the workflowInstance is null");
                    continue;
                }
                long timeRemain = DateUtils.getRemainTime(processInstance.getStartTime(),
                        (long) processInstance.getTimeout()
                                * Constants.SEC_2_MINUTES_TIME_UNIT);
                if (timeRemain < 0) {
                    log.info("Workflow instance {} timeout, adding timeout event", processInstance.getId());
                    addProcessTimeoutEvent(processInstance);
                    processInstanceTimeoutCheckList.remove(processInstance.getId());
                    log.info("Workflow instance timeout, added timeout event");
                }
            } catch (Exception ex) {
                log.error("Check workflow instance timeout error");
            } finally {
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    public void addTask4TimeoutCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        log.info("Adding task instance into timeout check list");
        if (taskInstanceTimeoutCheckList.contains(taskInstanceKey)) {
            log.warn("Task instance is already in timeout check list");
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            log.error("Failed to add task instance into timeout check list, taskDefinition is null");
            return;
        }
        if (TimeoutFlag.OPEN == taskDefinition.getTimeoutFlag()) {
            taskInstanceTimeoutCheckList.add(taskInstanceKey);
            log.info("Timeout flag is open, added task instance into timeout check list");
        }
    }

    public void removeTask4TimeoutCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        taskInstanceTimeoutCheckList.remove(taskInstanceKey);
        log.info("remove task instance from timeout check list");
    }

    public void addTask4RetryCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        log.info("Adding task instance into retry check list");
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceRetryCheckList.contains(taskInstanceKey)) {
            log.warn("Task instance is already in retry check list");
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            log.error("Add task instance into retry check list error, taskDefinition is null");
            return;
        }
        taskInstanceRetryCheckList.add(taskInstanceKey);
        log.info("[WorkflowInstance-{}][TaskInstanceKey-{}:{}] Added task instance into retry check list",
                processInstance.getId(), taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());
    }

    public void removeTask4RetryCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        taskInstanceRetryCheckList.remove(taskInstanceKey);
        log.info("remove task instance from retry check list");
    }

    public void clearAllTasks() {
        processInstanceTimeoutCheckList.clear();
        taskInstanceTimeoutCheckList.clear();
        taskInstanceRetryCheckList.clear();
    }

    private void checkTask4Timeout() {
        if (taskInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (TaskInstanceKey taskInstanceKey : taskInstanceTimeoutCheckList) {
            try {
                LogUtils.setWorkflowInstanceIdMDC(taskInstanceKey.getProcessInstanceId());
                int processInstanceId = taskInstanceKey.getProcessInstanceId();
                long taskCode = taskInstanceKey.getTaskCode();

                WorkflowExecuteRunnable workflowExecuteThread =
                        processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
                if (workflowExecuteThread == null) {
                    log.warn(
                            "Check task instance timeout failed, can not find workflowExecuteThread from cache manager, will remove this check task");
                    taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                    continue;
                }
                Optional<TaskInstance> taskInstanceOptional =
                        workflowExecuteThread.getActiveTaskInstanceByTaskCode(taskCode);
                if (!taskInstanceOptional.isPresent()) {
                    log.warn(
                            "Check task instance timeout failed, can not get taskInstance from workflowExecuteThread, taskCode: {}"
                                    + "will remove this check task",
                            taskCode);
                    taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                    continue;
                }
                TaskInstance taskInstance = taskInstanceOptional.get();
                if (TimeoutFlag.OPEN == taskInstance.getTaskDefine().getTimeoutFlag()) {
                    long timeRemain = DateUtils.getRemainTime(taskInstance.getStartTime(),
                            (long) taskInstance.getTaskDefine().getTimeout()
                                    * Constants.SEC_2_MINUTES_TIME_UNIT);
                    if (timeRemain < 0) {
                        log.info("Task instance is timeout, adding task timeout event and remove the check");
                        addTaskTimeoutEvent(taskInstance);
                        taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                    }
                }
            } catch (Exception ex) {
                log.error("Check task timeout error, taskInstanceKey: {}", taskInstanceKey, ex);
            } finally {
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private void checkTask4Retry() {
        if (taskInstanceRetryCheckList.isEmpty()) {
            return;
        }

        for (TaskInstanceKey taskInstanceKey : taskInstanceRetryCheckList) {
            int processInstanceId = taskInstanceKey.getProcessInstanceId();
            long taskCode = taskInstanceKey.getTaskCode();
            try {
                LogUtils.setWorkflowInstanceIdMDC(processInstanceId);

                WorkflowExecuteRunnable workflowExecuteThread =
                        processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);

                if (workflowExecuteThread == null) {
                    log.warn(
                            "Task instance retry check failed, can not find workflowExecuteThread from cache manager, "
                                    + "will remove this check task");
                    taskInstanceRetryCheckList.remove(taskInstanceKey);
                    continue;
                }

                Optional<TaskInstance> taskInstanceOptional =
                        workflowExecuteThread.getRetryTaskInstanceByTaskCode(taskCode);
                ProcessInstance processInstance =
                        workflowExecuteThread.getWorkflowExecuteContext().getWorkflowInstance();

                if (processInstance.getState().isReadyStop()) {
                    log.warn(
                            "The process instance is ready to stop, will send process stop event and remove the check task");
                    addProcessStopEvent(processInstance);
                    taskInstanceRetryCheckList.remove(taskInstanceKey);
                    break;
                }

                if (!taskInstanceOptional.isPresent()) {
                    log.warn(
                            "Task instance retry check failed, can not find taskInstance from workflowExecuteThread, will remove this check");
                    taskInstanceRetryCheckList.remove(taskInstanceKey);
                    continue;
                }

                TaskInstance taskInstance = taskInstanceOptional.get();
                // We check the status to avoid when we do worker failover we submit a failover task, this task may be
                // resubmit by this
                // thread
                if (taskInstance.getState() != TaskExecutionStatus.NEED_FAULT_TOLERANCE
                        && taskInstance.retryTaskIntervalOverTime()) {
                    // reset taskInstance endTime and state
                    // todo relative function: TaskInstance.retryTaskIntervalOverTime,
                    // WorkflowExecuteThread.cloneRetryTaskInstance
                    log.info("[TaskInstanceKey-{}:{}]The task instance can retry, will retry this task instance",
                            taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());
                    taskInstance.setEndTime(null);
                    taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);

                    addTaskRetryEvent(taskInstance);
                    taskInstanceRetryCheckList.remove(taskInstanceKey);
                }
            } catch (Exception ex) {
                log.error("Check task retry error, taskInstanceKey: {}", taskInstanceKey, ex);
            } finally {
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private void addProcessStopEvent(ProcessInstance processInstance) {
        WorkflowStateEvent stateEvent = WorkflowStateEvent.builder()
                .processInstanceId(processInstance.getId())
                .type(StateEventType.PROCESS_STATE_CHANGE)
                .status(WorkflowExecutionStatus.STOP)
                .build();
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addTaskRetryEvent(TaskInstance taskInstance) {
        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(taskInstance.getProcessInstanceId())
                .taskCode(taskInstance.getTaskCode())
                .status(TaskExecutionStatus.RUNNING_EXECUTION)
                .type(StateEventType.TASK_RETRY)
                .build();
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addTaskTimeoutEvent(TaskInstance taskInstance) {
        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(taskInstance.getProcessInstanceId())
                .taskInstanceId(taskInstance.getId())
                .type(StateEventType.TASK_TIMEOUT)
                .taskCode(taskInstance.getTaskCode())
                .build();
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addProcessTimeoutEvent(ProcessInstance processInstance) {
        WorkflowStateEvent stateEvent = WorkflowStateEvent.builder()
                .processInstanceId(processInstance.getId())
                .type(StateEventType.PROCESS_TIMEOUT)
                .build();
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

}
