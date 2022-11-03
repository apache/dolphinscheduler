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

package org.apache.dolphinscheduler.plugin.datasource.dm;

import java.sql.Connection;
import org.apache.dolphinscheduler.plugin.datasource.dm.param.DmConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.dm.param.DmDataSourceProcessor;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.junit.Assert;
import org.junit.Test;

public class DmDataSourceChannelFactoryTest {

    @Test
    public void testCreate() {
        DmDataSourceChannelFactory sourceChannelFactory = new DmDataSourceChannelFactory();
        DataSourceChannel dataSourceChannel = sourceChannelFactory.create();
        Assert.assertNotNull(dataSourceChannel);
    }

    @Test
    public void testGetDmConnection() {
        String connectionParamJson = "{\"address\":\"jdbc:dm://localhost:5236\",\"database\":\"\","
            + "\"jdbcUrl\":\"jdbc:dm://localhost:5236\",\"user\":\"SYSDBA\",\"password\":\"SYSDBA\"}";
        DmDataSourceProcessor dmDatasourceProcessor = new DmDataSourceProcessor();
        DmConnectionParam baseConnectionParam = (DmConnectionParam) dmDatasourceProcessor
            .createConnectionParams(connectionParamJson);

        DmDataSourceChannelFactory sourceChannelFactory = new DmDataSourceChannelFactory();
        DataSourceChannel dataSourceChannel = sourceChannelFactory.create();
        DataSourceClient dataSourceClient = dataSourceChannel.createDataSourceClient(baseConnectionParam, DbType.DM);
        Connection connection = dataSourceClient.getConnection();
        System.out.println(connection);
    }
}
