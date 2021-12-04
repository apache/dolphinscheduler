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

package org.apache.dolphinscheduler.alert;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
public final class AlertRequestProcessor implements NettyRequestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AlertRequestProcessor.class);

    private final AlertSender alertSender;

    public AlertRequestProcessor(AlertSender alertSender) {
        this.alertSender = alertSender;
    }

    @Override
    public void process(Channel channel, Command command) {
        checkArgument(CommandType.ALERT_SEND_REQUEST == command.getType(), "invalid command type: %s", command.getType());

        AlertSendRequestCommand alertSendRequestCommand = JsonSerializer.deserialize(
            command.getBody(), AlertSendRequestCommand.class);

        logger.info("Received command : {}", alertSendRequestCommand);

        AlertSendResponseCommand alertSendResponseCommand = alertSender.syncHandler(
            alertSendRequestCommand.getGroupId(),
            alertSendRequestCommand.getTitle(),
            alertSendRequestCommand.getContent());
        channel.writeAndFlush(alertSendResponseCommand.convert2Command(command.getOpaque()));
    }
}
