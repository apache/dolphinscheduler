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

package org.apache.dolphinscheduler.api.configuration;

import org.apache.dolphinscheduler.common.utils.NetUtils;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "register")
public class ApiConfig implements Validator {

    @Value("${server.port}")
    private int port;

    private Duration heartbeatInterval = Duration.ofSeconds(10);
    private int heartbeatErrorThreshold = 5;
    private String apiAddress;

    @Override
    public boolean supports(Class<?> clazz) {
        return ApiConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ApiConfig apiConfig = (ApiConfig) target;
        if (apiConfig.getHeartbeatInterval().toMillis() <= 0) {
            errors.rejectValue("heartbeat-interval", null, "shoule be a valid duration");
        }
        if (apiConfig.getHeartbeatErrorThreshold() <= 0) {
            errors.rejectValue("heartbeat-error-threshold", null, "should be a positive value");
        }
        apiConfig.setApiAddress(NetUtils.getAddr(port));
    }
}
