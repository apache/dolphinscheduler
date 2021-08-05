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
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogClientService.class, NetUtils.class, LoggerUtils.class, NettyRemotingClient.class})
public class LogClientServiceTest {

    @Test
    public void testViewLogFromLocal() {
        String localMachine = "LOCAL_MACHINE";
        int port = 1234;
        String path = "/tmp/log";

        PowerMockito.mockStatic(NetUtils.class);
        PowerMockito.when(NetUtils.getHost()).thenReturn(localMachine);
        PowerMockito.mockStatic(LoggerUtils.class);
        PowerMockito.when(LoggerUtils.readWholeFileContent(Mockito.anyString())).thenReturn("application_xx_11");

        LogClientService logClientService = new LogClientService();
        String log = logClientService.viewLog(localMachine, port, path);
        Assert.assertNotNull(log);
    }

    @Test
    public void testViewLogFromRemote() throws Exception {
        String localMachine = "127.0.0.1";
        int port = 1234;
        String path = "/tmp/log";

        PowerMockito.mockStatic(NetUtils.class);
        PowerMockito.when(NetUtils.getHost()).thenReturn(localMachine + "1");

        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);

        Command command = new Command();
        command.setBody(JSONUtils.toJsonString(new ViewLogResponseCommand("")).getBytes(StandardCharsets.UTF_8));
        PowerMockito.when(remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                .thenReturn(command);
        LogClientService logClientService = new LogClientService();
        String log = logClientService.viewLog(localMachine, port, path);
        Assert.assertNotNull(log);
    }

    @Test(expected = None.class)
    public void testClose() throws Exception {
        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);
        PowerMockito.doNothing().when(remotingClient).close();

        LogClientService logClientService = new LogClientService();
        logClientService.close();
    }

    @Test
    public void testRollViewLog() throws Exception {
        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);

        Command command = new Command();
        command.setBody(JSONUtils.toJsonByteArray(new RollViewLogResponseCommand("success")));
        PowerMockito.when(remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                .thenReturn(command);

        LogClientService logClientService = new LogClientService();
        String msg = logClientService.rollViewLog("localhost", 1234, "/tmp/log", 0, 10);
        Assert.assertNotNull(msg);
    }

    @Test
    public void testGetLogBytes() throws Exception {
        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);

        Command command = new Command();
        command.setBody(JSONUtils.toJsonByteArray(new GetLogBytesResponseCommand("log".getBytes(StandardCharsets.UTF_8))));
        PowerMockito.when(remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                .thenReturn(command);

        LogClientService logClientService = new LogClientService();
        byte[] logBytes = logClientService.getLogBytes("localhost", 1234, "/tmp/log");
        Assert.assertNotNull(logBytes);
    }

    @Test
    public void testRemoveTaskLog() throws Exception {
        NettyRemotingClient remotingClient = PowerMockito.mock(NettyRemotingClient.class);
        PowerMockito.whenNew(NettyRemotingClient.class).withAnyArguments().thenReturn(remotingClient);

        Command command = new Command();
        command.setBody(JSONUtils.toJsonByteArray(new RemoveTaskLogResponseCommand(true)));
        PowerMockito.when(remotingClient.sendSync(Mockito.any(Host.class), Mockito.any(Command.class), Mockito.anyLong()))
                .thenReturn(command);

        LogClientService logClientService = new LogClientService();
        Boolean status = logClientService.removeTaskLog("localhost", 1234, "/log/path");
        Assert.assertTrue(status);
    }

    @Test
    public void testIsRunning() {
        LogClientService logClientService = new LogClientService();
        Assert.assertTrue(logClientService.isRunning());
    }
}
