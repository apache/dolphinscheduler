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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleException;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.registry.api.ConnectStrategyProperties;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.StrategyType;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcServer;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorThreadPool;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * worker registry test
 */
@ExtendWith(MockitoExtension.class)
public class WorkerStrategyTest {

    private static final Logger log = LoggerFactory.getLogger(WorkerStrategyTest.class);
    @Mock
    private RegistryClient registryClient;
    @Mock
    private IStoppable stoppable;
    @Mock
    private WorkerConfig workerConfig;
    @Mock
    private WorkerRpcServer workerRpcServer;
    @Mock
    private MessageRetryRunner messageRetryRunner;
    @Mock
    private WorkerTaskExecutorThreadPool workerManagerThread;
    @Mock
    private ConnectStrategyProperties connectStrategyProperties;

    @Test
    public void testWorkerStopStrategy() {
        given(registryClient.getStoppable())
                .willReturn(stoppable);
        WorkerStopStrategy workerStopStrategy = new WorkerStopStrategy();
        workerStopStrategy.registryClient = registryClient;
        workerStopStrategy.reconnect();
        workerStopStrategy.disconnect();
        Assertions.assertEquals(workerStopStrategy.getStrategyType(), StrategyType.STOP);
    }

    @Test
    public void testWorkerWaitingStrategyreconnect() {
        WorkerWaitingStrategy workerWaitingStrategy = new WorkerWaitingStrategy(
                workerConfig,
                registryClient,
                messageRetryRunner,
                workerManagerThread);
        Assertions.assertEquals(workerWaitingStrategy.getStrategyType(), StrategyType.WAITING);

        try (
                MockedStatic<ServerLifeCycleManager> serverLifeCycleManagerMockedStatic =
                        Mockito.mockStatic(ServerLifeCycleManager.class)) {
            serverLifeCycleManagerMockedStatic
                    .when(() -> ServerLifeCycleManager.isRunning())
                    .thenReturn(true);
            workerWaitingStrategy.reconnect();

        }

        try (
                MockedStatic<ServerLifeCycleManager> serverLifeCycleManagerMockedStatic =
                        Mockito.mockStatic(ServerLifeCycleManager.class)) {
            doNothing().when(stoppable).stop(anyString());
            given(registryClient.getStoppable())
                    .willReturn(stoppable);
            serverLifeCycleManagerMockedStatic
                    .when(() -> ServerLifeCycleManager.recoverFromWaiting())
                    .thenThrow(new ServerLifeCycleException(""));
            workerWaitingStrategy.reconnect();
        }

        try (
                MockedStatic<ServerLifeCycleManager> serverLifeCycleManagerMockedStatic =
                        Mockito.mockStatic(ServerLifeCycleManager.class)) {
            serverLifeCycleManagerMockedStatic
                    .when(() -> ServerLifeCycleManager.recoverFromWaiting())
                    .thenAnswer(invocation -> null);
            workerWaitingStrategy.reconnect();
        }
    }

    @Test
    public void testWorkerWaitingStrategydisconnect() {
        WorkerWaitingStrategy workerWaitingStrategy = new WorkerWaitingStrategy(
                workerConfig,
                registryClient,
                messageRetryRunner,
                workerManagerThread);
        Assertions.assertEquals(workerWaitingStrategy.getStrategyType(), StrategyType.WAITING);

        try (
                MockedStatic<ServerLifeCycleManager> serverLifeCycleManagerMockedStatic =
                        Mockito.mockStatic(ServerLifeCycleManager.class)) {
            doNothing().when(stoppable).stop(anyString());
            given(registryClient.getStoppable())
                    .willReturn(stoppable);
            serverLifeCycleManagerMockedStatic
                    .when(() -> ServerLifeCycleManager.toWaiting())
                    .thenThrow(new ServerLifeCycleException(""));
            workerWaitingStrategy.disconnect();
        }

        try (
                MockedStatic<ServerLifeCycleManager> serverLifeCycleManagerMockedStatic =
                        Mockito.mockStatic(ServerLifeCycleManager.class)) {
            given(connectStrategyProperties.getMaxWaitingTime()).willReturn(Duration.ofSeconds(1));
            given(workerConfig.getRegistryDisconnectStrategy()).willReturn(connectStrategyProperties);
            Mockito.reset(registryClient);
            doNothing().when(registryClient).connectUntilTimeout(any());
            serverLifeCycleManagerMockedStatic
                    .when(() -> ServerLifeCycleManager.toWaiting())
                    .thenAnswer(invocation -> null);
            workerWaitingStrategy.disconnect();
        }

        try (
                MockedStatic<ServerLifeCycleManager> serverLifeCycleManagerMockedStatic =
                        Mockito.mockStatic(ServerLifeCycleManager.class)) {
            given(connectStrategyProperties.getMaxWaitingTime()).willReturn(Duration.ofSeconds(1));
            given(workerConfig.getRegistryDisconnectStrategy()).willReturn(connectStrategyProperties);
            Mockito.reset(registryClient);
            doNothing().when(stoppable).stop(anyString());
            given(registryClient.getStoppable())
                    .willReturn(stoppable);
            Mockito.doThrow(new RegistryException("TEST")).when(registryClient).connectUntilTimeout(any());
            serverLifeCycleManagerMockedStatic
                    .when(() -> ServerLifeCycleManager.toWaiting())
                    .thenAnswer(invocation -> null);
            workerWaitingStrategy.disconnect();
        }

        try (
                MockedStatic<ServerLifeCycleManager> serverLifeCycleManagerMockedStatic =
                        Mockito.mockStatic(ServerLifeCycleManager.class)) {
            Mockito.reset(workerConfig);
            given(workerConfig.getRegistryDisconnectStrategy()).willThrow(new NullPointerException(""));
            doNothing().when(stoppable).stop(anyString());
            given(registryClient.getStoppable())
                    .willReturn(stoppable);
            serverLifeCycleManagerMockedStatic
                    .when(() -> ServerLifeCycleManager.toWaiting())
                    .thenAnswer(invocation -> null);
            workerWaitingStrategy.disconnect();
        }
    }
}
