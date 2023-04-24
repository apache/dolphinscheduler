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

package org.apache.dolphinscheduler.plugin.datasource.databend.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
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
public class DatabendDataSourceProcessorTest {

    private DatabendDataSourceProcessor databendDataSourceProcessor = new DatabendDataSourceProcessor();

    @Test
    public void testCheckDatasourceParam() {
        DatabendDataSourceParamDTO databendDataSourceParamDTO = new DatabendDataSourceParamDTO();
        databendDataSourceParamDTO.setHost("localhost");
        databendDataSourceParamDTO.setDatabase("default");
        Map<String, String> other = new HashMap<>();
        other.put("ssl", "true");
        databendDataSourceParamDTO.setOther(other);
        DataSourceUtils.checkDatasourceParam(databendDataSourceParamDTO);
        Assertions.assertTrue(true);
    }

    @Test
    public void testBuildConnectionParams() {
        DatabendDataSourceParamDTO databendDataSourceParamDTO = new DatabendDataSourceParamDTO();
        databendDataSourceParamDTO.setHost("localhost");
        databendDataSourceParamDTO.setDatabase("default");
        databendDataSourceParamDTO.setUserName("root");
        databendDataSourceParamDTO.setPort(8000);
        databendDataSourceParamDTO.setPassword("123456");

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString()))
                    .thenReturn("123456");
            mockedStaticCommonUtils.when(CommonUtils::getKerberosStartupState).thenReturn(false);
            ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(databendDataSourceParamDTO);
            Assertions.assertNotNull(connectionParam);
            Assertions.assertEquals(connectionParam.getPassword(), "123456");
        }
    }

    @Test
    public void testBuildConnectionParams2() {
        DatabendDataSourceParamDTO databendDataSourceParamDTO = new DatabendDataSourceParamDTO();
        databendDataSourceParamDTO.setHost("127.0.0.1");
        databendDataSourceParamDTO.setDatabase("databend");
        databendDataSourceParamDTO.setUserName("root");
        databendDataSourceParamDTO.setPort(8000);
        databendDataSourceParamDTO.setPassword("databend");
        ConnectionParam connectionParam =
                DataSourceUtils.buildConnectionParams(DbType.DATABEND,
                        JSONUtils.toJsonString(databendDataSourceParamDTO));
        Assertions.assertNotNull(connectionParam);
    }

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("ssl", "true");
        DatabendDataSourceParamDTO databendDataSourceParamDTO = new DatabendDataSourceParamDTO();
        databendDataSourceParamDTO.setHost("localhost");
        databendDataSourceParamDTO.setPort(8000);
        databendDataSourceParamDTO.setUserName("root");
        databendDataSourceParamDTO.setDatabase("root");
        databendDataSourceParamDTO.setOther(props);

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            DatabendConnectionParam connectionParams = (DatabendConnectionParam) databendDataSourceProcessor
                    .createConnectionParams(databendDataSourceParamDTO);
            Assertions.assertNotNull(connectionParams);
            Assertions.assertEquals("jdbc:databend://localhost:8000", connectionParams.getAddress());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionParam = "{\"user\":\"default\",\"address\":\"jdbc:databend://localhost:8000\""
                + ",\"jdbcUrl\":\"jdbc:databend://localhost:8000/default\"}";
        DatabendConnectionParam connectionParams = (DatabendConnectionParam) databendDataSourceProcessor
                .createConnectionParams(connectionParam);
        Assertions.assertNotNull(connectionParam);
        Assertions.assertEquals("default", connectionParams.getUser());
        Assertions.assertEquals("jdbc:databend://localhost:8000/default", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateDatasourceParamDTO() {
        String connectionParam = "{\"user\":\"root\",\"address\":\"jdbc:databend://localhost:8000\""
                + ",\"jdbcUrl\":\"jdbc:databend://localhost:8000/default\"}";
        DatabendDataSourceParamDTO databendDataSourceParamDTO = (DatabendDataSourceParamDTO) databendDataSourceProcessor
                .createDatasourceParamDTO(connectionParam);
        Assertions.assertEquals("root", databendDataSourceParamDTO.getUserName());
        Assertions.assertEquals("localhost", databendDataSourceParamDTO.getHost());
        Assertions.assertEquals(8000, databendDataSourceParamDTO.getPort());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_DATABEND_JDBC_DRIVER,
                databendDataSourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        DatabendConnectionParam connectionParam = new DatabendConnectionParam();
        connectionParam.setJdbcUrl("jdbc:databend://localhost:8000/default");
        Map<String, String> other = new HashMap<>();
        other.put("ssl", "true");
        connectionParam.setOther(other);
        Assertions.assertEquals("jdbc:databend://localhost:8000/default?ssl=true",
                databendDataSourceProcessor.getJdbcUrl(connectionParam));
    }

    @Test
    public void testDbType() {
        Assertions.assertEquals(19, DbType.DATABEND.getCode());
        Assertions.assertEquals("databend", DbType.DATABEND.getDescp());
        Assertions.assertEquals(DbType.DATABEND, DbType.of(19));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.DATABEND, databendDataSourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.DATABEND_VALIDATION_QUERY,
                databendDataSourceProcessor.getValidationQuery());
    }

    @Test
    public void testBuildString() {
        DatabendDataSourceParamDTO databendDataSourceParamDTO = new DatabendDataSourceParamDTO();
        databendDataSourceParamDTO.setHost("localhost");
        databendDataSourceParamDTO.setDatabase("default");
        databendDataSourceParamDTO.setUserName("root");
        databendDataSourceParamDTO.setPort(8000);
        databendDataSourceParamDTO.setPassword("root");
        Assertions.assertNotNull(databendDataSourceParamDTO.toString());
    }

}
