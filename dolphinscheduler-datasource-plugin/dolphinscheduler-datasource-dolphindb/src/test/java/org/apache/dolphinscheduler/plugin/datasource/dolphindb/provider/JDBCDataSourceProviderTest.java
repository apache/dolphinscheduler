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

package org.apache.dolphinscheduler.plugin.datasource.dolphindb.provider;

import org.apache.dolphinscheduler.plugin.datasource.api.provider.JDBCDataSourceProvider;
import org.apache.dolphinscheduler.plugin.datasource.dolphindb.param.DolphinDBConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zaxxer.hikari.HikariDataSource;

@ExtendWith(MockitoExtension.class)
public class JDBCDataSourceProviderTest {

    @Test
    public void testCreateOneSessionJdbcDataSource() {
        try (
                MockedStatic<JDBCDataSourceProvider> mockedJDBCDataSourceProvider =
                        Mockito.mockStatic(JDBCDataSourceProvider.class)) {
            HikariDataSource dataSource = Mockito.mock(HikariDataSource.class);
            mockedJDBCDataSourceProvider
                    .when(() -> JDBCDataSourceProvider.createOneSessionJdbcDataSource(Mockito.any(), Mockito.any()))
                    .thenReturn(dataSource);
            Assertions.assertNotNull(
                    JDBCDataSourceProvider.createOneSessionJdbcDataSource(new DolphinDBConnectionParam(),
                            DbType.DOLPHINDB));
        }
    }

}
