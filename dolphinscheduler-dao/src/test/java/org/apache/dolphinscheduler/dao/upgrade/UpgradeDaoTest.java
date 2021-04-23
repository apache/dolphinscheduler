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

package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.common.utils.ScriptRunner;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectionFactory.class, ScriptRunner.class, FileReader.class})
public class UpgradeDaoTest {

    @Test
    public void testGetCurrentVersion() throws SQLException {
        PowerMockito.mockStatic(ConnectionFactory.class);
        ConnectionFactory mockConnectionFactory = PowerMockito.mock(ConnectionFactory.class);
        PowerMockito.when(ConnectionFactory.getInstance()).thenReturn(mockConnectionFactory);
        DataSource mockDatasource = PowerMockito.mock(DataSource.class);
        PowerMockito.when(mockConnectionFactory.getDataSource()).thenReturn(mockDatasource);
        Connection mockConnection = PowerMockito.mock(Connection.class);
        PowerMockito.when(mockDatasource.getConnection()).thenReturn(mockConnection);
        PreparedStatement mockPrepareStatement = PowerMockito.mock(PreparedStatement.class);
        PowerMockito.when(mockConnection.prepareStatement(Mockito.any())).thenReturn(mockPrepareStatement);
        ResultSet mockResultSet = PowerMockito.mock(ResultSet.class);
        PowerMockito.when(mockPrepareStatement.executeQuery()).thenReturn(mockResultSet);

        DatabaseMetaData mockMetaData = PowerMockito.mock(DatabaseMetaData.class);
        PowerMockito.when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        PowerMockito.when(mockMetaData.getDatabaseProductName()).thenReturn("mysql");

        UpgradeDao upgradeDao = MysqlUpgradeDao.getInstance();
        upgradeDao.getCurrentVersion("xx");
        Assert.assertTrue(true);
    }

    @Test(expected = IOException.class)
    public void testInitSchema() throws Exception {

        PowerMockito.mockStatic(ConnectionFactory.class);
        ConnectionFactory mockConnectionFactory = PowerMockito.mock(ConnectionFactory.class);
        PowerMockito.when(ConnectionFactory.getInstance()).thenReturn(mockConnectionFactory);

        DataSource mockDatasource = PowerMockito.mock(DataSource.class);
        PowerMockito.when(mockConnectionFactory.getDataSource()).thenReturn(mockDatasource);
        Connection mockConnection = PowerMockito.mock(Connection.class);
        PowerMockito.when(mockDatasource.getConnection()).thenReturn(mockConnection);

        DatabaseMetaData mockMetaData = PowerMockito.mock(DatabaseMetaData.class);
        PowerMockito.when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        PowerMockito.when(mockMetaData.getDatabaseProductName()).thenReturn("mysql");

        UpgradeDao upgradeDao = MysqlUpgradeDao.getInstance();
        upgradeDao.initSchema();
        Assert.assertTrue(true);

    }
}
