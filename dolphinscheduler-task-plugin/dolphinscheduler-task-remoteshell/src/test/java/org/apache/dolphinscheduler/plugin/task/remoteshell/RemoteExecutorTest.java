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

package org.apache.dolphinscheduler.plugin.task.remoteshell;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.datasource.ssh.SSHUtils;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.SystemUtils;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RemoteExecutorTest {

    private String connectJson =
            "{\"user\":\"root\",\"password\":\"123456\",\"host\":\"dolphinscheduler.com\",\"port\":22, \"privateKey\":\"ssh-rsa AAAAB\"}";

    SSHConnectionParam sshConnectionParam;

    ClientSession clientSession;

    MockedStatic<SSHUtils> sshConnectionUtilsMockedStatic = org.mockito.Mockito.mockStatic(SSHUtils.class);

    @BeforeEach
    void init() {
        SSHDataSourceProcessor sshDataSourceProcessor = new SSHDataSourceProcessor();
        SSHDataSourceParamDTO sshDataSourceParamDTO =
                (SSHDataSourceParamDTO) sshDataSourceProcessor.createDatasourceParamDTO(connectJson);
        sshConnectionParam = sshDataSourceProcessor.createConnectionParams(sshDataSourceParamDTO);
        clientSession = Mockito.mock(ClientSession.class, RETURNS_DEEP_STUBS);
        sshConnectionUtilsMockedStatic.when(() -> SSHUtils.getSession(Mockito.any(), Mockito.any()))
                .thenReturn(clientSession);
    }

    @AfterEach
    void tearDown() {
        sshConnectionUtilsMockedStatic.close();
    }

    @Test
    void testRunRemote() throws IOException {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);
        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(Mockito.anyString())).thenReturn(channel);
        when(channel.getExitStatus()).thenReturn(1);
        when(channel.getInvertedOut()).thenReturn(new NullInputStream());
        Assertions.assertThrows(TaskException.class, () -> remoteExecutor.runRemote("ls -l"));

        // Mock the streaming runRemote to simulate log output
        String output = "total 26392\n" +
                "dr-xr-xr-x.   6 root root      3072 Aug 15  2023 boot\n" +
                "drwxr-xr-x   18 root root      3120 Sep 23  2023 dev\n" +
                "drwxr-xr-x.  91 root root      4096 Sep 23  2023 etc\n";
        InputStream inputStream = IOUtils.toInputStream(output, StandardCharsets.UTF_8);
        when(channel.getInvertedOut()).thenReturn(inputStream);
        when(channel.getExitStatus()).thenReturn(0);
        String actualOut = Assertions.assertDoesNotThrow(() -> remoteExecutor.runRemote("ls -l"));
        Assertions.assertEquals(output, actualOut);
    }

    @Test
    void testGetTaskPid() throws IOException {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        String taskId = "1234";
        String command = String.format("ps -ef | grep \"%s.sh\" | grep -v grep | awk '{print $2}'", taskId);
        doReturn("10001").when(remoteExecutor).runRemote(command);
        Assertions.assertEquals("10001", remoteExecutor.getTaskPid(taskId));
    }

    @Test
    void testSaveCommand() throws IOException {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        doNothing().when(remoteExecutor).uploadScript(Mockito.anyString(), Mockito.anyString());
        String checkDirCommand =
                "if [ ! -d /tmp/dolphinscheduler-remote-shell-root/ ]; then mkdir -p /tmp/dolphinscheduler-remote-shell-root/; fi";
        String catScriptCommand = "cat /tmp/dolphinscheduler-remote-shell-root/1234.sh";
        doReturn("").when(remoteExecutor).runRemote(checkDirCommand);
        doReturn("").when(remoteExecutor).runRemote(catScriptCommand);

        remoteExecutor.saveCommand("1234", "/tmp/dolphinscheduler/test.sh");
        verify(remoteExecutor).runRemote(checkDirCommand);
    }

    @Test
    void testCleanData() throws IOException {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        String cleanCommand =
                "rm /tmp/dolphinscheduler-remote-shell-root/1234.sh /tmp/dolphinscheduler-remote-shell-root/1234.log";
        doReturn("").when(remoteExecutor).runRemote(cleanCommand);
        remoteExecutor.cleanData("1234");
        String cleanCommandError =
                "rm /tmp/dolphinscheduler-remote-shell-root/abcd.sh /tmp/dolphinscheduler-remote-shell-root/abcd.log";
        doThrow(new TaskException()).when(remoteExecutor).runRemote(cleanCommandError);
        remoteExecutor.cleanData("abcd");
    }

    @Test
    void testGetTaskExitCode() throws IOException {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        String taskId = "1234";
        doNothing().when(remoteExecutor).cleanData(taskId);
        String trackCommand = "tail -n 1 /tmp/dolphinscheduler-remote-shell-root/1234.log";
        doReturn("DOLPHINSCHEDULER-REMOTE-SHELL-TASK-STATUS-0").when(remoteExecutor).runRemote(trackCommand);
        Assertions.assertEquals(0, remoteExecutor.getTaskExitCode(taskId));

        doReturn("DOLPHINSCHEDULER-REMOTE-SHELL-TASK-STATUS-1").when(remoteExecutor).runRemote(trackCommand);
        Assertions.assertEquals(1, remoteExecutor.getTaskExitCode(taskId));
    }

    @Test
    void getAllRemotePidStr() throws IOException {

        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        // Mock pstree output based on OS
        if (SystemUtils.IS_OS_MAC) {
            doReturn("-+= 9527 root\n \\-+= 9528 root").when(remoteExecutor).runRemote(anyString());
        } else {
            doReturn("bash(9527)───sleep(9528)").when(remoteExecutor).runRemote(anyString());
        }
        String allPidStr = remoteExecutor.getAllRemotePidStr("9527");
        Assertions.assertEquals("9527 9528", allPidStr);

        if (SystemUtils.IS_OS_MAC) {
            doReturn("-+= 1 root\n \\-+= 9528 root").when(remoteExecutor).runRemote(anyString());
        } else {
            doReturn("systemd(1)───sleep(9528)").when(remoteExecutor).runRemote(anyString());
        }
        allPidStr = remoteExecutor.getAllRemotePidStr("9527");
        Assertions.assertEquals("9527", allPidStr);

        doThrow(new TaskException()).when(remoteExecutor).runRemote(anyString());
        allPidStr = remoteExecutor.getAllRemotePidStr("9527");
        Assertions.assertEquals("9527", allPidStr);

    }

    @Test
    void testTrack() throws Exception {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        String taskId = "1234";
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        // Mock getTaskPid to control the loop, return a valid pid 2 times, then return empty
        doReturn("9527")
                .doReturn("9527")
                .doReturn("").when(remoteExecutor).getTaskPid(taskId);
        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);

        // Mock the streaming runRemote to simulate log output
        String logContent = "some log line 1\n"
                + "echo \"${setValue(my_prop=my_value)}\"\n"
                + "some log line 2\n";
        InputStream inputStream = IOUtils.toInputStream(logContent, StandardCharsets.UTF_8);
        when(channel.getInvertedOut()).thenReturn(inputStream);
        when(channel.getExitStatus()).thenReturn(0);

        remoteExecutor.track(taskId);

        // Verify that the output parameter was parsed and stored
        Assertions.assertEquals(1, remoteExecutor.getTaskOutputParams().size());
        Assertions.assertEquals("my_value", remoteExecutor.getTaskOutputParams().get("my_prop"));
    }

    @Test
    void testRunRemoteWithEmptyOutput() throws Exception {
        // Test empty output scenario (readLines = 0)
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);
        when(channel.getInvertedOut()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(channel.getExitStatus()).thenReturn(0);
        when(channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0))
                .thenReturn(EnumSet.of(ClientChannelEvent.CLOSED));

        String result = Assertions.assertDoesNotThrow(() -> remoteExecutor.runRemote("echo"));
        Assertions.assertEquals("", result);
    }

    @Test
    void testRunRemoteWithNonZeroExitStatus() throws Exception {
        // Test command failure scenario (exitStatus != 0)
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);
        when(channel.getInvertedOut()).thenReturn(IOUtils.toInputStream("error output", StandardCharsets.UTF_8));
        when(channel.getExitStatus()).thenReturn(1);
        when(channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0))
                .thenReturn(EnumSet.of(ClientChannelEvent.CLOSED));

        Assertions.assertThrows(TaskException.class, () -> remoteExecutor.runRemote("failing_command"));
    }

    @Test
    void testRunRemoteWithNullExitStatus() throws Exception {
        // Test null exitStatus scenario
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);
        when(channel.getInvertedOut()).thenReturn(IOUtils.toInputStream("some output", StandardCharsets.UTF_8));
        when(channel.getExitStatus()).thenReturn(null);
        when(channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0))
                .thenReturn(EnumSet.of(ClientChannelEvent.CLOSED));

        Assertions.assertThrows(TaskException.class, () -> remoteExecutor.runRemote("command"));
    }

    @Test
    void testTrackWithEmptyLogOutput() throws Exception {
        // Test track with empty log output (readLines = 0 scenario in track loop)
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        String taskId = "1234";
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        doReturn("9527").doReturn("").when(remoteExecutor).getTaskPid(taskId);
        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);
        when(channel.getInvertedOut()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(channel.getExitStatus()).thenReturn(0);
        when(channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0))
                .thenReturn(EnumSet.of(ClientChannelEvent.CLOSED));

        Assertions.assertDoesNotThrow(() -> remoteExecutor.track(taskId));
    }
}
