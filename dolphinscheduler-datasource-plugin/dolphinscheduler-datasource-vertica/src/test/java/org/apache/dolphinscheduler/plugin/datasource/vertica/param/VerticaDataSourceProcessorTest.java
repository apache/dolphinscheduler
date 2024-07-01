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

package org.apache.dolphinscheduler.plugin.datasource.vertica.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VerticaDataSourceProcessorTest {

    private VerticaDataSourceProcessor verticaDatasourceProcessor = new VerticaDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        VerticaDataSourceParamDTO verticaDatasourceParamDTO = new VerticaDataSourceParamDTO();
        verticaDatasourceParamDTO.setUserName("root");
        verticaDatasourceParamDTO.setPassword("123456");
        verticaDatasourceParamDTO.setHost("localhost");
        verticaDatasourceParamDTO.setPort(5433);
        verticaDatasourceParamDTO.setDatabase("default");
        verticaDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            VerticaConnectionParam connectionParams = (VerticaConnectionParam) verticaDatasourceProcessor
                    .createConnectionParams(verticaDatasourceParamDTO);
            Assertions.assertEquals("jdbc:vertica://localhost:5433", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:vertica://localhost:5433/default", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:vertica://localhost:5433\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:vertica://localhost:5433/default\"}";
        VerticaConnectionParam connectionParams = (VerticaConnectionParam) verticaDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_VERTICA_JDBC_DRIVER,
                verticaDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        VerticaConnectionParam verticaConnectionParam = new VerticaConnectionParam();
        verticaConnectionParam.setJdbcUrl("jdbc:vertica://localhost:5433/default");
        Assertions.assertEquals(
                "jdbc:vertica://localhost:5433/default",
                verticaDatasourceProcessor.getJdbcUrl(verticaConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.VERTICA, verticaDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.VERTICA_VALIDATION_QUERY,
                verticaDatasourceProcessor.getValidationQuery());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        VerticaConnectionParam verticaConnectionParam = new VerticaConnectionParam();
        verticaConnectionParam.setJdbcUrl("jdbc:vertica://localhost:5433/default");
        verticaConnectionParam.setUser("root");
        verticaConnectionParam.setPassword("123456");
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("123456");
            Assertions.assertEquals("vertica@root@123456@jdbc:vertica://localhost:5433/default",
                    verticaDatasourceProcessor.getDatasourceUniqueId(verticaConnectionParam, DbType.VERTICA));
        }
    }
}
