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

package org.apache.dolphinscheduler.plugin.datasource.mysql.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.mysql.param.MySQLConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.mysql.param.MySQLDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataSourceUtilsTest {

    @Test
    public void testCheckDatasourceParam() {
        MySQLDataSourceParamDTO mysqlDatasourceParamDTO = new MySQLDataSourceParamDTO();
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setDatabase("default");
        Map<String, String> other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        other.put("queryTimeout", "-1");
        other.put("characterEncoding", "utf8");
        mysqlDatasourceParamDTO.setOther(other);
        DataSourceUtils.checkDatasourceParam(mysqlDatasourceParamDTO);
        Assertions.assertTrue(true);
    }

    @Test
    public void testBuildConnectionParams() {
        MySQLDataSourceParamDTO mysqlDatasourceParamDTO = new MySQLDataSourceParamDTO();
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setDatabase("default");
        mysqlDatasourceParamDTO.setUserName("root");
        mysqlDatasourceParamDTO.setPort(3306);
        mysqlDatasourceParamDTO.setPassword("123456");

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString()))
                    .thenReturn("123456");
            mockedStaticCommonUtils.when(CommonUtils::getKerberosStartupState).thenReturn(false);
            ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(mysqlDatasourceParamDTO);
            Assertions.assertNotNull(connectionParam);
        }
    }

    @Test
    public void testBuildConnectionParams2() {
        MySQLDataSourceParamDTO mysqlDatasourceParamDTO = new MySQLDataSourceParamDTO();
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setDatabase("default");
        mysqlDatasourceParamDTO.setUserName("root");
        mysqlDatasourceParamDTO.setPort(3306);
        mysqlDatasourceParamDTO.setPassword("123456");
        ConnectionParam connectionParam =
                DataSourceUtils.buildConnectionParams(DbType.MYSQL, JSONUtils.toJsonString(mysqlDatasourceParamDTO));
        Assertions.assertNotNull(connectionParam);
    }

    @Test
    public void testGetConnection() throws ExecutionException {
        try (
                MockedStatic<PropertyUtils> mockedStaticPropertyUtils = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<DataSourceClientProvider> mockedStaticDataSourceClientProvider =
                        Mockito.mockStatic(DataSourceClientProvider.class)) {
            mockedStaticPropertyUtils.when(() -> PropertyUtils.getLong("kerberos.expire.time", 24L)).thenReturn(24L);
            DataSourceClientProvider clientProvider = Mockito.mock(DataSourceClientProvider.class);
            mockedStaticDataSourceClientProvider.when(DataSourceClientProvider::getInstance).thenReturn(clientProvider);

            Connection connection = Mockito.mock(Connection.class);
            Mockito.when(clientProvider.getConnection(Mockito.any(), Mockito.any())).thenReturn(connection);

            MySQLConnectionParam connectionParam = new MySQLConnectionParam();
            connectionParam.setUser("root");
            connectionParam.setPassword("123456");
            connection = DataSourceClientProvider.getInstance().getConnection(DbType.MYSQL, connectionParam);

            Assertions.assertNotNull(connection);
        }
    }

    @Test
    public void testGetJdbcUrl() {
        MySQLConnectionParam mysqlConnectionParam = new MySQLConnectionParam();
        mysqlConnectionParam.setJdbcUrl("jdbc:mysql://localhost:3308");
        String jdbcUrl = DataSourceUtils.getJdbcUrl(DbType.MYSQL, mysqlConnectionParam);
        Assertions.assertEquals(
                "jdbc:mysql://localhost:3308?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
                jdbcUrl);
    }

    @Test
    public void testBuildDatasourceParamDTO() {
        MySQLConnectionParam connectionParam = new MySQLConnectionParam();
        connectionParam.setJdbcUrl(
                "jdbc:mysql://localhost:3308?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false");
        connectionParam.setAddress("jdbc:mysql://localhost:3308");
        connectionParam.setUser("root");
        connectionParam.setPassword("123456");

        Assertions.assertNotNull(
                DataSourceUtils.buildDatasourceParamDTO(DbType.MYSQL, JSONUtils.toJsonString(connectionParam)));

    }

    @Test
    public void testGetDatasourceProcessor() {
        Assertions.assertNotNull(DataSourceUtils.getDatasourceProcessor(DbType.MYSQL));
    }

    @Test
    public void testGetDatasourceProcessorError() {
        Assertions.assertThrows(Exception.class, () -> {
            DataSourceUtils.getDatasourceProcessor(null);
        });
    }
}
