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

package org.apache.dolphinscheduler.plugin.datasource.mariadb.param;

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

import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
public class MariaDBDataSourceProcessorTest {

    private MariaDBDataSourceProcessor mariadbDatasourceProcessor = new MariaDBDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        MariaDBDataSourceParamDTO mariadbDatasourceParamDTO = new MariaDBDataSourceParamDTO();
        mariadbDatasourceParamDTO.setUserName("root");
        mariadbDatasourceParamDTO.setPassword("123456");
        mariadbDatasourceParamDTO.setHost("localhost");
        mariadbDatasourceParamDTO.setPort(3306);
        mariadbDatasourceParamDTO.setDatabase("default");
        mariadbDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            MariaDBConnectionParam connectionParams = (MariaDBConnectionParam) mariadbDatasourceProcessor
                    .createConnectionParams(mariadbDatasourceParamDTO);
            Assertions.assertEquals("jdbc:mariadb://localhost:3306", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:mariadb://localhost:3306/default", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mariadb://localhost:3306\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:mariadb://localhost:3306/default\"}";
        MariaDBConnectionParam connectionParams = (MariaDBConnectionParam) mariadbDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_MARIADB_JDBC_DRIVER,
                mariadbDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        MariaDBConnectionParam mariadbConnectionParam = new MariaDBConnectionParam();
        mariadbConnectionParam.setJdbcUrl("jdbc:mariadb://localhost:3306/default");
        Assertions.assertEquals(
                "jdbc:mariadb://localhost:3306/default",
                mariadbDatasourceProcessor.getJdbcUrl(mariadbConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.MARIADB, mariadbDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.MARIADB_VALIDATION_QUERY,
                mariadbDatasourceProcessor.getValidationQuery());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        MariaDBConnectionParam mariadbConnectionParam = new MariaDBConnectionParam();
        mariadbConnectionParam.setJdbcUrl("jdbc:mariadb://localhost:3306/default");
        mariadbConnectionParam.setUser("root");
        mariadbConnectionParam.setPassword("123456");
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("123456");
            Assertions.assertEquals("mariadb@root@123456@jdbc:mariadb://localhost:3306/default",
                    mariadbDatasourceProcessor.getDatasourceUniqueId(mariadbConnectionParam, DbType.MARIADB));
        }
    }

    @Test
    public void testSplitAndRemoveComment() {
        String sql = "select * from table1;\nselect * from table2;\nselect * from table3;\r\n";
        List<String> expect = Lists.newArrayList(
                "select * from table1",
                "select * from table2",
                "select * from table3");
        Assertions.assertEquals(expect, mariadbDatasourceProcessor.splitAndRemoveComment(sql));

        // Variable
        sql = "select * from ${table1};";
        expect = Lists.newArrayList("select * from ${table1}");
        Assertions.assertEquals(expect, mariadbDatasourceProcessor.splitAndRemoveComment(sql));
    }

}
