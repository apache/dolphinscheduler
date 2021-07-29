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

package org.apache.dolphinscheduler.common.datasource.oracle;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;

import java.sql.DriverManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class, DriverManager.class})
public class OracleDatasourceProcessorTest {

    private OracleDatasourceProcessor oracleDatasourceProcessor = new OracleDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        OracleDatasourceParamDTO oracleDatasourceParamDTO = new OracleDatasourceParamDTO();
        oracleDatasourceParamDTO.setConnectType(DbConnectType.ORACLE_SID);
        oracleDatasourceParamDTO.setHost("localhost");
        oracleDatasourceParamDTO.setPort(3308);
        oracleDatasourceParamDTO.setUserName("root");
        oracleDatasourceParamDTO.setPassword("123456");
        oracleDatasourceParamDTO.setDatabase("default");

        OracleConnectionParam connectionParams = (OracleConnectionParam) oracleDatasourceProcessor
                .createConnectionParams(oracleDatasourceParamDTO);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("jdbc:oracle:thin:@localhost:3308", connectionParams.getAddress());
        Assert.assertEquals("jdbc:oracle:thin:@localhost:3308/default", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:oracle:thin:@localhost:3308\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:oracle:thin:@localhost:3308/default\",\"connectType\":\"ORACLE_SID\"}";
        OracleConnectionParam connectionParams = (OracleConnectionParam) oracleDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(Constants.COM_ORACLE_JDBC_DRIVER, oracleDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        OracleConnectionParam oracleConnectionParam = new OracleConnectionParam();
        oracleConnectionParam.setJdbcUrl("jdbc:oracle:thin:@localhost:3308/default");
        oracleConnectionParam.setOther("other=other");
        Assert.assertEquals("jdbc:oracle:thin:@localhost:3308/default?other=other",
                oracleDatasourceProcessor.getJdbcUrl(oracleConnectionParam));
    }

    @Test
    public void getDbType() {
        Assert.assertEquals(DbType.ORACLE, oracleDatasourceProcessor.getDbType());
    }
}