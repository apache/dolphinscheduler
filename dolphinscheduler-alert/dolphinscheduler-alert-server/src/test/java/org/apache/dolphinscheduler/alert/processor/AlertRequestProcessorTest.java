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

import static org.mockito.Mockito.mock;

import org.apache.dolphinscheduler.alert.AlertRequestProcessor;
import org.apache.dolphinscheduler.alert.AlertSender;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.netty.channel.Channel;

public class AlertRequestProcessorTest {
    private AlertRequestProcessor alertRequestProcessor;

    @Before
    public void before() {
        final AlertDao alertDao = mock(AlertDao.class);
        alertRequestProcessor = new AlertRequestProcessor(new AlertSender(alertDao, null));
    }

    @Test
    public void testProcess() {
        Channel channel = mock(Channel.class);
        AlertSendRequestCommand alertSendRequestCommand = new AlertSendRequestCommand(1, "title", "content", WarningType.FAILURE.getCode());
        Command reqCommand = alertSendRequestCommand.convert2Command();
        Assert.assertEquals(CommandType.ALERT_SEND_REQUEST, reqCommand.getType());
        alertRequestProcessor.process(channel, reqCommand);
    }
}
