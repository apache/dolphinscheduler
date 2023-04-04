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
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertRegistryClient implements AutoCloseable {

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private AlertConfig alertConfig;

    private AlertHeartbeatTask alertHeartbeatTask;

    public void start() {
        log.info("AlertRegistryClient starting...");
        registryClient.getLock(RegistryNodeType.ALERT_LOCK.getRegistryPath());
        alertHeartbeatTask = new AlertHeartbeatTask(alertConfig, registryClient);
        alertHeartbeatTask.start();
        // start heartbeat task
        log.info("AlertRegistryClient started...");
    }

    @Override
    public void close() {
        log.info("AlertRegistryClient closing...");
        alertHeartbeatTask.shutdown();
        registryClient.releaseLock(RegistryNodeType.ALERT_LOCK.getRegistryPath());
        log.info("AlertRegistryClient closed...");
    }
}
