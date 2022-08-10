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

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(StateEventHandler.class)
public class WorkflowStateEventHandler implements StateEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowStateEventHandler.class);

    @Override
    public boolean handleStateEvent(WorkflowExecuteRunnable workflowExecuteRunnable,
                                    StateEvent stateEvent) throws StateEventHandleException {
        WorkflowStateEvent workflowStateEvent = (WorkflowStateEvent) stateEvent;
        measureProcessState(workflowStateEvent);
        ProcessInstance processInstance = workflowExecuteRunnable.getProcessInstance();
        ProcessDefinition processDefinition = processInstance.getProcessDefinition();

        logger.info(
                "Handle workflow instance state event, the current workflow instance state {} will be changed to {}",
                processInstance.getState(), workflowStateEvent.getStatus());

        if (workflowStateEvent.getStatus().isStop()) {
            // serial wait execution type needs to wake up the waiting process
            if (processDefinition.getExecutionType().typeIsSerialWait() || processDefinition.getExecutionType()
                    .typeIsSerialPriority()) {
                workflowExecuteRunnable.endProcess();
                return true;
            }
            workflowExecuteRunnable.updateProcessInstanceState(workflowStateEvent);
            return true;
        }
        if (workflowExecuteRunnable.processComplementData()) {
            return true;
        }
        if (workflowStateEvent.getStatus().isFinished()) {
            workflowExecuteRunnable.endProcess();
        }
        if (processInstance.getState().isReadyStop()) {
            workflowExecuteRunnable.killAllTasks();
        }

        return true;
    }

    @Override
    public StateEventType getEventType() {
        return StateEventType.PROCESS_STATE_CHANGE;
    }

    private void measureProcessState(WorkflowStateEvent processStateEvent) {
        if (processStateEvent.getStatus().isFinished()) {
            ProcessInstanceMetrics.incProcessInstanceByState("finish");
        }
        switch (processStateEvent.getStatus()) {
            case STOP:
                ProcessInstanceMetrics.incProcessInstanceByState("stop");
                break;
            case SUCCESS:
                ProcessInstanceMetrics.incProcessInstanceByState("success");
                break;
            case FAILURE:
                ProcessInstanceMetrics.incProcessInstanceByState("fail");
                break;
            default:
                break;
        }
    }
}
