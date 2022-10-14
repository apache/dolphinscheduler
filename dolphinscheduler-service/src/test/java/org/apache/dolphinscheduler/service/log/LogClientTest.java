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
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.factory.NettyRemotingClientFactory;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
public class LogClientTest {

    @Test
    public void testViewLogFromLocal() {
        String localMachine = "LOCAL_MACHINE";
        int port = 1234;
        String path = "/tmp/log";

        try (
                MockedStatic<NetUtils> mockedNetUtils = Mockito.mockStatic(NetUtils.class);
                MockedStatic<LoggerUtils> mockedLoggerUtils = Mockito.mockStatic(LoggerUtils.class)) {
            mockedNetUtils.when(NetUtils::getHost)
                    .thenReturn(localMachine);
            mockedLoggerUtils.when(() -> LoggerUtils.readWholeFileContent(Mockito.anyString()))
                    .thenReturn("application_xx_11");
            LogClient logClient = new LogClient();
            String log = logClient.viewLog(localMachine, port, path);
            Assertions.assertNotNull(log);
        }
    }

    @Test
    public void testViewLogFromRemote() throws Exception {
        String localMachine = "127.0.0.1";
        int port = 1234;
        String path = "/tmp/log";

        try (MockedStatic<NetUtils> mockedNetUtils = Mockito.mockStatic(NetUtils.class)) {
            mockedNetUtils.when(NetUtils::getHost)
                    .thenReturn(localMachine + "1");
            LogClient logClient = new LogClient();
            String log = logClient.viewLog(localMachine, port, path);
            Assertions.assertNotNull(log);
        }

        Command command = new Command();
        command.setBody(JSONUtils.toJsonString(new ViewLogResponseCommand("")).getBytes(StandardCharsets.UTF_8));
        LogClient logClient = new LogClient();
        String log = logClient.viewLog(localMachine, port, path);
        Assertions.assertNotNull(log);
    }

    @Test
    public void testClose() {
        try (
                MockedStatic<NettyRemotingClientFactory> mockedNettyRemotingClientFactory =
                        Mockito.mockStatic(NettyRemotingClientFactory.class)) {
            NettyRemotingClient remotingClient = Mockito.mock(NettyRemotingClient.class);
            mockedNettyRemotingClientFactory.when(NettyRemotingClientFactory::buildNettyRemotingClient)
                    .thenReturn(remotingClient);
            LogClient logClient = new LogClient();
            logClient.close();
        }
    }

    @Test
    public void testRollViewLog() throws Exception {
        try (
                MockedStatic<NettyRemotingClientFactory> mockedNettyRemotingClientFactory =
                        Mockito.mockStatic(NettyRemotingClientFactory.class)) {
            NettyRemotingClient remotingClient = Mockito.mock(NettyRemotingClient.class);
            mockedNettyRemotingClientFactory.when(NettyRemotingClientFactory::buildNettyRemotingClient)
                    .thenReturn(remotingClient);
            Command command = new Command();
            command.setBody(JSONUtils.toJsonByteArray(new RollViewLogResponseCommand("success")));
            Mockito.when(
                    remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                    .thenReturn(command);

            LogClient logClient = new LogClient();
            String msg = logClient.rollViewLog("localhost", 1234, "/tmp/log", 0, 10);
            Assertions.assertNotNull(msg);
        }
    }

    @Test
    public void testGetLogBytes() throws Exception {
        try (
                MockedStatic<NettyRemotingClientFactory> mockedNettyRemotingClientFactory =
                        Mockito.mockStatic(NettyRemotingClientFactory.class)) {
            NettyRemotingClient remotingClient = Mockito.mock(NettyRemotingClient.class);
            mockedNettyRemotingClientFactory.when(NettyRemotingClientFactory::buildNettyRemotingClient)
                    .thenReturn(remotingClient);
            Command command = new Command();
            command.setBody(
                    JSONUtils.toJsonByteArray(new GetLogBytesResponseCommand("log".getBytes(StandardCharsets.UTF_8))));
            Mockito.when(
                    remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                    .thenReturn(command);

            LogClient logClient = new LogClient();
            byte[] logBytes = logClient.getLogBytes("localhost", 1234, "/tmp/log");
            Assertions.assertNotNull(logBytes);
        }
    }

    @Test
    public void testRemoveTaskLog() throws Exception {

        try (
                MockedStatic<NettyRemotingClientFactory> mockedNettyRemotingClientFactory =
                        Mockito.mockStatic(NettyRemotingClientFactory.class)) {
            NettyRemotingClient remotingClient = Mockito.mock(NettyRemotingClient.class);
            mockedNettyRemotingClientFactory.when(NettyRemotingClientFactory::buildNettyRemotingClient)
                    .thenReturn(remotingClient);
            Command command = new Command();
            command.setBody(JSONUtils.toJsonByteArray(new RemoveTaskLogResponseCommand(true)));
            Mockito.when(
                    remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                    .thenReturn(command);

            LogClient logClient = new LogClient();
            Boolean status = logClient.removeTaskLog("localhost", 1234, "/log/path");
            Assertions.assertTrue(status);
        }
    }
}
