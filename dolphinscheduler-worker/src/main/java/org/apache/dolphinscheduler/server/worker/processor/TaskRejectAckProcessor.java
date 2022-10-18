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

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskRejectAckCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskRejectAckProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskRejectAckProcessor.class);

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_REJECT_ACK == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        TaskRejectAckCommand taskRejectAckMessage = JSONUtils.parseObject(command.getBody(),
                TaskRejectAckCommand.class);
        if (taskRejectAckMessage == null) {
            logger.error("Receive task reject response, the response message is null");
            return;
        }

        try {
            LoggerUtils.setTaskInstanceIdMDC(taskRejectAckMessage.getTaskInstanceId());
            logger.info("Receive task reject response ack command: {}", taskRejectAckMessage);
            if (taskRejectAckMessage.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskRejectAckMessage.getTaskInstanceId(),
                        CommandType.TASK_REJECT);
                logger.debug("removeRecallCache: task instance id:{}", taskRejectAckMessage.getTaskInstanceId());
            } else {
                logger.error("Receive task reject ack message, the message status is not success, message: {}",
                        taskRejectAckMessage);
            }
        } finally {
            LoggerUtils.removeTaskInstanceIdMDC();
        }
    }
}
