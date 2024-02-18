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
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.workflow.WorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(StateEventHandler.class)
@Slf4j
public class WorkflowSubmitFailStateEventHandler implements StateEventHandler {

    @Override
    public boolean handleStateEvent(WorkflowExecutionRunnable workflowExecuteRunnable,
                                    StateEvent stateEvent) throws StateEventHandleException {
        WorkflowStateEvent workflowStateEvent = (WorkflowStateEvent) stateEvent;
        ProcessInstance processInstance =
                workflowExecuteRunnable.getWorkflowExecuteContext().getWorkflowInstance();
        measureProcessState(workflowStateEvent, processInstance.getProcessDefinitionCode().toString());
        log.info(
                "Handle workflow instance submit fail state event, the current workflow instance state {} will be changed to {}",
                processInstance.getState(), workflowStateEvent.getStatus());

        workflowExecuteRunnable.updateProcessInstanceState(workflowStateEvent);
        workflowExecuteRunnable.endProcess();
        return true;
    }

    @Override
    public StateEventType getEventType() {
        return StateEventType.PROCESS_SUBMIT_FAILED;
    }

    private void measureProcessState(WorkflowStateEvent processStateEvent, String processDefinitionCode) {
        if (processStateEvent.getStatus().isFinished()) {
            ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("finish", processDefinitionCode);
        }
        switch (processStateEvent.getStatus()) {
            case STOP:
                ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("stop", processDefinitionCode);
                break;
            case SUCCESS:
                ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("success",
                        processDefinitionCode);
                break;
            case FAILURE:
                ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("fail", processDefinitionCode);
                break;
            default:
                break;
        }
    }
}
