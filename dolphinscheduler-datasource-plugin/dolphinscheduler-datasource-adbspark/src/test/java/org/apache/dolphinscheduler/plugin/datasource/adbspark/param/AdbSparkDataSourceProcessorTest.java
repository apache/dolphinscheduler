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

package org.apache.dolphinscheduler.plugin.datasource.adbspark.param;

import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdbSparkDataSourceProcessorTest {

    private AdbSparkDataSourceProcessor processor;

    private final String connectionJson =
            "{\"aliyunAccessKeyId\":\"mockAccessKeyId\",\"aliyunAccessKeySecret\":\"mockAccessKeySecret\",\"aliyunRegionId\":\"cn-beijing\"}";

    @BeforeEach
    public void before() {
        processor = new AdbSparkDataSourceProcessor();
    }

    @Test
    public void testCheckDatasourceParam() {
        AdbSparkDataSourceParamDTO datasourceParamDTO = new AdbSparkDataSourceParamDTO();

        // check AccessKeyId
        IllegalArgumentException exceptionWithoutAccessKeyId =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> processor.checkDatasourceParam(datasourceParamDTO));
        Assertions.assertEquals("accessKeyId in param is not valid",
                exceptionWithoutAccessKeyId.getMessage());

        datasourceParamDTO.setAliyunAccessKeyId("mockAccessKeyId");

        // check AccessKeySecret
        IllegalArgumentException exceptionWithoutAccessKeySecret =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> processor.checkDatasourceParam(datasourceParamDTO));
        Assertions.assertEquals("accessKeySecret in param is not valid",
                exceptionWithoutAccessKeySecret.getMessage());

        datasourceParamDTO.setAliyunAccessKeySecret("mockAccessKeySecret");

        // check RegionId
        IllegalArgumentException exceptionWithoutRegionId =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> processor.checkDatasourceParam(datasourceParamDTO));
        Assertions.assertEquals("regionId in param is not valid",
                exceptionWithoutRegionId.getMessage());
    }

    @Test
    public void testCreateDatasourceParam() {
        AdbSparkDataSourceParamDTO datasourceParamDTO =
                (AdbSparkDataSourceParamDTO) processor.createDatasourceParamDTO(connectionJson);

        Assertions.assertEquals("mockAccessKeyId", datasourceParamDTO.getAliyunAccessKeyId());
        Assertions.assertEquals("mockAccessKeySecret", datasourceParamDTO.getAliyunAccessKeySecret());
        Assertions.assertEquals("cn-beijing", datasourceParamDTO.getAliyunRegionId());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        AdbSparkConnectionParam connectionParam = new AdbSparkConnectionParam();
        connectionParam.setAliyunAccessKeyId("mockAccessKeyId");
        connectionParam.setAliyunAccessKeySecret("mockAccessKeySecret");
        connectionParam.setAliyunRegionId("cn-beijing");

        Assertions.assertEquals("adbspark@cn-beijing@mockAccessKeyId@mockAccessKeySecret",
                processor.getDatasourceUniqueId(connectionParam, DbType.ADBSPARK));
    }
}
