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

package org.apache.dolphinscheduler.plugin.doris.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DorisDataSourceProcessorTest {

    private DorisDataSourceProcessor dorisDatasourceProcessor = new DorisDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        DorisDataSourceParamDTO dorisDatasourceParamDTO = new DorisDataSourceParamDTO();

        dorisDatasourceParamDTO.setUserName("root");
        dorisDatasourceParamDTO.setPassword("123456");
        dorisDatasourceParamDTO.setHost("localhost");
        dorisDatasourceParamDTO.setPort(3306);
        dorisDatasourceParamDTO.setDatabase("default");
        dorisDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            DorisConnectionParam connectionParams = (DorisConnectionParam) dorisDatasourceProcessor
                    .createConnectionParams(dorisDatasourceParamDTO);
            Assertions.assertEquals("jdbc:mysql:loadbalance://localhost:3306", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:mysql:loadbalance://localhost:3306/default", connectionParams.getJdbcUrl());
        }

        dorisDatasourceParamDTO.setUserName("root");
        dorisDatasourceParamDTO.setPassword("123456");
        dorisDatasourceParamDTO.setHost("localhost,localhost1");
        dorisDatasourceParamDTO.setPort(3306);
        dorisDatasourceParamDTO.setDatabase("default");
        dorisDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            DorisConnectionParam connectionParams = (DorisConnectionParam) dorisDatasourceProcessor
                    .createConnectionParams(dorisDatasourceParamDTO);
            Assertions.assertEquals("jdbc:mysql:loadbalance://localhost:3306,localhost1:3306",
                    connectionParams.getAddress());
            Assertions.assertEquals("jdbc:mysql:loadbalance://localhost:3306,localhost1:3306/default",
                    connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://localhost:3306\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:mysql://localhost:3306/default\"}";
        DorisConnectionParam connectionParams = (DorisConnectionParam) dorisDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_MYSQL_CJ_JDBC_DRIVER,
                dorisDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        DorisConnectionParam dorisConnectionParam = new DorisConnectionParam();
        dorisConnectionParam.setJdbcUrl(
                "jdbc:mysql://localhost:3306/default?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false");
        Assertions.assertEquals(
                "jdbc:mysql://localhost:3306/default?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
                dorisDatasourceProcessor.getJdbcUrl(dorisConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.DORIS, dorisDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.MYSQL_VALIDATION_QUERY,
                dorisDatasourceProcessor.getValidationQuery());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        DorisConnectionParam dorisConnectionParam = new DorisConnectionParam();
        dorisConnectionParam.setJdbcUrl("jdbc:mysql://localhost:3306/default");
        dorisConnectionParam.setUser("root");
        dorisConnectionParam.setPassword("123456");
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("123456");
            Assertions.assertEquals("doris@root@123456@jdbc:mysql://localhost:3306/default",
                    dorisDatasourceProcessor.getDatasourceUniqueId(dorisConnectionParam, DbType.DORIS));
        }
    }

    @Test
    public void splitAndRemoveComment() {
        String sql =
                "set enable_unique_key_partial_update = true;\r\n\r\n" +
                        "insert into demo.table\r\n(age,name)\r\nselect 1, 'tom';\r\n\r\n" +
                        "set enable_unique_key_partial_update = false;\r\n\r\n\r\n";
        List<String> sqls = dorisDatasourceProcessor.splitAndRemoveComment(sql);
        Assertions.assertEquals(3, sqls.size());
        Assertions.assertEquals("set enable_unique_key_partial_update = true", sqls.get(0));
        Assertions.assertEquals("insert into demo.table\r\n(age,name)\r\nselect 1, 'tom'", sqls.get(1));
        Assertions.assertEquals("set enable_unique_key_partial_update = false", sqls.get(2));
    }

}
