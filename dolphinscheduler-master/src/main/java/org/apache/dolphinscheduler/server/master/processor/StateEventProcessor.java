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

package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.workflow.WorkflowStateEventChangeRequest;
import org.apache.dolphinscheduler.remote.processor.MasterRpcProcessor;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowStateEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.StateEventResponseService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * handle state event received from master/api
 */
@Component
@Slf4j
public class StateEventProcessor implements MasterRpcProcessor {

    @Autowired
    private StateEventResponseService stateEventResponseService;

    @Override
    public void process(Channel channel, Message message) {
        WorkflowStateEventChangeRequest workflowStateEventChangeRequest =
                JSONUtils.parseObject(message.getBody(), WorkflowStateEventChangeRequest.class);
        StateEvent stateEvent;
        if (workflowStateEventChangeRequest.getDestTaskInstanceId() == 0) {
            stateEvent = createWorkflowStateEvent(workflowStateEventChangeRequest);
        } else {
            stateEvent = createTaskStateEvent(workflowStateEventChangeRequest);
        }

        try (
                final LogUtils.MDCAutoClosableContext mdcAutoClosableContext = LogUtils.setWorkflowAndTaskInstanceIDMDC(
                        stateEvent.getProcessInstanceId(), stateEvent.getTaskInstanceId())) {
            log.info("Received state change command, event: {}", stateEvent);
            stateEventResponseService.addStateChangeEvent(stateEvent);
        }

    }

    @Override
    public MessageType getCommandType() {
        return MessageType.STATE_EVENT_REQUEST;
    }

    private TaskStateEvent createTaskStateEvent(WorkflowStateEventChangeRequest workflowStateEventChangeRequest) {
        return TaskStateEvent.builder()
                .processInstanceId(workflowStateEventChangeRequest.getDestProcessInstanceId())
                .taskInstanceId(workflowStateEventChangeRequest.getDestTaskInstanceId())
                .type(StateEventType.TASK_STATE_CHANGE)
                .key(workflowStateEventChangeRequest.getKey())
                .build();
    }

    private WorkflowStateEvent createWorkflowStateEvent(WorkflowStateEventChangeRequest workflowStateEventChangeRequest) {
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
