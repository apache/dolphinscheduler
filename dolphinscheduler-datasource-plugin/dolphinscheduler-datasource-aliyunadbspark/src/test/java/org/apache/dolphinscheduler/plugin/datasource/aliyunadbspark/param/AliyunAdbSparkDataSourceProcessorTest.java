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

package org.apache.dolphinscheduler.plugin.datasource.aliyunadbspark.param;

import org.apache.dolphinscheduler.spi.enums.DbType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AliyunAdbSparkDataSourceProcessorTest {

    private AliyunAdbSparkDataSourceProcessor processor;

    private final String connectionJson =
            "{\"accessKeyId\":\"mockAccessKeyId\",\"accessKeySecret\":\"mockAccessKeySecret\",\"regionId\":\"cn-beijing\"}";

    @BeforeEach
    public void before() {
        processor = new AliyunAdbSparkDataSourceProcessor();
    }

    @Test
    public void testCheckDatasourceParam() {
        AliyunAdbSparkDataSourceParamDTO datasourceParamDTO = new AliyunAdbSparkDataSourceParamDTO();

        // check AccessKeyId
        IllegalArgumentException exceptionWithoutAccessKeyId =
                Assertions.assertThrows(IllegalArgumentException.class,
                () -> processor.checkDatasourceParam(datasourceParamDTO));
        Assertions.assertEquals("accessKeyId in param is not valid",
                exceptionWithoutAccessKeyId.getMessage());

        datasourceParamDTO.setAccessKeyId("mockAccessKeyId");

        // check AccessKeySecret
        IllegalArgumentException exceptionWithoutAccessKeySecret =
                Assertions.assertThrows(IllegalArgumentException.class,
                () -> processor.checkDatasourceParam(datasourceParamDTO));
        Assertions.assertEquals("accessKeySecret in param is not valid",
                exceptionWithoutAccessKeySecret.getMessage());

        datasourceParamDTO.setAccessKeySecret("mockAccessKeySecret");

        // check RegionId
        IllegalArgumentException exceptionWithoutRegionId =
                Assertions.assertThrows(IllegalArgumentException.class,
                () -> processor.checkDatasourceParam(datasourceParamDTO));
        Assertions.assertEquals("regionId in param is not valid",
                exceptionWithoutRegionId.getMessage());
    }

    @Test
    public void testCreateDatasourceParam() {
        AliyunAdbSparkDataSourceParamDTO datasourceParamDTO =
                (AliyunAdbSparkDataSourceParamDTO) processor.createDatasourceParamDTO(connectionJson);

        Assertions.assertEquals("mockAccessKeyId", datasourceParamDTO.getAccessKeyId());
        Assertions.assertEquals("mockAccessKeySecret", datasourceParamDTO.getAccessKeySecret());
        Assertions.assertEquals("cn-beijing", datasourceParamDTO.getRegionId());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        AliyunAdbSparkConnectionParam connectionParam = new AliyunAdbSparkConnectionParam();
        connectionParam.setAccessKeyId("mockAccessKeyId");
        connectionParam.setAccessKeySecret("mockAccessKeySecret");
        connectionParam.setRegionId("cn-beijing");

        Assertions.assertEquals("aliyun_adb_spark@cn-beijing@mockAccessKeyId@mockAccessKeySecret",
                processor.getDatasourceUniqueId(connectionParam, DbType.ALIYUN_ADB_SPARK));
    }
}
