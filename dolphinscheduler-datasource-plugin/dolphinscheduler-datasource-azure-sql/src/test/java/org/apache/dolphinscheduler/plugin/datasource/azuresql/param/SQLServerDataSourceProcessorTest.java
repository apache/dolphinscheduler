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

package org.apache.dolphinscheduler.plugin.datasource.azuresql.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
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
public class SQLServerDataSourceProcessorTest {

    private AzureSQLDataSourceProcessor sqlServerDatasourceProcessor = new AzureSQLDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        AzureSQLDataSourceParamDTO sqlServerDatasourceParamDTO = new AzureSQLDataSourceParamDTO();
        sqlServerDatasourceParamDTO.setUserName("root");
        sqlServerDatasourceParamDTO.setPassword("123456");
        sqlServerDatasourceParamDTO.setDatabase("default");
        sqlServerDatasourceParamDTO.setHost("localhost");
        sqlServerDatasourceParamDTO.setPort(1234);
        sqlServerDatasourceParamDTO.setOther(props);
        sqlServerDatasourceParamDTO.setMode(AzureSQLAuthMode.SQL_PASSWORD);

        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            AzureSQLConnectionParam connectionParams = (AzureSQLConnectionParam) sqlServerDatasourceProcessor
                    .createConnectionParams(sqlServerDatasourceParamDTO);
            Assertions.assertEquals("jdbc:sqlserver://localhost:1234", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default;authentication=SqlPassword",
                    connectionParams.getJdbcUrl());
            Assertions.assertEquals("root", connectionParams.getUser());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:sqlserver://localhost:1234\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:sqlserver://localhost:1234;databaseName=default\"}";
        AzureSQLConnectionParam sqlServerConnectionParam =
                JSONUtils.parseObject(connectionJson, AzureSQLConnectionParam.class);
        Assertions.assertNotNull(sqlServerConnectionParam);
        Assertions.assertEquals("root", sqlServerConnectionParam.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_SQLSERVER_JDBC_DRIVER,
                sqlServerDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        AzureSQLConnectionParam sqlServerConnectionParam = new AzureSQLConnectionParam();
        sqlServerConnectionParam.setJdbcUrl("jdbc:sqlserver://localhost:1234;databaseName=default");
        Assertions.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default",
                sqlServerDatasourceProcessor.getJdbcUrl(sqlServerConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.AZURESQL, sqlServerDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.SQLSERVER_VALIDATION_QUERY,
                sqlServerDatasourceProcessor.getValidationQuery());
    }
}
