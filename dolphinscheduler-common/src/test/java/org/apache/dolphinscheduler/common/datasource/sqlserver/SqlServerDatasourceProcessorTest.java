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

package org.apache.dolphinscheduler.common.datasource.sqlserver;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.sql.DriverManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class, DriverManager.class})
public class SqlServerDatasourceProcessorTest {

    private SqlServerDatasourceProcessor sqlServerDatasourceProcessor = new SqlServerDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        SqlServerDatasourceParamDTO sqlServerDatasourceParamDTO = new SqlServerDatasourceParamDTO();
        sqlServerDatasourceParamDTO.setUserName("root");
        sqlServerDatasourceParamDTO.setPassword("123456");
        sqlServerDatasourceParamDTO.setDatabase("default");
        sqlServerDatasourceParamDTO.setHost("localhost");
        sqlServerDatasourceParamDTO.setPort(1234);

        SqlServerConnectionParam connectionParams = (SqlServerConnectionParam) sqlServerDatasourceProcessor
                .createConnectionParams(sqlServerDatasourceParamDTO);
        Assert.assertEquals("jdbc:sqlserver://localhost:1234", connectionParams.getAddress());
        Assert.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default", connectionParams.getJdbcUrl());
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:sqlserver://localhost:1234\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:sqlserver://localhost:1234;databaseName=default\"}";
        SqlServerConnectionParam sqlServerConnectionParam = JSONUtils.parseObject(connectionJson, SqlServerConnectionParam.class);
        Assert.assertNotNull(sqlServerConnectionParam);
        Assert.assertEquals("root", sqlServerConnectionParam.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(Constants.COM_SQLSERVER_JDBC_DRIVER, sqlServerDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        SqlServerConnectionParam sqlServerConnectionParam = new SqlServerConnectionParam();
        sqlServerConnectionParam.setJdbcUrl("jdbc:sqlserver://localhost:1234;databaseName=default");
        sqlServerConnectionParam.setOther("other");
        Assert.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default;other",
                sqlServerDatasourceProcessor.getJdbcUrl(sqlServerConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.SQLSERVER, sqlServerDatasourceProcessor.getDbType());
    }
}