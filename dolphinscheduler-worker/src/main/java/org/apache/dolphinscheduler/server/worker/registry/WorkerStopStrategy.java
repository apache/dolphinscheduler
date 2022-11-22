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

package org.apache.dolphinscheduler.server.worker.registry;

import org.apache.dolphinscheduler.registry.api.StrategyType;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "worker.registry-disconnect-strategy", name = "strategy", havingValue = "stop", matchIfMissing = true)
public class WorkerStopStrategy implements WorkerConnectStrategy {

    private final Logger logger = LoggerFactory.getLogger(WorkerStopStrategy.class);

    @Autowired
    public RegistryClient registryClient;
    @Autowired
    private WorkerConfig workerConfig;

    @Override
    public void disconnect() {
        registryClient.getStoppable()
                .stop("Worker disconnected from registry, will stop myself due to the stop strategy");
    }

    @Override
    public void reconnect() {
        logger.warn("The current connect strategy is stop, so the worker will not reconnect to registry");
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.STOP;
    }
}
