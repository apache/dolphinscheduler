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
import java.time.Duration;
import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("mysql")
class MysqlJdbcRegistryTestCase extends JdbcRegistryTestCase {

    private static GenericContainer<?> mysqlContainer;

    @SneakyThrows
    @BeforeAll
    public static void setUpTestingServer() {
        mysqlContainer = new MySQLContainer(DockerImageName.parse("mysql:8.0"))
                .withUsername("root")
                .withPassword("root")
                .withDatabaseName("dolphinscheduler")
                .withNetwork(Network.newNetwork())
                .withExposedPorts(3306)
                .waitingFor(Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(300)));

        Startables.deepStart(Stream.of(mysqlContainer)).join();

        String jdbcUrl = "jdbc:mysql://localhost:" + mysqlContainer.getMappedPort(3306)
                + "/dolphinscheduler?useSSL=false&serverTimezone=UTC";
        System.clearProperty("spring.datasource.url");
        System.setProperty("spring.datasource.url", jdbcUrl);

        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
                Statement statement = connection.createStatement();) {
            statement.execute(
                    "CREATE TABLE `t_ds_jdbc_registry_data`\n" +
                            "(\n" +
                            "    `id`               bigint(11)   NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                            "    `data_key`         varchar(256) NOT NULL COMMENT 'key, like zookeeper node path',\n" +
                            "    `data_value`       text         NOT NULL COMMENT 'data, like zookeeper node value',\n"
                            +
                            "    `data_type`        varchar(64)  NOT NULL COMMENT 'EPHEMERAL, PERSISTENT',\n" +
                            "    `client_id`        bigint       NOT NULL COMMENT 'client id',\n" +
                            "    `create_time`      timestamp    NOT NULL COMMENT 'create time',\n" +
                            "    `last_update_time` timestamp    NOT NULL COMMENT 'last update time',\n" +
                            "    PRIMARY KEY (`id`),\n" +
                            "    unique (`data_key`)\n" +
                            ") ENGINE = InnoDB\n" +
                            "  DEFAULT CHARSET = utf8;");

            statement.execute(
                    "CREATE TABLE `t_ds_jdbc_registry_lock`\n" +
                            "(\n" +
                            "    `id`          bigint(11)   NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                            "    `lock_key`    varchar(256) NOT NULL COMMENT 'lock path',\n" +
                            "    `lock_owner`  varchar(256) NOT NULL COMMENT 'the lock owner, ip_processId',\n" +
                            "    `client_id`   bigint       NOT NULL COMMENT 'client id',\n" +
                            "    `create_time` timestamp    NOT NULL COMMENT 'create time',\n" +
                            "    PRIMARY KEY (`id`),\n" +
                            "    unique Key `uk_t_ds_jdbc_registry_lockKey` (`lock_key`)\n" +
                            ") ENGINE = InnoDB\n" +
                            "  DEFAULT CHARSET = utf8;");

            statement.execute("CREATE TABLE `t_ds_jdbc_registry_client_heartbeat`\n" +
                    "(\n" +
                    "    `id`                  bigint(11)   NOT NULL   COMMENT 'primary key',\n" +
                    "    `client_name`         varchar(256) NOT NULL COMMENT 'client name, ip_processId',\n" +
                    "    `last_heartbeat_time` bigint       NOT NULL COMMENT 'last heartbeat timestamp',\n" +
                    "    `connection_config`   text         NOT NULL COMMENT 'connection config',\n" +
                    "    `create_time`         timestamp    NOT NULL COMMENT 'create time',\n" +
                    "    PRIMARY KEY (`id`)\n" +
                    ") ENGINE = InnoDB\n" +
                    "  DEFAULT CHARSET = utf8;");

            statement.execute("CREATE TABLE `t_ds_jdbc_registry_data_change_event`\n" +
                    "(\n" +
                    "    `id`                 bigint(11)  NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                    "    `event_type`         varchar(64) NOT NULL COMMENT 'ADD, UPDATE, DELETE',\n" +
                    "    `jdbc_registry_data` text        NOT NULL COMMENT 'jdbc registry data',\n" +
                    "    `create_time`        timestamp   NOT NULL COMMENT 'create time',\n" +
                    "    PRIMARY KEY (`id`)\n" +
                    ") ENGINE = InnoDB\n" +
                    "  DEFAULT CHARSET = utf8;");
        }
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {
        mysqlContainer.close();
    }
}
