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

import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleException;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.StrategyType;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorHolder;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorThreadPool;

import java.time.Duration;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "worker.registry-disconnect-strategy", name = "strategy", havingValue = "waiting")
@Slf4j
public class WorkerWaitingStrategy implements WorkerConnectStrategy {

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Autowired
    private WorkerTaskExecutorThreadPool workerManagerThread;

    public WorkerWaitingStrategy(@NonNull WorkerConfig workerConfig,
                                 @NonNull RegistryClient registryClient,
                                 @NonNull MessageRetryRunner messageRetryRunner,
                                 @NonNull WorkerTaskExecutorThreadPool workerManagerThread) {
        this.workerConfig = workerConfig;
        this.registryClient = registryClient;
        this.messageRetryRunner = messageRetryRunner;
        this.workerManagerThread = workerManagerThread;
    }

    @Override
    public void disconnect() {
        try {
            ServerLifeCycleManager.toWaiting();
            clearWorkerResource();
            Duration maxWaitingTime = workerConfig.getRegistryDisconnectStrategy().getMaxWaitingTime();
            try {
                log.info("Worker disconnect from registry will try to reconnect in {} s",
                        maxWaitingTime.getSeconds());
                registryClient.connectUntilTimeout(maxWaitingTime);
            } catch (RegistryException ex) {
                throw new ServerLifeCycleException(
                        String.format("Waiting to reconnect to registry in %s failed", maxWaitingTime), ex);
            }

        } catch (ServerLifeCycleException e) {
            String errorMessage = String.format(
                    "Disconnect from registry and change the current status to waiting error, the current server state is %s, will stop the current server",
                    ServerLifeCycleManager.getServerStatus());
            log.error(errorMessage, e);
            registryClient.getStoppable().stop(errorMessage);
        } catch (RegistryException ex) {
            String errorMessage = "Disconnect from registry and waiting to reconnect failed, will stop the server";
            log.error(errorMessage, ex);
            registryClient.getStoppable().stop(errorMessage);
        } catch (Exception ex) {
            String errorMessage = "Disconnect from registry and get an unknown exception, will stop the server";
            log.error(errorMessage, ex);
            registryClient.getStoppable().stop(errorMessage);
        }
    }

    @Override
    public void reconnect() {
        if (ServerLifeCycleManager.isRunning()) {
            log.info("no need to reconnect, as the current server status is running");
        } else {
            try {
                ServerLifeCycleManager.recoverFromWaiting();
                log.info("Recover from waiting success, the current server status is {}",
                        ServerLifeCycleManager.getServerStatus());
            } catch (Exception e) {
                String errorMessage =
                        String.format(
                                "Recover from waiting failed, the current server status is %s, will stop the server",
                                ServerLifeCycleManager.getServerStatus());
                log.error(errorMessage, e);
                registryClient.getStoppable().stop(errorMessage);
            }
        }
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.WAITING;
    }

    private void clearWorkerResource() {
        workerManagerThread.clearTask();
        WorkerTaskExecutorHolder.clear();
        log.warn("Worker server clear the tasks due to lost connection from registry");
        messageRetryRunner.clearMessage();
        log.warn("Worker server clear the retry message due to lost connection from registry");
    }

}
