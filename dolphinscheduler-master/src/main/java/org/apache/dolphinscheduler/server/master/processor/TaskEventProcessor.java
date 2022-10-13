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

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.StateEventResponseService;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * handle state event received from master/api
 */
@Component
public class TaskEventProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskEventProcessor.class);

    @Autowired
    private StateEventResponseService stateEventResponseService;

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_FORCE_STATE_EVENT_REQUEST == command.getType()
                || CommandType.TASK_WAKEUP_EVENT_REQUEST == command.getType(),
                String.format("invalid command type: %s", command.getType()));

        TaskEventChangeCommand taskEventChangeCommand =
                JSONUtils.parseObject(command.getBody(), TaskEventChangeCommand.class);
        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(taskEventChangeCommand.getProcessInstanceId())
                .taskInstanceId(taskEventChangeCommand.getTaskInstanceId())
                .key(taskEventChangeCommand.getKey())
                .type(StateEventType.WAIT_TASK_GROUP)
                .build();
        try {
            LoggerUtils.setWorkflowAndTaskInstanceIDMDC(stateEvent.getProcessInstanceId(),
                    stateEvent.getTaskInstanceId());
            logger.info("Received task event change command, event: {}", stateEvent);
            stateEventResponseService.addEvent2WorkflowExecute(stateEvent);
        } finally {
            LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }

}
