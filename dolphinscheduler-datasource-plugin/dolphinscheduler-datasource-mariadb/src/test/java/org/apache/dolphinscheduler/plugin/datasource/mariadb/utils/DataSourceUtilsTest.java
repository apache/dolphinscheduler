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

package org.apache.dolphinscheduler.plugin.datasource.mariadb.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.mariadb.param.MariaDBConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.mariadb.param.MariaDBDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.SQLException;
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
        MariaDBDataSourceParamDTO mariadbDatasourceParamDTO = new MariaDBDataSourceParamDTO();
        mariadbDatasourceParamDTO.setHost("0.0.0.0");
        mariadbDatasourceParamDTO.setDatabase("default");
        Map<String, String> other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        other.put("queryTimeout", "-1");
        other.put("characterEncoding", "utf8");
        mariadbDatasourceParamDTO.setOther(other);
        DataSourceUtils.checkDatasourceParam(mariadbDatasourceParamDTO);
        Assertions.assertTrue(true);
    }
    @Test
    public void testCheckIpv6DatasourceParam() {
        MariaDBDataSourceParamDTO mariadbDatasourceParamDTO = new MariaDBDataSourceParamDTO();
        mariadbDatasourceParamDTO.setHost("0000:0000:0000::0000");
        mariadbDatasourceParamDTO.setDatabase("default");
        Map<String, String> other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        other.put("queryTimeout", "-1");
        other.put("characterEncoding", "utf8");
        mariadbDatasourceParamDTO.setOther(other);
        DataSourceUtils.checkDatasourceParam(mariadbDatasourceParamDTO);
        Assertions.assertTrue(true);
    }
    @Test
    public void testBuildConnectionParams() {
        MariaDBDataSourceParamDTO mariadbDatasourceParamDTO = new MariaDBDataSourceParamDTO();
        mariadbDatasourceParamDTO.setHost("localhost");
        mariadbDatasourceParamDTO.setDatabase("default");
        mariadbDatasourceParamDTO.setUserName("root");
        mariadbDatasourceParamDTO.setPort(3306);
        mariadbDatasourceParamDTO.setPassword("123456");

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString()))
                    .thenReturn("123456");
            mockedStaticCommonUtils.when(CommonUtils::getKerberosStartupState).thenReturn(false);
            ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(mariadbDatasourceParamDTO);
            Assertions.assertNotNull(connectionParam);
        }
    }

    @Test
    public void testBuildConnectionParams2() {
        MariaDBDataSourceParamDTO mariadbDatasourceParamDTO = new MariaDBDataSourceParamDTO();
        mariadbDatasourceParamDTO.setHost("localhost");
        mariadbDatasourceParamDTO.setDatabase("default");
        mariadbDatasourceParamDTO.setUserName("root");
        mariadbDatasourceParamDTO.setPort(3306);
        mariadbDatasourceParamDTO.setPassword("123456");
        ConnectionParam connectionParam =
                DataSourceUtils.buildConnectionParams(DbType.MARIADB,
                        JSONUtils.toJsonString(mariadbDatasourceParamDTO));
        Assertions.assertNotNull(connectionParam);
    }

    @Test
    public void testGetConnection() throws ExecutionException, SQLException {
        try (
                MockedStatic<PropertyUtils> mockedStaticPropertyUtils = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<DataSourceClientProvider> mockedStaticDataSourceClientProvider =
                        Mockito.mockStatic(DataSourceClientProvider.class)) {
            mockedStaticPropertyUtils.when(() -> PropertyUtils.getLong("kerberos.expire.time", 24L)).thenReturn(24L);

            Connection connection = Mockito.mock(Connection.class);
            Mockito.when(DataSourceClientProvider.getAdHocConnection(Mockito.any(), Mockito.any()))
                    .thenReturn(connection);

            MariaDBConnectionParam connectionParam = new MariaDBConnectionParam();
            connectionParam.setUser("root");
            connectionParam.setPassword("123456");

            Assertions.assertNotNull(connection);
        }
    }

    @Test
    public void testGetJdbcUrl() {
        MariaDBConnectionParam mariadbConnectionParam = new MariaDBConnectionParam();
        mariadbConnectionParam.setJdbcUrl("jdbc:mariadb://localhost:3308");
        String jdbcUrl = DataSourceUtils.getJdbcUrl(DbType.MARIADB, mariadbConnectionParam);
        Assertions.assertEquals(
                "jdbc:mariadb://localhost:3308",
                jdbcUrl);
    }

    @Test
    public void testBuildDatasourceParamDTO() {
        MariaDBConnectionParam connectionParam = new MariaDBConnectionParam();
        connectionParam.setJdbcUrl(
                "jdbc:mariadb://localhost:3308?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false");
        connectionParam.setAddress("jdbc:mariadb://localhost:3308");
        connectionParam.setUser("root");
        connectionParam.setPassword("123456");

        Assertions.assertNotNull(
                DataSourceUtils.buildDatasourceParamDTO(DbType.MARIADB, JSONUtils.toJsonString(connectionParam)));

    }

    @Test
    public void testGetDatasourceProcessor() {
        Assertions.assertNotNull(DataSourceUtils.getDatasourceProcessor(DbType.MARIADB));
    }

    @Test
    public void testGetDatasourceProcessorError() {
        Assertions.assertThrows(Exception.class, () -> {
            DataSourceUtils.getDatasourceProcessor(null);
        });
    }
}
