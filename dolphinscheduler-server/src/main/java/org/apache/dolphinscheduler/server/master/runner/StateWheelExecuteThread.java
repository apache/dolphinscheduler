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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.hadoop.util.ThreadUtil;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1. timeout check wheel
 * 2. dependent task check wheel
 */
public class StateWheelExecuteThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(StateWheelExecuteThread.class);

    private ProcessService processService;
    private ConcurrentHashMap<Integer, ProcessInstance> processInstanceTimeoutCheckList;
    private ConcurrentHashMap<Integer, TaskInstance> taskInstanceTimeoutCheckList;
    private ConcurrentHashMap<Integer, TaskInstance> taskInstanceRetryCheckList;
    private ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceExecMaps;

    /**
     * start process failed map
     */
    private final ConcurrentHashMap<Integer, WorkflowExecuteThread> startProcessFailedMap;

    private int stateCheckIntervalSecs;

    /**
     * master exec service
     */
    private MasterExecService masterExecService;

    public StateWheelExecuteThread(
            MasterExecService masterExecService,
            ProcessService processService,
            ConcurrentHashMap<Integer, WorkflowExecuteThread> startProcessFailedMap,
            ConcurrentHashMap<Integer, ProcessInstance> processInstanceTimeoutCheckList,
            ConcurrentHashMap<Integer, TaskInstance> taskInstanceTimeoutCheckList,
            ConcurrentHashMap<Integer, TaskInstance> taskInstanceRetryCheckList,
            ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceExecMaps,
            int stateCheckIntervalSecs) {
        this.masterExecService = masterExecService;
        this.processService = processService;
        this.startProcessFailedMap = startProcessFailedMap;
        this.processInstanceTimeoutCheckList = processInstanceTimeoutCheckList;
        this.taskInstanceTimeoutCheckList = taskInstanceTimeoutCheckList;
        this.taskInstanceRetryCheckList = taskInstanceRetryCheckList;
        this.processInstanceExecMaps = processInstanceExecMaps;
        this.stateCheckIntervalSecs = stateCheckIntervalSecs;
    }

    @Override
    public void run() {

        logger.info("state wheel thread start");
        while (Stopper.isRunning()) {
            try {
                check4StartProcessFailed();
                checkTask4Timeout();
                checkTask4Retry();
                checkProcess4Timeout();
            } catch (Exception e) {
                logger.error("state wheel thread check error:", e);
            }
            ThreadUtil.sleepAtLeastIgnoreInterrupts(stateCheckIntervalSecs);
        }
    }

    public void addProcess4TimeoutCheck(ProcessInstance processInstance) {
        this.processInstanceTimeoutCheckList.put(processInstance.getId(), processInstance);
    }

    public void addTask4TimeoutCheck(TaskInstance taskInstance) {
        this.taskInstanceTimeoutCheckList.put(taskInstance.getId(), taskInstance);
    }

    public void addTask4RetryCheck(TaskInstance taskInstance) {
        this.taskInstanceRetryCheckList.put(taskInstance.getId(), taskInstance);
    }

    public void checkTask4Timeout() {
        if (taskInstanceTimeoutCheckList.isEmpty()) {
            return;
        }
        for (TaskInstance taskInstance : taskInstanceTimeoutCheckList.values()) {
            if (TimeoutFlag.OPEN == taskInstance.getTaskDefine().getTimeoutFlag()) {
                if (taskInstance.getStartTime() == null) {
                    TaskInstance newTaskInstance = processService.findTaskInstanceById(taskInstance.getId());
                    taskInstance.setStartTime(newTaskInstance.getStartTime());
                }
                long timeRemain = DateUtils.getRemainTime(taskInstance.getStartTime(), taskInstance.getTaskDefine().getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
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

        for (TaskInstance taskInstance : this.taskInstanceRetryCheckList.values()) {
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
        for (ProcessInstance processInstance : this.processInstanceTimeoutCheckList.values()) {

            long timeRemain = DateUtils.getRemainTime(processInstance.getStartTime(), processInstance.getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
            if (timeRemain < 0) {
                addProcessTimeoutEvent(processInstance);
                processInstanceTimeoutCheckList.remove(processInstance.getId());
            }
        }
    }

    private boolean addTaskStateChangeEvent(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        addEvent(stateEvent);
        return true;
    }

    private boolean addTaskTimeoutEvent(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_TIMEOUT);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        addEvent(stateEvent);
        return true;
    }

    private boolean addProcessTimeoutEvent(ProcessInstance processInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.PROCESS_TIMEOUT);
        stateEvent.setProcessInstanceId(processInstance.getId());
        addEvent(stateEvent);
        return true;
    }

    private void addEvent(StateEvent stateEvent) {
        if (!processInstanceExecMaps.containsKey(stateEvent.getProcessInstanceId())) {
            return;
        }
        WorkflowExecuteThread workflowExecuteThread = this.processInstanceExecMaps.get(stateEvent.getProcessInstanceId());
        workflowExecuteThread.addStateEvent(stateEvent);
    }

    private void check4StartProcessFailed() {
        if (startProcessFailedMap.isEmpty()) {
            return;
        }
        for (WorkflowExecuteThread workflowExecuteThread : this.startProcessFailedMap.values()) {
            masterExecService.execute(workflowExecuteThread);
        }
    }
}
