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
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;

/**
 * task execute running ack, from master to worker
 */
@Slf4j
@Component
public class TaskExecuteResultAckProcessor implements NettyRequestProcessor {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_RESULT_ACK == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        TaskExecuteAckCommand taskExecuteAckMessage = JSONUtils.parseObject(command.getBody(),
                TaskExecuteAckCommand.class);

        if (taskExecuteAckMessage == null) {
            log.error("task execute response ack command is null");
            return;
        }

        try {
            LogUtils.setTaskInstanceIdMDC(taskExecuteAckMessage.getTaskInstanceId());
            log.info("Receive task execute response ack command : {}", taskExecuteAckMessage);
            if (taskExecuteAckMessage.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskExecuteAckMessage.getTaskInstanceId(),
                        CommandType.TASK_EXECUTE_RESULT);
                log.debug("remove REMOTE_CHANNELS, task instance id:{}", taskExecuteAckMessage.getTaskInstanceId());
            } else {
                // master handle worker response error, will still retry
                log.error("Receive task execute result ack message, the message status is not success, message: {}",
                        taskExecuteAckMessage);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();

        }
    }

}
