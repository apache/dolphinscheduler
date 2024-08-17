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

import java.time.Duration;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "registry")
public class EtcdRegistryProperties {

    private String endpoints;
    private String namespace = "dolphinscheduler";
    private Duration connectionTimeout = Duration.ofSeconds(9);

    private Duration ttl = Duration.ofSeconds(30);

    // auth
    private String user;
    private String password;
    private String authority;

    // RetryPolicy
    private Duration retryDelay = Duration.ofMillis(60);
    private Duration retryMaxDelay = Duration.ofMillis(300);
    private Duration retryMaxDuration = Duration.ofMillis(1500);

    // loadBalancerPolicy
    private String loadBalancerPolicy;

    // ssl
    private String certFile;
    private String keyCertChainFile;
    private String keyFile;
}
