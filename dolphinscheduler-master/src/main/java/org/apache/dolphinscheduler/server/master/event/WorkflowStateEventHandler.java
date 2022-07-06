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

package org.apache.dolphinscheduler.server.master.event;

import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

@AutoService(StateEventHandler.class)
public class WorkflowStateEventHandler implements StateEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowStateEventHandler.class);

    @Override
    public boolean handleStateEvent(WorkflowExecuteRunnable workflowExecuteRunnable, StateEvent stateEvent)
        throws StateEventHandleException {
        measureProcessState(stateEvent);
        ProcessInstance processInstance = workflowExecuteRunnable.getProcessInstance();
        ProcessDefinition processDefinition = processInstance.getProcessDefinition();

        logger.info("process:{} state {} change to {}",
            processInstance.getId(),
            processInstance.getState(),
            stateEvent.getExecutionStatus());

        if (stateEvent.getExecutionStatus() == ExecutionStatus.STOP) {
            // serial wait execution type needs to wake up the waiting process
            if (processDefinition.getExecutionType().typeIsSerialWait() || processDefinition.getExecutionType()
                .typeIsSerialPriority()) {
                workflowExecuteRunnable.endProcess();
                return true;
            }
            workflowExecuteRunnable.updateProcessInstanceState(stateEvent);
            return true;
        }
        if (workflowExecuteRunnable.processComplementData()) {
            return true;
        }
        if (stateEvent.getExecutionStatus().typeIsFinished()) {
            workflowExecuteRunnable.endProcess();
        }
        if (processInstance.getState() == ExecutionStatus.READY_STOP) {
            workflowExecuteRunnable.killAllTasks();
        }

        return true;
    }

    @Override
    public StateEventType getEventType() {
        return StateEventType.PROCESS_STATE_CHANGE;
    }

    private void measureProcessState(StateEvent processStateEvent) {
        if (processStateEvent.getExecutionStatus().typeIsFinished()) {
            ProcessInstanceMetrics.incProcessInstanceFinish();
        }
        switch (processStateEvent.getExecutionStatus()) {
            case STOP:
                ProcessInstanceMetrics.incProcessInstanceStop();
                break;
            case SUCCESS:
                ProcessInstanceMetrics.incProcessInstanceSuccess();
                break;
            case FAILURE:
                ProcessInstanceMetrics.incProcessInstanceFailure();
                break;
            default:
                break;
        }
    }
}
