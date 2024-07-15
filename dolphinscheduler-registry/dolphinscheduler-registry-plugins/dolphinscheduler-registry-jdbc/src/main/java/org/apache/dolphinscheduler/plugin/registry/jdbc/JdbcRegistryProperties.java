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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import org.apache.dolphinscheduler.common.utils.NetUtils;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import com.zaxxer.hikari.HikariConfig;

@Data
@Slf4j
@Validated
@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "jdbc")
@ConfigurationProperties(prefix = "registry")
public class JdbcRegistryProperties implements Validator {

    private static final Duration MIN_HEARTBEAT_REFRESH_INTERVAL = Duration.ofSeconds(1);

    @Value("${server.port:8080}")
    private int serverPort;

    private String jdbcRegistryClientName;

    private Duration heartbeatRefreshInterval = Duration.ofSeconds(3);
    private Duration sessionTimeout = Duration.ofSeconds(60);
    private HikariConfig hikariConfig;

    @Override
    public boolean supports(Class<?> clazz) {
        return JdbcRegistryProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        JdbcRegistryProperties jdbcRegistryProperties = (JdbcRegistryProperties) target;
        if (jdbcRegistryProperties.getHeartbeatRefreshInterval().compareTo(MIN_HEARTBEAT_REFRESH_INTERVAL) < 0) {
            errors.rejectValue("heartbeatRefreshInterval", "heartbeatRefreshInterval",
                    "heartbeatRefreshInterval must be greater than 1s");
        }

        if (jdbcRegistryProperties.getSessionTimeout().toMillis() < 3
                * jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis()) {
            errors.rejectValue("sessionTimeout", "sessionTimeout",
                    "sessionTimeout must be greater than 3 * heartbeatRefreshInterval");
        }
        if (StringUtils.isEmpty(jdbcRegistryClientName)) {
            jdbcRegistryClientName = NetUtils.getHost() + ":" + serverPort;
        }
        print();

    }

    private void print() {
        String config =
                "\n****************************JdbcRegistryProperties**************************************" +
                        "\n  jdbcRegistryClientName -> " + jdbcRegistryClientName +
                        "\n  heartbeatRefreshInterval -> " + heartbeatRefreshInterval +
                        "\n  sessionTimeout -> " + sessionTimeout +
                        "\n  hikariConfig -> " + hikariConfig +
                        "\n****************************JdbcRegistryProperties**************************************";
        log.info(config);
    }
}
