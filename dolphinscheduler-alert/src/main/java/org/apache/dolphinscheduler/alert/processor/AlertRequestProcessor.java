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

package org.apache.dolphinscheduler.alert.processor;

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.runner.AlertSender;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * alert request processor
 */
public class AlertRequestProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(AlertRequestProcessor.class);
    private AlertDao alertDao;
    private AlertPluginManager alertPluginManager;

    public AlertRequestProcessor(AlertDao alertDao, AlertPluginManager alertPluginManager) {
        this.alertDao = alertDao;
        this.alertPluginManager = alertPluginManager;
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.ALERT_SEND_REQUEST == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        AlertSendRequestCommand alertSendRequestCommand = JsonSerializer.deserialize(
                command.getBody(), AlertSendRequestCommand.class);
        logger.info("received command : {}", alertSendRequestCommand);

        AlertSender alertSender = new AlertSender(alertDao, alertPluginManager);
        AlertSendResponseCommand alertSendResponseCommand = alertSender.syncHandler(alertSendRequestCommand.getGroupId(), alertSendRequestCommand.getTitle(), alertSendRequestCommand.getContent());
        channel.writeAndFlush(alertSendResponseCommand.convert2Command(command.getOpaque()));

    }
}
