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
import org.apache.dolphinscheduler.remote.command.task.TaskExecuteRunningMessageAck;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * task execute running ack processor
 */
@Component
@Slf4j
public class WorkerTaskExecuteRunningAckProcessor implements WorkerRpcProcessor {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void process(Channel channel, Message message) {
        TaskExecuteRunningMessageAck runningAckCommand = JSONUtils.parseObject(message.getBody(),
                TaskExecuteRunningMessageAck.class);
        if (runningAckCommand == null) {
            log.error("task execute running ack command is null");
            return;
        }
        try {
            LogUtils.setTaskInstanceIdMDC(runningAckCommand.getTaskInstanceId());
            log.info("task execute running ack command : {}", runningAckCommand);

            if (runningAckCommand.isSuccess()) {
                messageRetryRunner.removeRetryMessage(runningAckCommand.getTaskInstanceId(),
                        MessageType.TASK_EXECUTE_RUNNING_MESSAGE);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_EXECUTE_RUNNING_MESSAGE_ACK;
    }

}
