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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
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

import org.apache.commons.lang3.SystemUtils;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
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

    /** Helper: make channel write data to whichever OutputStream is passed to setOut(). */
    private void mockChannelOutput(ChannelExec channel, String data) throws IOException {
        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(0);
            out.write(data.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(channel).setOut(any(OutputStream.class));
    }

    @Test
    void testRunRemote() throws IOException {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);
        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(Mockito.anyString())).thenReturn(channel);
        when(channel.getExitStatus()).thenReturn(1);
        Assertions.assertThrows(TaskException.class, () -> remoteExecutor.runRemote("ls -l"));

        String output = "total 26392\n" +
                "dr-xr-xr-x.   6 root root      3072 Aug 15  2023 boot\n" +
                "drwxr-xr-x   18 root root      3120 Sep 23  2023 dev\n" +
                "drwxr-xr-x.  91 root root      4096 Sep 23  2023 etc\n";
        mockChannelOutput(channel, output);
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

        doReturn("9527")
                .doReturn("9527")
                .doReturn("").when(remoteExecutor).getTaskPid(taskId);
        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);

        String logContent = "some log line 1\n"
                + "echo \"${setValue(my_prop=my_value)}\"\n"
                + "some log line 2\n";
        mockChannelOutput(channel, logContent);
        when(channel.getExitStatus()).thenReturn(0);

        remoteExecutor.track(taskId);

        Assertions.assertEquals(1, remoteExecutor.getTaskOutputParams().size());
        Assertions.assertEquals("my_value", remoteExecutor.getTaskOutputParams().get("my_prop"));
    }

    @Test
    void testRunRemoteWithEmptyOutput() throws Exception {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);
        // no mockChannelOutput → setOut() is a no-op → ByteArrayOutputStream stays empty
        when(channel.getExitStatus()).thenReturn(0);
        when(channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0))
                .thenReturn(EnumSet.of(ClientChannelEvent.CLOSED));

        String result = Assertions.assertDoesNotThrow(() -> remoteExecutor.runRemote("echo"));
        Assertions.assertEquals("", result);
    }

    @Test
    void testRunRemoteWithNonZeroExitStatus() throws Exception {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);
        mockChannelOutput(channel, "partial output before failure\n");
        when(channel.getExitStatus()).thenReturn(1);
        when(channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0))
                .thenReturn(EnumSet.of(ClientChannelEvent.CLOSED));

        TaskException ex =
                Assertions.assertThrows(TaskException.class, () -> remoteExecutor.runRemote("failing_command"));
        Assertions.assertTrue(ex.getMessage().contains("exitStatus: 1"));
    }

    @Test
    void testRunRemoteWithNullExitStatus() throws Exception {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);
        mockChannelOutput(channel, "some output\n");
        when(channel.getExitStatus()).thenReturn(null);
        when(channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0))
                .thenReturn(EnumSet.of(ClientChannelEvent.CLOSED));

        TaskException ex =
                Assertions.assertThrows(TaskException.class, () -> remoteExecutor.runRemote("command"));
        Assertions.assertTrue(ex.getMessage().contains("exitStatus: null"));
    }

    @Test
    void testTrackWithEmptyLogOutput() throws Exception {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        String taskId = "1234";
        ChannelExec channel = Mockito.mock(ChannelExec.class, RETURNS_DEEP_STUBS);

        doReturn("9527").doReturn("").when(remoteExecutor).getTaskPid(taskId);
        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        when(clientSession.createExecChannel(anyString())).thenReturn(channel);
        // no output → readLines=0 → sleep once, then getTaskPid returns "" → exit
        when(channel.getExitStatus()).thenReturn(0);
        when(channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0))
                .thenReturn(EnumSet.of(ClientChannelEvent.CLOSED));

        Assertions.assertDoesNotThrow(() -> remoteExecutor.track(taskId));
    }
}
