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
import org.apache.dolphinscheduler.remote.command.BaseCommand;
import org.apache.dolphinscheduler.remote.command.CommandType;

import org.apache.commons.collections4.MapUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageRetryRunner extends BaseDaemonThread {

    protected MessageRetryRunner() {
        super("WorkerMessageRetryRunnerThread");
    }

    private static long MESSAGE_RETRY_WINDOW = Duration.ofMinutes(5L).toMillis();

    @Lazy
    @Autowired
    private List<MessageSender> messageSenders;

    private Map<CommandType, MessageSender<BaseCommand>> messageSenderMap = new HashMap<>();

    private Map<Integer, Map<CommandType, BaseCommand>> needToRetryMessages = new ConcurrentHashMap<>();

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

    public void addRetryMessage(int taskInstanceId, @NonNull CommandType messageType, BaseCommand baseCommand) {
        needToRetryMessages.computeIfAbsent(taskInstanceId, k -> new ConcurrentHashMap<>()).put(messageType,
                baseCommand);
    }

    public void removeRetryMessage(int taskInstanceId, @NonNull CommandType messageType) {
        Map<CommandType, BaseCommand> retryMessages = needToRetryMessages.get(taskInstanceId);
        if (retryMessages != null) {
            retryMessages.remove(messageType);
        }
    }

    public void removeRetryMessages(int taskInstanceId) {
        needToRetryMessages.remove(taskInstanceId);
    }

    public void updateMessageHost(int taskInstanceId, String messageReceiverHost) {
        Map<CommandType, BaseCommand> needToRetryMessages = this.needToRetryMessages.get(taskInstanceId);
        if (needToRetryMessages != null) {
            needToRetryMessages.values().forEach(baseMessage -> {
                baseMessage.setMessageReceiverAddress(messageReceiverHost);
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
                Iterator<Map.Entry<Integer, Map<CommandType, BaseCommand>>> iterator =
                        needToRetryMessages.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Map<CommandType, BaseCommand>> taskEntry = iterator.next();
                    Integer taskInstanceId = taskEntry.getKey();
                    Map<CommandType, BaseCommand> retryMessageMap = taskEntry.getValue();
                    if (retryMessageMap.isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                    LogUtils.setTaskInstanceIdMDC(taskInstanceId);
                    try {
                        for (Map.Entry<CommandType, BaseCommand> messageEntry : retryMessageMap.entrySet()) {
                            CommandType messageType = messageEntry.getKey();
                            BaseCommand message = messageEntry.getValue();
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
}
