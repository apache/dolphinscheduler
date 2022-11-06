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

import java.time.Duration;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ssh.session")
public class SSHSessionConfig {

    private PoolProperties pool = new PoolProperties();

    private SftpProperties sftp = new SftpProperties();

    @Data
    public static final class PoolProperties {

        private int maxIdlePerKey;

        private int maxTotalPerKey;

        private int maxTotal;

        private boolean blockWhenExhausted;

        private Duration maxWaitDuration;

        private Duration minEvictableIdleDuration;

        private Duration durationBetweenEvictionRuns;

        private boolean removeAbandonedOnBorrow;

        private Duration removeAbandonedTimeoutDuration;

    }

    @Data
    public static final class SftpProperties {

        private boolean enableUploadMonitor;

        private int maxUploadRate;

        private int maxFileSize;

    }

}
