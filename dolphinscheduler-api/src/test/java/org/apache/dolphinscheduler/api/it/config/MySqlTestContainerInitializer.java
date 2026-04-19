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

package org.apache.dolphinscheduler.api.it.config;

import java.util.Arrays;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.MySQLContainer;

/**
 * Selects the database backend for integration tests:
 * <ul>
 *   <li>When the {@code mysql} profile is active (via {@code -Pintegration-mysql}):
 *       boots a MySQL TestContainer and injects its JDBC settings. The {@code mysql}
 *       profile also activates {@code MysqlDaoPluginAutoConfiguration} and
 *       {@code MySqlDatabaseCleaner}.</li>
 *   <li>Otherwise: activates the {@code h2} profile so the H2-backed DAO plugin
 *       and {@link org.apache.dolphinscheduler.api.it.core.db.H2DatabaseCleaner}
 *       are used — this keeps the default developer workflow intact.</li>
 * </ul>
 * Neither {@code h2} nor {@code mysql} is listed in
 * {@link org.apache.dolphinscheduler.api.it.core.ApiIntegrationTestBase}'s
 * {@code @ActiveProfiles} so that this initializer is the single place that decides
 * which database backend (and thus which {@code *DaoPluginAutoConfiguration}) is used.
 */
public class MySqlTestContainerInitializer
        implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static MySQLContainer<?> mysql;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        // Check both env active profiles (set by @ActiveProfiles) and the
        // spring.profiles.active system property (set by -Pintegration-mysql Maven profile).
        // @ActiveProfiles overrides the system property in Spring Test contexts, so we must
        // read the system property directly to detect the MySQL backend selection.
        String sysProp = System.getProperty("spring.profiles.active", "");
        boolean mysqlActive = Arrays.stream(env.getActiveProfiles()).anyMatch("mysql"::equals)
                || Arrays.stream(sysProp.split(",")).map(String::trim).anyMatch("mysql"::equals);

        if (!mysqlActive) {
            // Default path: activate H2 backend
            env.addActiveProfile("h2");
            return;
        }

        // Ensure the mysql Spring profile is active so that @Profile("mysql") beans
        // (MysqlDaoPluginAutoConfiguration, MySqlDatabaseCleaner) are registered.
        env.addActiveProfile("mysql");

        synchronized (MySqlTestContainerInitializer.class) {
            if (mysql == null) {
                mysql = new MySQLContainer<>("mysql:8.0")
                        .withDatabaseName("dolphinscheduler_it")
                        .withUsername("root")
                        .withPassword("root")
                        .withReuse(true);
                mysql.start();
            }
        }

        TestPropertyValues.of(
                "spring.datasource.url=" + mysql.getJdbcUrl()
                        + "?useSSL=false&allowPublicKeyRetrieval=true",
                "spring.datasource.username=" + mysql.getUsername(),
                "spring.datasource.password=" + mysql.getPassword(),
                "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                // Override schema location and force execution for non-embedded datasource
                "spring.sql.init.schema-locations=classpath:sql/dolphinscheduler_mysql.sql",
                "spring.sql.init.mode=always").applyTo(env);
    }
}
