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

import org.apache.commons.lang3.RandomUtils;

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

import com.google.common.collect.Lists;

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
        int exposedPort = RandomUtils.nextInt(10000, 65535);

        postgresqlContainer.setPortBindings(Lists.newArrayList(exposedPort + ":5432"));
        Startables.deepStart(Stream.of(postgresqlContainer)).join();

        String jdbcUrl = "jdbc:postgresql://localhost:" + exposedPort + "/dolphinscheduler";
        System.setProperty("spring.datasource.url", jdbcUrl);
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
                Statement statement = connection.createStatement();) {
            statement.execute(
                    "create table t_ds_jdbc_registry_data\n" +
                            "(\n" +
                            "    id               serial\n" +
                            "        constraint t_ds_jdbc_registry_data_pk primary key,\n" +
                            "    data_key         varchar                             not null,\n" +
                            "    data_value       text                                not null,\n" +
                            "    data_type        int4                                not null,\n" +
                            "    last_term        bigint                              not null,\n" +
                            "    last_update_time timestamp default current_timestamp not null,\n" +
                            "    create_time      timestamp default current_timestamp not null\n" +
                            ");");
            statement.execute(
                    "create unique index t_ds_jdbc_registry_data_key_uindex on t_ds_jdbc_registry_data (data_key);");
            statement.execute(
                    "create table t_ds_jdbc_registry_lock\n" +
                            "(\n" +
                            "    id               serial\n" +
                            "        constraint t_ds_jdbc_registry_lock_pk primary key,\n" +
                            "    lock_key         varchar                             not null,\n" +
                            "    lock_owner       varchar                             not null,\n" +
                            "    last_term        bigint                              not null,\n" +
                            "    last_update_time timestamp default current_timestamp not null,\n" +
                            "    create_time      timestamp default current_timestamp not null\n" +
                            ");");
            statement.execute(
                    "create unique index t_ds_jdbc_registry_lock_key_uindex on t_ds_jdbc_registry_lock (lock_key);");
        }
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {
        postgresqlContainer.close();
    }
}
