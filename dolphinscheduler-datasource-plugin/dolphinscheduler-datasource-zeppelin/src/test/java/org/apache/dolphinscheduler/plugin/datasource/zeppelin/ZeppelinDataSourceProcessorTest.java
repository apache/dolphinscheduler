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

package org.apache.dolphinscheduler.plugin.datasource.zeppelin;

import org.apache.dolphinscheduler.plugin.datasource.zeppelin.param.ZeppelinConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.zeppelin.param.ZeppelinDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.zeppelin.param.ZeppelinDataSourceProcessor;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ZeppelinDataSourceProcessorTest {

    private ZeppelinDataSourceProcessor zeppelinDataSourceProcessor;

    private String connectJson =
            "{\"username\":\"lucky\",\"password\":\"123456\",\"restEndpoint\":\"https://dolphinscheduler.com:8080\"}";

    @BeforeEach
    public void init() {
        zeppelinDataSourceProcessor = new ZeppelinDataSourceProcessor();
    }

    @Test
    void testCheckDatasourceParam() {
        ZeppelinDataSourceParamDTO zeppelinDataSourceParamDTO = new ZeppelinDataSourceParamDTO();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> zeppelinDataSourceProcessor.checkDatasourceParam(zeppelinDataSourceParamDTO));
        zeppelinDataSourceParamDTO.setRestEndpoint("http://dolphinscheduler.com:8080");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> zeppelinDataSourceProcessor.checkDatasourceParam(zeppelinDataSourceParamDTO));
        zeppelinDataSourceParamDTO.setUserName("root");
        Assertions
                .assertDoesNotThrow(() -> zeppelinDataSourceProcessor.checkDatasourceParam(zeppelinDataSourceParamDTO));
    }

    @Test
    void testGetDatasourceUniqueId() {
        ZeppelinConnectionParam zeppelinConnectionParam = new ZeppelinConnectionParam();
        zeppelinConnectionParam.setRestEndpoint("https://dolphinscheduler.com:8080");
        zeppelinConnectionParam.setUsername("root");
        zeppelinConnectionParam.setPassword("123456");
        Assertions.assertEquals("zeppelin@https://dolphinscheduler.com:8080@root@123456",
                zeppelinDataSourceProcessor.getDatasourceUniqueId(zeppelinConnectionParam, DbType.ZEPPELIN));

    }

    @Test
    void testCreateDatasourceParamDTO() {
        ZeppelinDataSourceParamDTO zeppelinDataSourceParamDTO =
                (ZeppelinDataSourceParamDTO) zeppelinDataSourceProcessor.createDatasourceParamDTO(connectJson);
        Assertions.assertEquals("lucky", zeppelinDataSourceParamDTO.getUserName());
        Assertions.assertEquals("123456", zeppelinDataSourceParamDTO.getPassword());
        Assertions.assertEquals("https://dolphinscheduler.com:8080", zeppelinDataSourceParamDTO.getRestEndpoint());
    }

    @Test
    void testCreateConnectionParams() {
        ZeppelinDataSourceParamDTO zeppelinDataSourceParamDTO =
                (ZeppelinDataSourceParamDTO) zeppelinDataSourceProcessor.createDatasourceParamDTO(connectJson);
        ZeppelinConnectionParam zeppelinConnectionParam =
                zeppelinDataSourceProcessor.createConnectionParams(zeppelinDataSourceParamDTO);
        Assertions.assertEquals("lucky", zeppelinConnectionParam.getUsername());
        Assertions.assertEquals("123456", zeppelinConnectionParam.getPassword());
        Assertions.assertEquals("https://dolphinscheduler.com:8080", zeppelinConnectionParam.getRestEndpoint());
    }

    @Test
    void testTestConnection() {
        ZeppelinDataSourceParamDTO zeppelinDataSourceParamDTO =
                (ZeppelinDataSourceParamDTO) zeppelinDataSourceProcessor.createDatasourceParamDTO(connectJson);
        ZeppelinConnectionParam connectionParam =
                zeppelinDataSourceProcessor.createConnectionParams(zeppelinDataSourceParamDTO);
        Assertions.assertFalse(zeppelinDataSourceProcessor.checkDataSourceConnectivity(connectionParam));
        try (
                MockedConstruction<ZeppelinClientWrapper> sshClientWrapperMockedConstruction =
                        Mockito.mockConstruction(ZeppelinClientWrapper.class, (mock, context) -> {
                            Mockito.when(
                                    mock.checkConnect(connectionParam.getUsername(), connectionParam.getPassword()))
                                    .thenReturn(true);
                        })) {
            Assertions.assertTrue(zeppelinDataSourceProcessor.checkDataSourceConnectivity(connectionParam));
        }
    }
}
