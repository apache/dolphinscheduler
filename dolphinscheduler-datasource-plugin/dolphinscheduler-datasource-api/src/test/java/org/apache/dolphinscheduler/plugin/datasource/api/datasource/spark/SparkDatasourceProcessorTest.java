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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource.spark;

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DatasourceUtil;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;

import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class, DriverManager.class, DatasourceUtil.class, CommonUtils.class, DataSourceClientProvider.class, PasswordUtils.class})
public class SparkDatasourceProcessorTest {

    private SparkDatasourceProcessor sparkDatasourceProcessor = new SparkDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        SparkDatasourceParamDTO sparkDatasourceParamDTO = new SparkDatasourceParamDTO();
        sparkDatasourceParamDTO.setUserName("root");
        sparkDatasourceParamDTO.setPassword("12345");
        sparkDatasourceParamDTO.setHost("localhost1,localhost2");
        sparkDatasourceParamDTO.setPort(1234);
        sparkDatasourceParamDTO.setDatabase("default");
        sparkDatasourceParamDTO.setOther(props);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.getKerberosStartupState()).thenReturn(false);
        SparkConnectionParam connectionParams = (SparkConnectionParam) sparkDatasourceProcessor
                .createConnectionParams(sparkDatasourceParamDTO);
        Assert.assertEquals("jdbc:hive2://localhost1:1234,localhost2:1234", connectionParams.getAddress());
        Assert.assertEquals("jdbc:hive2://localhost1:1234,localhost2:1234/default", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"12345\",\"address\":\"jdbc:hive2://localhost1:1234,localhost2:1234\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:hive2://localhost1:1234,localhost2:1234/default\"}";
        SparkConnectionParam connectionParams = (SparkConnectionParam) sparkDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER, sparkDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        SparkConnectionParam sparkConnectionParam = new SparkConnectionParam();
        sparkConnectionParam.setJdbcUrl("jdbc:hive2://localhost1:1234,localhost2:1234/default");
        sparkConnectionParam.setOther("other");
        Assert.assertEquals("jdbc:hive2://localhost1:1234,localhost2:1234/default;other",
                sparkDatasourceProcessor.getJdbcUrl(sparkConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.SPARK, sparkDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assert.assertEquals(Constants.HIVE_VALIDATION_QUERY, sparkDatasourceProcessor.getValidationQuery());
    }
}