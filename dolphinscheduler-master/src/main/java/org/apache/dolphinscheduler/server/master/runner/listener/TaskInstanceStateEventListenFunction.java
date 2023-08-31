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

package org.apache.dolphinscheduler.server.master.runner.listener;

import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStateChangeEvent;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowStateEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.StateEventResponseService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskInstanceStateEventListenFunction
        implements
            ITaskInstanceExecutionEventListenFunction<WorkflowInstanceStateChangeEvent> {

    @Autowired
    private StateEventResponseService stateEventResponseService;

    @Override
    public void handleTaskInstanceExecutionEvent(WorkflowInstanceStateChangeEvent taskInstanceInstanceStateChangeEvent) {
        final StateEvent stateEvent;
        if (taskInstanceInstanceStateChangeEvent.getDestTaskInstanceId() == 0) {
            stateEvent = createWorkflowStateEvent(taskInstanceInstanceStateChangeEvent);
        } else {
            stateEvent = createTaskStateEvent(taskInstanceInstanceStateChangeEvent);
        }

        try {
            LogUtils.setWorkflowAndTaskInstanceIDMDC(stateEvent.getProcessInstanceId(), stateEvent.getTaskInstanceId());
            log.info("Received state change command, event: {}", stateEvent);
            stateEventResponseService.addStateChangeEvent(stateEvent);
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }

    private TaskStateEvent createTaskStateEvent(WorkflowInstanceStateChangeEvent workflowStateEventChangeRequest) {
        return TaskStateEvent.builder()
                .processInstanceId(workflowStateEventChangeRequest.getDestProcessInstanceId())
                .taskInstanceId(workflowStateEventChangeRequest.getDestTaskInstanceId())
                .type(StateEventType.TASK_STATE_CHANGE)
                .key(workflowStateEventChangeRequest.getKey())
                .build();
    }

    private WorkflowStateEvent createWorkflowStateEvent(WorkflowInstanceStateChangeEvent workflowStateEventChangeRequest) {
        WorkflowExecutionStatus workflowExecutionStatus = workflowStateEventChangeRequest.getSourceStatus();
        if (workflowStateEventChangeRequest.getSourceProcessInstanceId() != workflowStateEventChangeRequest
                .getDestProcessInstanceId()) {
            workflowExecutionStatus = WorkflowExecutionStatus.RUNNING_EXECUTION;
        }
        return WorkflowStateEvent.builder()
                .processInstanceId(workflowStateEventChangeRequest.getDestProcessInstanceId())
                .type(StateEventType.PROCESS_STATE_CHANGE)
                .status(workflowExecutionStatus)
                .key(workflowStateEventChangeRequest.getKey())
                .build();
    }
}
