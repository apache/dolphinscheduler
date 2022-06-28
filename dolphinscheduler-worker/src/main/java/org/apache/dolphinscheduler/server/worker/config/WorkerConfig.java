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

package org.apache.dolphinscheduler.server.worker.config;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.Duration;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Sets;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "worker")
public class WorkerConfig {
    private int listenPort = 1234;
    private int execThreads = 10;
    private Duration heartbeatInterval = Duration.ofSeconds(10);
    private int hostWeight = 100;
    private boolean tenantAutoCreate = true;
    private boolean tenantDistributedUser = false;
    private int maxCpuLoadAvg = -1;
    private double reservedMemory = 0.3;
    private Set<String> groups = Sets.newHashSet("default");
    private String alertListenHost = "localhost";
    private int alertListenPort = 50052;

    @PostConstruct
    public void validate() {
        checkArgument(execThreads > 0, "exec-threads " + execThreads + " should bigger then 0");
        checkArgument(heartbeatInterval.toMillis() > 0, "heartbeat-interval " + heartbeatInterval + " should bigger then 0");
        if (maxCpuLoadAvg <= 0) {
            maxCpuLoadAvg = Runtime.getRuntime().availableProcessors() * 2;
        }
    }
}
