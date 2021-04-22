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
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MysqlUpgradeDao.class, ConnectionFactory.class})
public class UpgradeDaoTest {

    @Test
    public void testInitMysqlSchema() throws Exception {

        PowerMockito.mockStatic(ConnectionFactory.class);
        ConnectionFactory mockConnectionFactory = PowerMockito.mock(ConnectionFactory.class);
        PowerMockito.when(ConnectionFactory.getInstance()).thenReturn(mockConnectionFactory);

        DataSource mockDatasource = PowerMockito.mock(DataSource.class);
        PowerMockito.when(mockConnectionFactory.getDataSource()).thenReturn(mockDatasource);
        Connection mockConnection = PowerMockito.mock(Connection.class);
        PowerMockito.when(mockDatasource.getConnection()).thenReturn(mockConnection);
        ScriptRunner mockScript = PowerMockito.mock(ScriptRunner.class);
        FileReader mockFileReader = PowerMockito.mock(FileReader.class);
        PowerMockito.whenNew(ScriptRunner.class).withAnyArguments().thenReturn(mockScript);
        PowerMockito.whenNew(FileReader.class).withAnyArguments().thenReturn(mockFileReader);

        UpgradeDao upgradeDao = MysqlUpgradeDao.getInstance();
        upgradeDao.initSchema();
        Assert.assertTrue(true);

    }
}
