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

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.datasource.ssh.SSHUtils;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;

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
            "{\"user\":\"lucky\",\"password\":\"123456\",\"host\":\"dolphinscheduler.com\",\"port\":22, \"publicKey\":\"ssh-rsa AAAAB\"}";

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
        Assertions.assertThrows(TaskException.class, () -> remoteExecutor.runRemote("ls -l"));
        when(channel.getExitStatus()).thenReturn(0);
        Assertions.assertDoesNotThrow(() -> remoteExecutor.runRemote("ls -l"));
    }

    @Test
    void testGetTaskPid() throws IOException {
        RemoteExecutor remoteExecutor = spy(new RemoteExecutor(sshConnectionParam));
        String taskId = "1234";
        String command = String.format("ps -ef | grep \"%s.sh\" | grep -v grep | awk '{print $2}'", taskId);

        doReturn("10001").when(remoteExecutor).runRemote(command);

        Assertions.assertEquals("10001", remoteExecutor.getTaskPid(taskId));
    }
}
