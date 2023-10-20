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

import org.apache.dolphinscheduler.tools.datasource.BaseDolphinSchedulerManagerIT;
import org.apache.dolphinscheduler.tools.datasource.DolphinSchedulerManager;

import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.Lists;

// todo: use TestTemplate to test multiple mysql version
@Slf4j
@ActiveProfiles("mysql")
public class BaseDolphinSchedulerDatabaseWithMysqlIT extends BaseDolphinSchedulerManagerIT {

    @Autowired
    protected DolphinSchedulerManager dolphinSchedulerManager;

    @Autowired
    protected DataSource dataSource;

    protected static GenericContainer databaseContainer;

    @BeforeAll
    public static void initializeContainer() {
        // todo: test with multiple mysql version
        databaseContainer = new MySQLContainer(DockerImageName.parse("mysql:8.0"))
                .withUsername("root")
                .withPassword("root")
                .withDatabaseName("dolphinscheduler")
                .withNetwork(NETWORK)
                .withExposedPorts(3306)
                .waitingFor(Wait.forHealthcheck());
        databaseContainer.setPortBindings(Lists.newArrayList("3306:3306"));

        log.info("Create MySQLContainer successfully.");
        databaseContainer.start();

        log.info("Starting MySQLContainer...");
        Startables.deepStart(Stream.of(databaseContainer)).join();
        log.info("MySQLContainer started");
    }

    @AfterAll
    public static void closeContainer() {
        if (databaseContainer != null) {
            databaseContainer.stop();
        }
    }

}
