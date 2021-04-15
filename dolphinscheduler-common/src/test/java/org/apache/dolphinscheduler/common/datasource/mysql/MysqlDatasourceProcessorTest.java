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

package org.apache.dolphinscheduler.common.datasource.mysql;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class, DriverManager.class, MysqlDatasourceProcessor.class})
public class MysqlDatasourceProcessorTest {

    private MysqlDatasourceProcessor mysqlDatasourceProcessor = new MysqlDatasourceProcessor();

    @Test
    public void createConnectionParams() {
        MysqlDatasourceParamDTO mysqlDatasourceParamDTO = new MysqlDatasourceParamDTO();
        mysqlDatasourceParamDTO.setUserName("root");
        mysqlDatasourceParamDTO.setPassword("123456");
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setPort(3306);
        mysqlDatasourceParamDTO.setDatabase("default");

        MysqlConnectionParam connectionParams = (MysqlConnectionParam) mysqlDatasourceProcessor
                .createConnectionParams(mysqlDatasourceParamDTO);
        System.out.println(JSONUtils.toJsonString(connectionParams));
        Assert.assertEquals("jdbc:mysql://localhost:3306", connectionParams.getAddress());
        Assert.assertEquals("jdbc:mysql://localhost:3306/default", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://localhost:3306\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:mysql://localhost:3306/default\"}";
        MysqlConnectionParam connectionParams = (MysqlConnectionParam) mysqlDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionJson);
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void getDatasourceDriver() {
        Assert.assertEquals(Constants.COM_MYSQL_JDBC_DRIVER, mysqlDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void getJdbcUrl() {
        MysqlConnectionParam mysqlConnectionParam = new MysqlConnectionParam();
        mysqlConnectionParam.setJdbcUrl("jdbc:mysql://localhost:3306/default");
        Assert.assertEquals("jdbc:mysql://localhost:3306/default?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
                mysqlDatasourceProcessor.getJdbcUrl(mysqlConnectionParam));
    }

    @Test
    public void getConnection() throws SQLException, ClassNotFoundException {
        MysqlConnectionParam mysqlConnectionParam = new MysqlConnectionParam();
        mysqlConnectionParam.setUser("root");
        mysqlConnectionParam.setPassword("123456");
        PowerMockito.mockStatic(Class.class);
        PowerMockito.mockStatic(DriverManager.class);
        PowerMockito.when(DriverManager.getConnection(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
        mysqlDatasourceProcessor.getConnection(mysqlConnectionParam);
        Assert.assertTrue(true);
    }

    @Test
    public void getDbType() {
        Assert.assertEquals(DbType.MYSQL, mysqlDatasourceProcessor.getDbType());
    }
}