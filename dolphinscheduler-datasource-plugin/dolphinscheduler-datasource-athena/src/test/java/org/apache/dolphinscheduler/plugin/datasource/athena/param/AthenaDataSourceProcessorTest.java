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

package org.apache.dolphinscheduler.plugin.datasource.athena.param;

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
@PrepareForTest({Class.class, DriverManager.class, DataSourceUtils.class, CommonUtils.class, DataSourceClientProvider.class, PasswordUtils.class})
public class AthenaDataSourceProcessorTest {

    private final AthenaDataSourceProcessor athenaDataSourceProcessor = new AthenaDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("LogLevel", "6");
        AthenaDataSourceParamDTO athenaDataSourceParamDTO = new AthenaDataSourceParamDTO();
        athenaDataSourceParamDTO.setDatabase("");
        athenaDataSourceParamDTO.setUserName("awsuser");
        athenaDataSourceParamDTO.setPassword("123456");
        athenaDataSourceParamDTO.setAwsRegion("cn-north-1");
        athenaDataSourceParamDTO.setOther(props);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        AthenaConnectionParam connectionParams = (AthenaConnectionParam) this.athenaDataSourceProcessor
            .createConnectionParams(athenaDataSourceParamDTO);
        Assert.assertEquals("jdbc:awsathena://AwsRegion=cn-north-1;", connectionParams.getAddress());
        Assert.assertEquals("jdbc:awsathena://AwsRegion=cn-north-1;", connectionParams.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"awsuser\",\"password\":\"123456\",\"address\":\"jdbc:awsathena://AwsRegion=cn-north-1;\""
            + ",\"database\":\"\",\"jdbcUrl\":\"jdbc:awsathena://AwsRegion=cn-north-1;\", \"awsRegion\":\"cn-north-1\"}";
        AthenaConnectionParam connectionParams = (AthenaConnectionParam) this.athenaDataSourceProcessor
            .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("awsuser", connectionParams.getUser());
        Assert.assertEquals("cn-north-1", connectionParams.getAwsRegion());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_ATHENA_JDBC_DRIVER,
                this.athenaDataSourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        AthenaConnectionParam athenaConnectionParam = new AthenaConnectionParam();
        athenaConnectionParam.setJdbcUrl("jdbc:awsathena://AwsRegion=cn-north-1;");
        athenaConnectionParam.setOther("LogLevel=6;LogPath=/tmp;");
        Assert.assertEquals("jdbc:awsathena://AwsRegion=cn-north-1;LogLevel=6;LogPath=/tmp;",
            this.athenaDataSourceProcessor.getJdbcUrl(athenaConnectionParam));

    }

    @Test
    public void testGetJdbcUrlNoOther() {
        AthenaConnectionParam athenaConnectionParam = new AthenaConnectionParam();
        athenaConnectionParam.setJdbcUrl("jdbc:awsathena://AwsRegion=cn-north-1;");
        athenaConnectionParam.setOther("");
        Assert.assertEquals("jdbc:awsathena://AwsRegion=cn-north-1;",
            this.athenaDataSourceProcessor.getJdbcUrl(athenaConnectionParam));

    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.ATHENA, this.athenaDataSourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.ATHENA_VALIDATION_QUERY,
                this.athenaDataSourceProcessor.getValidationQuery());
    }

    @Test
    public void testCreateDatasourceParamDTO() {
        String connectionJson = "{\"user\":\"awsuser\",\"password\":\"123456\",\"address\":\"jdbc:awsathena://AwsRegion=cn-north-1;\""
            + ",\"database\":\"\",\"jdbcUrl\":\"jdbc:awsathena://AwsRegion=cn-north-1;\", \"awsRegion\":\"cn-north-1\"}";
        AthenaDataSourceParamDTO athenaDataSourceParamDTO = (AthenaDataSourceParamDTO) this.athenaDataSourceProcessor
            .createDatasourceParamDTO(connectionJson);
        Assert.assertEquals("awsuser", athenaDataSourceParamDTO.getUserName());
        Assert.assertEquals("cn-north-1", athenaDataSourceParamDTO.getAwsRegion());
        Assert.assertEquals("", athenaDataSourceParamDTO.getDatabase());
    }
}