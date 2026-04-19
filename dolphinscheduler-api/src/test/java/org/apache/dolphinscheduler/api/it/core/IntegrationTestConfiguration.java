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

import org.apache.curator.test.TestingServer;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * IT framework Spring configuration:
 *   - @ComponentScan auto-wires @Component beans under it.core and it.core.db
 *   - Starts an embedded ZooKeeper because dolphinscheduler-api needs a registry on boot
 *
 * Note: it.config holds only a manually-registered ApplicationContextInitializer,
 * no @Component classes, so it is intentionally not scanned.
 */
@TestConfiguration
@ComponentScan(basePackages = {
        "org.apache.dolphinscheduler.api.it.core",
        "org.apache.dolphinscheduler.api.it.core.db"
})
public class IntegrationTestConfiguration {

    private TestingServer testingServer;

    @PostConstruct
    public void startEmbeddedZk() throws Exception {
        testingServer = new TestingServer(true);
        System.setProperty("registry.zookeeper.connect-string", testingServer.getConnectString());
    }

    @PreDestroy
    public void stopEmbeddedZk() throws IOException {
        if (testingServer != null) {
            testingServer.close();
        }
    }
}
