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

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Data
@Validated
@Configuration
@ConfigurationProperties(value = "api")
public class ApiConfig implements Validator {

    private boolean auditEnable = false;

    private TrafficConfiguration trafficControl = new TrafficConfiguration();

    private PythonGatewayConfiguration pythonGateway = new PythonGatewayConfiguration();

    @Override
    public boolean supports(Class<?> clazz) {
        return ApiConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        printConfig();
    }

    private void printConfig() {
        log.info("API config: auditEnable -> {} ", auditEnable);
        log.info("API config: trafficControl -> {} ", trafficControl);
        log.info("API config: pythonGateway -> {} ", pythonGateway);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrafficConfiguration {

        private boolean globalSwitch = false;
        private Integer maxGlobalQpsRate = 300;
        private boolean tenantSwitch = false;
        private Integer defaultTenantQpsRate = 10;
        private Map<String, Integer> customizeTenantQpsRate = new HashMap<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PythonGatewayConfiguration {

        private boolean enabled = true;
        private String gatewayServerAddress = "0.0.0.0";
        private int gatewayServerPort = 25333;
        private String pythonAddress = "127.0.0.1";
        private int pythonPort = 25334;
        private int connectTimeout = 0;
        private int readTimeout = 0;
        private String authToken = "jwUDzpLsNKEFER4*a8gruBH_GsAurNxU7A@Xc";
    }

}
