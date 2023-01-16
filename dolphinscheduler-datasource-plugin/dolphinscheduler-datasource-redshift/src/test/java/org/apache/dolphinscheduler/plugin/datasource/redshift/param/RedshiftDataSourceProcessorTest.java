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

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
public class RedshiftDataSourceProcessorTest {

    private RedshiftDataSourceProcessor redshiftDatasourceProcessor = new RedshiftDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        RedshiftDataSourceParamDTO redshiftDatasourceParamDTO = new RedshiftDataSourceParamDTO();
        redshiftDatasourceParamDTO.setHost("localhost");
        redshiftDatasourceParamDTO.setPort(5439);
        redshiftDatasourceParamDTO.setDatabase("dev");
        redshiftDatasourceParamDTO.setUserName("awsuser");
        redshiftDatasourceParamDTO.setPassword("123456");
        redshiftDatasourceParamDTO.setOther(props);
        redshiftDatasourceParamDTO.setMode(RedshiftAuthMode.PASSWORD);
        try (MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            RedshiftConnectionParam connectionParams = (RedshiftConnectionParam) redshiftDatasourceProcessor
                    .createConnectionParams(redshiftDatasourceParamDTO);
            Assertions.assertEquals("jdbc:redshift://localhost:5439", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:redshift://localhost:5439/dev", connectionParams.getJdbcUrl());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"awsuser\",\"password\":\"123456\",\"address\":\"jdbc:redshift://localhost:5439\""
                        + ",\"database\":\"dev\",\"jdbcUrl\":\"jdbc:redshift://localhost:5439/dev\",\"mode\":\"password\"}";
        RedshiftConnectionParam connectionParams = (RedshiftConnectionParam) redshiftDatasourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionParams);
        Assertions.assertEquals("awsuser", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_REDSHIFT_JDBC_DRIVER,
                redshiftDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        RedshiftConnectionParam redshiftConnectionParam = new RedshiftConnectionParam();
        redshiftConnectionParam.setJdbcUrl("jdbc:redshift://localhost:5439/default");
        ImmutableMap<String, String> map = new ImmutableMap.Builder<String, String>()
                .put("DSILogLevel", "6")
                .put("defaultRowFetchSize", "100")
                .build();
        redshiftConnectionParam.setOther(map);
        Assertions.assertEquals("jdbc:redshift://localhost:5439/default?DSILogLevel=6;defaultRowFetchSize=100",
                redshiftDatasourceProcessor.getJdbcUrl(redshiftConnectionParam));

    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.REDSHIFT, redshiftDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.REDHIFT_VALIDATION_QUERY,
                redshiftDatasourceProcessor.getValidationQuery());
    }
}
