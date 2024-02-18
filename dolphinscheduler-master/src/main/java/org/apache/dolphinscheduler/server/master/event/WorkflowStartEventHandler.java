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

import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.runner.StateWheelExecuteThread;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkflowStartEventHandler implements WorkflowEventHandler {

    @Autowired
    private IWorkflowExecuteRunnableRepository IWorkflowExecuteRunnableRepository;

    @Autowired
    private StateWheelExecuteThread stateWheelExecuteThread;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Override
    public void handleWorkflowEvent(final WorkflowEvent workflowEvent) throws WorkflowEventHandleError {
        // log.info("Handle workflow start event, begin to start a workflow, event: {}", workflowEvent);
        // DefaultWorkflowExecutionRunnable workflowExecuteRunnable =
        // IWorkflowExecuteRunnableRepository.getByProcessInstanceId(
        // workflowEvent.getWorkflowInstanceId());
        // if (workflowExecuteRunnable == null) {
        // throw new WorkflowEventHandleError(
        // "The workflow start event is invalid, cannot find the workflow instance from cache");
        // }
        // ProcessInstance processInstance =
        // workflowExecuteRunnable.getWorkflowExecuteContext().getWorkflowInstance();
        // ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("submit",
        // processInstance.getProcessDefinitionCode().toString());
        // CompletableFuture.runAsync(workflowExecuteRunnable::start, workflowExecuteThreadPool)
        // .thenAccept((unused) -> {
        // log.info("Success submit the workflow instance");
        // if (processInstance.getTimeout() > 0) {
        // stateWheelExecuteThread.addProcess4TimeoutCheck(processInstance);
        // }
        // })
        // .exceptionally(e -> {
        // log.error(
        // "Failed to submit the workflow instance, will send fail state event: {}",
        // .processInstanceId(processInstance.getId())
        // .type(StateEventType.PROCESS_SUBMIT_FAILED)
        // .status(WorkflowExecutionStatus.FAILURE)
        // .build();
        // workflowExecuteRunnable.addStateEvent(stateEvent);
        // return null;
        // });
    }

    @Override
    public WorkflowEventType getHandleWorkflowEventType() {
        return WorkflowEventType.START_WORKFLOW;
    }
}
