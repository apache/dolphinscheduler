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

package org.apache.dolphinscheduler.plugin.datasource.sqlserver.param;

import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

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

    private SQLServerDataSourceProcessor sqlServerDatasourceProcessor = new SQLServerDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        SQLServerDataSourceParamDTO sqlServerDatasourceParamDTO = new SQLServerDataSourceParamDTO();
        sqlServerDatasourceParamDTO.setUserName("root");
        sqlServerDatasourceParamDTO.setPassword("123456");
        sqlServerDatasourceParamDTO.setDatabase("default");
        sqlServerDatasourceParamDTO.setHost("localhost");
        sqlServerDatasourceParamDTO.setPort(1234);
        sqlServerDatasourceParamDTO.setOther(props);

        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            SQLServerConnectionParam connectionParams = (SQLServerConnectionParam) sqlServerDatasourceProcessor
                    .createConnectionParams(sqlServerDatasourceParamDTO);
            Assertions.assertEquals("jdbc:sqlserver://localhost:1234", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default",
                    connectionParams.getJdbcUrl());
            Assertions.assertEquals("root", connectionParams.getUser());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:sqlserver://localhost:1234\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:sqlserver://localhost:1234;databaseName=default\"}";
        SQLServerConnectionParam sqlServerConnectionParam =
                JSONUtils.parseObject(connectionJson, SQLServerConnectionParam.class);
        Assertions.assertNotNull(sqlServerConnectionParam);
        Assertions.assertEquals("root", sqlServerConnectionParam.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(Constants.COM_SQLSERVER_JDBC_DRIVER,
                sqlServerDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        SQLServerConnectionParam sqlServerConnectionParam = new SQLServerConnectionParam();
        sqlServerConnectionParam.setJdbcUrl("jdbc:sqlserver://localhost:1234;databaseName=default");
        sqlServerConnectionParam.setOther("other");
        Assertions.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default;other",
                sqlServerDatasourceProcessor.getJdbcUrl(sqlServerConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.SQLSERVER, sqlServerDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(Constants.SQLSERVER_VALIDATION_QUERY,
                sqlServerDatasourceProcessor.getValidationQuery());
    }
}
