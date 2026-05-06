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

package org.apache.dolphinscheduler.server.master;

import org.apache.dolphinscheduler.dao.DaoConfiguration;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.integration.MasterContainer;
import org.apache.dolphinscheduler.server.master.integration.Repository;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContextFactory;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

/**
 * The abstract class for master integration test.
 * <p> Used to create a text environment to test master server.
 * <p> In order to separate the environment for each text case, the context will be dirtied before each test method.
 */
@Slf4j
@SpringBootTest(classes = {
        MasterServer.class,
        DaoConfiguration.class}, properties = "spring.config.additional-location=classpath:/spring-it-application.yaml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractMasterIntegrationTestCase {

    @Autowired
    protected WorkflowTestCaseContextFactory workflowTestCaseContextFactory;

    @Autowired
    protected WorkflowOperator workflowOperator;

    @Autowired
    protected Repository repository;

    @Autowired
    protected MasterContainer masterContainer;

    @Autowired
    protected RegistryClient registryClient;

    @Autowired
    protected MasterConfig masterConfig;

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * Unbind this test method's {@link MeterRegistry} from {@link Metrics#globalRegistry}.
     * <p>
     * Spring's metrics auto-configuration adds the per-context registry to {@code globalRegistry}
     * but does not remove it on teardown, so {@code @DirtiesContext} would otherwise leave dangling
     * child registries that pollute counter reads in later test methods.
     */
    @AfterEach
    public void unbindMeterRegistryFromGlobal() {
        Metrics.globalRegistry.remove(meterRegistry);
    }
}
