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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource.presto;

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
public class PrestoDatasourceProcessorTest {

    private PrestoDatasourceProcessor prestoDatasourceProcessor = new PrestoDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        PrestoDatasourceParamDTO prestoDatasourceParamDTO = new PrestoDatasourceParamDTO();
        prestoDatasourceParamDTO.setHost("localhost");
        prestoDatasourceParamDTO.setPort(1234);
        prestoDatasourceParamDTO.setDatabase("default");
        prestoDatasourceParamDTO.setUserName("root");
        prestoDatasourceParamDTO.setPassword("123456");
        prestoDatasourceParamDTO.setOther(props);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        PrestoConnectionParam connectionParams = (PrestoConnectionParam) prestoDatasourceProcessor
                .createConnectionParams(prestoDatasourceParamDTO);
        Assert.assertEquals("jdbc:presto://localhost:1234", connectionParams.getAddress());
        Assert.assertEquals("jdbc:presto://localhost:1234/default", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:presto://localhost:1234\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:presto://localhost:1234/default\"}";
        PrestoConnectionParam connectionParams = (PrestoConnectionParam) prestoDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(Constants.COM_PRESTO_JDBC_DRIVER, prestoDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        PrestoConnectionParam prestoConnectionParam = new PrestoConnectionParam();
        prestoConnectionParam.setJdbcUrl("jdbc:postgresql://localhost:1234/default");
        prestoConnectionParam.setOther("other");
        Assert.assertEquals("jdbc:postgresql://localhost:1234/default?other",
                prestoDatasourceProcessor.getJdbcUrl(prestoConnectionParam));

    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.PRESTO, prestoDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assert.assertEquals(Constants.PRESTO_VALIDATION_QUERY, prestoDatasourceProcessor.getValidationQuery());
    }
}