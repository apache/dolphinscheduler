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
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;

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

    ConcurrentHashMap<Integer, ProcessInstance> processInstanceCheckList;
    ConcurrentHashMap<Integer, TaskInstance> taskInstanceCheckList;
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    private int stateCheckIntervalSecs;

    public StateWheelExecuteThread(ConcurrentHashMap<Integer, ProcessInstance> processInstances,
                                   ConcurrentHashMap<Integer, TaskInstance> taskInstances,
                                   ProcessInstanceExecCacheManager processInstanceExecCacheManager,
                                   int stateCheckIntervalSecs) {
        this.processInstanceCheckList = processInstances;
        this.taskInstanceCheckList = taskInstances;
        this.processInstanceExecCacheManager = processInstanceExecCacheManager;
        this.stateCheckIntervalSecs = stateCheckIntervalSecs;
    }

    @Override
    public void run() {

        logger.info("state wheel thread start");
        while (Stopper.isRunning()) {
            try {
                checkProcess();
                checkTask();
            } catch (Exception e) {
                logger.error("state wheel thread check error:", e);
            }
            ThreadUtil.sleepAtLeastIgnoreInterrupts(stateCheckIntervalSecs);
        }
    }

    public boolean addProcess(ProcessInstance processInstance) {
        this.processInstanceCheckList.put(processInstance.getId(), processInstance);
        return true;
    }

    public boolean addTask(TaskInstance taskInstance) {
        this.taskInstanceCheckList.put(taskInstance.getId(), taskInstance);
        return true;
    }

    private void checkTask() {
        if (taskInstanceCheckList.isEmpty()) {
            return;
        }

        for (TaskInstance taskInstance : this.taskInstanceCheckList.values()) {
            if (TimeoutFlag.OPEN == taskInstance.getTaskDefine().getTimeoutFlag()) {
                long timeRemain = DateUtils.getRemainTime(taskInstance.getStartTime(), taskInstance.getTaskDefine().getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
                if (0 <= timeRemain && processTimeout(taskInstance)) {
                    taskInstanceCheckList.remove(taskInstance.getId());
                    return;
                }
            }
            if (taskInstance.taskCanRetry() && taskInstance.retryTaskIntervalOverTime()) {
                processDependCheck(taskInstance);
                taskInstanceCheckList.remove(taskInstance.getId());
            }
            if (taskInstance.isSubProcess() || taskInstance.isDependTask()) {
                processDependCheck(taskInstance);
            }
        }
    }

    private void checkProcess() {
        if (processInstanceCheckList.isEmpty()) {
            return;
        }
        for (ProcessInstance processInstance : this.processInstanceCheckList.values()) {

            long timeRemain = DateUtils.getRemainTime(processInstance.getStartTime(), processInstance.getTimeout() * Constants.SEC_2_MINUTES_TIME_UNIT);
            if (0 <= timeRemain && processTimeout(processInstance)) {
                processInstanceCheckList.remove(processInstance.getId());
            }
        }
    }

    private void putEvent(StateEvent stateEvent) {

        if (!processInstanceExecCacheManager.contains(stateEvent.getProcessInstanceId())) {
            return;
        }
        WorkflowExecuteThread workflowExecuteThread = this.processInstanceExecCacheManager.getByProcessInstanceId(stateEvent.getProcessInstanceId());
        workflowExecuteThread.addStateEvent(stateEvent);
    }

    private boolean processDependCheck(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        putEvent(stateEvent);
        return true;
    }

    private boolean processTimeout(TaskInstance taskInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.TASK_TIMEOUT);
        stateEvent.setProcessInstanceId(taskInstance.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskInstance.getId());
        putEvent(stateEvent);
        return true;
    }

    private boolean processTimeout(ProcessInstance processInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.PROCESS_TIMEOUT);
        stateEvent.setProcessInstanceId(processInstance.getId());
        putEvent(stateEvent);
        return true;
    }

}
