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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskRejectMessage;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * task recall processor
 */
@Component
@Slf4j
public class TaskRecallProcessor implements NettyRequestProcessor {

    @Autowired
    private TaskEventService taskEventService;

    /**
     * task ack process
     *
     * @param channel channel channel
     * @param message command TaskExecuteAckCommand
     */
    @Override
    public void process(Channel channel, Message message) {
        TaskRejectMessage recallCommand = JSONUtils.parseObject(message.getBody(), TaskRejectMessage.class);
        TaskEvent taskEvent = TaskEvent.newRecallEvent(recallCommand, channel);
        try (
                final LogUtils.MDCAutoClosableContext mdcAutoClosableContext = LogUtils.setWorkflowAndTaskInstanceIDMDC(
                        recallCommand.getProcessInstanceId(), recallCommand.getTaskInstanceId())) {
            log.info("Receive task recall command: {}", recallCommand);
            taskEventService.addEvent(taskEvent);
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_REJECT;
    }
}
