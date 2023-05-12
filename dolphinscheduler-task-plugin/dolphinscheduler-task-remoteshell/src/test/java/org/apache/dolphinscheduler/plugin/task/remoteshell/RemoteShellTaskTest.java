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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Disabled
@ExtendWith(MockitoExtension.class)
public class RemoteShellTaskTest {

    private String connectJson =
            "{\"user\":\"root\",\"password\":\"123456\",\"host\":\"dolphinscheduler.com\",\"port\":22, \"publicKey\":\"ssh-rsa AAAAB\"}";

    SSHConnectionParam sshConnectionParam;

    ClientSession clientSession;

    @BeforeEach
    void init() {
        SSHDataSourceProcessor sshDataSourceProcessor = new SSHDataSourceProcessor();
        SSHDataSourceParamDTO sshDataSourceParamDTO =
                (SSHDataSourceParamDTO) sshDataSourceProcessor.createDatasourceParamDTO(connectJson);
        sshConnectionParam = sshDataSourceProcessor.createConnectionParams(sshDataSourceParamDTO);
        clientSession = Mockito.mock(ClientSession.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void testBuildCommand() throws Exception {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId("1");
        taskExecutionContext
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo 1\",\"resourceList\":[],\"udfList\":[]}");
        taskExecutionContext.setExecutePath("/tmp");
        taskExecutionContext.setEnvironmentConfig("export PATH=/opt/anaconda3/bin:$PATH");
        RemoteShellTask remoteShellTask = spy(new RemoteShellTask(taskExecutionContext));
        doNothing().when(remoteShellTask).initRemoteExecutor();
        remoteShellTask.init();

        MockedStatic<Files> filesMockedStatic = org.mockito.Mockito.mockStatic(Files.class);
        filesMockedStatic.when(() -> Files.exists(Mockito.any())).thenReturn(false);
        String script = "#!/bin/bash\n" +
                "export PATH=/opt/anaconda3/bin:$PATH\n" +
                "echo 1\n" +
                "echo DOLPHINSCHEDULER-REMOTE-SHELL-TASK-STATUS-$?";
        Path path = Paths.get("/tmp/1_node.sh");
        filesMockedStatic.when(() -> Files.write(path, script.getBytes(), StandardOpenOption.APPEND))
                .thenThrow(new IOException("script match"));

        IOException exception = Assertions.assertThrows(IOException.class, () -> {
            remoteShellTask.buildCommand();
        });
        Assertions.assertEquals("script match", exception.getMessage());
    }

}
