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

import lombok.NonNull;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerConnectionStateListener implements ConnectionListener {

    private final Logger logger = LoggerFactory.getLogger(WorkerConnectionStateListener.class);
    private final WorkerConfig workerConfig;
    private final RegistryClient registryClient;
    private final WorkerConnectStrategy workerConnectStrategy;

    public WorkerConnectionStateListener(@NonNull WorkerConfig workerConfig,
                                         @NonNull RegistryClient registryClient,
                                         @NonNull WorkerConnectStrategy workerConnectStrategy) {
        this.workerConfig = workerConfig;
        this.registryClient = registryClient;
        this.workerConnectStrategy = workerConnectStrategy;
    }

    @Override
    public void onUpdate(ConnectionState state) {
        logger.info("Worker received a {} event from registry, the current server state is {}", state,
                ServerLifeCycleManager.getServerStatus());
        switch (state) {
            case CONNECTED:
                break;
            case SUSPENDED:
                break;
            case RECONNECTED:
                workerConnectStrategy.reconnect();
                break;
            case DISCONNECTED:
                workerConnectStrategy.disconnect();
            default:
        }
    }
}
