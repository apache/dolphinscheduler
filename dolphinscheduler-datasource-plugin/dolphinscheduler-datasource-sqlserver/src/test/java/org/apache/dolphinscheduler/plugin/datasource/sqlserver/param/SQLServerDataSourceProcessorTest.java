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
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.DriverManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class, DriverManager.class, DataSourceUtils.class, CommonUtils.class, DataSourceClientProvider.class, PasswordUtils.class})
public class SQLServerDataSourceProcessorTest {

    private SQLServerDataSourceProcessor sqlServerDatasourceProcessor = new SQLServerDataSourceProcessor();

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:sqlserver://localhost:1234\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:sqlserver://localhost:1234;databaseName=default\"}";
        SQLServerConnectionParam sqlServerConnectionParam = JSONUtils.parseObject(connectionJson, SQLServerConnectionParam.class);
        Assert.assertNotNull(sqlServerConnectionParam);
        Assert.assertEquals("root", sqlServerConnectionParam.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_SQLSERVER_JDBC_DRIVER,
                sqlServerDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        SQLServerConnectionParam sqlServerConnectionParam = new SQLServerConnectionParam();
        sqlServerConnectionParam.setJdbcUrl("jdbc:sqlserver://localhost:1234;databaseName=default");
        sqlServerConnectionParam.setOther("other");
        Assert.assertEquals("jdbc:sqlserver://localhost:1234;databaseName=default;other",
                sqlServerDatasourceProcessor.getJdbcUrl(sqlServerConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.SQLSERVER, sqlServerDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.SQLSERVER_VALIDATION_QUERY,
                sqlServerDatasourceProcessor.getValidationQuery());
    }
}