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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.BaseMessage;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.message.MessageSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkerMessageSender {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Autowired
    private List<MessageSender> messageSenders;

    private Map<MessageType, MessageSender> messageSenderMap = new HashMap<>();

    @PostConstruct
    public void init() {
        messageSenders.forEach(messageSender -> messageSenderMap.put(messageSender.getMessageType(),
                messageSender));
    }

    // todo: use message rather than context
    public void sendMessageWithRetry(@NonNull TaskExecutionContext taskExecutionContext,
                                     @NonNull MessageType messageType) {
        MessageSender messageSender = messageSenderMap.get(messageType);
        if (messageSender == null) {
            throw new IllegalArgumentException("The messageType is invalidated, messageType: " + messageType);
        }
        BaseMessage baseMessage = messageSender.buildMessage(taskExecutionContext);
        try {
            messageRetryRunner.addRetryMessage(taskExecutionContext.getTaskInstanceId(), messageType, baseMessage);
            messageSender.sendMessage(baseMessage);
        } catch (RemotingException e) {
            log.error("Send message error, messageType: {}, message: {}", messageType, baseMessage);
        }
    }

    public void sendMessage(@NonNull TaskExecutionContext taskExecutionContext, @NonNull MessageType messageType) {
        MessageSender messageSender = messageSenderMap.get(messageType);
        if (messageSender == null) {
            throw new IllegalArgumentException("The messageType is invalidated, messageType: " + messageType);
        }
        BaseMessage baseMessage = messageSender.buildMessage(taskExecutionContext);
        try {
            messageSender.sendMessage(baseMessage);
        } catch (RemotingException e) {
            log.error("Send message error, messageType: {}, message: {}", messageType, baseMessage);
        }
    }

}
