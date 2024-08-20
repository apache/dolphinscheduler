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

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ZKClientConfig;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.DumbWatcher;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import java.util.Collections;
import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("digest")
@SpringBootTest(classes = ZookeeperRegistryProperties.class)
@SpringBootApplication(scanBasePackageClasses = ZookeeperRegistryProperties.class)
public class ZookeeperRegistryDigestTestCase extends RegistryTestCase<ZookeeperRegistry> {

    @Autowired
    private ZookeeperRegistryProperties zookeeperRegistryProperties;

    private static GenericContainer<?> zookeeperContainer;

    private static final Network NETWORK = Network.newNetwork();

    private static ZooKeeper zk;

    private static final String ROOT_USER = "root";

    private static final String ROOT_PASSWORD = "root_passwd";

    private static final String ID_PASSWORD = String.format("%s:%s", ROOT_USER, ROOT_PASSWORD);

    private static void setupRootACLForDigest(final ZooKeeper zk) throws Exception {
        final String digest = DigestAuthenticationProvider.generateDigest(ID_PASSWORD);
        final ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", digest));
        zk.setACL("/", Collections.singletonList(acl), -1);
    }

    @SneakyThrows
    @BeforeAll
    public static void setUpTestingServer() {
        zookeeperContainer = new GenericContainer<>(DockerImageName.parse("zookeeper:3.8"))
                .withNetwork(NETWORK)
                .withExposedPorts(2181);
        Startables.deepStart(Stream.of(zookeeperContainer)).join();
        System.clearProperty("registry.zookeeper.connect-string");
        System.setProperty("registry.zookeeper.connect-string", "localhost:" + zookeeperContainer.getMappedPort(2181));
        zk = new ZooKeeper("localhost:" + zookeeperContainer.getMappedPort(2181),
                30000, new DumbWatcher(), new ZKClientConfig());
        setupRootACLForDigest(zk);
    }

    @SneakyThrows
    @Override
    public ZookeeperRegistry createRegistry() {
        return new ZookeeperRegistry(zookeeperRegistryProperties);
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {
        zk.close();
        zookeeperContainer.close();
    }
}
