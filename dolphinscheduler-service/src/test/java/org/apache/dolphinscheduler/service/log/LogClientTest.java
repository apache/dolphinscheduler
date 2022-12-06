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
<<<<<<< HEAD
=======
import org.apache.dolphinscheduler.remote.factory.NettyRemotingClientFactory;
>>>>>>> refs/remotes/origin/3.1.1-release
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;

import java.nio.charset.StandardCharsets;

<<<<<<< HEAD
import org.junit.Assert;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogClient.class, NetUtils.class, LoggerUtils.class, NettyRemotingClient.class})
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
>>>>>>> refs/remotes/origin/3.1.1-release
public class LogClientTest {

    @Test
    public void testViewLogFromLocal() {
<<<<<<< HEAD
//        String localMachine = "LOCAL_MACHINE";
//        int port = 1234;
//        String path = "/tmp/log";
//
//        PowerMockito.mockStatic(NetUtils.class);
//        PowerMockito.when(NetUtils.getHost()).thenReturn(localMachine);
//        PowerMockito.mockStatic(LoggerUtils.class);
//        PowerMockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("application_xx_11");
//
//        LogClient logClient = new LogClient();
//        String log = logClient.viewLog(localMachine, port, path);
//        Assert.assertNotNull(log);
=======
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
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testViewLogFromRemote() throws Exception {
<<<<<<< HEAD
//        String localMachine = "127.0.0.1";
//        int port = 1234;
//        String path = "/tmp/log";
//
//        PowerMockito.mockStatic(NetUtils.class);
//        PowerMockito.when(NetUtils.getHost()).thenReturn(localMachine + "1");
//
//        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
//        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);
//
//        Command command = new Command();
//        command.setBody(JSONUtils.toJsonString(new ViewLogResponseCommand("")).getBytes(StandardCharsets.UTF_8));
//        PowerMockito.when(remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
//                .thenReturn(command);
//        LogClient logClient = new LogClient();
//        String log = logClient.viewLog(localMachine, port, path);
//        Assert.assertNotNull(log);
    }

    @Test(expected = None.class)
    public void testClose() throws Exception {
        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);
        PowerMockito.doNothing().when(remotingClient).close();

        LogClient logClient = new LogClient();
        logClient.close();
=======
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
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testRollViewLog() throws Exception {
<<<<<<< HEAD
        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);

        Command command = new Command();
        command.setBody(JSONUtils.toJsonByteArray(new RollViewLogResponseCommand("success")));
        PowerMockito.when(remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                .thenReturn(command);

        LogClient logClient = new LogClient();
        String msg = logClient.rollViewLog("localhost", 1234, "/tmp/log", 0, 10);
        Assert.assertNotNull(msg);
=======
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
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testGetLogBytes() throws Exception {
<<<<<<< HEAD
        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);

        Command command = new Command();
        command.setBody(JSONUtils.toJsonByteArray(new GetLogBytesResponseCommand("log".getBytes(StandardCharsets.UTF_8))));
        PowerMockito.when(remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                .thenReturn(command);

        LogClient logClient = new LogClient();
        byte[] logBytes = logClient.getLogBytes("localhost", 1234, "/tmp/log");
        Assert.assertNotNull(logBytes);
=======
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
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void testRemoveTaskLog() throws Exception {
<<<<<<< HEAD
        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);

        Command command = new Command();
        command.setBody(JSONUtils.toJsonByteArray(new RemoveTaskLogResponseCommand(true)));
        PowerMockito.when(remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                .thenReturn(command);

        LogClient logClient = new LogClient();
        Boolean status = logClient.removeTaskLog("localhost", 1234, "/log/path");
        Assert.assertTrue(status);
    }

=======

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
            Boolean status = logClient.removeTaskLog(Host.of("localhost:1234"), "/log/path");
            Assertions.assertTrue(status);
        }
    }
>>>>>>> refs/remotes/origin/3.1.1-release
}
