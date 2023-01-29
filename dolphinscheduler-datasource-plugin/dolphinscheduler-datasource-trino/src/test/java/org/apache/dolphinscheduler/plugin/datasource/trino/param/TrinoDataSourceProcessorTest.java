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

package org.apache.dolphinscheduler.plugin.datasource.trino.param;

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
public class TrinoDataSourceProcessorTest {

    private TrinoDataSourceProcessor TrinoDatasourceProcessor = new TrinoDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        TrinoDataSourceParamDTO TrinoDatasourceParamDTO = new TrinoDataSourceParamDTO();
        TrinoDatasourceParamDTO.setHost("localhost");
        TrinoDatasourceParamDTO.setPort(8080);
        TrinoDatasourceParamDTO.setDatabase("default");
        TrinoDatasourceParamDTO.setUserName("trino");
        TrinoDatasourceParamDTO.setPassword("trino");
        TrinoDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            TrinoConnectionParam connectionParams = (TrinoConnectionParam) TrinoDatasourceProcessor
                    .createConnectionParams(TrinoDatasourceParamDTO);
            Assertions.assertEquals("jdbc:trino://localhost:8080", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:trino://localhost:8080/default", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"trino\",\"password\":\"trino\",\"address\":\"jdbc:trino://localhost:8080\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:trino://localhost:8080/default\"}";
        TrinoConnectionParam connectionParams = (TrinoConnectionParam) TrinoDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionParams);
        Assertions.assertEquals("trino", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_TRINO_JDBC_DRIVER,
                TrinoDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        TrinoConnectionParam TrinoConnectionParam = new TrinoConnectionParam();
        TrinoConnectionParam.setJdbcUrl("jdbc:postgresql://localhost:8080/default");
        Assertions.assertEquals("jdbc:postgresql://localhost:8080/default",
                TrinoDatasourceProcessor.getJdbcUrl(TrinoConnectionParam));

    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.TRINO, TrinoDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.TRINO_VALIDATION_QUERY,
                TrinoDatasourceProcessor.getValidationQuery());
    }
}
