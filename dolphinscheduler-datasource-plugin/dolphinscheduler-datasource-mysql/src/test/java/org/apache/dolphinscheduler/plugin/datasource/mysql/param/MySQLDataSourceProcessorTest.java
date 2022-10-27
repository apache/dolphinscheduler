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

package org.apache.dolphinscheduler.plugin.datasource.mysql.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
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
public class MySQLDataSourceProcessorTest {

    private MySQLDataSourceProcessor mysqlDatasourceProcessor = new MySQLDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        MySQLDataSourceParamDTO mysqlDatasourceParamDTO = new MySQLDataSourceParamDTO();
        mysqlDatasourceParamDTO.setUserName("root");
        mysqlDatasourceParamDTO.setPassword("123456");
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setPort(3306);
        mysqlDatasourceParamDTO.setDatabase("default");
        mysqlDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            MySQLConnectionParam connectionParams = (MySQLConnectionParam) mysqlDatasourceProcessor
                    .createConnectionParams(mysqlDatasourceParamDTO);
            Assertions.assertEquals("jdbc:mysql://localhost:3306", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:mysql://localhost:3306/default", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://localhost:3306\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:mysql://localhost:3306/default\"}";
        MySQLConnectionParam connectionParams = (MySQLConnectionParam) mysqlDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_MYSQL_CJ_JDBC_DRIVER,
                mysqlDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        MySQLConnectionParam mysqlConnectionParam = new MySQLConnectionParam();
        mysqlConnectionParam.setJdbcUrl("jdbc:mysql://localhost:3306/default");
        Assertions.assertEquals(
                "jdbc:mysql://localhost:3306/default?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
                mysqlDatasourceProcessor.getJdbcUrl(mysqlConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.MYSQL, mysqlDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.MYSQL_VALIDATION_QUERY,
                mysqlDatasourceProcessor.getValidationQuery());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        MySQLConnectionParam mysqlConnectionParam = new MySQLConnectionParam();
        mysqlConnectionParam.setJdbcUrl("jdbc:mysql://localhost:3306/default");
        mysqlConnectionParam.setUser("root");
        mysqlConnectionParam.setPassword("123456");
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("123456");
            Assertions.assertEquals("mysql@root@123456@jdbc:mysql://localhost:3306/default",
                    mysqlDatasourceProcessor.getDatasourceUniqueId(mysqlConnectionParam, DbType.MYSQL));
        }
    }
}
