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

package org.apache.dolphinscheduler.server.master.registry;

import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.StrategyType;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * This strategy will stop the master server, when disconnected from {@link org.apache.dolphinscheduler.registry.api.Registry}.
 */
@Service
@ConditionalOnProperty(prefix = "master.registry-disconnect-strategy", name = "strategy", havingValue = "stop", matchIfMissing = true)
@Slf4j
public class MasterStopStrategy implements MasterConnectStrategy {

    @Autowired
    private RegistryClient registryClient;
    @Autowired
    private MasterConfig masterConfig;

    @Override
    public void disconnect() {
        registryClient.getStoppable()
                .stop("Master disconnected from registry, will stop myself due to the stop strategy");
    }

    @Override
    public void reconnect() {
        log.warn("The current connect strategy is stop, so the master will not reconnect to registry");
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.STOP;
    }
}
