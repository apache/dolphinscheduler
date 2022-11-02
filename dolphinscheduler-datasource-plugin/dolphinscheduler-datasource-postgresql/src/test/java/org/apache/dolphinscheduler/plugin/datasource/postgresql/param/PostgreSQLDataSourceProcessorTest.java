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

package org.apache.dolphinscheduler.plugin.datasource.postgresql.param;

import static org.apache.dolphinscheduler.common.constants.DataSourceConstants.ORG_POSTGRESQL_DRIVER;
import static org.apache.dolphinscheduler.common.constants.DataSourceConstants.POSTGRESQL_VALIDATION_QUERY;

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
public class PostgreSQLDataSourceProcessorTest {

    private PostgreSQLDataSourceProcessor postgreSqlDatasourceProcessor = new PostgreSQLDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        PostgreSQLDataSourceParamDTO postgreSqlDatasourceParamDTO = new PostgreSQLDataSourceParamDTO();
        postgreSqlDatasourceParamDTO.setUserName("root");
        postgreSqlDatasourceParamDTO.setPassword("123456");
        postgreSqlDatasourceParamDTO.setHost("localhost");
        postgreSqlDatasourceParamDTO.setPort(3308);
        postgreSqlDatasourceParamDTO.setDatabase("default");
        postgreSqlDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            PostgreSQLConnectionParam connectionParams = (PostgreSQLConnectionParam) postgreSqlDatasourceProcessor
                    .createConnectionParams(postgreSqlDatasourceParamDTO);
            Assertions.assertEquals("jdbc:postgresql://localhost:3308", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:postgresql://localhost:3308/default", connectionParams.getJdbcUrl());
            Assertions.assertEquals("root", connectionParams.getUser());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:postgresql://localhost:3308\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:postgresql://localhost:3308/default\"}";
        PostgreSQLConnectionParam connectionParams = (PostgreSQLConnectionParam) postgreSqlDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionParams);
        Assertions.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(ORG_POSTGRESQL_DRIVER, postgreSqlDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        PostgreSQLConnectionParam postgreSqlConnectionParam = new PostgreSQLConnectionParam();
        postgreSqlConnectionParam.setJdbcUrl("jdbc:postgresql://localhost:3308/default");
        postgreSqlConnectionParam.setOther("other");

        String jdbcUrl = postgreSqlDatasourceProcessor.getJdbcUrl(postgreSqlConnectionParam);
        Assertions.assertEquals("jdbc:postgresql://localhost:3308/default?other", jdbcUrl);

    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.POSTGRESQL, postgreSqlDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(POSTGRESQL_VALIDATION_QUERY,
                postgreSqlDatasourceProcessor.getValidationQuery());
    }
}
