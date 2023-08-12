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

package org.apache.dolphinscheduler.server.worker.message;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.BaseMessage;
import org.apache.dolphinscheduler.remote.command.MessageType;

import org.apache.commons.collections4.MapUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.base.Objects;

@Component
@Slf4j
public class MessageRetryRunner extends BaseDaemonThread {

    protected MessageRetryRunner() {
        super("WorkerMessageRetryRunnerThread");
    }

    private static final long MESSAGE_RETRY_WINDOW = Duration.ofMinutes(5L).toMillis();

    @Lazy
    @Autowired
    private List<MessageSender> messageSenders;

    private final Map<MessageType, MessageSender<BaseMessage>> messageSenderMap = new HashMap<>();

    private final Map<Integer, List<TaskInstanceMessage>> needToRetryMessages = new ConcurrentHashMap<>();

    @Override
    public synchronized void start() {
        log.info("Message retry runner staring");
        messageSenders.forEach(messageSender -> {
            messageSenderMap.put(messageSender.getMessageType(), messageSender);
            log.info("Injected message sender: {}", messageSender.getClass().getName());
        });
        super.start();
        log.info("Message retry runner started");
    }

    public void addRetryMessage(int taskInstanceId, @NonNull MessageType messageType, BaseMessage baseMessage) {
        needToRetryMessages.computeIfAbsent(taskInstanceId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(TaskInstanceMessage.of(taskInstanceId, messageType, baseMessage));
    }

    public void removeRetryMessage(int taskInstanceId, @NonNull MessageType messageType) {
        List<TaskInstanceMessage> taskInstanceMessages = needToRetryMessages.get(taskInstanceId);
        if (taskInstanceMessages != null) {
            taskInstanceMessages.remove(TaskInstanceMessage.of(taskInstanceId, messageType, null));
        }
    }

    public void removeRetryMessages(int taskInstanceId) {
        needToRetryMessages.remove(taskInstanceId);
    }

    public void updateMessageHost(int taskInstanceId, String messageReceiverHost) {
        List<TaskInstanceMessage> taskInstanceMessages = this.needToRetryMessages.get(taskInstanceId);
        if (taskInstanceMessages != null) {
            taskInstanceMessages.forEach(taskInstanceMessage -> {
                taskInstanceMessage.getMessage().setMessageReceiverAddress(messageReceiverHost);
            });
        }
    }

    public void run() {
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                if (MapUtils.isEmpty(needToRetryMessages)) {
                    Thread.sleep(MESSAGE_RETRY_WINDOW);
                }

                long now = System.currentTimeMillis();
                Iterator<Map.Entry<Integer, List<TaskInstanceMessage>>> iterator =
                        needToRetryMessages.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, List<TaskInstanceMessage>> taskEntry = iterator.next();
                    Integer taskInstanceId = taskEntry.getKey();
                    List<TaskInstanceMessage> taskInstanceMessages = taskEntry.getValue();
                    if (taskInstanceMessages.isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                    LogUtils.setTaskInstanceIdMDC(taskInstanceId);
                    try {
                        for (TaskInstanceMessage taskInstanceMessage : taskInstanceMessages) {
                            MessageType messageType = taskInstanceMessage.getMessageType();
                            BaseMessage message = taskInstanceMessage.getMessage();
                            if (now - message.getMessageSendTime() > MESSAGE_RETRY_WINDOW) {
                                log.info("Begin retry send message to master, message: {}", message);
                                message.setMessageSendTime(now);
                                messageSenderMap.get(messageType).sendMessage(message);
                                log.info("Success send message to master, message: {}", message);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Retry send message to master error", e);
                    } finally {
                        LogUtils.removeTaskInstanceIdMDC();
                    }
                }
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (InterruptedException instance) {
                log.warn("The message retry thread is interrupted, will break this loop", instance);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                log.error("Retry send message failed, get an known exception.", ex);
            }
        }
    }

    public void clearMessage() {
        needToRetryMessages.clear();
    }

    /**
     * If two message has the same taskInstanceId and messageType they will be considered as the same message
     */
    @Data
    public static class TaskInstanceMessage {

        private long taskInstanceId;
        private MessageType messageType;
        private BaseMessage message;

        public static TaskInstanceMessage of(long taskInstanceId, MessageType messageType, BaseMessage message) {
            TaskInstanceMessage taskInstanceMessage = new TaskInstanceMessage();
            taskInstanceMessage.setTaskInstanceId(taskInstanceId);
            taskInstanceMessage.setMessageType(messageType);
            taskInstanceMessage.setMessage(message);
            return taskInstanceMessage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TaskInstanceMessage that = (TaskInstanceMessage) o;
            return taskInstanceId == that.taskInstanceId && messageType == that.messageType;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(taskInstanceId, messageType);
        }
    }
}
