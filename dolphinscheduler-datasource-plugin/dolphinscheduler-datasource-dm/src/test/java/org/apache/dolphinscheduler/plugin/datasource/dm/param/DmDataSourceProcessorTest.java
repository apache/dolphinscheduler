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

package org.apache.dolphinscheduler.plugin.datasource.dm.param;

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
public class DmDataSourceProcessorTest {

    private DmDataSourceProcessor dmDatasourceProcessor = new DmDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        DmDataSourceParamDTO dmDatasourceParamDTO = new DmDataSourceParamDTO();
        dmDatasourceParamDTO.setUserName("SYSDBA");
        dmDatasourceParamDTO.setPassword("SYSDBA");
        dmDatasourceParamDTO.setHost("localhost");
        dmDatasourceParamDTO.setPort(5236);
        dmDatasourceParamDTO.setDatabase("PERSON");

        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            DmConnectionParam connectionParams = (DmConnectionParam) dmDatasourceProcessor
                    .createConnectionParams(dmDatasourceParamDTO);
            Assertions.assertEquals("jdbc:dm://localhost:5236", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:dm://localhost:5236/PERSON", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"SYSDBA\",\"password\":\"SYSDBA\","
                + "\"address\":\"jdbc:dm://localhost:5236\""
                + ",\"database\":\"PERSON\",\"jdbcUrl\":\"jdbc:dm://localhost:5236/PERSON\"}";
        DmConnectionParam connectionParams = (DmConnectionParam) dmDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("SYSDBA", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_DM_JDBC_DRIVER,
                dmDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        DmConnectionParam dmConnectionParam = new DmConnectionParam();
        dmConnectionParam.setJdbcUrl("jdbc:dm://localhost:5236/PERSON");
        Assertions.assertEquals(
                "jdbc:dm://localhost:5236/PERSON",
                dmDatasourceProcessor.getJdbcUrl(dmConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.DM, dmDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.DM_VALIDATION_QUERY,
                dmDatasourceProcessor.getValidationQuery());
    }

}
