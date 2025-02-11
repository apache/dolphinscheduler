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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SeatunnelParametersTest {

    @Test
    void testGetResources() {
        String sourceConfig =
                "{\"dbType\":\"MYSQL\",\"databaseId\":1,\"table\":\"source_mysql_table\",\"customParams\":[]}";
        String targetConfig =
                "{\"dbType\":\"MYSQL\",\"databaseId\":2,\"table\":\"target_mysql_table\",\"customParams\":[]}";

        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();

        seatunnelParameters.setSourceConfig(sourceConfig);
        seatunnelParameters.setTargetConfig(targetConfig);
        seatunnelParameters.setSourceType("MYSQL");
        seatunnelParameters.setTargetType("MYSQL");
        ResourceParametersHelper resources = seatunnelParameters.getResources();

        Assertions.assertEquals(2, resources.getResourceMap(ResourceType.DATASOURCE).size());
        Assertions.assertTrue(resources.getResourceMap(ResourceType.DATASOURCE).containsKey(1));
        Assertions.assertTrue(resources.getResourceMap(ResourceType.DATASOURCE).containsKey(2));
    }

    @Test
    void testGenerateExtendedContext() {
        ResourceParametersHelper resourceParametersHelper = new ResourceParametersHelper();

        String connectionParams =
                "{\"user\":\"root\",\"password\":\"root123\",\"address\":\"jdbc:mysql://xxx:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://xxx:3306/test\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\"}";
        String sourceConfig =
                "{\"dbType\":\"MYSQL\",\"databaseId\":1,\"table\":\"source_mysql_table\",\"customParams\":[]}";
        String targetConfig =
                "{\"dbType\":\"MYSQL\",\"databaseId\":2,\"table\":\"target_mysql_table\",\"customParams\":[]}";

        DataSourceParameters dataSourceParameters = new DataSourceParameters();
        dataSourceParameters.setType(DbType.MYSQL);
        dataSourceParameters.setResourceType("DATASOURCE");
        dataSourceParameters.setConnectionParams(connectionParams);

        resourceParametersHelper.put(ResourceType.DATASOURCE, 1, dataSourceParameters);
        resourceParametersHelper.put(ResourceType.DATASOURCE, 2, dataSourceParameters);

        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setSourceType("MYSQL");
        seatunnelParameters.setTargetType("MYSQL");
        seatunnelParameters.setSourceConfig(sourceConfig);
        seatunnelParameters.setTargetConfig(targetConfig);

        SeatunnelTaskExecutionContext seatunnelTaskExecutionContext =
                seatunnelParameters.generateExtendedContext(resourceParametersHelper);

        Assertions.assertEquals(connectionParams, seatunnelTaskExecutionContext.getSourceConnectionParams());
        Assertions.assertEquals(connectionParams, seatunnelTaskExecutionContext.getTargetConnectionParams());
        Assertions.assertEquals(DbType.MYSQL, seatunnelTaskExecutionContext.getDataSourceType());
        Assertions.assertEquals(DbType.MYSQL, seatunnelTaskExecutionContext.getDataTargetType());
        Assertions.assertEquals(1, seatunnelTaskExecutionContext.getDataSourceId());
        Assertions.assertEquals(2, seatunnelTaskExecutionContext.getDataTargetId());
    }

}
