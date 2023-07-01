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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskUpdateRuntimeAckMessage;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * task execute running ack processor
 */
@Component
@Slf4j
public class WorkerTaskUpdatePidAckProcessor implements WorkerRpcProcessor {

    @Resource
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void process(Channel channel, Message message) {

        TaskUpdateRuntimeAckMessage updatePidAckCommand = JSONUtils.parseObject(message.getBody(),
                TaskUpdateRuntimeAckMessage.class);
        if (updatePidAckCommand == null) {
            log.error("task execute update pid ack command is null");
            return;
        }
        try (
                LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                        LogUtils.setTaskInstanceIdMDC(updatePidAckCommand.getTaskInstanceId());) {
            LogUtils.setTaskInstanceIdMDC(updatePidAckCommand.getTaskInstanceId());
            log.info("task execute update pid ack command : {}", updatePidAckCommand);

            if (updatePidAckCommand.isSuccess()) {
                messageRetryRunner.removeRetryMessage(updatePidAckCommand.getTaskInstanceId(),
                        MessageType.TASK_UPDATE_RUNTIME_MESSAGE);
            }
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_UPDATE_RUNTIME_MESSAGE_ACK;
    }

}
