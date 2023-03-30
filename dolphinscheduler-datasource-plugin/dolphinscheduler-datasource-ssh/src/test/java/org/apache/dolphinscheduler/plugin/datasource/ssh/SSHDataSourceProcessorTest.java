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

package org.apache.dolphinscheduler.plugin.datasource.ssh;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHDataSourceProcessor;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SSHDataSourceProcessorTest {

    private SSHDataSourceProcessor sshDataSourceProcessor;

    private String connectJson =
            "{\"user\":\"lucky\",\"password\":\"123456\",\"host\":\"dolphinscheduler.com\",\"port\":22, \"publicKey\":\"ssh-rsa AAAAB\"}";

    @BeforeEach
    public void init() {
        sshDataSourceProcessor = new SSHDataSourceProcessor();
    }

    @Test
    void testCheckDatasourceParam() {
        BaseDataSourceParamDTO baseDataSourceParamDTO = new SSHDataSourceParamDTO();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sshDataSourceProcessor.checkDatasourceParam(baseDataSourceParamDTO));
        baseDataSourceParamDTO.setHost("localhost");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sshDataSourceProcessor.checkDatasourceParam(baseDataSourceParamDTO));
        baseDataSourceParamDTO.setUserName("root");
        Assertions.assertDoesNotThrow(() -> sshDataSourceProcessor.checkDatasourceParam(baseDataSourceParamDTO));

    }

    @Test
    void testGetDatasourceUniqueId() {
        SSHConnectionParam sshConnectionParam = new SSHConnectionParam();
        sshConnectionParam.setHost("localhost");
        sshConnectionParam.setUser("root");
        sshConnectionParam.setPassword("123456");
        Assertions.assertEquals("ssh@localhost@root@123456",
                sshDataSourceProcessor.getDatasourceUniqueId(sshConnectionParam, DbType.SSH));

    }

    @Test
    void testCreateDatasourceParamDTO() {
        SSHDataSourceParamDTO sshDataSourceParamDTO =
                (SSHDataSourceParamDTO) sshDataSourceProcessor.createDatasourceParamDTO(connectJson);
        Assertions.assertEquals("lucky", sshDataSourceParamDTO.getUserName());
        Assertions.assertEquals("123456", sshDataSourceParamDTO.getPassword());
        Assertions.assertEquals("dolphinscheduler.com", sshDataSourceParamDTO.getHost());
        Assertions.assertEquals(22, sshDataSourceParamDTO.getPort());
        Assertions.assertEquals("ssh-rsa AAAAB", sshDataSourceParamDTO.getPublicKey());
    }

    @Test
    void testCreateConnectionParams() {
        SSHDataSourceParamDTO sshDataSourceParamDTO =
                (SSHDataSourceParamDTO) sshDataSourceProcessor.createDatasourceParamDTO(connectJson);
        SSHConnectionParam sshConnectionParam = sshDataSourceProcessor.createConnectionParams(sshDataSourceParamDTO);
        Assertions.assertEquals("lucky", sshConnectionParam.getUser());
        Assertions.assertEquals("123456", sshConnectionParam.getPassword());
        Assertions.assertEquals("dolphinscheduler.com", sshConnectionParam.getHost());
        Assertions.assertEquals(22, sshConnectionParam.getPort());
        Assertions.assertEquals("ssh-rsa AAAAB", sshConnectionParam.getPublicKey());
    }

    @Test
    void testTestConnection() throws IOException {
        SSHDataSourceParamDTO sshDataSourceParamDTO =
                (SSHDataSourceParamDTO) sshDataSourceProcessor.createDatasourceParamDTO(connectJson);
        ConnectionParam connectionParam = sshDataSourceProcessor.createConnectionParams(sshDataSourceParamDTO);
        MockedStatic<SSHUtils> sshConnectionUtilsMockedStatic = org.mockito.Mockito.mockStatic(SSHUtils.class);
        sshConnectionUtilsMockedStatic.when(() -> SSHUtils.getSession(Mockito.any(), Mockito.any())).thenReturn(null);
        Assertions.assertFalse(sshDataSourceProcessor.testConnection(connectionParam));

        ClientSession clientSession = Mockito.mock(ClientSession.class, RETURNS_DEEP_STUBS);
        sshConnectionUtilsMockedStatic.when(() -> SSHUtils.getSession(Mockito.any(), Mockito.any()))
                .thenReturn(clientSession);
        when(clientSession.auth().verify().isSuccess()).thenReturn(true);
        Assertions.assertTrue(sshDataSourceProcessor.testConnection(connectionParam));

    }

}
