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

package org.apache.dolphinscheduler.api.registry;

import static org.mockito.BDDMockito.given;

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ApiRegistryClientTest {

    @InjectMocks
    private ApiRegistryClient apiRegistryClient;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private ApiConfig apiConfig;

    @Mock
    private ScheduledExecutorService heartBeatExecutor;

    @Before
    public void before() {
        given(heartBeatExecutor.scheduleAtFixedRate(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class))).willReturn(null);
    }

    @Test
    public void testRegistry() {
        apiRegistryClient.init();

        given(registryClient.checkNodeExists(Mockito.anyString(), Mockito.any(NodeType.class))).willReturn(true);

        given(apiConfig.getHeartbeatInterval()).willReturn(Duration.ofSeconds(1));

        apiRegistryClient.registry();
        Mockito.verify(registryClient, Mockito.times(1)).handleDeadServer(Mockito.anyCollection(), Mockito.any(NodeType.class), Mockito.anyString());
    }
}
