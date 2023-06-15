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

package org.apache.dolphinscheduler.plugin.datasource.impala.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.impala.param.ImpalaConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.impala.param.ImpalaDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.impala.param.ImpalaDataSourceProcessor;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.spi.utils.PropertyUtils")
@PrepareForTest({Class.class, DriverManager.class, ImpalaDataSourceProcessor.class, DataSourceClientProvider.class,
        PasswordUtils.class, CommonUtils.class, PropertyUtils.class})
public class DataSourceUtilsTest {

    @Test
    public void testCheckDatasourceParam() {
        ImpalaDataSourceParamDTO impalaDatasourceParamDTO = new ImpalaDataSourceParamDTO();
        impalaDatasourceParamDTO.setHost("localhost");
        impalaDatasourceParamDTO.setDatabase("default");
        Map<String, String> other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        other.put("queryTimeout", "-1");
        other.put("characterEncoding", "utf8");
        impalaDatasourceParamDTO.setOther(other);
        DataSourceUtils.checkDatasourceParam(impalaDatasourceParamDTO);
        Assert.assertTrue(true);
    }

    @Test
    public void testBuildConnectionParams() {
        ImpalaDataSourceParamDTO impalaDatasourceParamDTO = new ImpalaDataSourceParamDTO();
        impalaDatasourceParamDTO.setHost("localhost");
        impalaDatasourceParamDTO.setDatabase("default");
        impalaDatasourceParamDTO.setUserName("admin");
        impalaDatasourceParamDTO.setPort(21050);
        impalaDatasourceParamDTO.setPassword("admin");
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("admin");
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.getKerberosStartupState()).thenReturn(false);
        ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(impalaDatasourceParamDTO);
        Assert.assertNotNull(connectionParam);
    }

    @Test
    public void testBuildConnectionParams2() {
        ImpalaDataSourceParamDTO impalaDatasourceParamDTO = new ImpalaDataSourceParamDTO();
        impalaDatasourceParamDTO.setHost("localhost");
        impalaDatasourceParamDTO.setDatabase("default");
        impalaDatasourceParamDTO.setUserName("root");
        impalaDatasourceParamDTO.setPort(21050);
        impalaDatasourceParamDTO.setPassword("admin");
        ConnectionParam connectionParam =
                DataSourceUtils.buildConnectionParams(DbType.IMPALA, JSONUtils.toJsonString(impalaDatasourceParamDTO));
        Assert.assertNotNull(connectionParam);
    }

    @Test
    public void testGetConnection() throws ExecutionException {
        PowerMockito.mockStatic(PropertyUtils.class);
        PowerMockito.when(PropertyUtils.getLong("kerberos.expire.time", 24L)).thenReturn(24L);
        PowerMockito.mockStatic(DataSourceClientProvider.class);
        DataSourceClientProvider clientProvider = PowerMockito.mock(DataSourceClientProvider.class);
        PowerMockito.when(DataSourceClientProvider.getInstance()).thenReturn(clientProvider);

        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.when(clientProvider.getConnection(Mockito.any(), Mockito.any())).thenReturn(connection);

        ImpalaConnectionParam connectionParam = new ImpalaConnectionParam();
        connectionParam.setUser("admin");
        connectionParam.setPassword("admin");
        connection = DataSourceClientProvider.getInstance().getConnection(DbType.IMPALA, connectionParam);

        Assert.assertNotNull(connection);

    }

    @Test
    public void testGetJdbcUrl() {
        ImpalaConnectionParam impalaConnectionParam = new ImpalaConnectionParam();
        impalaConnectionParam.setJdbcUrl("jdbc:impala://localhost:21050");
        String jdbcUrl = DataSourceUtils.getJdbcUrl(DbType.IMPALA, impalaConnectionParam);
        Assert.assertEquals("jdbc:impala://localhost:21050;AuthMech=0", jdbcUrl);
    }

    @Test
    public void testBuildDatasourceParamDTO() {
        ImpalaConnectionParam connectionParam = new ImpalaConnectionParam();
        connectionParam.setJdbcUrl("jdbc:impala://localhost:21050;AuthMech=0");
        connectionParam.setAddress("jdbc:impala://localhost:21050");
        connectionParam.setUser("admin");
        connectionParam.setPassword("admin");

        Assert.assertNotNull(
                DataSourceUtils.buildDatasourceParamDTO(DbType.IMPALA, JSONUtils.toJsonString(connectionParam)));

    }

    @Test
    public void testGetDatasourceProcessor() {
        Assert.assertNotNull(DataSourceUtils.getDatasourceProcessor(DbType.IMPALA));
    }

    @Test(expected = Exception.class)
    public void testGetDatasourceProcessorError() {
        DataSourceUtils.getDatasourceProcessor(null);
    }
}
