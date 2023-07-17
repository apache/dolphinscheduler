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

package org.apache.dolphinscheduler.plugin.datasource.db2.param;

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

import com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
public class Db2DataSourceProcessorTest {

    private Db2DataSourceProcessor db2DatasourceProcessor = new Db2DataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        Db2DataSourceParamDTO db2DatasourceParamDTO = new Db2DataSourceParamDTO();
        db2DatasourceParamDTO.setUserName("root");
        db2DatasourceParamDTO.setPassword("123456");
        db2DatasourceParamDTO.setHost("localhost");
        db2DatasourceParamDTO.setPort(5142);
        db2DatasourceParamDTO.setDatabase("default");
        db2DatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");

            Db2ConnectionParam connectionParams = (Db2ConnectionParam) db2DatasourceProcessor
                    .createConnectionParams(db2DatasourceParamDTO);
            Assertions.assertNotNull(connectionParams);
            Assertions.assertEquals("jdbc:db2://localhost:5142", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:db2://localhost:5142/default", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:db2://localhost:5142\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:db2://localhost:5142/default\"}";
        Db2ConnectionParam connectionParams = (Db2ConnectionParam) db2DatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("root", connectionParams.getUser());

    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_DB2_JDBC_DRIVER, db2DatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        Db2ConnectionParam db2ConnectionParam = new Db2ConnectionParam();
        db2ConnectionParam.setJdbcUrl("jdbc:db2://localhost:5142/default");
        ImmutableMap<String, String> map = new ImmutableMap.Builder<String, String>()
                .put("other", "other")
                .build();
        db2ConnectionParam.setOther(map);
        String jdbcUrl = db2DatasourceProcessor.getJdbcUrl(db2ConnectionParam);
        Assertions.assertEquals("jdbc:db2://localhost:5142/default:other=other", jdbcUrl);
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.DB2, db2DatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.DB2_VALIDATION_QUERY, db2DatasourceProcessor.getValidationQuery());
    }
}
