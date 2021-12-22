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
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import org.apache.hadoop.util.ThreadUtil;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 1. timeout check wheel
 * 2. dependent task check wheel
 */
@Component
public class StateWheelExecuteThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(StateWheelExecuteThread.class);

    /**
     * process timeout check list
     */
    private ConcurrentLinkedQueue<Integer> processInstanceTimeoutCheckList = new ConcurrentLinkedQueue<>();

    /**
     * task time out check list, key is taskInstanceId, value is processInstanceId
     */
    private ConcurrentHashMap<Integer, Integer> taskInstanceTimeoutCheckList = new ConcurrentHashMap<>();

    /**
     * task retry check list, key is taskInstanceId, value is processInstanceId
     */
    private ConcurrentHashMap<Integer, Integer> taskInstanceRetryCheckList = new ConcurrentHashMap<>();

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Override
    public void run() {
        while (Stopper.isRunning()) {
            try {
                checkTask4Timeout();
                checkTask4Retry();
                checkProcess4Timeout();
            } catch (Exception e) {
                logger.error("state wheel thread check error:", e);
            }
            ThreadUtil.sleepAtLeastIgnoreInterrupts((long) masterConfig.getStateWheelInterval() * Constants.SLEEP_TIME_MILLIS);
        }
    }

    public void addProcess4TimeoutCheck(ProcessInstance processInstance) {
        processInstanceTimeoutCheckList.add(processInstance.getId());
    }

    public void removeProcess4TimeoutCheck(ProcessInstance processInstance) {
        processInstanceTimeoutCheckList.remove(processInstance.getId());
    }

    public void addTask4TimeoutCheck(TaskInstance taskInstance) {
        if (taskInstanceTimeoutCheckList.containsKey(taskInstance.getId())) {
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            logger.error("taskDefinition is null, taskId:{}", taskInstance.getId());
            return;
        }
        if (TimeoutFlag.OPEN == taskDefinition.getTimeoutFlag()) {
            taskInstanceTimeoutCheckList.put(taskInstance.getId(), taskInstance.getProcessInstanceId());
        }
        if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
            taskInstanceTimeoutCheckList.put(taskInstance.getId(), taskInstance.getProcessInstanceId());
        }
    }

    public void removeTask4TimeoutCheck(TaskInstance taskInstance) {
        taskInstanceTimeoutCheckList.remove(taskInstance.getId());
    }

    public void addTask4RetryCheck(TaskInstance taskInstance) {
        if (taskInstanceRetryCheckList.containsKey(taskInstance.getId())) {
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            logger.error("taskDefinition is null, taskId:{}", taskInstance.getId());
            return;
        }
        if (taskInstance.taskCanRetry()) {
            taskInstanceRetryCheckList.put(taskInstance.getId(), taskInstance.getProcessInstanceId());
        }

        if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
            taskInstanceRetryCheckList.put(taskInstance.getId(), taskInstance.getProcessInstanceId());
        }
    }

    public void removeTask4RetryCheck(TaskInstance taskInstance) {
        taskInstanceRetryCheckList.remove(taskInstance.getId());
    }

    private void checkTask4Timeout() {
        if (taskInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (Entry<Integer, Integer> entry : taskInstanceTimeoutCheckList.entrySet()) {
            int processInstanceId = entry.getValue();
            int taskInstanceId = entry.getKey();

            WorkflowExecuteThread workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
            if (workflowExecuteThread == null) {
                logger.warn("can not find workflowExecuteThread, this check event will remove, processInstanceId:{}, taskInstanceId:{}",
                        processInstanceId, taskInstanceId);
                taskInstanceTimeoutCheckList.remove(taskInstanceId);
                continue;
            }
            TaskInstance taskInstance = workflowExecuteThread.getTaskInstance(taskInstanceId);
            if (taskInstance == null) {
                continue;
            }
            if (TimeoutFlag.OPEN == taskInstance.getTaskDefine().getTimeoutFlag()) {
                long timeRemain = DateUtils.getRemainTime(taskInstance.getStartTime(), (long) taskInstance.getTaskDefine().getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
                if (timeRemain < 0) {
                    addTaskTimeoutEvent(taskInstance);
                    taskInstanceTimeoutCheckList.remove(taskInstance.getId());
                }
            }
        }
    }

    private void checkTask4Retry() {
        if (taskInstanceRetryCheckList.isEmpty()) {
            return;
        }
        for (Entry<Integer, Integer> entry : taskInstanceRetryCheckList.entrySet()) {
            int processInstanceId = entry.getValue();
            int taskInstanceId = entry.getKey();

            WorkflowExecuteThread workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
            if (workflowExecuteThread == null) {
                logger.warn("can not find workflowExecuteThread, this check event will remove, processInstanceId:{}, taskInstanceId:{}",
                        processInstanceId, taskInstanceId);
                taskInstanceRetryCheckList.remove(taskInstanceId);
                continue;
            }
            TaskInstance taskInstance = workflowExecuteThread.getTaskInstance(taskInstanceId);
            if (taskInstance == null) {
                continue;
            }

            if (!taskInstance.getState().typeIsFinished() && (taskInstance.isSubProcess() || taskInstance.isDependTask())) {
                addTaskStateChangeEvent(taskInstance);
            } else if (taskInstance.taskCanRetry() && taskInstance.retryTaskIntervalOverTime()) {
                addTaskStateChangeEvent(taskInstance);
                taskInstanceRetryCheckList.remove(taskInstance.getId());
            }
        }
    }

    private void checkProcess4Timeout() {
        if (processInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (Integer processInstanceId : processInstanceTimeoutCheckList) {
            if (processInstanceId == null) {
                continue;
            }
            WorkflowExecuteThread workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
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

    private void addTaskStateChangeEvent(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addTaskTimeoutEvent(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_TIMEOUT);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    private void addProcessTimeoutEvent(ProcessInstance processInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.PROCESS_TIMEOUT);
        stateEvent.setProcessInstanceId(processInstance.getId());
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

}
