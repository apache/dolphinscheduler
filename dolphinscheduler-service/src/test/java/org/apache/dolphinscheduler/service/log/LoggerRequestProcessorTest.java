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

package org.apache.dolphinscheduler.service.log;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.processor.LoggerRequestProcessor;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.channel.Channel;
import java.util.LinkedList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class LoggerRequestProcessorTest {

    private MockedStatic<LoggerUtils> mockedStaticLoggerUtils;

    @BeforeEach
    public void setUp() {
        mockedStaticLoggerUtils = Mockito.mockStatic(LoggerUtils.class);
    }

    @AfterEach
    public void after() {
        mockedStaticLoggerUtils.close();
    }

    @Test
    public void testProcessViewWholeLogRequest() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(channel.writeAndFlush(Mockito.any(Command.class))).thenReturn(null);
        Mockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("");
        String userDir = System.getProperty("user.dir");
        ViewLogRequestCommand logRequestCommand = new ViewLogRequestCommand(userDir + "/log/path/a.log");

        Command command = new Command();
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        command.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        LoggerRequestProcessor loggerRequestProcessor = new LoggerRequestProcessor("logs", null);
        Assertions.assertDoesNotThrow(() -> loggerRequestProcessor.process(channel, command));
    }

    @Test
    public void testProcessViewWholeLogRequestError() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("");
        String userDir = System.getProperty("user.dir");
        ViewLogRequestCommand logRequestCommand = new ViewLogRequestCommand(userDir + "/log/path/a");

        Command command = new Command();
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        command.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        LoggerRequestProcessor loggerRequestProcessor = new LoggerRequestProcessor("logs", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> loggerRequestProcessor.process(channel, command));
    }

    @Test
    public void testProcessViewWholeLogRequestErrorRelativePath() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("");
        String userDir = System.getProperty("user.dir");
        ViewLogRequestCommand logRequestCommand = new ViewLogRequestCommand(userDir + "/log/../../a.log");

        Command command = new Command();
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        command.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        LoggerRequestProcessor loggerRequestProcessor = new LoggerRequestProcessor("logs", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> loggerRequestProcessor.process(channel, command));
    }

    @Test
    public void testProcessViewWholeLogRequestErrorStartWith() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("");
        ViewLogRequestCommand logRequestCommand = new ViewLogRequestCommand("/log/a.log");

        Command command = new Command();
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        command.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        LoggerRequestProcessor loggerRequestProcessor = new LoggerRequestProcessor("logs", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> loggerRequestProcessor.process(channel, command));
    }

    @Test
    public void testProcessViewWholeLogRequestStartWithSpecifyDirs() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("");

        String dir1 = "/var/log/dolphinscheduler/";
        String dir2 = "/var/log/dolphinscheduler_01";
        String dir3 = "/var/log/dolphinscheduler_02";
        List<String> pastDirs = new LinkedList<>();
        pastDirs.add(dir2);
        pastDirs.add(dir3);
        LoggerRequestProcessor loggerRequestProcessor = new LoggerRequestProcessor(dir1, pastDirs);

        ViewLogRequestCommand logRequestCommand = new ViewLogRequestCommand(dir1 + "/20220101/task01.log");
        Command command = new Command();
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        command.setBody(JSONUtils.toJsonByteArray(logRequestCommand));
        Assertions.assertDoesNotThrow(() -> loggerRequestProcessor.process(channel, command));

        logRequestCommand = new ViewLogRequestCommand(dir2 + "/20220101/task02.log");
        command.setBody(JSONUtils.toJsonByteArray(logRequestCommand));
        Assertions.assertDoesNotThrow(() -> loggerRequestProcessor.process(channel, command));
    }

    @Test
    public void testProcessViewWholeLogRequestStartWithSpecifyDirsError() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("");

        String dir = "/var/log/dolphinscheduler";
        LoggerRequestProcessor loggerRequestProcessor = new LoggerRequestProcessor(dir, null);
        // access log file in  /var/log/dolphinscheduler_01
        ViewLogRequestCommand logRequestCommand = new ViewLogRequestCommand(dir + "_01" + "/20220101/task01.log");
        Command command = new Command();
        command.setType(CommandType.VIEW_WHOLE_LOG_REQUEST);
        command.setBody(JSONUtils.toJsonByteArray(logRequestCommand));
        Assertions.assertThrows(IllegalArgumentException.class, () -> loggerRequestProcessor.process(channel, command));
    }
}
