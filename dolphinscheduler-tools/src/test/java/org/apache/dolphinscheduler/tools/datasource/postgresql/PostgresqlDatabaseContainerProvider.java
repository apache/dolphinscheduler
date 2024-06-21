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

package org.apache.dolphinscheduler.tools.datasource.postgresql;

import org.apache.dolphinscheduler.tools.datasource.jupiter.DatabaseContainerProvider;
import org.apache.dolphinscheduler.tools.datasource.jupiter.DolphinSchedulerDatabaseContainer;

import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import com.google.auto.service.AutoService;

@Slf4j
@AutoService(DatabaseContainerProvider.class)
public class PostgresqlDatabaseContainerProvider implements DatabaseContainerProvider {

    private static final Network NETWORK = Network.newNetwork();

    @SuppressWarnings("unchecked")
    @Override
    public GenericContainer<?> getContainer(DolphinSchedulerDatabaseContainer dataSourceContainer) {
        // todo: test with multiple pg version
        GenericContainer<?> postgresqlContainer =
                new PostgreSQLContainer(DockerImageName.parse(dataSourceContainer.imageName()))
                        .withUsername("root")
                        .withPassword("root")
                        .withDatabaseName("dolphinscheduler")
                        .withNetwork(NETWORK)
                        .withExposedPorts(5432);

        log.info("Create {} successfully.", postgresqlContainer.getDockerImageName());
        postgresqlContainer.start();

        log.info("Starting {}...", postgresqlContainer.getDockerImageName());
        Startables.deepStart(Stream.of(postgresqlContainer)).join();
        log.info("{} started", postgresqlContainer.getDockerImageName());

        String jdbcUrl = "jdbc:mysql://localhost:" + postgresqlContainer.getMappedPort(5432)
                + "/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8";
        System.clearProperty("spring.datasource.url");
        System.setProperty("spring.datasource.url", jdbcUrl);
        return postgresqlContainer;
    }

    @Override
    public String getType() {
        return "postgres";
    }
}
