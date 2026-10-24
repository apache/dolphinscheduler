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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.kyuubi.jdbc.KyuubiHiveDriver;
import org.apache.kyuubi.jdbc.hive.JdbcConnectionParams;
import org.apache.kyuubi.jdbc.hive.Utils;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
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
        Assertions.assertEquals("jdbc:kyuubi://localhost1:5142,localhost2:5142/default;serverTimezone=Asia/Shanghai",
                kyuubiDatasourceProcessor.getJdbcUrl(connectionParam));
    }

    @Test
    public void testDbType() {
        Assertions.assertEquals(18, DbType.KYUUBI.getCode());
        Assertions.assertEquals("kyuubi", DbType.KYUUBI.getName());
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

    @ParameterizedTest
    @ValueSource(strings = {
            "jdbc:kyuubi://localhost:10009/default;socketTimeout=600000?kyuubi.engine.share.level=USER;spark.app.name=raw-test",
            "jdbc:kyuubi://localhost:10009/default#kyuubi.engine.share.level=USER",
            "jdbc:hive2://localhost:10009/spark_catalog/default?x=a=b#marker=test",
            "jdbc:kyuubi://localhost:10009/default;transportMode=http;httpPath=cliservice",
            "jdbc:kyuubi://zk-a:2181,zk-b:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=kyuubi",
            "jdbc:kyuubi://[::1]:10009/default?marker=hello%20world",
            "jdbc:kyuubi://localhost:10009/default"
    })
    public void testRawJdbcUrlRoundTrip(String rawJdbcUrl) {
        String requestJson = "{\"type\":\"KYUUBI\",\"userName\":\"test\",\"password\":\"secret\",\"rawJdbcUrl\":"
                + JSONUtils.toJsonString(rawJdbcUrl) + "}";
        KyuubiDataSourceParamDTO dto =
                (KyuubiDataSourceParamDTO) kyuubiDatasourceProcessor.castDatasourceParamDTO(requestJson);
        kyuubiDatasourceProcessor.checkDatasourceParam(dto);
        KyuubiConnectionParam created = (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(dto);
        Assertions.assertEquals(rawJdbcUrl, created.getRawJdbcUrl());
        Assertions.assertEquals(rawJdbcUrl, created.getJdbcUrl());
        Assertions.assertNull(created.getOther());
        String storedJson = JSONUtils.toJsonString(created);
        KyuubiConnectionParam restored =
                (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(storedJson);
        Assertions.assertEquals(rawJdbcUrl, kyuubiDatasourceProcessor.getJdbcUrl(restored));
        KyuubiDataSourceParamDTO editable =
                (KyuubiDataSourceParamDTO) kyuubiDatasourceProcessor.createDatasourceParamDTO(storedJson);
        Assertions.assertEquals(rawJdbcUrl, editable.getRawJdbcUrl());
        editable.setPassword("secret");
        kyuubiDatasourceProcessor.checkDatasourceParam(editable);
        Assertions.assertEquals(rawJdbcUrl, kyuubiDatasourceProcessor.getJdbcUrl(
                kyuubiDatasourceProcessor.createConnectionParams(editable)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  \t"})
    public void testOldRequestAndBlankRawUrlRemainCompatible(String rawJdbcUrl) {
        KyuubiDataSourceParamDTO dto = legacyDto();
        dto.setRawJdbcUrl(rawJdbcUrl);
        kyuubiDatasourceProcessor.checkDatasourceParam(dto);
        KyuubiConnectionParam param = (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(dto);
        Assertions.assertNull(param.getRawJdbcUrl());
        Assertions.assertEquals("jdbc:kyuubi://localhost:10009/default;socketTimeout=600000",
                kyuubiDatasourceProcessor.getJdbcUrl(param));
        KyuubiDataSourceParamDTO editable =
                (KyuubiDataSourceParamDTO) kyuubiDatasourceProcessor.createDatasourceParamDTO(
                        JSONUtils.toJsonString(param));
        Assertions.assertNull(editable.getRawJdbcUrl());
        Assertions.assertEquals(dto.getHost(), editable.getHost());
        Assertions.assertEquals(dto.getPort(), editable.getPort());
        Assertions.assertEquals(dto.getOther(), editable.getOther());
    }

    @Test
    public void testOldStoredJsonAndUnchangedEdit() {
        String oldJson = "{\"user\":\"test\",\"password\":\"secret\",\"address\":\"jdbc:kyuubi://localhost:10009\","
                + "\"database\":\"default\",\"jdbcUrl\":\"jdbc:kyuubi://localhost:10009/default\","
                + "\"other\":{\"socketTimeout\":\"600000\"}}";
        KyuubiConnectionParam original =
                (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(oldJson);
        KyuubiDataSourceParamDTO editable =
                (KyuubiDataSourceParamDTO) kyuubiDatasourceProcessor.createDatasourceParamDTO(oldJson);
        editable.setPassword("secret");
        editable.setName("renamed-only");
        KyuubiConnectionParam saved =
                (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(editable);
        Assertions.assertEquals(kyuubiDatasourceProcessor.getJdbcUrl(original),
                kyuubiDatasourceProcessor.getJdbcUrl(saved));
        Assertions.assertEquals(original.getOther(), saved.getOther());
        Assertions.assertEquals(original.getPassword(), saved.getPassword());
        Assertions.assertNull(saved.getRawJdbcUrl());
    }

    @Test
    public void testRawUrlOverridesStaleLegacyInput() {
        KyuubiDataSourceParamDTO dto = legacyDto();
        String url = "jdbc:kyuubi://new-host:10010/catalog/new_schema?kyuubi.engine.share.level=USER";
        dto.setRawJdbcUrl("  " + url + "  ");
        dto.setHost("not a valid old host");
        dto.setOther(Collections.singletonMap("invalid#oldKey", "ignored"));
        kyuubiDatasourceProcessor.checkDatasourceParam(dto);
        KyuubiConnectionParam param = (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(dto);
        Assertions.assertEquals(url, param.getRawJdbcUrl());
        Assertions.assertEquals(url, param.getJdbcUrl());
        Assertions.assertEquals("new_schema", param.getDatabase());
        Assertions.assertEquals("jdbc:kyuubi://new-host:10010", param.getAddress());
        Assertions.assertNull(param.getOther());
    }

    @Test
    public void testStoredRawUrlIsAuthoritativeForRuntimeAndCache() {
        String storedJson = "{\"rawJdbcUrl\":\"jdbc:kyuubi://new-host:10009/default#marker=new\","
                + "\"jdbcUrl\":\"jdbc:kyuubi://old-host:10009/default\",\"other\":{\"marker\":\"stale\"}}";
        KyuubiConnectionParam param =
                (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(storedJson);
        Assertions.assertEquals(param.getRawJdbcUrl(), param.getJdbcUrl());
        Assertions.assertEquals(param.getRawJdbcUrl(), kyuubiDatasourceProcessor.getJdbcUrl(param));
        Assertions.assertNull(param.getOther());
        String cacheKey = kyuubiDatasourceProcessor.getDatasourceUniqueId(param, DbType.KYUUBI);
        Assertions.assertTrue(cacheKey.contains("new-host"));
        Assertions.assertFalse(cacheKey.contains("old-host"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"?", "#"})
    public void testActualDriverReceivesSessionProperties(String separator) throws Exception {
        KyuubiDataSourceParamDTO dto = new KyuubiDataSourceParamDTO();
        dto.setRawJdbcUrl("jdbc:kyuubi://localhost:10009/default;socketTimeout=600000"
                + separator + "kyuubi.engine.share.level=USER;spark.app.name=raw-test");
        String url = kyuubiDatasourceProcessor.getJdbcUrl(kyuubiDatasourceProcessor.createConnectionParams(dto));
        JdbcConnectionParams parsed = Utils.extractURLComponents(url, new Properties());
        Map<String, String> sessionProperties = "?".equals(separator) ? parsed.getHiveConfs() : parsed.getHiveVars();
        Assertions.assertEquals("600000", parsed.getSessionVars().get("socketTimeout"));
        Assertions.assertEquals("USER", sessionProperties.get("kyuubi.engine.share.level"));
        Assertions.assertEquals("raw-test", sessionProperties.get("spark.app.name"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "jdbc:mysql://localhost:3306/default",
            "not a JDBC URL",
            "jdbc:kyuubi://localhost:10009/default?invalid space=value",
            "jdbc:kyuubi://localhost:10009/default;user=test;password=do-not-print",
            "jdbc:kyuubi://localhost:10009/default;password=do-not-print"
    })
    public void testInvalidRawUrlWithoutLeakingInput(String url) {
        KyuubiDataSourceParamDTO dto = new KyuubiDataSourceParamDTO();
        dto.setRawJdbcUrl(url);
        IllegalArgumentException error = Assertions.assertThrows(IllegalArgumentException.class,
                () -> kyuubiDatasourceProcessor.checkDatasourceParam(dto));
        Assertions.assertFalse(error.getMessage().contains(url));
        Assertions.assertFalse(error.getMessage().contains("do-not-print"));
        Assertions.assertNull(error.getCause());
    }

    @Test
    public void testDifferentRawRoutingUrlsHaveDifferentPools() {
        KyuubiDataSourceParamDTO dto = legacyDto();
        dto.setRawJdbcUrl("jdbc:kyuubi://localhost:10009/default?kyuubi.engine.share.level.subdomain=a");
        ConnectionParam first = kyuubiDatasourceProcessor.createConnectionParams(dto);
        dto.setRawJdbcUrl("jdbc:kyuubi://localhost:10009/default?kyuubi.engine.share.level.subdomain=b");
        ConnectionParam second = kyuubiDatasourceProcessor.createConnectionParams(dto);
        Assertions.assertNotEquals(kyuubiDatasourceProcessor.getDatasourceUniqueId(first, DbType.KYUUBI),
                kyuubiDatasourceProcessor.getDatasourceUniqueId(second, DbType.KYUUBI));
    }

    @Test
    public void testSwitchFromRawUrlBackToLegacy() {
        KyuubiDataSourceParamDTO dto = legacyDto();
        dto.setRawJdbcUrl("jdbc:kyuubi://different-host:10009/default#marker=raw");
        KyuubiConnectionParam raw = (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(dto);
        Assertions.assertNotNull(raw.getRawJdbcUrl());
        dto.setRawJdbcUrl(null);
        KyuubiConnectionParam standard = (KyuubiConnectionParam) kyuubiDatasourceProcessor.createConnectionParams(dto);
        Assertions.assertNull(standard.getRawJdbcUrl());
        Assertions.assertEquals("jdbc:kyuubi://localhost:10009/default;socketTimeout=600000",
                kyuubiDatasourceProcessor.getJdbcUrl(standard));
    }

    @Test
    public void testDirectConnectionPassesRawUrlAndSeparateCredentials() throws Exception {
        KyuubiDataSourceParamDTO dto = legacyDto();
        dto.setRawJdbcUrl("jdbc:kyuubi://localhost:10009/default#kyuubi.engine.share.level=USER");
        Connection connection = Mockito.mock(Connection.class);
        try (
                MockedConstruction<KyuubiHiveDriver> drivers = Mockito.mockConstruction(KyuubiHiveDriver.class,
                        (driver, context) -> Mockito
                                .when(driver.connect(Mockito.anyString(), Mockito.any(Properties.class)))
                                .thenReturn(connection))) {
            Assertions.assertSame(connection, kyuubiDatasourceProcessor.getConnection(
                    kyuubiDatasourceProcessor.createConnectionParams(dto)));
            ArgumentCaptor<Properties> properties = ArgumentCaptor.forClass(Properties.class);
            Mockito.verify(drivers.constructed().get(0)).connect(Mockito.eq(dto.getRawJdbcUrl()), properties.capture());
            Assertions.assertEquals(dto.getUserName(), properties.getValue().getProperty("user"));
            Assertions.assertEquals(dto.getPassword(), properties.getValue().getProperty("password"));
        }
    }

    private KyuubiDataSourceParamDTO legacyDto() {
        KyuubiDataSourceParamDTO dto = new KyuubiDataSourceParamDTO();
        dto.setHost("localhost");
        dto.setPort(10009);
        dto.setDatabase("default");
        dto.setUserName("test");
        dto.setPassword("secret");
        dto.setOther(Collections.singletonMap("socketTimeout", "600000"));
        return dto;
    }

}
