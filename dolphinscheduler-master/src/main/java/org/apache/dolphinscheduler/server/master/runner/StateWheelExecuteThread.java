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
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.task.TaskInstanceKey;

import org.apache.commons.lang3.ThreadUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
     * process timeout check list
     */
    private ConcurrentLinkedQueue<Integer> processInstanceTimeoutCheckList = new ConcurrentLinkedQueue<>();

    /**
     * task time out check list
     */
    private ConcurrentLinkedQueue<TaskInstanceKey> taskInstanceTimeoutCheckList = new ConcurrentLinkedQueue<>();

    /**
     * task retry check list
     */
    private ConcurrentLinkedQueue<TaskInstanceKey> taskInstanceRetryCheckList = new ConcurrentLinkedQueue<>();

    /**
     * task state check list
     */
    private ConcurrentLinkedQueue<TaskInstanceKey> taskInstanceStateCheckList = new ConcurrentLinkedQueue<>();

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    protected StateWheelExecuteThread() {
        super("StateWheelExecuteThread");
    }

    @Override
    public void run() {
        Duration checkInterval = Duration.ofMillis(masterConfig.getStateWheelInterval() * Constants.SLEEP_TIME_MILLIS);
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
                ThreadUtils.sleep(checkInterval);
            } catch (InterruptedException e) {
                logger.error("state wheel thread sleep error", e);
            }
        }
    }

    public void addProcess4TimeoutCheck(ProcessInstance processInstance) {
        processInstanceTimeoutCheckList.add(processInstance.getId());
    }

    public void removeProcess4TimeoutCheck(ProcessInstance processInstance) {
        processInstanceTimeoutCheckList.remove(processInstance.getId());
    }

    private void checkProcess4Timeout() {
        if (processInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (Integer processInstanceId : processInstanceTimeoutCheckList) {
            if (processInstanceId == null) {
                continue;
            }
            WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
            if (workflowExecuteThread == null) {
                logger.warn("can not find workflowExecuteThread, this check event will remove, processInstanceId:{}", processInstanceId);
                processInstanceTimeoutCheckList.remove(processInstanceId);
                continue;
            }
            ProcessInstance processInstance = workflowExecuteThread.getProcessInstance();
            if (processInstance == null) {
                continue;
            }
            long timeRemain = DateUtils.getRemainTime(processInstance.getStartTime(), (long) processInstance.getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
            if (timeRemain < 0) {
                addProcessTimeoutEvent(processInstance);
                processInstanceTimeoutCheckList.remove(processInstance.getId());
            }
        }
    }

    public void addTask4TimeoutCheck(ProcessInstance processInstance, TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceKey == null) {
            logger.error("taskInstanceKey is null");
            return;
        }
        if (taskInstanceTimeoutCheckList.contains(taskInstanceKey)) {
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            logger.error("taskDefinition is null, taskId:{}", taskInstance.getId());
            return;
        }
        if (TimeoutFlag.OPEN == taskDefinition.getTimeoutFlag()) {
            taskInstanceTimeoutCheckList.add(taskInstanceKey);
        }
        if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
            taskInstanceTimeoutCheckList.add(taskInstanceKey);
        }
    }

    public void removeTask4TimeoutCheck(ProcessInstance processInstance, TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceKey == null) {
            logger.error("taskInstanceKey is null");
            return;
        }
        taskInstanceTimeoutCheckList.remove(taskInstanceKey);
    }

    public void addTask4RetryCheck(ProcessInstance processInstance, TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceKey == null) {
            logger.error("taskInstanceKey is null");
            return;
        }
        if (taskInstanceRetryCheckList.contains(taskInstanceKey)) {
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            logger.error("taskDefinition is null, taskId:{}", taskInstance.getId());
            return;
        }
        logger.debug("addTask4RetryCheck, taskCode:{}, processInstanceId:{}", taskInstance.getTaskCode(), taskInstance.getProcessInstanceId());
        taskInstanceRetryCheckList.add(taskInstanceKey);
    }

    public void removeTask4RetryCheck(ProcessInstance processInstance, TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceKey == null) {
            logger.error("taskInstanceKey is null");
            return;
        }
        taskInstanceRetryCheckList.remove(taskInstanceKey);
    }

    public void addTask4StateCheck(ProcessInstance processInstance, TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceKey == null) {
            logger.error("taskInstanceKey is null");
            return;
        }
        if (taskInstanceStateCheckList.contains(taskInstanceKey)) {
            return;
        }
        if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
            taskInstanceStateCheckList.add(taskInstanceKey);
        }
    }

    public void removeTask4StateCheck(ProcessInstance processInstance, TaskInstance taskInstance) {
        TaskInstanceKey taskInstanceKey = TaskInstanceKey.getTaskInstanceKey(processInstance, taskInstance);
        if (taskInstanceKey == null) {
            logger.error("taskInstanceKey is null");
            return;
        }
        taskInstanceStateCheckList.remove(taskInstanceKey);
    }

    private void checkTask4Timeout() {
        if (taskInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (TaskInstanceKey taskInstanceKey : taskInstanceTimeoutCheckList) {
            int processInstanceId = taskInstanceKey.getProcessInstanceId();
            long taskCode = taskInstanceKey.getTaskCode();

            WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
            if (workflowExecuteThread == null) {
                logger.warn("can not find workflowExecuteThread, this check event will remove, processInstanceId:{}, taskCode:{}",
                        processInstanceId, taskCode);
                taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                continue;
            }
            Optional<TaskInstance> taskInstanceOptional = workflowExecuteThread.getActiveTaskInstanceByTaskCode(taskCode);
            if (!taskInstanceOptional.isPresent()) {
                logger.warn("can not find taskInstance from workflowExecuteThread, this check event will remove, processInstanceId:{}, taskCode:{}",
                        processInstanceId, taskCode);
                taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                continue;
            }
            TaskInstance taskInstance = taskInstanceOptional.get();
            if (TimeoutFlag.OPEN == taskInstance.getTaskDefine().getTimeoutFlag()) {
                long timeRemain = DateUtils.getRemainTime(taskInstance.getStartTime(), (long) taskInstance.getTaskDefine().getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
                if (timeRemain < 0) {
                    addTaskTimeoutEvent(taskInstance);
                    taskInstanceTimeoutCheckList.remove(taskInstanceKey);
                }
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

            WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);

            if (workflowExecuteThread == null) {
                logger.warn("can not find workflowExecuteThread, this check event will remove, processInstanceId:{}, taskCode:{}",
                        processInstanceId, taskCode);
                taskInstanceRetryCheckList.remove(taskInstanceKey);
                continue;
            }

            Optional<TaskInstance> taskInstanceOptional = workflowExecuteThread.getRetryTaskInstanceByTaskCode(taskCode);
            ProcessInstance processInstance = workflowExecuteThread.getProcessInstance();

            if (processInstance.getState() == ExecutionStatus.READY_STOP) {
                addProcessStopEvent(processInstance);
                taskInstanceRetryCheckList.remove(taskInstanceKey);
                break;
            }

            if (!taskInstanceOptional.isPresent()) {
                logger.warn("can not find taskInstance from workflowExecuteThread, this check event will remove, processInstanceId:{}, taskCode:{}",
                        processInstanceId, taskCode);
                taskInstanceRetryCheckList.remove(taskInstanceKey);
                continue;
            }

            TaskInstance taskInstance = taskInstanceOptional.get();
            if (taskInstance.retryTaskIntervalOverTime()) {
                // reset taskInstance endTime and state
                // todo relative funtion: TaskInstance.retryTaskIntervalOverTime, WorkflowExecuteThread.cloneRetryTaskInstance
                taskInstance.setEndTime(null);
                taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);

                addTaskRetryEvent(taskInstance);
                taskInstanceRetryCheckList.remove(taskInstanceKey);
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

            WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
            if (workflowExecuteThread == null) {
                logger.warn("can not find workflowExecuteThread, this check event will remove, processInstanceId:{}, taskCode:{}",
                        processInstanceId, taskCode);
                taskInstanceStateCheckList.remove(taskInstanceKey);
                continue;
            }
            Optional<TaskInstance> taskInstanceOptional = workflowExecuteThread.getActiveTaskInstanceByTaskCode(taskCode);
            if (!taskInstanceOptional.isPresent()) {
                logger.warn("can not find taskInstance from workflowExecuteThread, this check event will remove, processInstanceId:{}, taskCode:{}",
                        processInstanceId, taskCode);
                taskInstanceStateCheckList.remove(taskInstanceKey);
                continue;
            }
            TaskInstance taskInstance = taskInstanceOptional.get();
            if (taskInstance.getState().typeIsFinished()) {
                continue;
            }
            addTaskStateChangeEvent(taskInstance);
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
