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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.runner.task.TaskInstanceKey;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.NonNull;

/**
 * Check thread
 * 1. timeout task check
 * 2. dependent task state check
 * 3. retry task check
 * 4. timeout process check
 */
@Component
public class StateWheelExecuteThread extends BaseDaemonThread {

    private static final Logger logger = LoggerFactory.getLogger(StateWheelExecuteThread.class);

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

    /**
     * task state check list
     */
    private final ConcurrentLinkedQueue<TaskInstanceKey> taskInstanceStateCheckList = new ConcurrentLinkedQueue<>();

    @Autowired
    private MasterConfig masterConfig;

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
        while (Stopper.isRunning()) {
            try {
                checkTask4Timeout();
                checkTask4Retry();
                checkTask4State();
                checkProcess4Timeout();
            } catch (Exception e) {
                logger.error("state wheel thread check error:", e);
            }
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                logger.error("state wheel thread sleep error, will close the loop", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void addProcess4TimeoutCheck(ProcessInstance processInstance) {
        processInstanceTimeoutCheckList.add(processInstance.getId());
        logger.info("Success add workflow instance into timeout check list");
    }

    public void removeProcess4TimeoutCheck(int processInstanceId) {
        boolean removeFlag = processInstanceTimeoutCheckList.remove(processInstanceId);
        if (removeFlag) {
            logger.info("Success remove workflow instance from timeout check list");
        } else {
            logger.warn("Failed to remove workflow instance from timeout check list");
        }
    }

    private void checkProcess4Timeout() {
        if (processInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (Integer processInstanceId : processInstanceTimeoutCheckList) {
            WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
            if (workflowExecuteThread == null) {
                logger.warn("Check workflow timeout failed, can not find workflowExecuteThread from cache manager, will remove this workflowInstance from check list");
                processInstanceTimeoutCheckList.remove(processInstanceId);
                continue;
            }
            ProcessInstance processInstance = workflowExecuteThread.getProcessInstance();
            if (processInstance == null) {
                logger.warn("Check workflow timeout failed, the workflowInstance is null");
                continue;
            }
            long timeRemain = DateUtils.getRemainTime(processInstance.getStartTime(), (long) processInstance.getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
            if (timeRemain < 0) {
                logger.info("Workflow instance timeout, adding timeout event");
                addProcessTimeoutEvent(processInstance);
                processInstanceTimeoutCheckList.remove(processInstance.getId());
                logger.info("Workflow instance timeout, added timeout event");
            }
        }
    }

    public void addTask4TimeoutCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        logger.info("Adding task instance into timeout check list");
        if (taskInstanceTimeoutCheckList.contains(taskInstanceKey)) {
            logger.warn("Task instance is already in timeout check list");
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            logger.error("Failed to add task instance into timeout check list, taskDefinition is null");
            return;
        }
        if (TimeoutFlag.OPEN == taskDefinition.getTimeoutFlag()) {
            taskInstanceTimeoutCheckList.add(taskInstanceKey);
            logger.info("Timeout flag is open, added task instance into timeout check list");
        }
        if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
            taskInstanceTimeoutCheckList.add(taskInstanceKey);
            logger.info("task instance is dependTask orSubProcess, added task instance into timeout check list");
        }
    }

    public void removeTask4TimeoutCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        taskInstanceTimeoutCheckList.remove(taskInstanceKey);
        logger.info("remove task instance from timeout check list");
    }

    public void addTask4RetryCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        logger.info("Adding task instance into retry check list");
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceRetryCheckList.contains(taskInstanceKey)) {
            logger.warn("Task instance is already in retry check list");
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            logger.error("Add task instance into retry check list error, taskDefinition is null");
            return;
        }
        taskInstanceRetryCheckList.add(taskInstanceKey);
        logger.info("[WorkflowInstance-{}][TaskInstance-{}] Added task instance into retry check list",
            processInstance.getId(), taskInstance.getId());
    }

    public void removeTask4RetryCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        taskInstanceRetryCheckList.remove(taskInstanceKey);
        logger.info("remove task instance from retry check list");
    }

    public void addTask4StateCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        logger.info("Adding task instance into state check list");
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceStateCheckList.contains(taskInstanceKey)) {
            logger.warn("Task instance is already in state check list");
            return;
        }
        if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
            taskInstanceStateCheckList.add(taskInstanceKey);
            logger.info("Added task instance into state check list");
        }
    }

    public void removeTask4StateCheck(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        taskInstanceStateCheckList.remove(taskInstanceKey);
        logger.info("Removed task instance from state check list");
    }

    private void checkTask4Timeout() {
        if (taskInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (TaskInstanceKey taskInstanceKey : taskInstanceTimeoutCheckList) {
            try {
                int processInstanceId = taskInstanceKey.getProcessInstanceId();
                LoggerUtils.setWorkflowInstanceIdMDC(processInstanceId);
                long taskCode = taskInstanceKey.getTaskCode();

                WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
                if (workflowExecuteThread == null) {
                    logger.warn("Check task instance timeout failed, can not find workflowExecuteThread from cache manager, will remove this check task");
                    taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                    continue;
                }
                Optional<TaskInstance> taskInstanceOptional = workflowExecuteThread.getActiveTaskInstanceByTaskCode(taskCode);
                if (!taskInstanceOptional.isPresent()) {
                    logger.warn("Check task instance timeout failed, can not get taskInstance from workflowExecuteThread, taskCode: {}"
                        + "will remove this check task", taskCode);
                    taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                    continue;
                }
                TaskInstance taskInstance = taskInstanceOptional.get();
                if (TimeoutFlag.OPEN == taskInstance.getTaskDefine().getTimeoutFlag()) {
                    long timeRemain = DateUtils.getRemainTime(taskInstance.getStartTime(), (long) taskInstance.getTaskDefine().getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
                    if (timeRemain < 0) {
                        logger.info("Task instance is timeout, adding task timeout event and remove the check");
                        addTaskTimeoutEvent(taskInstance);
                        taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                    }
                }
            } finally {
                LoggerUtils.removeWorkflowInstanceIdMDC();
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
                LoggerUtils.setWorkflowInstanceIdMDC(processInstanceId);

                WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);

                if (workflowExecuteThread == null) {
                    logger.warn("Task instance retry check failed, can not find workflowExecuteThread from cache manager, "
                        + "will remove this check task");
                    taskInstanceRetryCheckList.remove(taskInstanceKey);
                    continue;
                }

                Optional<TaskInstance> taskInstanceOptional = workflowExecuteThread.getRetryTaskInstanceByTaskCode(taskCode);
                ProcessInstance processInstance = workflowExecuteThread.getProcessInstance();

                if (processInstance.getState() == ExecutionStatus.READY_STOP) {
                    logger.warn("The process instance is ready to stop, will send process stop event and remove the check task");
                    addProcessStopEvent(processInstance);
                    taskInstanceRetryCheckList.remove(taskInstanceKey);
                    break;
                }

                if (!taskInstanceOptional.isPresent()) {
                    logger.warn(
                        "Task instance retry check failed, can not find taskInstance from workflowExecuteThread, will remove this check");
                    taskInstanceRetryCheckList.remove(taskInstanceKey);
                    continue;
                }

                TaskInstance taskInstance = taskInstanceOptional.get();
                // We check the status to avoid when we do worker failover we submit a failover task, this task may be resubmit by this
                // thread
                if (taskInstance.getState() != ExecutionStatus.NEED_FAULT_TOLERANCE
                    && taskInstance.retryTaskIntervalOverTime()) {
                    // reset taskInstance endTime and state
                    // todo relative funtion: TaskInstance.retryTaskIntervalOverTime, WorkflowExecuteThread.cloneRetryTaskInstance
                    logger.info("[TaskInstance-{}]The task instance can retry, will retry this task instance",
                        taskInstance.getId());
                    taskInstance.setEndTime(null);
                    taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);

                    addTaskRetryEvent(taskInstance);
                    taskInstanceRetryCheckList.remove(taskInstanceKey);
                }
            } finally {
                LoggerUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private void checkTask4State() {
        if (taskInstanceStateCheckList.isEmpty()) {
            return;
        }
        for (TaskInstanceKey taskInstanceKey : taskInstanceStateCheckList) {
            int processInstanceId = taskInstanceKey.getProcessInstanceId();
            long taskCode = taskInstanceKey.getTaskCode();

            try {
                LoggerUtils.setTaskInstanceIdMDC(processInstanceId);
                WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
                if (workflowExecuteThread == null) {
                    logger.warn("Task instance state check failed, can not find workflowExecuteThread from cache manager, will remove this check task");
                    taskInstanceStateCheckList.remove(taskInstanceKey);
                    continue;
                }
                Optional<TaskInstance> taskInstanceOptional = workflowExecuteThread.getActiveTaskInstanceByTaskCode(taskCode);
                if (!taskInstanceOptional.isPresent()) {
                    logger.warn(
                        "Task instance state check failed, can not find taskInstance from workflowExecuteThread, will remove this check event");
                    taskInstanceStateCheckList.remove(taskInstanceKey);
                    continue;
                }
                TaskInstance taskInstance = taskInstanceOptional.get();
                if (taskInstance.getState().typeIsFinished()) {
                    continue;
                }
                addTaskStateChangeEvent(taskInstance);
            } finally {
                LoggerUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private void addTaskStateChangeEvent(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setTaskCode(taskInstance.getTaskCode());
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addProcessStopEvent(ProcessInstance processInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.PROCESS_STATE_CHANGE);
        stateEvent.setProcessInstanceId(processInstance.getId());
        stateEvent.setExecutionStatus(ExecutionStatus.STOP);
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addTaskRetryEvent(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_RETRY);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setTaskCode(taskInstance.getTaskCode());
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addTaskTimeoutEvent(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_TIMEOUT);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setTaskCode(taskInstance.getTaskCode());
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addProcessTimeoutEvent(ProcessInstance processInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.PROCESS_TIMEOUT);
        stateEvent.setProcessInstanceId(processInstance.getId());
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

}
