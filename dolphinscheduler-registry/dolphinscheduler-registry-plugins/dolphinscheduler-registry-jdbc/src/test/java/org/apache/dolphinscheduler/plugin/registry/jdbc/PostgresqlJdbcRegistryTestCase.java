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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("postgresql")
@SpringBootTest(classes = {JdbcRegistryProperties.class})
@SpringBootApplication(scanBasePackageClasses = JdbcRegistryProperties.class)
public class PostgresqlJdbcRegistryTestCase extends JdbcRegistryTestCase {

    private static GenericContainer<?> postgresqlContainer;

    @SneakyThrows
    @BeforeAll
    public static void setUpTestingServer() {
        postgresqlContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:16.0"))
                .withUsername("root")
                .withPassword("root")
                .withDatabaseName("dolphinscheduler")
                .withNetwork(Network.newNetwork())
                .withExposedPorts(5432);

        Startables.deepStart(Stream.of(postgresqlContainer)).join();

        String jdbcUrl = "jdbc:postgresql://localhost:" + postgresqlContainer.getMappedPort(5432) + "/dolphinscheduler";
        System.clearProperty("spring.datasource.url");
        System.setProperty("spring.datasource.url", jdbcUrl);
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
                Statement statement = connection.createStatement();) {
            statement.execute(
                    "create table t_ds_jdbc_registry_data\n" +
                            "(\n" +
                            "    id               bigserial NOT NULL,\n" +
                            "    data_key         varchar   not null,\n" +
                            "    data_value       text      not null,\n" +
                            "    data_type        varchar   not null,\n" +
                            "    client_id        bigint    not null,\n" +
                            "    create_time      timestamp not null,\n" +
                            "    last_update_time timestamp not null,\n" +
                            "PRIMARY KEY (id)\n" +
                            ");");
            statement.execute(
                    "create unique index uk_t_ds_jdbc_registry_dataKey on t_ds_jdbc_registry_data (data_key);");

            statement.execute(
                    "create table t_ds_jdbc_registry_lock\n" +
                            "(\n" +
                            "    id          bigserial NOT NULL,\n" +
                            "    lock_key    varchar   not null,\n" +
                            "    lock_owner  varchar   not null,\n" +
                            "    client_id   bigint    not null,\n" +
                            "    create_time timestamp not null,\n" +
                            "PRIMARY KEY (id)\n" +
                            ");");
            statement.execute(
                    "create unique index uk_t_ds_jdbc_registry_lockKey on t_ds_jdbc_registry_lock (lock_key);");

            statement.execute("create table t_ds_jdbc_registry_client_heartbeat\n" +
                    "(\n" +
                    "    id                  bigint    NOT NULL,\n" +
                    "    client_name         varchar   not null,\n" +
                    "    last_heartbeat_time bigint    not null,\n" +
                    "    connection_config   text      not null,\n" +
                    "    create_time         timestamp not null,\n" +
                    "PRIMARY KEY (id)\n" +
                    ");");

            statement.execute("create table t_ds_jdbc_registry_data_change_event\n" +
                    "(\n" +
                    "    id                 bigserial NOT NULL,\n" +
                    "    event_type         varchar   not null,\n" +
                    "    jdbc_registry_data text      not null,\n" +
                    "    create_time        timestamp not null,\n" +
                    "PRIMARY KEY (id)\n" +
                    ");");

        }
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {
        postgresqlContainer.close();
    }
}
