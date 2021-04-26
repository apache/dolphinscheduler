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

package org.apache.dolphinscheduler.common.datasource.postgresql;

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
public class PostgreSqlDatasourceProcessorTest {

    private PostgreSqlDatasourceProcessor postgreSqlDatasourceProcessor = new PostgreSqlDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        PostgreSqlDatasourceParamDTO postgreSqlDatasourceParamDTO = new PostgreSqlDatasourceParamDTO();
        postgreSqlDatasourceParamDTO.setUserName("root");
        postgreSqlDatasourceParamDTO.setPassword("123456");
        postgreSqlDatasourceParamDTO.setHost("localhost");
        postgreSqlDatasourceParamDTO.setPort(3308);
        postgreSqlDatasourceParamDTO.setDatabase("default");

        PostgreSqlConnectionParam connectionParams = (PostgreSqlConnectionParam) postgreSqlDatasourceProcessor
                .createConnectionParams(postgreSqlDatasourceParamDTO);
        Assert.assertEquals("jdbc:postgresql://localhost:3308", connectionParams.getAddress());
        Assert.assertEquals("jdbc:postgresql://localhost:3308/default", connectionParams.getJdbcUrl());
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:postgresql://localhost:3308\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:postgresql://localhost:3308/default\"}";
        PostgreSqlConnectionParam connectionParams = (PostgreSqlConnectionParam) postgreSqlDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(Constants.ORG_POSTGRESQL_DRIVER, postgreSqlDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        PostgreSqlConnectionParam postgreSqlConnectionParam = new PostgreSqlConnectionParam();
        postgreSqlConnectionParam.setJdbcUrl("jdbc:postgresql://localhost:3308/default");
        postgreSqlConnectionParam.setOther("other");

        String jdbcUrl = postgreSqlDatasourceProcessor.getJdbcUrl(postgreSqlConnectionParam);
        Assert.assertEquals("jdbc:postgresql://localhost:3308/default?other", jdbcUrl);

    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.POSTGRESQL, postgreSqlDatasourceProcessor.getDbType());
    }
}