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

package org.apache.dolphinscheduler.server.log;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequestCommand;

import org.junit.Test;
import org.junit.Test.None;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.netty.channel.Channel;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerUtils.class})
public class LoggerRequestProcessorTest {

    @Test(expected = None.class)
    public void testProcessViewWholeLogRequest() {
        Channel channel = PowerMockito.mock(Channel.class);
        PowerMockito.when(channel.writeAndFlush(Mockito.any(Command.class))).thenReturn(null);
        PowerMockito.mockStatic(LoggerUtils.class);
        PowerMockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("");

        ViewLogRequestCommand logRequestCommand = new ViewLogRequestCommand("/log/path");

        Command command = new Command();
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        command.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        LoggerRequestProcessor loggerRequestProcessor = new LoggerRequestProcessor();
        loggerRequestProcessor.process(channel, command);
    }
}