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

package org.apache.dolphinscheduler.common.datasource.clickhouse;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;

import java.sql.DriverManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class, DriverManager.class})
public class ClickHouseDatasourceProcessorTest {

    private ClickHouseDatasourceProcessor clickHouseDatasourceProcessor = new ClickHouseDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        ClickHouseDatasourceParamDTO clickhouseConnectionParam = new ClickHouseDatasourceParamDTO();
        clickhouseConnectionParam.setUserName("user");
        clickhouseConnectionParam.setPassword("password");
        clickhouseConnectionParam.setHost("localhost");
        clickhouseConnectionParam.setPort(8123);
        clickhouseConnectionParam.setDatabase("default");
        ClickhouseConnectionParam connectionParams = (ClickhouseConnectionParam) clickHouseDatasourceProcessor
                .createConnectionParams(clickhouseConnectionParam);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("jdbc:clickhouse://localhost:8123", connectionParams.getAddress());
        Assert.assertEquals("jdbc:clickhouse://localhost:8123/default", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionParamJson = "{\"address\":\"jdbc:clickhouse://localhost:8123\",\"database\":\"default\","
                + "\"jdbcUrl\":\"jdbc:clickhouse://localhost:8123/default\",\"user\":\"default\",\"password\":\"123456\"}";
        ClickhouseConnectionParam clickhouseConnectionParam = (ClickhouseConnectionParam) clickHouseDatasourceProcessor
                .createConnectionParams(connectionParamJson);
        Assert.assertNotNull(clickhouseConnectionParam);
        Assert.assertEquals("default", clickhouseConnectionParam.getUser());
        Assert.assertEquals("123456", clickhouseConnectionParam.getPassword());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertNotNull(clickHouseDatasourceProcessor.getDatasourceDriver());
        Assert.assertEquals(Constants.COM_CLICKHOUSE_JDBC_DRIVER, clickHouseDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        ClickhouseConnectionParam connectionParam = new ClickhouseConnectionParam();
        connectionParam.setUser("default");
        connectionParam.setJdbcUrl("jdbc:clickhouse://localhost:8123/default");
        connectionParam.setOther("other=other1");
        String jdbcUrl = clickHouseDatasourceProcessor.getJdbcUrl(connectionParam);
        Assert.assertEquals("jdbc:clickhouse://localhost:8123/default?other=other1", jdbcUrl);
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.CLICKHOUSE, clickHouseDatasourceProcessor.getDbType());
    }
}