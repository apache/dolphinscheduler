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

package org.apache.dolphinscheduler.alert.registry;

import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.common.model.AlertServerHeartBeat;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlertHeartbeatTask extends BaseHeartBeatTask<AlertServerHeartBeat> {

    private final AlertConfig alertConfig;
    private final Integer processId;
    private final RegistryClient registryClient;
    private final String heartBeatPath;
    private final long startupTime;

    public AlertHeartbeatTask(AlertConfig alertConfig,
                              RegistryClient registryClient) {
        super("AlertHeartbeatTask", alertConfig.getHeartbeatInterval().toMillis());
        this.startupTime = System.currentTimeMillis();
        this.alertConfig = alertConfig;
        this.registryClient = registryClient;
        this.heartBeatPath =
                RegistryNodeType.ALERT_SERVER.getRegistryPath() + "/" + alertConfig.getAlertServerAddress();
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public AlertServerHeartBeat getHeartBeat() {
        return AlertServerHeartBeat.builder()
                .processId(processId)
                .startupTime(startupTime)
                .reportTime(System.currentTimeMillis())
                .cpuUsage(OSUtils.cpuUsagePercentage())
                .memoryUsage(OSUtils.memoryUsagePercentage())
                .availablePhysicalMemorySize(OSUtils.availablePhysicalMemorySize())
                .alertServerAddress(alertConfig.getAlertServerAddress())
                .build();
    }

    @Override
    public void writeHeartBeat(AlertServerHeartBeat heartBeat) {
        String heartBeatJson = JSONUtils.toJsonString(heartBeat);
        registryClient.persistEphemeral(heartBeatPath, heartBeatJson);
        log.debug("Success write master heartBeatInfo into registry, masterRegistryPath: {}, heartBeatInfo: {}",
                heartBeatPath, heartBeatJson);
    }
}
