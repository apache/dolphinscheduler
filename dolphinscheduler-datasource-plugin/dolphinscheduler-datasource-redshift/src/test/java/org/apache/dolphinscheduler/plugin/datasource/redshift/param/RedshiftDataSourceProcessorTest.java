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

package org.apache.dolphinscheduler.plugin.datasource.redshift.param;

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
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
@PrepareForTest({Class.class, DriverManager.class, DataSourceUtils.class, CommonUtils.class, DataSourceClientProvider.class, PasswordUtils.class})
public class RedshiftDataSourceProcessorTest {

    private RedshiftDataSourceProcessor redshiftDataSourceProcessor = new RedshiftDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        RedshiftDataSourceParamDTO redshiftDataSourceParamDTO = new RedshiftDataSourceParamDTO();
        redshiftDataSourceParamDTO.setHost("localhost");
        redshiftDataSourceParamDTO.setPort(5439);
        redshiftDataSourceParamDTO.setDatabase("dev");
        redshiftDataSourceParamDTO.setUserName("awsuser");
        redshiftDataSourceParamDTO.setPassword("123456");
        redshiftDataSourceParamDTO.setOther(props);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        RedshiftConnectionParam connectionParams = (RedshiftConnectionParam) redshiftDataSourceProcessor
                .createConnectionParams(redshiftDataSourceParamDTO);
        Assert.assertEquals("jdbc:redshift://localhost:5439", connectionParams.getAddress());
        Assert.assertEquals("jdbc:redshift://localhost:5439/dev", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"awsuser\",\"password\":\"123456\",\"address\":\"jdbc:redshift://localhost:5439\""
                + ",\"database\":\"dev\",\"jdbcUrl\":\"jdbc:redshift://localhost:5439/dev\"}";
        RedshiftConnectionParam connectionParams = (RedshiftConnectionParam) redshiftDataSourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("awsuser", connectionParams.getUser());
    }

    @Test
    public void testGetDataSourceDriver() {
        Assert.assertEquals(Constants.COM_REDSHIFT_JDBC_DRIVER, redshiftDataSourceProcessor.getDataSourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        RedshiftConnectionParam redshiftConnectionParam = new RedshiftConnectionParam();
        redshiftConnectionParam.setJdbcUrl("jdbc:redshift://localhost:5439/default");
        redshiftConnectionParam.setOther("DSILogLevel=6;defaultRowFetchSize=100");
        Assert.assertEquals("jdbc:redshift://localhost:5439/default?DSILogLevel=6;defaultRowFetchSize=100",
                redshiftDataSourceProcessor.getJdbcUrl(redshiftConnectionParam));

    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.REDSHIFT, redshiftDataSourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assert.assertEquals(Constants.REDHIFT_VALIDATION_QUERY, redshiftDataSourceProcessor.getValidationQuery());
    }
}