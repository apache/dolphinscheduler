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

package org.apache.dolphinscheduler.plugin.datasource.api.provider;

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourcePluginManager;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.DataSourceParam;
import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JdbcDataSourceProvider.class, BasicDataSource.class,  DriverManager.class, DataSourceClientProvider.class,
        PasswordUtils.class, CommonUtils.class, DataSourcePluginManager.class})
public class JdbcDataSourceProviderTest {

    @Test
    public void testGetDataSourceFactory() {
        PowerMockito.mockStatic(JdbcDataSourceProvider.class);
        DataSourceFactory dataSourceFactoryBasic = PowerMockito.mock(DataSourceFactoryBasicImpl.class);
        PowerMockito.when(JdbcDataSourceProvider.getDataSourceFactory()).thenReturn(dataSourceFactoryBasic);
        Assert.assertNotNull(JdbcDataSourceProvider.getDataSourceFactory());
    }

    @Test
    public void testBuildConnectionParams() {
        DataSourceParam mysqlDatasourceParam = new DataSourceParam();
        mysqlDatasourceParam.setDbType(DbType.MYSQL);
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:postgresql://172.16.133.200:3306/dolphinscheduler");
        props.put("user", "mysql");
        props.put("password", "123456");
        mysqlDatasourceParam.setProps(props);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("123456");
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.getKerberosStartupState()).thenReturn(false);
        JdbcConnectionParam connectionParam = JdbcDataSourceProvider.buildConnectionParams(mysqlDatasourceParam);
        Assert.assertNotNull(connectionParam);
    }

    @Test
    public void testBuildConnectionParams2() {
        DataSourceParam mysqlDatasourceParam = new DataSourceParam();
        mysqlDatasourceParam.setDbType(DbType.POSTGRESQL);
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:mysql://172.16.133.200:5432/dolphinscheduler");
        props.put("user", "postgres");
        props.put("password", "");
        mysqlDatasourceParam.setProps(props);
        JdbcConnectionParam connectionParam = JdbcDataSourceProvider.buildConnectionParams(JSONUtils.toJsonString(mysqlDatasourceParam));
        Assert.assertNotNull(connectionParam);
    }

    @Test
    public void testGetConnection() {

        PowerMockito.mockStatic(DataSourcePluginManager.class);
        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.when(DataSourcePluginManager.getConnection(Mockito.any())).thenReturn(connection);

        JdbcConnectionParam connectionParam = new JdbcConnectionParam();
        connectionParam.setUser("root");
        connectionParam.setPassword("123456");
        connection = DataSourcePluginManager.getConnection(connectionParam);

        Assert.assertNotNull(connection);

    }

}
