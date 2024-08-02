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

package org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark;

import org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark.param.AliyunServerlessSparkConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark.param.AliyunServerlessSparkDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark.param.AliyunServerlessSparkDataSourceProcessor;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AliyunServerlessSparkDataSourceProcessorTest {

    private AliyunServerlessSparkDataSourceProcessor aliyunServerlessSparkDataSourceProcessor;

    private String connectJson =
            "{\"accessKeyId\":\"mockAccessKeyId\",\"accessKeySecret\":\"mockAccessKeySecret\",\"regionId\":\"cn-hangzhou\"}";

    @BeforeEach
    public void init() {
        aliyunServerlessSparkDataSourceProcessor = new AliyunServerlessSparkDataSourceProcessor();
    }

    @Test
    void testCheckDatasourceParam() {
        AliyunServerlessSparkDataSourceParamDTO aliyunServerlessSparkDataSourceParamDTO =
                new AliyunServerlessSparkDataSourceParamDTO();
        aliyunServerlessSparkDataSourceParamDTO.setRegionId("cn-hangzhou");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> aliyunServerlessSparkDataSourceProcessor
                        .checkDatasourceParam(aliyunServerlessSparkDataSourceParamDTO));
        aliyunServerlessSparkDataSourceParamDTO.setAccessKeyId("mockAccessKeyId");
        aliyunServerlessSparkDataSourceParamDTO.setAccessKeySecret("mockAccessKeySecret");
        Assertions
                .assertDoesNotThrow(() -> aliyunServerlessSparkDataSourceProcessor
                        .checkDatasourceParam(aliyunServerlessSparkDataSourceParamDTO));
    }

    @Test
    void testGetDatasourceUniqueId() {
        AliyunServerlessSparkConnectionParam aliyunServerlessSparkConnectionParam =
                new AliyunServerlessSparkConnectionParam();
        aliyunServerlessSparkConnectionParam.setRegionId("cn-hangzhou");
        aliyunServerlessSparkConnectionParam.setAccessKeyId("mockAccessKeyId");
        aliyunServerlessSparkConnectionParam.setAccessKeySecret("mockAccessKeySecret");
        Assertions.assertEquals("aliyun_serverless_spark@cn-hangzhou@mockAccessKeyId@mockAccessKeySecret",
                aliyunServerlessSparkDataSourceProcessor.getDatasourceUniqueId(aliyunServerlessSparkConnectionParam,
                        DbType.ALIYUN_SERVERLESS_SPARK));
    }

    @Test
    void testCreateDatasourceParamDTO() {
        AliyunServerlessSparkDataSourceParamDTO aliyunServerlessSparkDataSourceParamDTO =
                (AliyunServerlessSparkDataSourceParamDTO) aliyunServerlessSparkDataSourceProcessor
                        .createDatasourceParamDTO(connectJson);
        Assertions.assertEquals("cn-hangzhou", aliyunServerlessSparkDataSourceParamDTO.getRegionId());
        Assertions.assertEquals("mockAccessKeyId", aliyunServerlessSparkDataSourceParamDTO.getAccessKeyId());
        Assertions.assertEquals("mockAccessKeySecret", aliyunServerlessSparkDataSourceParamDTO.getAccessKeySecret());
    }

    @Test
    void testCreateConnectionParams() {
        AliyunServerlessSparkDataSourceParamDTO aliyunServerlessSparkDataSourceParamDTO =
                (AliyunServerlessSparkDataSourceParamDTO) aliyunServerlessSparkDataSourceProcessor
                        .createDatasourceParamDTO(connectJson);
        AliyunServerlessSparkConnectionParam aliyunServerlessSparkConnectionParam =
                aliyunServerlessSparkDataSourceProcessor
                        .createConnectionParams(aliyunServerlessSparkDataSourceParamDTO);
        Assertions.assertEquals("cn-hangzhou", aliyunServerlessSparkConnectionParam.getRegionId());
        Assertions.assertEquals("mockAccessKeyId", aliyunServerlessSparkConnectionParam.getAccessKeyId());
        Assertions.assertEquals("mockAccessKeySecret", aliyunServerlessSparkConnectionParam.getAccessKeySecret());
    }

    @Test
    void testTestConnection() {
        AliyunServerlessSparkDataSourceParamDTO aliyunServerlessSparkDataSourceParamDTO =
                (AliyunServerlessSparkDataSourceParamDTO) aliyunServerlessSparkDataSourceProcessor
                        .createDatasourceParamDTO(connectJson);
        AliyunServerlessSparkConnectionParam connectionParam =
                aliyunServerlessSparkDataSourceProcessor
                        .createConnectionParams(aliyunServerlessSparkDataSourceParamDTO);
        Assertions.assertTrue(aliyunServerlessSparkDataSourceProcessor.checkDataSourceConnectivity(connectionParam));
        try (
                MockedConstruction<AliyunServerlessSparkClientWrapper> AliyunServerlessSparkClientWrapper =
                        Mockito.mockConstruction(AliyunServerlessSparkClientWrapper.class, (mock, context) -> {
                            Mockito.when(
                                    mock.checkConnect(connectionParam.getAccessKeyId(),
                                            connectionParam.getAccessKeySecret(), connectionParam.getRegionId()))
                                    .thenReturn(true);
                        })) {
            Assertions
                    .assertTrue(aliyunServerlessSparkDataSourceProcessor.checkDataSourceConnectivity(connectionParam));
        }
    }
}
