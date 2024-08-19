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

package org.apache.dolphinscheduler.plugin.registry.raft;

import org.apache.dolphinscheduler.plugin.registry.raft.server.RaftRegistryServer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "raft")
@EnableConfigurationProperties(RaftRegistryProperties.class)
public class RaftRegistryAutoConfiguration {

    public RaftRegistryAutoConfiguration() {
        log.info("Load RaftRegistryAutoConfiguration");
    }

    @Bean
    @ConditionalOnProperty(prefix = "registry", name = "module", havingValue = "master")
    public RaftRegistryServer raftRegistryServer(RaftRegistryProperties raftRegistryProperties) {
        RaftRegistryServer raftRegistryServer = new RaftRegistryServer(raftRegistryProperties);
        raftRegistryServer.start();
        return raftRegistryServer;
    }

    @Bean
    @DependsOn("raftRegistryServer")
    @ConditionalOnProperty(prefix = "registry", name = "module", havingValue = "master")
    public RaftRegistry masterRaftRegistryClient(RaftRegistryProperties raftRegistryProperties) {
        RaftRegistry raftRegistry = new RaftRegistry(raftRegistryProperties);
        raftRegistry.start();
        return raftRegistry;
    }

    @Bean
    @ConditionalOnProperty(prefix = "registry", name = "module", havingValue = "worker")
    public RaftRegistry workerRaftRegistryClient(RaftRegistryProperties raftRegistryProperties) {
        RaftRegistry raftRegistry = new RaftRegistry(raftRegistryProperties);
        raftRegistry.start();
        return raftRegistry;
    }

    @Bean
    @ConditionalOnProperty(prefix = "registry", name = "module", havingValue = "api")
    public RaftRegistry apiRaftRegistryClient(RaftRegistryProperties raftRegistryProperties) {
        RaftRegistry raftRegistry = new RaftRegistry(raftRegistryProperties);
        raftRegistry.start();
        return raftRegistry;
    }
}
