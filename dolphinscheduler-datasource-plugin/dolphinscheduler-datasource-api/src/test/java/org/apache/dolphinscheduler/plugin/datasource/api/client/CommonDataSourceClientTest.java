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
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommonDataSourceClientTest {

    @Mock
    private CommonDataSourceClient commonDataSourceClient;

    @Test
    public void testPreInit() {
        Mockito.doNothing().when(commonDataSourceClient).preInit();
        commonDataSourceClient.preInit();
        Mockito.verify(commonDataSourceClient).preInit();
    }

    @Test
    public void testCheckEnv() {
        BaseConnectionParam baseConnectionParam = new MySQLConnectionParam();
        Mockito.doNothing().when(commonDataSourceClient).checkEnv(Mockito.any(BaseConnectionParam.class));
        commonDataSourceClient.checkEnv(baseConnectionParam);
        Mockito.verify(commonDataSourceClient).checkEnv(Mockito.any(BaseConnectionParam.class));

        Mockito.doNothing().when(commonDataSourceClient).checkValidationQuery(Mockito.any(BaseConnectionParam.class));
        commonDataSourceClient.checkValidationQuery(baseConnectionParam);
        Mockito.verify(commonDataSourceClient).checkValidationQuery(Mockito.any(BaseConnectionParam.class));

        Mockito.doNothing().when(commonDataSourceClient).checkUser(Mockito.any(BaseConnectionParam.class));
        commonDataSourceClient.checkUser(baseConnectionParam);
        Mockito.verify(commonDataSourceClient).checkUser(Mockito.any(BaseConnectionParam.class));

        Mockito.doNothing().when(commonDataSourceClient).setDefaultUsername(Mockito.any(BaseConnectionParam.class));
        commonDataSourceClient.setDefaultUsername(baseConnectionParam);
        Mockito.verify(commonDataSourceClient).setDefaultUsername(Mockito.any(BaseConnectionParam.class));
    }

    @Test
    public void testInitClient() {
        BaseConnectionParam baseConnectionParam = new MySQLConnectionParam();
        Mockito.doNothing().when(commonDataSourceClient).initClient(Mockito.any(BaseConnectionParam.class),
                Mockito.any());
        commonDataSourceClient.initClient(baseConnectionParam, DbType.MYSQL);
        Mockito.verify(commonDataSourceClient).initClient(Mockito.any(BaseConnectionParam.class), Mockito.any());
    }

    @Test
    public void testCheckClient() {
        Mockito.doNothing().when(this.commonDataSourceClient).checkClient();
        this.commonDataSourceClient.checkClient();
        Mockito.verify(this.commonDataSourceClient).checkClient();
    }

    @Test
    public void testGetConnection() {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(commonDataSourceClient.getConnection()).thenReturn(connection);
        Assertions.assertNotNull(commonDataSourceClient.getConnection());
    }
}
