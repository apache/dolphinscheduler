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
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import io.netty.channel.Channel;

/**
 * alert request processor test
 */
public class AlertRequestProcessorTest {

    private AlertDao alertDao;
    private AlertPluginManager alertPluginManager;

    private AlertRequestProcessor alertRequestProcessor;

    @Before
    public void before() {
        alertDao = PowerMockito.mock(AlertDao.class);
        alertPluginManager = PowerMockito.mock(AlertPluginManager.class);
        alertRequestProcessor = new AlertRequestProcessor(alertDao, alertPluginManager);
    }

    @Test
    public void testProcess() {
        Channel channel = PowerMockito.mock(Channel.class);
        AlertSendRequestCommand alertSendRequestCommand = new AlertSendRequestCommand(1, "title", "content");
        Command reqCommand = alertSendRequestCommand.convert2Command();
        Assert.assertEquals(CommandType.ALERT_SEND_REQUEST, reqCommand.getType());
        alertRequestProcessor.process(channel, reqCommand);
    }
}
