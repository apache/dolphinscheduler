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

import org.apache.dolphinscheduler.registry.api.Registry;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@ComponentScan
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "zookeeper")
public class ZookeeperRegistryAutoConfiguration {

    public ZookeeperRegistryAutoConfiguration() {
        log.info("Load ZookeeperRegistryAutoConfiguration");
    }

    @Bean
    @ConditionalOnMissingBean(value = Registry.class)
    public ZookeeperRegistry zookeeperRegistry(ZookeeperRegistryProperties zookeeperRegistryProperties) {
        ZookeeperRegistry zookeeperRegistry = new ZookeeperRegistry(zookeeperRegistryProperties);
        zookeeperRegistry.start();
        return zookeeperRegistry;
    }

}
