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

import org.apache.dolphinscheduler.plugin.datasource.api.provider.JdbcDataSourceProvider;
import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;

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

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient")
@PrepareForTest(value = {CommonDataSourceClient.class, JdbcDataSourceProvider.class, JdbcTemplate.class, Connection.class})
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
        JdbcConnectionParam connectionParam = new JdbcConnectionParam();
        PowerMockito.doNothing().when(commonDataSourceClient).checkEnv(Mockito.any(JdbcConnectionParam.class));
        commonDataSourceClient.checkEnv(connectionParam);
        Mockito.verify(commonDataSourceClient).checkEnv(Mockito.any(JdbcConnectionParam.class));

        PowerMockito.doNothing().when(commonDataSourceClient).checkValidationQuery(Mockito.any(JdbcConnectionParam.class));
        commonDataSourceClient.checkValidationQuery(connectionParam);
        Mockito.verify(commonDataSourceClient).checkValidationQuery(Mockito.any(JdbcConnectionParam.class));

        PowerMockito.doNothing().when(commonDataSourceClient).checkUser(Mockito.any(JdbcConnectionParam.class));
        commonDataSourceClient.checkUser(connectionParam);
        Mockito.verify(commonDataSourceClient).checkUser(Mockito.any(JdbcConnectionParam.class));

        PowerMockito.doNothing().when(commonDataSourceClient).setDefaultUsername(Mockito.any(JdbcConnectionParam.class));
        commonDataSourceClient.setDefaultUsername(connectionParam);
        Mockito.verify(commonDataSourceClient).setDefaultUsername(Mockito.any(JdbcConnectionParam.class));

        PowerMockito.doNothing().when(commonDataSourceClient).setDefaultPassword(Mockito.any(JdbcConnectionParam.class));
        commonDataSourceClient.setDefaultPassword(connectionParam);
        Mockito.verify(commonDataSourceClient).setDefaultPassword(Mockito.any(JdbcConnectionParam.class));

    }

    @Test
    public void testInitClient() {
        JdbcConnectionParam connectionParam = new JdbcConnectionParam();
        PowerMockito.doNothing().when(commonDataSourceClient).initClient(Mockito.any(JdbcConnectionParam.class));
        commonDataSourceClient.initClient(connectionParam);
        Mockito.verify(commonDataSourceClient).initClient(Mockito.any(JdbcConnectionParam.class));
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
