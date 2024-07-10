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

import org.apache.dolphinscheduler.common.sql.SqlScriptRunner;

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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername("root");
        config.setPassword("root");

        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            SqlScriptRunner sqlScriptRunner = new SqlScriptRunner(dataSource, "postgresql_registry_init.sql");
            sqlScriptRunner.execute();
        }
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {
        postgresqlContainer.close();
    }
}
