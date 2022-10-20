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

package org.apache.dolphinscheduler.plugin.datasource.presto.param;

import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PrestoDataSourceProcessorTest {

    private PrestoDataSourceProcessor prestoDatasourceProcessor = new PrestoDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        PrestoDataSourceParamDTO prestoDatasourceParamDTO = new PrestoDataSourceParamDTO();
        prestoDatasourceParamDTO.setHost("localhost");
        prestoDatasourceParamDTO.setPort(1234);
        prestoDatasourceParamDTO.setDatabase("default");
        prestoDatasourceParamDTO.setUserName("root");
        prestoDatasourceParamDTO.setPassword("123456");
        prestoDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            PrestoConnectionParam connectionParams = (PrestoConnectionParam) prestoDatasourceProcessor
                    .createConnectionParams(prestoDatasourceParamDTO);
            Assertions.assertEquals("jdbc:presto://localhost:1234", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:presto://localhost:1234/default", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:presto://localhost:1234\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:presto://localhost:1234/default\"}";
        PrestoConnectionParam connectionParams = (PrestoConnectionParam) prestoDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionParams);
        Assertions.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(Constants.COM_PRESTO_JDBC_DRIVER, prestoDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        PrestoConnectionParam prestoConnectionParam = new PrestoConnectionParam();
        prestoConnectionParam.setJdbcUrl("jdbc:postgresql://localhost:1234/default");
        prestoConnectionParam.setOther("other");
        Assertions.assertEquals("jdbc:postgresql://localhost:1234/default?other",
                prestoDatasourceProcessor.getJdbcUrl(prestoConnectionParam));

    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.PRESTO, prestoDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(Constants.PRESTO_VALIDATION_QUERY, prestoDatasourceProcessor.getValidationQuery());
    }
}
