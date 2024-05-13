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

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "registry")
class ZookeeperRegistryProperties implements Validator {

    private ZookeeperProperties zookeeper = new ZookeeperProperties();

    private String type;

    @Override
    public boolean supports(Class<?> clazz) {
        return ZookeeperRegistryProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ZookeeperRegistryProperties zookeeperRegistryProperties = (ZookeeperRegistryProperties) target;
        if (zookeeperRegistryProperties.getZookeeper() == null) {
            errors.rejectValue("zookeeper", "zookeeper", "zookeeper properties is required");
        }

        ZookeeperProperties zookeeper = zookeeperRegistryProperties.getZookeeper();
        if (StringUtils.isEmpty(zookeeper.getNamespace())) {
            errors.rejectValue("zookeeper.namespace", "", "zookeeper.namespace cannot be null");
        }
        if (StringUtils.isEmpty(zookeeper.getConnectString())) {
            errors.rejectValue("zookeeper.connectString", "", "zookeeper.connectString cannot be null");
        }
        if (zookeeper.getRetryPolicy() == null) {
            errors.rejectValue("zookeeper.retryPolicy", "", "zookeeper.retryPolicy cannot be null");
        }
        if (zookeeper.getSessionTimeout() == null || zookeeper.getSessionTimeout().isZero()
                || zookeeper.getSessionTimeout().isNegative()) {
            errors.rejectValue("zookeeper.sessionTimeout", "", "zookeeper.sessionTimeout should be positive");
        }
        if (zookeeper.getConnectionTimeout() == null || zookeeper.getConnectionTimeout().isZero()
                || zookeeper.getConnectionTimeout().isNegative()) {
            errors.rejectValue("zookeeper.connectionTimeout", "", "zookeeper.connectionTimeout should be positive");
        }
        if (zookeeper.getBlockUntilConnected() == null || zookeeper.getBlockUntilConnected().isZero()
                || zookeeper.getBlockUntilConnected().isNegative()) {
            errors.rejectValue("zookeeper.blockUntilConnected", "", "zookeeper.blockUntilConnected should be positive");
        }
        printConfig();
    }

    private void printConfig() {
        String config =
                "\n****************************ZookeeperRegistryProperties**************************************" +
                        "\n  namespace -> " + zookeeper.getNamespace() +
                        "\n  connectString -> " + zookeeper.getConnectString() +
                        "\n  retryPolicy -> " + zookeeper.getRetryPolicy() +
                        "\n  digest -> " + zookeeper.getDigest() +
                        "\n  sessionTimeout -> " + zookeeper.getSessionTimeout() +
                        "\n  connectionTimeout -> " + zookeeper.getConnectionTimeout() +
                        "\n  blockUntilConnected -> " + zookeeper.getBlockUntilConnected() +
                        "\n****************************ZookeeperRegistryProperties**************************************";
        log.info(config);
    }

    @Data
    public static final class ZookeeperProperties {

        private String namespace = "dolphinscheduler";
        private String connectString;
        private RetryPolicy retryPolicy = new RetryPolicy();
        private String digest;
        private Duration sessionTimeout = Duration.ofSeconds(30);
        private Duration connectionTimeout = Duration.ofSeconds(9);
        private Duration blockUntilConnected = Duration.ofMillis(600);

        @Data
        public static final class RetryPolicy {

            private Duration baseSleepTime = Duration.ofMillis(60);
            private int maxRetries;
            private Duration maxSleep = Duration.ofMillis(300);

        }
    }

}
