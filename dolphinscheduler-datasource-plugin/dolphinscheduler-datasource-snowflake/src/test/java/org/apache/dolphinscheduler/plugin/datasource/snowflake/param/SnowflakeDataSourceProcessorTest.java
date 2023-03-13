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

package org.apache.dolphinscheduler.plugin.datasource.snowflake.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SnowflakeDataSourceProcessorTest {

    private SnowflakeDatasourceProcessor snowflakeDataSourceProcessor = new SnowflakeDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        SnowflakeDatasourceParamDTO snowflakeDataSourceParamDTO = new SnowflakeDatasourceParamDTO();
        snowflakeDataSourceParamDTO.setHost("localhost1,localhost2");
        snowflakeDataSourceParamDTO.setPort(5142);
        snowflakeDataSourceParamDTO.setUserName("default");
        snowflakeDataSourceParamDTO.setDatabase("default");
        snowflakeDataSourceParamDTO.setOther(props);

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            SnowflakeConnectionParam connectionParams = (SnowflakeConnectionParam) snowflakeDataSourceProcessor
                    .createConnectionParams(snowflakeDataSourceParamDTO);
            Assertions.assertNotNull(connectionParams);
            Assertions.assertEquals("jdbc:snowflake://localhost1:5142,localhost2:5142", connectionParams.getAddress());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionParam = "{\"user\":\"default\",\"address\":\"jdbc:snowflake://localhost1:5142,localhost2:5142\""
                + ",\"jdbcUrl\":\"jdbc:snowflake://localhost1:5142,localhost2:5142/default\"}";
        SnowflakeConnectionParam connectionParams = (SnowflakeConnectionParam) snowflakeDataSourceProcessor
                .createConnectionParams(connectionParam);
        Assertions.assertNotNull(connectionParam);
        Assertions.assertEquals("default", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.NET_SNOWFLAKE_JDBC_DRIVER,
                snowflakeDataSourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        SnowflakeConnectionParam connectionParam = new SnowflakeConnectionParam();
        connectionParam.setJdbcUrl("jdbc:snowflake://localhost1:5142,localhost2:5142/default");
        Assertions.assertEquals("jdbc:snowflake://localhost1:5142,localhost2:5142/default",
                snowflakeDataSourceProcessor.getJdbcUrl(connectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.SNOWFLAKE, snowflakeDataSourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.SNOWFLAKE_VALIDATION_QUERY,
                snowflakeDataSourceProcessor.getValidationQuery());
    }
}
