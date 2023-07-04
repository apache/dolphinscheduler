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
import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequest;
import org.apache.dolphinscheduler.remote.processor.ViewWholeLogProcessor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.channel.Channel;

@ExtendWith(MockitoExtension.class)
public class LoggerRequestProcessorTest {

    private MockedStatic<LogUtils> mockedStaticLoggerUtils;

    @BeforeEach
    public void setUp() {
        mockedStaticLoggerUtils = Mockito.mockStatic(LogUtils.class);
    }

    @AfterEach
    public void after() {
        mockedStaticLoggerUtils.close();
    }

    @Test
    public void testProcessViewWholeLogRequest() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(channel.writeAndFlush(Mockito.any(Message.class))).thenReturn(null);
        Mockito.when(LogUtils.readWholeFileContentFromLocal(Mockito.anyString())).thenReturn("");
        String userDir = System.getProperty("user.dir");
        ViewLogRequest logRequestCommand = new ViewLogRequest(userDir + "/log/path/a.log");

        Message message = new Message();
        message.setType(MessageType.VIEW_WHOLE_LOG_REQUEST);
        message.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        ViewWholeLogProcessor loggerRequestProcessor = new ViewWholeLogProcessor();
        loggerRequestProcessor.process(channel, message);
    }

    @Test
    public void testProcessViewWholeLogRequestError() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(LogUtils.readWholeFileContentFromLocal(Mockito.anyString())).thenReturn("");
        String userDir = System.getProperty("user.dir");
        ViewLogRequest logRequestCommand = new ViewLogRequest(userDir + "/log/path/a");

        Message message = new Message();
        message.setType(MessageType.VIEW_WHOLE_LOG_REQUEST);
        message.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        ViewWholeLogProcessor loggerRequestProcessor = new ViewWholeLogProcessor();
        loggerRequestProcessor.process(channel, message);
    }

    @Test
    public void testProcessViewWholeLogRequestErrorRelativePath() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(LogUtils.readWholeFileContentFromLocal(Mockito.anyString())).thenReturn("");
        String userDir = System.getProperty("user.dir");
        ViewLogRequest logRequestCommand = new ViewLogRequest(userDir + "/log/../../a.log");

        Message message = new Message();
        message.setType(MessageType.VIEW_WHOLE_LOG_REQUEST);
        message.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        ViewWholeLogProcessor loggerRequestProcessor = new ViewWholeLogProcessor();
        loggerRequestProcessor.process(channel, message);
    }

    @Test
    public void testProcessViewWholeLogRequestErrorStartWith() {
        System.setProperty("DOLPHINSCHEDULER_WORKER_HOME", System.getProperty("user.dir"));
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(LogUtils.readWholeFileContentFromLocal(Mockito.anyString())).thenReturn("");
        ViewLogRequest logRequestCommand = new ViewLogRequest("/log/a.log");

        Message message = new Message();
        message.setType(MessageType.VIEW_WHOLE_LOG_REQUEST);
        message.setBody(JSONUtils.toJsonByteArray(logRequestCommand));

        ViewWholeLogProcessor loggerRequestProcessor = new ViewWholeLogProcessor();
        loggerRequestProcessor.process(channel, message);
    }
}
