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
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningAckMessage;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;

/**
 * task execute running ack processor
 */
@Component
@Slf4j
public class TaskExecuteRunningAckProcessor implements NettyRequestProcessor {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_RUNNING_ACK == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        TaskExecuteRunningAckMessage runningAckCommand = JSONUtils.parseObject(command.getBody(),
                TaskExecuteRunningAckMessage.class);
        if (runningAckCommand == null) {
            log.error("task execute running ack command is null");
            return;
        }
        try {
            LogUtils.setTaskInstanceIdMDC(runningAckCommand.getTaskInstanceId());
            log.info("task execute running ack command : {}", runningAckCommand);

            if (runningAckCommand.isSuccess()) {
                messageRetryRunner.removeRetryMessage(runningAckCommand.getTaskInstanceId(),
                        CommandType.TASK_EXECUTE_RUNNING);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

}
