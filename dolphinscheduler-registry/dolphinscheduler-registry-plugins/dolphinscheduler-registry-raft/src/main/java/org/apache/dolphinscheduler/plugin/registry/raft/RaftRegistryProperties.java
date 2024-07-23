/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.registry.raft;

import java.time.Duration;

import lombok.Data;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "raft")
@ConfigurationProperties(prefix = "registry")
public class RaftRegistryProperties {

    private String clusterName;
    private String serverAddressList;
    private String serverAddress;
    private int serverPort;
    private String logStorageDir;
    private Duration distributedLockTimeout = Duration.ofSeconds(3);
    private Duration distributedLockRetryInterval = Duration.ofSeconds(5);
    private String module = "master";
    private Duration listenerCheckInterval = Duration.ofSeconds(3);
    private int cliMaxRetries = 3;
    private Duration cliTimeout = Duration.ofSeconds(5);
    private Duration refreshLeaderTimeout = Duration.ofSeconds(2);
    private Duration connectStateCheckInterval = Duration.ofSeconds(2);
    private int subscribeListenerThreadPoolSize = 1;
    private int connectionListenerThreadPoolSize = 1;

}
