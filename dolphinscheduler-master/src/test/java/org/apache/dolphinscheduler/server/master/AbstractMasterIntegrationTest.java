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

import org.apache.curator.test.TestingServer;

import javax.annotation.PostConstruct;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

/**
 * The abstract class for master integration test.
 * <p> Used to create a text environment to test master server.
 * <p> In order to separate the environment for each text case, the context will be dirtied before each test method.
 */
@SpringBootTest(classes = {
        MasterServer.class,
        DaoConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractMasterIntegrationTest {

    @Configuration
    public static class RegistryServer {

        @PostConstruct
        public void startEmbedRegistryServer() throws Exception {
            final TestingServer server = new TestingServer(true);
            System.setProperty("registry.zookeeper.connect-string", server.getConnectString());
        }
    }
}
