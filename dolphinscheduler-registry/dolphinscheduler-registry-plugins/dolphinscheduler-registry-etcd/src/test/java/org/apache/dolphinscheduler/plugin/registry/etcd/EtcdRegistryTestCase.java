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

package org.apache.dolphinscheduler.plugin.registry.etcd;

import org.apache.dolphinscheduler.plugin.registry.RegistryTestCase;

import java.net.URI;
import java.util.stream.Collectors;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import io.etcd.jetcd.launcher.EtcdCluster;
import io.etcd.jetcd.test.EtcdClusterExtension;

@SpringBootTest(classes = EtcdRegistryProperties.class)
@SpringBootApplication(scanBasePackageClasses = EtcdRegistryProperties.class)
public class EtcdRegistryTestCase extends RegistryTestCase<EtcdRegistry> {

    @Autowired
    private EtcdRegistryProperties etcdRegistryProperties;

    private static EtcdCluster etcdCluster;

    @SneakyThrows
    @BeforeAll
    public static void setUpTestingServer() {
        etcdCluster = EtcdClusterExtension.builder()
                .withNodes(1)
                .withImage("ibmcom/etcd:3.2.24")
                .build()
                .cluster();
        etcdCluster.start();
        System.setProperty("registry.endpoints",
                etcdCluster.clientEndpoints().stream().map(URI::toString).collect(Collectors.joining(",")));
    }

    @SneakyThrows
    @Override
    public EtcdRegistry createRegistry() {
        return new EtcdRegistry(etcdRegistryProperties);
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {
        try (EtcdCluster cluster = etcdCluster) {
        }
    }
}
