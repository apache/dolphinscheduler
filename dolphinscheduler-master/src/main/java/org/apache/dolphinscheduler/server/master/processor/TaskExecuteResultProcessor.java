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
import org.apache.dolphinscheduler.remote.command.task.TaskExecuteResultMessage;
import org.apache.dolphinscheduler.remote.processor.MasterRpcProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * task execute response processor
 */
@Component
@Slf4j
public class TaskExecuteResultProcessor implements MasterRpcProcessor {

    @Autowired
    private TaskEventService taskEventService;

    /**
     * task final result response
     * need master process , state persistence
     *
     * @param channel channel
     * @param message command
     */
    @Override
    public void process(Channel channel, Message message) {
        TaskExecuteResultMessage taskExecuteResultMessage = JSONUtils.parseObject(message.getBody(),
                TaskExecuteResultMessage.class);
        TaskEvent taskResultEvent = TaskEvent.newResultEvent(taskExecuteResultMessage,
                channel,
                taskExecuteResultMessage.getMessageSenderAddress());
        try (
                final LogUtils.MDCAutoClosableContext mdcAutoClosableContext = LogUtils.setWorkflowAndTaskInstanceIDMDC(
                        taskResultEvent.getProcessInstanceId(), taskResultEvent.getTaskInstanceId())) {
            log.info("Received task execute result, event: {}", taskResultEvent);

            taskEventService.addEvent(taskResultEvent);
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_EXECUTE_RESULT_MESSAGE;
    }
}
