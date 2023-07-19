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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class AbstractDataSourceProcessorTest {

    @Test
    public void checkOtherTest() {
        AbstractDataSourceProcessor mockDataSourceProcessor = mock(AbstractDataSourceProcessor.class);
        Map<String, String> other = new HashMap<>();
        other.put("principal", "hadoop/_HOST@TEST.COM");
        doNothing().when(mockDataSourceProcessor).checkOther(other);
    }

    @Test
    public void checkOtherExceptionTest() {
        AbstractDataSourceProcessor mockDataSourceProcessor = mock(AbstractDataSourceProcessor.class);
        Map<String, String> other = new HashMap<>();
        other.put("arg0", "%");
        doThrow(new IllegalArgumentException()).when(mockDataSourceProcessor).checkOther(other);
    }

    @Test
    public void shouldNotIncludeMaliciousParams() {
        AbstractDataSourceProcessor mockDataSourceProcessor = mock(AbstractDataSourceProcessor.class);
        Map<String, String> other = new HashMap<>();
        other.put("allowLoadLocalInfile", "whatever");
        other.put("OutputLocation", "s3://test-data-xini/athena-query");
        doThrow(new IllegalArgumentException()).when(mockDataSourceProcessor).checkOther(other);
    }

    @org.junit.Test
    public void transformOtherParamToMap() {
        AbstractDataSourceProcessor abstractDataSourceProcessor = new AbstractDataSourceProcessor() {

            @Override
            public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
                return null;
            }

            @Override
            public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
                return null;
            }

            @Override
            public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
                return null;
            }

            @Override
            public ConnectionParam createConnectionParams(String connectionJson) {
                return null;
            }

            @Override
            public String getDatasourceDriver() {
                return null;
            }

            @Override
            public String getValidationQuery() {
                return null;
            }

            @Override
            public String getJdbcUrl(ConnectionParam connectionParam) {
                return null;
            }

            @Override
            public Connection getConnection(ConnectionParam connectionParam) {
                return null;
            }

            @Override
            public DbType getDbType() {
                return null;
            }

            @Override
            public DataSourceProcessor create() {
                return null;
            }
        };
        String other = "{\"useSSL\": \"true\"}";
        Map<String, String> stringStringMap = abstractDataSourceProcessor.transformOtherParamToMap(other);
        assertEquals("true", stringStringMap.get("useSSL"));
    }
}
