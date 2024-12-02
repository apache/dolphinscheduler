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

import static org.mockito.BDDMockito.given;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.config.WorkerServerLoadProtection;
import org.apache.dolphinscheduler.server.worker.executor.PhysicalTaskExecutorContainerProvider;
import org.apache.dolphinscheduler.task.executor.container.ExclusiveThreadTaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.TaskExecutorContainerConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * worker registry test
 */
@ExtendWith(MockitoExtension.class)
public class WorkerRegistryClientTest {

    @InjectMocks
    private WorkerRegistryClient workerRegistryClient;
    @Mock
    private RegistryClient registryClient;
    @Mock
    private WorkerConfig workerConfig;
    @Mock
    private MetricsProvider metricsProvider;
    @Mock
    private PhysicalTaskExecutorContainerProvider physicalTaskExecutorContainerDelegator;
    @Mock
    private IStoppable stoppable;

    @Test
    public void testWorkerRegistryClientbasic() {

        final TaskExecutorContainerConfig containerConfig = TaskExecutorContainerConfig.builder()
                .taskExecutorThreadPoolSize(10)
                .containerName("test")
                .build();
        final ExclusiveThreadTaskExecutorContainer container =
                new ExclusiveThreadTaskExecutorContainer(containerConfig);
        given(physicalTaskExecutorContainerDelegator.getExecutorContainer()).willReturn(container);

        given(workerConfig.getWorkerAddress()).willReturn(NetUtils.getAddr(1234));
        given(workerConfig.getMaxHeartbeatInterval()).willReturn(Duration.ofSeconds(1));
        given(workerConfig.getServerLoadProtection()).willReturn(new WorkerServerLoadProtection());
        given(metricsProvider.getSystemMetrics()).willReturn(new SystemMetrics());
        given(registryClient.checkNodeExists(Mockito.anyString(), Mockito.any(RegistryNodeType.class)))
                .willReturn(true);

        workerRegistryClient.initWorkRegistry();
        workerRegistryClient.start();

        workerRegistryClient.setRegistryStoppable(stoppable);
    }

    @Test
    public void testWorkerRegistryClientgetAlertServerAddress() {
        given(registryClient.getServerList(Mockito.any(RegistryNodeType.class)))
                .willReturn(new ArrayList<Server>());
        Assertions.assertEquals(workerRegistryClient.getAlertServerAddress(), Optional.empty());
        Mockito.reset(registryClient);
        String host = "test";
        Integer port = 1;
        Server server = new Server();
        server.setHost(host);
        server.setPort(port);
        given(registryClient.getServerList(Mockito.any(RegistryNodeType.class)))
                .willReturn(new ArrayList<Server>(Arrays.asList(server)));
        Assertions.assertEquals(workerRegistryClient.getAlertServerAddress().get().getAddress(),
                String.format("%s:%d", host, port));
    }
}
