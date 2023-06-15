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

package org.apache.dolphinscheduler.plugin.datasource.impala.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class, DriverManager.class, DataSourceUtils.class, CommonUtils.class,
        DataSourceClientProvider.class, PasswordUtils.class})
public class ImpalaDataSourceProcessorTest {

    private ImpalaDataSourceProcessor ImpalaDatasourceProcessor = new ImpalaDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        ImpalaDataSourceParamDTO impalaDatasourceParamDTO = new ImpalaDataSourceParamDTO();
        impalaDatasourceParamDTO.setUserName("admin");
        impalaDatasourceParamDTO.setPassword("admin");
        impalaDatasourceParamDTO.setHost("localhost");
        impalaDatasourceParamDTO.setPort(21050);
        impalaDatasourceParamDTO.setDatabase("default");
        impalaDatasourceParamDTO.setOther(props);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        ImpalaConnectionParam connectionParams = (ImpalaConnectionParam) ImpalaDatasourceProcessor
                .createConnectionParams(impalaDatasourceParamDTO);
        Assert.assertEquals("jdbc:impala://localhost:21050", connectionParams.getAddress());
        Assert.assertEquals("jdbc:impala://localhost:21050/default", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"admin\",\"password\":\"admin\",\"address\":\"jdbc:impala://localhost:21050\""
                        + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:impala://localhost:21050/default\"}";
        ImpalaConnectionParam connectionParams = (ImpalaConnectionParam) ImpalaDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionJson);
        Assert.assertEquals("admin", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_IMPALA_JDBC_DRIVER,
                ImpalaDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        ImpalaConnectionParam impalaConnectionParam = new ImpalaConnectionParam();
        impalaConnectionParam.setJdbcUrl("jdbc:impala://localhost:21050/default");
        Assert.assertEquals(
                "jdbc:impala://localhost:21050/default?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
                ImpalaDatasourceProcessor.getJdbcUrl(impalaConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.IMPALA, ImpalaDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.IMPALA_VALIDATION_QUERY,
                ImpalaDatasourceProcessor.getValidationQuery());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        ImpalaConnectionParam impalaConnectionParam = new ImpalaConnectionParam();
        impalaConnectionParam.setJdbcUrl("jdbc:impala://localhost:21050/default");
        impalaConnectionParam.setUser("admin");
        impalaConnectionParam.setPassword("admin");
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("admin");
        Assert.assertEquals("impala@admin@admin@jdbc:impala://localhost:21050/default",
                ImpalaDatasourceProcessor.getDatasourceUniqueId(impalaConnectionParam, DbType.IMPALA));
    }
}
