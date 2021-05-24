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

package org.apache.dolphinscheduler.common.datasource;

import org.apache.dolphinscheduler.common.datasource.mysql.MysqlConnectionParam;
import org.apache.dolphinscheduler.common.datasource.mysql.MysqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.mysql.MysqlDatasourceProcessor;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.sql.Connection;
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
public class DatasourceUtilTest {

    @Test
    public void testCheckDatasourceParam() {
        MysqlDatasourceParamDTO mysqlDatasourceParamDTO = new MysqlDatasourceParamDTO();
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setDatabase("default");
        mysqlDatasourceParamDTO.setOther(null);
        DatasourceUtil.checkDatasourceParam(mysqlDatasourceParamDTO);
        Assert.assertTrue(true);
    }

    @Test
    public void testBuildConnectionParams() {
        MysqlDatasourceParamDTO mysqlDatasourceParamDTO = new MysqlDatasourceParamDTO();
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setDatabase("default");
        mysqlDatasourceParamDTO.setUserName("root");
        mysqlDatasourceParamDTO.setPort(3306);
        mysqlDatasourceParamDTO.setPassword("123456");
        ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(mysqlDatasourceParamDTO);
        Assert.assertNotNull(connectionParam);
    }

    @Test
    public void testBuildConnectionParams2() {
        MysqlDatasourceParamDTO mysqlDatasourceParamDTO = new MysqlDatasourceParamDTO();
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setDatabase("default");
        mysqlDatasourceParamDTO.setUserName("root");
        mysqlDatasourceParamDTO.setPort(3306);
        mysqlDatasourceParamDTO.setPassword("123456");
        ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(DbType.MYSQL, JSONUtils.toJsonString(mysqlDatasourceParamDTO));
        Assert.assertNotNull(connectionParam);
    }

    @Test
    public void testGetConnection() throws ClassNotFoundException, SQLException {
        PowerMockito.mockStatic(Class.class);
        PowerMockito.when(Class.forName(Mockito.any())).thenReturn(null);
        PowerMockito.mockStatic(DriverManager.class);
        PowerMockito.when(DriverManager.getConnection(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

        MysqlConnectionParam connectionParam = new MysqlConnectionParam();
        connectionParam.setUser("root");
        connectionParam.setPassword("123456");
        Connection connection = DatasourceUtil.getConnection(DbType.MYSQL, connectionParam);

        Assert.assertNull(connection);

    }

    @Test
    public void testGetJdbcUrl() {
        MysqlConnectionParam mysqlConnectionParam = new MysqlConnectionParam();
        mysqlConnectionParam.setJdbcUrl("jdbc:mysql://localhost:3308");
        String jdbcUrl = DatasourceUtil.getJdbcUrl(DbType.MYSQL, mysqlConnectionParam);
        Assert.assertEquals("jdbc:mysql://localhost:3308?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
                jdbcUrl);
    }

    @Test
    public void testBuildDatasourceParamDTO() {
        MysqlConnectionParam connectionParam = new MysqlConnectionParam();
        connectionParam.setJdbcUrl("jdbc:mysql://localhost:3308?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false");
        connectionParam.setAddress("jdbc:mysql://localhost:3308");
        connectionParam.setUser("root");
        connectionParam.setPassword("123456");

        Assert.assertNotNull(DatasourceUtil.buildDatasourceParamDTO(DbType.MYSQL, JSONUtils.toJsonString(connectionParam)));

    }

    @Test
    public void testGetDatasourceProcessor() {
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.MYSQL));
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.POSTGRESQL));
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.HIVE));
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.SPARK));
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.CLICKHOUSE));
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.ORACLE));
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.SQLSERVER));
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.DB2));
        Assert.assertNotNull(DatasourceUtil.getDatasourceProcessor(DbType.PRESTO));
    }

    @Test(expected = Exception.class)
    public void testGetDatasourceProcessorError() {
        DatasourceUtil.getDatasourceProcessor(null);
    }
}