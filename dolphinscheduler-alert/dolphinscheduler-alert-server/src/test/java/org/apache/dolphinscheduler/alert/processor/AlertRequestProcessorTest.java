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
import org.apache.dolphinscheduler.alert.AlertSenderService;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.channel.Channel;

@ExtendWith(MockitoExtension.class)
public class AlertRequestProcessorTest {

    @InjectMocks
    private AlertRequestProcessor alertRequestProcessor;

    @Mock
    private AlertSenderService alertSenderService;

    @Test
    public void testProcess() {
        Mockito.when(alertSenderService.syncHandler(1, "title", "content", WarningType.FAILURE.getCode()))
                .thenReturn(new AlertSendResponseCommand());
        Channel channel = mock(Channel.class);
        AlertSendRequestCommand alertSendRequestCommand =
                new AlertSendRequestCommand(1, "title", "content", WarningType.FAILURE.getCode());
        Command reqCommand = alertSendRequestCommand.convert2Command();
        Assertions.assertEquals(CommandType.ALERT_SEND_REQUEST, reqCommand.getType());
        alertRequestProcessor.process(channel, reqCommand);
    }
}
