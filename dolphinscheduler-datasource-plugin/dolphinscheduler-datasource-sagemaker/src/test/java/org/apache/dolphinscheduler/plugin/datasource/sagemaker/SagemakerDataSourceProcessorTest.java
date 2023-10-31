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

package org.apache.dolphinscheduler.plugin.datasource.sagemaker;

import org.apache.dolphinscheduler.plugin.datasource.sagemaker.param.SagemakerConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.sagemaker.param.SagemakerDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.sagemaker.param.SagemakerDataSourceProcessor;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SagemakerDataSourceProcessorTest {

    private SagemakerDataSourceProcessor sagemakerDataSourceProcessor;

    private String connectJson =
            "{\"userName\":\"access key\",\"password\":\"secret access key\",\"awsRegion\":\"region\"}";

    @BeforeEach
    public void init() {
        sagemakerDataSourceProcessor = new SagemakerDataSourceProcessor();
    }

    @Test
    void testCheckDatasourceParam() {
        SagemakerDataSourceParamDTO sagemakerDataSourceParamDTO = new SagemakerDataSourceParamDTO();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sagemakerDataSourceProcessor.checkDatasourceParam(sagemakerDataSourceParamDTO));
        sagemakerDataSourceParamDTO.setUserName("access key");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sagemakerDataSourceProcessor.checkDatasourceParam(sagemakerDataSourceParamDTO));
        sagemakerDataSourceParamDTO.setPassword("secret access key");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sagemakerDataSourceProcessor.checkDatasourceParam(sagemakerDataSourceParamDTO));
        sagemakerDataSourceParamDTO.setAwsRegion("region");

        Assertions
                .assertDoesNotThrow(
                        () -> sagemakerDataSourceProcessor.checkDatasourceParam(sagemakerDataSourceParamDTO));
    }

    @Test
    void testGetDatasourceUniqueId() {
        SagemakerConnectionParam sagemakerConnectionParam = new SagemakerConnectionParam();
        sagemakerConnectionParam.setUserName("access key");
        sagemakerConnectionParam.setPassword("secret access key");
        sagemakerConnectionParam.setAwsRegion("region");
        Assertions.assertEquals("sagemaker@access key@secret access key@region",
                sagemakerDataSourceProcessor.getDatasourceUniqueId(sagemakerConnectionParam, DbType.SAGEMAKER));

    }

    @Test
    void testCreateDatasourceParamDTO() {
        SagemakerDataSourceParamDTO sagemakerDataSourceParamDTO =
                (SagemakerDataSourceParamDTO) sagemakerDataSourceProcessor.createDatasourceParamDTO(connectJson);
        Assertions.assertEquals("access key", sagemakerDataSourceParamDTO.getUserName());
        Assertions.assertEquals("secret access key", sagemakerDataSourceParamDTO.getPassword());
        Assertions.assertEquals("region", sagemakerDataSourceParamDTO.getAwsRegion());
    }

    @Test
    void testCreateConnectionParams() {
        SagemakerDataSourceParamDTO sagemakerDataSourceParamDTO =
                (SagemakerDataSourceParamDTO) sagemakerDataSourceProcessor.createDatasourceParamDTO(connectJson);
        SagemakerConnectionParam sagemakerConnectionParam =
                sagemakerDataSourceProcessor.createConnectionParams(sagemakerDataSourceParamDTO);
        Assertions.assertEquals("access key", sagemakerConnectionParam.getUserName());
        Assertions.assertEquals("secret access key", sagemakerConnectionParam.getPassword());
        Assertions.assertEquals("region", sagemakerConnectionParam.getAwsRegion());
    }

    @Test
    void testTestConnection() {
        SagemakerDataSourceParamDTO sagemakerDataSourceParamDTO =
                (SagemakerDataSourceParamDTO) sagemakerDataSourceProcessor.createDatasourceParamDTO(connectJson);
        SagemakerConnectionParam connectionParam =
                sagemakerDataSourceProcessor.createConnectionParams(sagemakerDataSourceParamDTO);
        Assertions.assertFalse(sagemakerDataSourceProcessor.checkDataSourceConnectivity(connectionParam));

        try (
                MockedConstruction<SagemakerClientWrapper> sshClientWrapperMockedConstruction =
                        Mockito.mockConstruction(SagemakerClientWrapper.class, (mock, context) -> {
                            Mockito.when(
                                    mock.checkConnect())
                                    .thenReturn(true);
                        })) {
            Assertions.assertTrue(sagemakerDataSourceProcessor.checkDataSourceConnectivity(connectionParam));
        }

    }
}
