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

package org.apache.dolphinscheduler.common.datasource.db2;

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
public class Db2DatasourceProcessorTest {

    private Db2DatasourceProcessor db2DatasourceProcessor = new Db2DatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Db2DatasourceParamDTO db2DatasourceParamDTO = new Db2DatasourceParamDTO();
        db2DatasourceParamDTO.setUserName("root");
        db2DatasourceParamDTO.setPassword("123456");
        db2DatasourceParamDTO.setHost("localhost");
        db2DatasourceParamDTO.setPort(5142);
        db2DatasourceParamDTO.setDatabase("default");

        Db2ConnectionParam connectionParams = (Db2ConnectionParam) db2DatasourceProcessor
                .createConnectionParams(db2DatasourceParamDTO);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("jdbc:db2://localhost:5142", connectionParams.getAddress());
        Assert.assertEquals("jdbc:db2://localhost:5142/default", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:db2://localhost:5142\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:db2://localhost:5142/default\"}";
        Db2ConnectionParam connectionParams = (Db2ConnectionParam) db2DatasourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionJson);
        Assert.assertEquals("root", connectionParams.getUser());

    }

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(Constants.COM_DB2_JDBC_DRIVER, db2DatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        Db2ConnectionParam db2ConnectionParam = new Db2ConnectionParam();
        db2ConnectionParam.setJdbcUrl("jdbc:db2://localhost:5142/default");
        db2ConnectionParam.setOther("other=other");
        String jdbcUrl = db2DatasourceProcessor.getJdbcUrl(db2ConnectionParam);
        Assert.assertEquals("jdbc:db2://localhost:5142/default;other=other", jdbcUrl);
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.DB2, db2DatasourceProcessor.getDbType());
    }

}