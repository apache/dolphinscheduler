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

package org.apache.dolphinscheduler.alert;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Slf4j
@Data
@Validated
@Configuration
@ConfigurationProperties("alert")
public class AlertConfig implements Validator {

    private int listenPort;

    private int waitTimeout;

    private Duration heartbeatInterval = Duration.ofSeconds(60);

    private String alertServerAddress;
    private String alertServerRegistryPath;

    @Override
    public boolean supports(Class<?> clazz) {
        return AlertConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AlertConfig alertConfig = (AlertConfig) target;

        if (heartbeatInterval.getSeconds() <= 0) {
            errors.rejectValue("heartbeat-interval", null, "should be a valid duration");
        }

        alertConfig.setAlertServerAddress(NetUtils.getAddr(listenPort));
        alertConfig.setAlertServerRegistryPath(
                NodeType.ALERT_SERVER.getRegistryPath() + "/" + alertConfig.alertServerAddress);
        printConfig();
    }

    private void printConfig() {
        log.info("Alert config: listenPort -> {}", listenPort);
        log.info("Alert config: waitTimeout -> {}", waitTimeout);
        log.info("Alert config: alertServerAddress -> {}", alertServerAddress);
        log.info("Alert config: alertServerRegistryPath -> {}", alertServerRegistryPath);
    }
}
