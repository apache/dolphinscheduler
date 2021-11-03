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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource.clickhouse;

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DatasourceUtil;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;

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
@PrepareForTest({Class.class, DriverManager.class, DatasourceUtil.class, CommonUtils.class, DataSourceClientProvider.class, PasswordUtils.class})
public class ClickHouseDatasourceProcessorTest {

    private ClickHouseDatasourceProcessor clickHouseDatasourceProcessor = new ClickHouseDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        ClickHouseDatasourceParamDTO clickhouseConnectionParam = new ClickHouseDatasourceParamDTO();
        clickhouseConnectionParam.setUserName("user");
        clickhouseConnectionParam.setPassword("password");
        clickhouseConnectionParam.setHost("localhost");
        clickhouseConnectionParam.setPort(8123);
        clickhouseConnectionParam.setDatabase("default");
        clickhouseConnectionParam.setOther(props);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
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

    @Test
    public void testGetValidationQuery() {
        Assert.assertEquals(Constants.CLICKHOUSE_VALIDATION_QUERY, clickHouseDatasourceProcessor.getValidationQuery());
    }
}