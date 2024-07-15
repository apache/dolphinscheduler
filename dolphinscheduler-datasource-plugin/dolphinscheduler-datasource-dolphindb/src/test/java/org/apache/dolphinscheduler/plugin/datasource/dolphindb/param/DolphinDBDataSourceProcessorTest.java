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

package org.apache.dolphinscheduler.plugin.datasource.dolphindb.param;

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
class DolphinDBDataSourceProcessorTest {

    private DolphinDBDataSourceProcessor processor = new DolphinDBDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        DolphinDBDataSourceParamDTO dolphinDBDataSourceParamDTO = new DolphinDBDataSourceParamDTO();
        dolphinDBDataSourceParamDTO.setUserName("admin");
        dolphinDBDataSourceParamDTO.setPassword("123456");
        dolphinDBDataSourceParamDTO.setHost("localhost");
        dolphinDBDataSourceParamDTO.setPort(8848);
        dolphinDBDataSourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            DolphinDBConnectionParam connectionParams = (DolphinDBConnectionParam) processor
                    .createConnectionParams(dolphinDBDataSourceParamDTO);
            Assertions.assertEquals("jdbc:dolphindb://localhost:8848", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:dolphindb://localhost:8848", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"admin\",\"password\":\"123456\",\"address\":\"jdbc:dolphindb://localhost:8848\""
                        + ",\"jdbcUrl\":\"jdbc:dolphindb://localhost:8848/default\"}";
        DolphinDBConnectionParam connectionParams = (DolphinDBConnectionParam) processor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("admin", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_DOLPHINDB_JDBC_DRIVER,
                processor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        DolphinDBConnectionParam param = new DolphinDBConnectionParam();
        param.setJdbcUrl("jdbc:dolphindb://localhost:8848");
        Assertions.assertEquals(
                "jdbc:dolphindb://localhost:8848",
                processor.getJdbcUrl(param));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.DOLPHINDB, processor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.DOLPHINDB_VALIDATION_QUERY,
                processor.getValidationQuery());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        DolphinDBConnectionParam param = new DolphinDBConnectionParam();
        param.setJdbcUrl("jdbc:dolphindb://localhost:8848/");
        param.setUser("admin");
        param.setPassword("123456");
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("123456");
            Assertions.assertEquals("dolphindb@admin@123456@jdbc:dolphindb://localhost:8848/",
                    processor.getDatasourceUniqueId(param, DbType.DOLPHINDB));
        }
    }
}
