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

package org.apache.dolphinscheduler.tools.datasource.mysql;

import org.apache.dolphinscheduler.tools.datasource.jupiter.DatabaseContainerProvider;
import org.apache.dolphinscheduler.tools.datasource.jupiter.DolphinSchedulerDatabaseContainer;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;

@AutoService(DatabaseContainerProvider.class)
public class MysqlDatabaseContainerProvider implements DatabaseContainerProvider {

    private static final Network NETWORK = Network.newNetwork();

    @Override
    public GenericContainer<?> getContainer(DolphinSchedulerDatabaseContainer dataSourceContainer) {
        GenericContainer mysqlContainer = new MySQLContainer(DockerImageName.parse(dataSourceContainer.imageName()))
                .withUsername("root")
                .withPassword("root")
                .withDatabaseName("dolphinscheduler")
                .withNetwork(NETWORK)
                .withExposedPorts(3306)
                .waitingFor(Wait.forHealthcheck());
        mysqlContainer.setPortBindings(Lists.newArrayList("3306:3306"));
        return mysqlContainer;
    }

    @Override
    public String getType() {
        return "mysql";
    }

}
