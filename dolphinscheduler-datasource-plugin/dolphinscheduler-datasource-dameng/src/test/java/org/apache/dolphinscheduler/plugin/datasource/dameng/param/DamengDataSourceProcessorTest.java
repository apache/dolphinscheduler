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

package org.apache.dolphinscheduler.plugin.datasource.dameng.param;

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
public class DamengDataSourceProcessorTest {

    private DamengDataSourceProcessor damengDatasourceProcessor = new DamengDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        DamengDataSourceParamDTO damengDatasourceParamDTO = new DamengDataSourceParamDTO();
        damengDatasourceParamDTO.setUserName("SYSDBA");
        damengDatasourceParamDTO.setPassword("SYSDBA");
        damengDatasourceParamDTO.setHost("localhost");
        damengDatasourceParamDTO.setPort(5236);
        damengDatasourceParamDTO.setDatabase("PERSON");

        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            DamengConnectionParam connectionParams = (DamengConnectionParam) damengDatasourceProcessor
                    .createConnectionParams(damengDatasourceParamDTO);
            Assertions.assertEquals("jdbc:dm://localhost:5236", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:dm://localhost:5236/PERSON", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"SYSDBA\",\"password\":\"SYSDBA\","
                + "\"address\":\"jdbc:dm://localhost:5236\""
                + ",\"database\":\"PERSON\",\"jdbcUrl\":\"jdbc:dm://localhost:5236/PERSON\"}";
        DamengConnectionParam connectionParams = (DamengConnectionParam) damengDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("SYSDBA", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_DAMENG_JDBC_DRIVER,
                damengDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        DamengConnectionParam damengConnectionParam = new DamengConnectionParam();
        damengConnectionParam.setJdbcUrl("jdbc:dm://localhost:5236/PERSON");
        Assertions.assertEquals(
                "jdbc:dm://localhost:5236/PERSON",
                damengDatasourceProcessor.getJdbcUrl(damengConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.DAMENG, damengDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.DAMENG_VALIDATION_QUERY,
                damengDatasourceProcessor.getValidationQuery());
    }

}
