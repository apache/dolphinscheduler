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

package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import org.apache.dolphinscheduler.plugin.registry.RegistryTestCase;

import org.apache.commons.lang3.RandomUtils;

import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.Lists;

@SpringBootTest(classes = ZookeeperRegistryProperties.class)
@SpringBootApplication(scanBasePackageClasses = ZookeeperRegistryProperties.class)
class ZookeeperRegistryTestCase extends RegistryTestCase<ZookeeperRegistry> {

    @Autowired
    private ZookeeperRegistryProperties zookeeperRegistryProperties;

    private static GenericContainer<?> zookeeperContainer;

    private static final Network NETWORK = Network.newNetwork();

    @SneakyThrows
    @BeforeAll
    public static void setUpTestingServer() {
        zookeeperContainer = new GenericContainer<>(DockerImageName.parse("zookeeper:3.8"))
                .withNetwork(NETWORK);
        int randomPort = RandomUtils.nextInt(10000, 65535);
        zookeeperContainer.setPortBindings(Lists.newArrayList(randomPort + ":2181"));
        Startables.deepStart(Stream.of(zookeeperContainer)).join();
        System.setProperty("registry.zookeeper.connect-string", "localhost:" + randomPort);
    }

    @SneakyThrows
    @Override
    public ZookeeperRegistry createRegistry() {
        return new ZookeeperRegistry(zookeeperRegistryProperties);
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {
        zookeeperContainer.close();
    }
}
