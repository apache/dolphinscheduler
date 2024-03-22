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

package org.apache.dolphinscheduler.plugin.datasource.xugu.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class XuguDataSourceProcessorTest {

    private XuguDataSourceProcessor xuguDataSourceProcessor = new XuguDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        XuguDataSourceParamDTO xuguDataSourceParamDTO = new XuguDataSourceParamDTO();
        xuguDataSourceParamDTO.setUserName("SYSDBA");
        xuguDataSourceParamDTO.setPassword("SYSDBA");
        xuguDataSourceParamDTO.setHost("localhost");
        xuguDataSourceParamDTO.setPort(5138);
        xuguDataSourceParamDTO.setDatabase("SYSTEM");

        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            XuguConnectionParam connectionParams = (XuguConnectionParam) xuguDataSourceProcessor
                    .createConnectionParams(xuguDataSourceParamDTO);
            Assertions.assertEquals("jdbc:xugu://localhost:5138", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:xugu://localhost:5138/SYSTEM", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"SYSDBA\",\"password\":\"SYSDBA\","
                + "\"address\":\"jdbc:xugu://localhost:5138\""
                + ",\"database\":\"SYSTEM\",\"jdbcUrl\":\"jdbc:xugu://localhost:5138/SYSTEM\"}";
        XuguConnectionParam connectionParams = (XuguConnectionParam) xuguDataSourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("SYSDBA", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_XUGU_JDBC_DRIVER,
                xuguDataSourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        XuguConnectionParam xuguConnectionParam = new XuguConnectionParam();
        xuguConnectionParam.setJdbcUrl("jdbc:xugu://localhost:5138/SYSTEM");
        Assertions.assertEquals(
                "jdbc:xugu://localhost:5138/SYSTEM",
                xuguDataSourceProcessor.getJdbcUrl(xuguConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.XUGU, xuguDataSourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.XUGU_VALIDATION_QUERY,
                xuguDataSourceProcessor.getValidationQuery());
    }

}
