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

package org.apache.dolphinscheduler.plugin.datasource.xugu.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.xugu.param.XuguConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.xugu.param.XuguDataSourceParamDTO;
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
        XuguDataSourceParamDTO xuguDataSourceParamDTO = new XuguDataSourceParamDTO();
        xuguDataSourceParamDTO.setHost("0.0.0.0");
        xuguDataSourceParamDTO.setDatabase("default");
        Map<String, String> other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        other.put("queryTimeout", "-1");
        other.put("characterEncoding", "utf8");
        xuguDataSourceParamDTO.setOther(other);
        DataSourceUtils.checkDatasourceParam(xuguDataSourceParamDTO);
        Assertions.assertTrue(true);
    }
    @Test
    public void testCheckIpv6DatasourceParam() {
        XuguDataSourceParamDTO xuguDataSourceParamDTO = new XuguDataSourceParamDTO();
        xuguDataSourceParamDTO.setHost("0000:0000:0000::0000");
        xuguDataSourceParamDTO.setDatabase("default");
        Map<String, String> other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        other.put("queryTimeout", "-1");
        other.put("characterEncoding", "utf8");
        xuguDataSourceParamDTO.setOther(other);
        DataSourceUtils.checkDatasourceParam(xuguDataSourceParamDTO);
        Assertions.assertTrue(true);
    }
    @Test
    public void testBuildConnectionParams() {
        XuguDataSourceParamDTO xuguDataSourceParamDTO = new XuguDataSourceParamDTO();
        xuguDataSourceParamDTO.setHost("localhost");
        xuguDataSourceParamDTO.setDatabase("SYSTEM");
        xuguDataSourceParamDTO.setUserName("SYSDBA");
        xuguDataSourceParamDTO.setPort(5138);
        xuguDataSourceParamDTO.setPassword("SYSDBA");

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString()))
                    .thenReturn("SYSDBA");
            mockedStaticCommonUtils.when(CommonUtils::getKerberosStartupState).thenReturn(false);
            ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(xuguDataSourceParamDTO);
            Assertions.assertNotNull(connectionParam);
        }
    }

    @Test
    public void testBuildConnectionParams2() {
        XuguDataSourceParamDTO xuguDataSourceParamDTO = new XuguDataSourceParamDTO();
        xuguDataSourceParamDTO.setHost("localhost");
        xuguDataSourceParamDTO.setDatabase("SYSTEM");
        xuguDataSourceParamDTO.setUserName("SYSDBA");
        xuguDataSourceParamDTO.setPort(5138);
        xuguDataSourceParamDTO.setPassword("SYSDBA");
        ConnectionParam connectionParam =
                DataSourceUtils.buildConnectionParams(DbType.XUGU, JSONUtils.toJsonString(xuguDataSourceParamDTO));
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

            XuguConnectionParam connectionParam = new XuguConnectionParam();
            connectionParam.setUser("SYSDBA");
            connectionParam.setPassword("SYSDBA");

            Assertions.assertNotNull(connection);
        }
    }

    @Test
    public void testGetJdbcUrl() {
        XuguConnectionParam xuguConnectionParam = new XuguConnectionParam();
        xuguConnectionParam.setJdbcUrl("jdbc:xugu://localhost:5138");
        String jdbcUrl = DataSourceUtils.getJdbcUrl(DbType.XUGU, xuguConnectionParam);
        Assertions.assertEquals(
                "jdbc:xugu://localhost:5138",
                jdbcUrl);
    }

    @Test
    public void testBuildDatasourceParamDTO() {
        XuguConnectionParam connectionParam = new XuguConnectionParam();
        connectionParam.setJdbcUrl(
                "jdbc:xugu://localhost:5138?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false");
        connectionParam.setAddress("jdbc:xugu://localhost:5138");
        connectionParam.setUser("SYSDBA");
        connectionParam.setPassword("SYSDBA");

        Assertions.assertNotNull(
                DataSourceUtils.buildDatasourceParamDTO(DbType.XUGU, JSONUtils.toJsonString(connectionParam)));

    }

    @Test
    public void testGetDatasourceProcessor() {
        Assertions.assertNotNull(DataSourceUtils.getDatasourceProcessor(DbType.XUGU));
    }

    @Test
    public void testGetDatasourceProcessorError() {
        Assertions.assertThrows(Exception.class, () -> {
            DataSourceUtils.getDatasourceProcessor(null);
        });
    }
}
