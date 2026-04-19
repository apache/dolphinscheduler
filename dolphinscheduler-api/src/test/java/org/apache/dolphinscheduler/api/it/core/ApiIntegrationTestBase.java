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

package org.apache.dolphinscheduler.api.it.core;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.it.config.MySqlTestContainerInitializer;
import org.apache.dolphinscheduler.api.it.core.db.DatabaseCleaner;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = {ApiApplicationServer.class,
        IntegrationTestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"it"})
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@Tag("integration")
@ExtendWith(LoadYamlExtension.class)
public abstract class ApiIntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected YamlDataLoader yamlDataLoader;
    @Autowired
    protected DatabaseCleaner databaseCleaner;
    @Autowired
    protected TestSessionHelper sessionHelper;

    @BeforeEach
    void setUpData() {
        databaseCleaner.cleanAll();
        List<String> base = baseFixtures();
        if (!base.isEmpty()) {
            yamlDataLoader.load(base);
        }
    }

    /** Subclasses may override; defaults load base-tenants and base-users. */
    protected List<String> baseFixtures() {
        return Arrays.asList(
                "classpath:it/fixtures/base-tenants.yaml",
                "classpath:it/fixtures/base-users.yaml");
    }
}
