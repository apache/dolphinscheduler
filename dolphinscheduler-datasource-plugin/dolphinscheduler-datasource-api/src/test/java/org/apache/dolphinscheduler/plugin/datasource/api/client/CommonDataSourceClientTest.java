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

package org.apache.dolphinscheduler.plugin.datasource.api.client;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.MySQLConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.api.provider.JDBCDataSourceProvider;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient")
@PrepareForTest(value = {HikariDataSource.class, CommonDataSourceClient.class, JDBCDataSourceProvider.class, JdbcTemplate.class, Connection.class})
public class CommonDataSourceClientTest {

    @Mock
    private CommonDataSourceClient commonDataSourceClient;

    @Test
    public void testPreInit() {
        PowerMockito.doNothing().when(commonDataSourceClient).preInit();
        commonDataSourceClient.preInit();
        Mockito.verify(commonDataSourceClient).preInit();
    }

    @Test
    public void testCheckEnv() {
        BaseConnectionParam baseConnectionParam = new MySQLConnectionParam();
        PowerMockito.doNothing().when(commonDataSourceClient).checkEnv(Mockito.any(BaseConnectionParam.class));
        commonDataSourceClient.checkEnv(baseConnectionParam);
        Mockito.verify(commonDataSourceClient).checkEnv(Mockito.any(BaseConnectionParam.class));

        PowerMockito.doNothing().when(commonDataSourceClient).checkValidationQuery(Mockito.any(BaseConnectionParam.class));
        commonDataSourceClient.checkValidationQuery(baseConnectionParam);
        Mockito.verify(commonDataSourceClient).checkValidationQuery(Mockito.any(BaseConnectionParam.class));

        PowerMockito.doNothing().when(commonDataSourceClient).checkUser(Mockito.any(BaseConnectionParam.class));
        commonDataSourceClient.checkUser(baseConnectionParam);
        Mockito.verify(commonDataSourceClient).checkUser(Mockito.any(BaseConnectionParam.class));

        PowerMockito.doNothing().when(commonDataSourceClient).setDefaultUsername(Mockito.any(BaseConnectionParam.class));
        commonDataSourceClient.setDefaultUsername(baseConnectionParam);
        Mockito.verify(commonDataSourceClient).setDefaultUsername(Mockito.any(BaseConnectionParam.class));
    }

    @Test
    public void testInitClient() {
        BaseConnectionParam baseConnectionParam = new MySQLConnectionParam();
        PowerMockito.doNothing().when(commonDataSourceClient).initClient(Mockito.any(BaseConnectionParam.class), Mockito.any());
        commonDataSourceClient.initClient(baseConnectionParam, DbType.MYSQL);
        Mockito.verify(commonDataSourceClient).initClient(Mockito.any(BaseConnectionParam.class), Mockito.any());
    }

    @Test
    public void testCheckClient() {
        PowerMockito.doNothing().when(this.commonDataSourceClient).checkClient();
        this.commonDataSourceClient.checkClient();
        Mockito.verify(this.commonDataSourceClient).checkClient();
    }

    @Test
    public void testGetConnection() {
        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.when(commonDataSourceClient.getConnection()).thenReturn(connection);
        Assert.assertNotNull(commonDataSourceClient.getConnection());
    }
}
