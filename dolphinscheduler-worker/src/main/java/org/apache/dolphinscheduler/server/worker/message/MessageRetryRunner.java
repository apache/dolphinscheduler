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

import lombok.NonNull;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.remote.command.BaseCommand;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageRetryRunner extends BaseDaemonThread {

    private final Logger logger = LoggerFactory.getLogger(MessageRetryRunner.class);

    protected MessageRetryRunner() {
        super("WorkerMessageRetryRunnerThread");
    }

    private static long MESSAGE_RETRY_WINDOW = Duration.ofMinutes(5L).toMillis();

    @Autowired
    private ApplicationContext applicationContext;

    private Map<CommandType, MessageSender<BaseCommand>> messageSenderMap = new HashMap<>();

    private Map<Integer, Map<CommandType, BaseCommand>> needToRetryMessages = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Map<String, MessageSender> messageSenders = applicationContext.getBeansOfType(MessageSender.class);
        messageSenders.values().forEach(messageSender -> {
            messageSenderMap.put(messageSender.getMessageType(), messageSender);
            logger.info("Injected message sender: {}", messageSender.getClass().getName());
        });
    }

    @Override
    public synchronized void start() {
        logger.info("Message retry runner staring");
        super.start();
        logger.info("Message retry runner started");
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
                if (needToRetryMessages.isEmpty()) {
                    Thread.sleep(MESSAGE_RETRY_WINDOW);
                }

                long now = System.currentTimeMillis();
                for (Map.Entry<Integer, Map<CommandType, BaseCommand>> taskEntry : needToRetryMessages.entrySet()) {
                    Integer taskInstanceId = taskEntry.getKey();
                    LoggerUtils.setTaskInstanceIdMDC(taskInstanceId);
                    try {
                        for (Map.Entry<CommandType, BaseCommand> messageEntry : taskEntry.getValue().entrySet()) {
                            CommandType messageType = messageEntry.getKey();
                            BaseCommand message = messageEntry.getValue();
                            if (now - message.getMessageSendTime() > MESSAGE_RETRY_WINDOW) {
                                logger.info("Begin retry send message to master, message: {}", message);
                                message.setMessageSendTime(now);
                                messageSenderMap.get(messageType).sendMessage(message);
                                logger.info("Success send message to master, message: {}", message);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Retry send message to master error", e);
                    } finally {
                        LoggerUtils.removeTaskInstanceIdMDC();
                    }
                }
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (InterruptedException instance) {
                logger.warn("The message retry thread is interrupted, will break this loop", instance);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                logger.error("Retry send message failed, get an known exception.", ex);
            }
        }
    }

    public void clearMessage() {
        needToRetryMessages.clear();
    }
}
