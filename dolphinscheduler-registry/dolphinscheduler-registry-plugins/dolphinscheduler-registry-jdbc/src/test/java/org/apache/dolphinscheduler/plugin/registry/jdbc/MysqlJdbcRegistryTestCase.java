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
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.Lists;

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
                .waitingFor(Wait.forHealthcheck());

        int exposedPort = RandomUtils.nextInt(10000, 65535);
        mysqlContainer.setPortBindings(Lists.newArrayList(exposedPort + ":3306"));
        Startables.deepStart(Stream.of(mysqlContainer)).join();

        String jdbcUrl = "jdbc:mysql://localhost:" + exposedPort + "/dolphinscheduler?useSSL=false&serverTimezone=UTC";
        System.setProperty("spring.datasource.url", jdbcUrl);

        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
                Statement statement = connection.createStatement();) {
            statement.execute(
                    "CREATE TABLE `t_ds_jdbc_registry_data`\n" +
                            "(\n" +
                            "    `id`               bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                            "    `data_key`         varchar(256) NOT NULL COMMENT 'key, like zookeeper node path',\n" +
                            "    `data_value`       text         NOT NULL COMMENT 'data, like zookeeper node value',\n"
                            +
                            "    `data_type`        tinyint(4) NOT NULL COMMENT '1: ephemeral node, 2: persistent node',\n"
                            +
                            "    `last_term`        bigint       NOT NULL COMMENT 'last term time',\n" +
                            "    `last_update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last update time',\n"
                            +
                            "    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',\n"
                            +
                            "    PRIMARY KEY (`id`),\n" +
                            "    unique (`data_key`)\n" +
                            ") ENGINE = InnoDB\n" +
                            "  DEFAULT CHARSET = utf8;");
            statement.execute(
                    "CREATE TABLE `t_ds_jdbc_registry_lock`\n" +
                            "(\n" +
                            "    `id`               bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                            "    `lock_key`         varchar(256) NOT NULL COMMENT 'lock path',\n" +
                            "    `lock_owner`       varchar(256) NOT NULL COMMENT 'the lock owner, ip_processId',\n" +
                            "    `last_term`        bigint       NOT NULL COMMENT 'last term time',\n" +
                            "    `last_update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last update time',\n"
                            +
                            "    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',\n"
                            +
                            "    PRIMARY KEY (`id`),\n" +
                            "    unique (`lock_key`)\n" +
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
