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

package org.apache.dolphinscheduler.plugin.datasource.kyuubi.param;

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
public class KyuubiDataSourceProcessorTest {

    private KyuubiDataSourceProcessor kyuubiDatasourceProcessor = new KyuubiDataSourceProcessor();
    @Test
    public void testCheckDatasourceParam() {
        KyuubiDataSourceParamDTO kyuubiDatasourceParamDTO = new KyuubiDataSourceParamDTO();
        kyuubiDatasourceParamDTO.setHost("localhost");
        kyuubiDatasourceParamDTO.setDatabase("default");
        Map<String, String> other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        kyuubiDatasourceParamDTO.setOther(other);
        DataSourceUtils.checkDatasourceParam(kyuubiDatasourceParamDTO);
        Assertions.assertTrue(true);
    }

    @Test
    public void testBuildConnectionParams() {
        KyuubiDataSourceParamDTO kyuubiDataSourceParamDTO = new KyuubiDataSourceParamDTO();
        kyuubiDataSourceParamDTO.setHost("localhost");
        kyuubiDataSourceParamDTO.setDatabase("default");
        kyuubiDataSourceParamDTO.setUserName("root");
        kyuubiDataSourceParamDTO.setPort(3306);
        kyuubiDataSourceParamDTO.setPassword("123456");

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString()))
                    .thenReturn("123456");
            mockedStaticCommonUtils.when(CommonUtils::getKerberosStartupState).thenReturn(false);
            ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(kyuubiDataSourceParamDTO);
            Assertions.assertNotNull(connectionParam);
        }
    }

    @Test
    public void testBuildConnectionParams2() {
        KyuubiDataSourceParamDTO kyuubiDataSourceParamDTO = new KyuubiDataSourceParamDTO();
        kyuubiDataSourceParamDTO.setHost("localhost");
        kyuubiDataSourceParamDTO.setDatabase("default");
        kyuubiDataSourceParamDTO.setUserName("root");
        kyuubiDataSourceParamDTO.setPort(3306);
        kyuubiDataSourceParamDTO.setPassword("123456");
        ConnectionParam connectionParam =
                DataSourceUtils.buildConnectionParams(DbType.KYUUBI, JSONUtils.toJsonString(kyuubiDataSourceParamDTO));
        Assertions.assertNotNull(connectionParam);
    }
    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        KyuubiDataSourceParamDTO kyuubiDataSourceParamDTO = new KyuubiDataSourceParamDTO();
        kyuubiDataSourceParamDTO.setHost("localhost1,localhost2");
        kyuubiDataSourceParamDTO.setPort(5142);
        kyuubiDataSourceParamDTO.setUserName("default");
        kyuubiDataSourceParamDTO.setDatabase("default");
        kyuubiDataSourceParamDTO.setOther(props);

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            KyuubiConnectionParam connectionParams = (KyuubiConnectionParam) kyuubiDatasourceProcessor
                    .createConnectionParams(kyuubiDataSourceParamDTO);
            Assertions.assertNotNull(connectionParams);
            Assertions.assertEquals("jdbc:kyuubi://localhost1:5142,localhost2:5142", connectionParams.getAddress());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionParam = "{\"user\":\"default\",\"address\":\"jdbc:kyuubi://localhost1:5142,localhost2:5142\""
                + ",\"jdbcUrl\":\"jdbc:kyuubi://localhost1:5142,localhost2:5142/default\"}";
        KyuubiConnectionParam connectionParams = (KyuubiConnectionParam) kyuubiDatasourceProcessor
                .createConnectionParams(connectionParam);
        Assertions.assertNotNull(connectionParam);
        Assertions.assertEquals("default", connectionParams.getUser());
    }
    @Test
    public void testCreateDatasourceParamDTO() {
        String connectionParam = "{\"user\":\"default\",\"address\":\"jdbc:kyuubi://localhost1:5142,localhost2:5142\""
                + ",\"jdbcUrl\":\"jdbc:kyuubi://localhost1:5142,localhost2:5142/default\"}";
        KyuubiDataSourceParamDTO kyuubiDataSourceParamDTO = (KyuubiDataSourceParamDTO) kyuubiDatasourceProcessor
                .createDatasourceParamDTO(connectionParam);
        Assertions.assertEquals("default", kyuubiDataSourceParamDTO.getUserName());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.ORG_APACHE_KYUUBI_JDBC_DRIVER,
                kyuubiDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        KyuubiConnectionParam connectionParam = new KyuubiConnectionParam();
        connectionParam.setJdbcUrl("jdbc:kyuubi://localhost1:5142,localhost2:5142/default");
        Map<String, String> other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        connectionParam.setOther(other);
        Assertions.assertEquals("jdbc:kyuubi://localhost1:5142,localhost2:5142/default?serverTimezone=Asia/Shanghai",
                kyuubiDatasourceProcessor.getJdbcUrl(connectionParam));
    }

    @Test
    public void testDbType() {
        Assertions.assertEquals(18, DbType.KYUUBI.getCode());
        Assertions.assertEquals("kyuubi", DbType.KYUUBI.getDescp());
        Assertions.assertEquals(DbType.KYUUBI, DbType.of(18));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.KYUUBI, kyuubiDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.KYUUBI_VALIDATION_QUERY,
                kyuubiDatasourceProcessor.getValidationQuery());
    }

    @Test
    public void testBuildString() {
        KyuubiDataSourceParamDTO kyuubiDataSourceParamDTO = new KyuubiDataSourceParamDTO();
        kyuubiDataSourceParamDTO.setHost("localhost");
        kyuubiDataSourceParamDTO.setDatabase("default");
        kyuubiDataSourceParamDTO.setUserName("root");
        kyuubiDataSourceParamDTO.setPort(3306);
        kyuubiDataSourceParamDTO.setPassword("123456");
        Assertions.assertNotNull(kyuubiDataSourceParamDTO.toString());
    }

}
