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
import org.apache.dolphinscheduler.remote.command.task.TaskExecuteResultMessageAck;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * task execute running ack, from master to worker
 */
@Slf4j
@Component
public class WorkerTaskExecuteResultAckProcessor implements WorkerRpcProcessor {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void process(Channel channel, Message message) {
        TaskExecuteResultMessageAck taskExecuteAckMessage =
                JSONUtils.parseObject(message.getBody(), TaskExecuteResultMessageAck.class);

        if (taskExecuteAckMessage == null) {
            log.error("task execute response ack command is null");
            return;
        }

        try (
                LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                        LogUtils.setTaskInstanceIdMDC(taskExecuteAckMessage.getTaskInstanceId())) {
            log.info("Receive task execute response ack command : {}", taskExecuteAckMessage);
            if (taskExecuteAckMessage.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskExecuteAckMessage.getTaskInstanceId(),
                        MessageType.TASK_EXECUTE_RESULT_MESSAGE);
                log.debug("remove REMOTE_CHANNELS, task instance id:{}", taskExecuteAckMessage.getTaskInstanceId());
            } else {
                // master handle worker response error, will still retry
                log.error("Receive task execute result ack message, the message status is not success, message: {}",
                        taskExecuteAckMessage);
            }
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_EXECUTE_RESULT_MESSAGE_ACK;
    }

}
