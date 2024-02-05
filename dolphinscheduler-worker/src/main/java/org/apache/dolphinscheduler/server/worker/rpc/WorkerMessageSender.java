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

import org.apache.dolphinscheduler.extract.master.transportor.ITaskInstanceExecutionEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.message.TaskInstanceExecutionEventSender;

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
    private List<TaskInstanceExecutionEventSender> messageSenders;

    private final Map<ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType, TaskInstanceExecutionEventSender> messageSenderMap =
            new HashMap<>();

    @PostConstruct
    public void init() {
        messageSenders.forEach(messageSender -> messageSenderMap.put(messageSender.getMessageType(),
                messageSender));
    }

    // todo: use message rather than context
    public void sendMessageWithRetry(@NonNull TaskExecutionContext taskExecutionContext,
                                     @NonNull ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType eventType) {
        TaskInstanceExecutionEventSender messageSender = messageSenderMap.get(eventType);
        if (messageSender == null) {
            throw new IllegalArgumentException("The messageType is invalidated, messageType: " + eventType);
        }
        ITaskInstanceExecutionEvent iTaskInstanceExecutionEvent = messageSender.buildEvent(taskExecutionContext);
        try {
            messageRetryRunner.addRetryMessage(taskExecutionContext.getTaskInstanceId(), iTaskInstanceExecutionEvent);
            messageSender.sendEvent(iTaskInstanceExecutionEvent);
        } catch (Exception e) {
            log.error("Send message error, eventType: {}, event: {}", eventType, iTaskInstanceExecutionEvent);
        }
    }

    public void sendMessage(@NonNull TaskExecutionContext taskExecutionContext,
                            @NonNull ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType taskInstanceExecutionEventType) {
        TaskInstanceExecutionEventSender messageSender = messageSenderMap.get(taskInstanceExecutionEventType);
        if (messageSender == null) {
            throw new IllegalArgumentException(
                    "The eventType is invalidated, eventType: " + taskInstanceExecutionEventType);
        }
        ITaskInstanceExecutionEvent iTaskInstanceExecutionEvent = messageSender.buildEvent(taskExecutionContext);
        try {
            messageSender.sendEvent(iTaskInstanceExecutionEvent);
        } catch (Exception e) {
            log.error("Send message error, eventType: {}, event: {}", taskInstanceExecutionEventType,
                    iTaskInstanceExecutionEvent);
        }
    }

}
