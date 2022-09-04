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

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

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
@PrepareForTest({Class.class, DriverManager.class, DataSourceUtils.class, CommonUtils.class, DataSourceClientProvider.class, PasswordUtils.class})
public class SQLServerDataSourceProcessorTest {

    private SQLServerDataSourceProcessor sqlServerDataSourceProcessor = new SQLServerDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        SQLServerDataSourceParamDTO sqlServerDataSourceParamDTO = new SQLServerDataSourceParamDTO();
        sqlServerDataSourceParamDTO.setUserName("root");
        sqlServerDataSourceParamDTO.setPassword("123456");
        sqlServerDataSourceParamDTO.setDatabase("default");
        sqlServerDataSourceParamDTO.setHost("localhost");
        sqlServerDataSourceParamDTO.setPort(1234);
        sqlServerDataSourceParamDTO.setOther(props);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        SQLServerConnectionParam connectionParams = (SQLServerConnectionParam) sqlServerDataSourceProcessor
                .createConnectionParams(sqlServerDataSourceParamDTO);
        Assert.assertEquals("jdbc:sqlserver://localhost:1234", connectionParams.getAddress());
        Assert.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default", connectionParams.getJdbcUrl());
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:sqlserver://localhost:1234\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:sqlserver://localhost:1234;databaseName=default\"}";
        SQLServerConnectionParam sqlServerConnectionParam = JSONUtils.parseObject(connectionJson, SQLServerConnectionParam.class);
        Assert.assertNotNull(sqlServerConnectionParam);
        Assert.assertEquals("root", sqlServerConnectionParam.getUser());
    }

    @Test
    public void testGetDataSourceDriver() {
        Assert.assertEquals(Constants.COM_SQLSERVER_JDBC_DRIVER, sqlServerDataSourceProcessor.getDataSourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        SQLServerConnectionParam sqlServerConnectionParam = new SQLServerConnectionParam();
        sqlServerConnectionParam.setJdbcUrl("jdbc:sqlserver://localhost:1234;databaseName=default");
        sqlServerConnectionParam.setOther("other");
        Assert.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default;other",
                sqlServerDataSourceProcessor.getJdbcUrl(sqlServerConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.SQLSERVER, sqlServerDataSourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assert.assertEquals(Constants.SQLSERVER_VALIDATION_QUERY, sqlServerDataSourceProcessor.getValidationQuery());
    }
}