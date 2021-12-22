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

package org.apache.dolphinscheduler.plugin.datasource.api.provider;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.mysql.MySQLConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.zaxxer.hikari.HikariDataSource;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {HikariDataSource.class, JDBCDataSourceProvider.class})
public class JDBCDataSourceProviderTest {

    @Test
    public void testCreateJdbcDataSource() {
        PowerMockito.mockStatic(JDBCDataSourceProvider.class);
        HikariDataSource dataSource = PowerMockito.mock(HikariDataSource.class);
        PowerMockito.when(JDBCDataSourceProvider.createJdbcDataSource(Mockito.any(), Mockito.any())).thenReturn(dataSource);
        Assert.assertNotNull(JDBCDataSourceProvider.createJdbcDataSource(new MySQLConnectionParam(), DbType.MYSQL));
    }

    @Test
    public void testCreateOneSessionJdbcDataSource() {
        PowerMockito.mockStatic(JDBCDataSourceProvider.class);
        HikariDataSource dataSource = PowerMockito.mock(HikariDataSource.class);
        PowerMockito.when(JDBCDataSourceProvider.createOneSessionJdbcDataSource(Mockito.any(), Mockito.any())).thenReturn(dataSource);
        Assert.assertNotNull(JDBCDataSourceProvider.createOneSessionJdbcDataSource(new MySQLConnectionParam(), DbType.MYSQL));
    }

}
