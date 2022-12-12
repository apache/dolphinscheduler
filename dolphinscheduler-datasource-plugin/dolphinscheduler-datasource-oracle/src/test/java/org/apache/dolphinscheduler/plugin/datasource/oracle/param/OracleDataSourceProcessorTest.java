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

package org.apache.dolphinscheduler.plugin.datasource.oracle.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbConnectType;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
public class OracleDataSourceProcessorTest {

    private OracleDataSourceProcessor oracleDatasourceProcessor = new OracleDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        OracleDataSourceParamDTO oracleDatasourceParamDTO = new OracleDataSourceParamDTO();
        oracleDatasourceParamDTO.setConnectType(DbConnectType.ORACLE_SID);
        oracleDatasourceParamDTO.setHost("localhost");
        oracleDatasourceParamDTO.setPort(3308);
        oracleDatasourceParamDTO.setUserName("root");
        oracleDatasourceParamDTO.setPassword("123456");
        oracleDatasourceParamDTO.setDatabase("default");
        oracleDatasourceParamDTO.setOther(props);

        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            OracleConnectionParam connectionParams = (OracleConnectionParam) oracleDatasourceProcessor
                    .createConnectionParams(oracleDatasourceParamDTO);
            Assertions.assertNotNull(connectionParams);
            Assertions.assertEquals("jdbc:oracle:thin:@localhost:3308", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:oracle:thin:@localhost:3308:default", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:oracle:thin:@localhost:3308\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:oracle:thin:@localhost:3308:default\",\"connectType\":\"ORACLE_SID\"}";
        OracleConnectionParam connectionParams = (OracleConnectionParam) oracleDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionParams);
        Assertions.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_ORACLE_JDBC_DRIVER,
                oracleDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        OracleConnectionParam oracleConnectionParam = new OracleConnectionParam();
        oracleConnectionParam.setJdbcUrl("jdbc:oracle:thin:@localhost:3308:default");
        ImmutableMap<String, String> map = new ImmutableMap.Builder<String, String>()
                .put("other", "other")
                .build();
        oracleConnectionParam.setOther(map);
        Assertions.assertEquals("jdbc:oracle:thin:@localhost:3308:default?other=other",
                oracleDatasourceProcessor.getJdbcUrl(oracleConnectionParam));
    }

    @Test
    public void getDbType() {
        Assertions.assertEquals(DbType.ORACLE, oracleDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.ORACLE_VALIDATION_QUERY,
                oracleDatasourceProcessor.getValidationQuery());
    }
}
