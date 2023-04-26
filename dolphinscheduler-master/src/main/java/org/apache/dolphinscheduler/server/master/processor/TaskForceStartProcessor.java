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
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskForceStartRequest;
import org.apache.dolphinscheduler.remote.processor.MasterRpcProcessor;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.StateEventResponseService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
@Slf4j
public class TaskForceStartProcessor implements MasterRpcProcessor {

    @Autowired
    private StateEventResponseService stateEventResponseService;

    @Override
    public void process(Channel channel, Message message) {
        TaskForceStartRequest taskEventChangeCommand =
                JSONUtils.parseObject(message.getBody(), TaskForceStartRequest.class);
        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(taskEventChangeCommand.getProcessInstanceId())
                .taskInstanceId(taskEventChangeCommand.getTaskInstanceId())
                .key(taskEventChangeCommand.getKey())
                .type(StateEventType.WAKE_UP_TASK_GROUP)
                .build();
        try (
                LogUtils.MDCAutoClosableContext mdcAutoClosableContext = LogUtils.setWorkflowAndTaskInstanceIDMDC(
                        stateEvent.getProcessInstanceId(), stateEvent.getTaskInstanceId())) {
            log.info("Received task event change command, event: {}", stateEvent);
            stateEventResponseService.addEvent2WorkflowExecute(stateEvent);
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_FORCE_STATE_EVENT_REQUEST;
    }
}
