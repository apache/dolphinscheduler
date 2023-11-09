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

package org.apache.dolphinscheduler;

import org.apache.dolphinscheduler.registry.StandaloneRegistry;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class StandaloneServer {

    @Value("${registry.type}")
    private String registryType;

    private final List<StandaloneRegistry> registries;

    @Autowired
    public StandaloneServer(List<StandaloneRegistry> registries) {
        this.registries = registries;
    }

    public static void main(String[] args) throws Exception {
        try {
            SpringApplication.run(StandaloneServer.class, args);
        } catch (Exception ex) {
            log.error("StandaloneServer start failed", ex);
            System.exit(1);
        }
    }

    @PostConstruct
    private void initRegistry() {
        Optional<StandaloneRegistry> registry = registries.stream()
                .filter(r -> r.supports(registryType))
                .findFirst();

        registry.ifPresent(StandaloneRegistry::init);
    }

}
