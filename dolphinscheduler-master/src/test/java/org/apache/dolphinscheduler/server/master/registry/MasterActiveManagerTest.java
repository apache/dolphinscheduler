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

import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.server.master.config.MasterActiveConfig;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MasterActiveManagerTest {

    @Test
    void testSlotListener() {
        // mock
        MasterConfig masterConfig = Mockito.mock(MasterConfig.class);
        RegistryClient registryClient = Mockito.mock(RegistryClient.class);

        // when
        MasterActiveConfig masterActiveConfig = new MasterActiveConfig();
        masterActiveConfig.setStrategy(MasterActiveConfig.MasterActiveStrategy.RESOURCE);
        Mockito.when(masterConfig.getMasterActiveConfig()).thenReturn(masterActiveConfig);
        Mockito.when(masterConfig.getMasterAddress()).thenReturn("127.0.0.1:5680");

        // new
        MasterActiveManager masterActiveManager = new MasterActiveManager(masterConfig, registryClient);
        MasterActiveManager.SlotListener slotListener = masterActiveManager.new SlotListener();

        // add
        Event event = new Event("",
                MasterActiveConfig.MasterActiveStrategy.RESOURCE.getActiveRegistryNodeType().getRegistryPath(),
                "", Event.Type.ADD);
        Server server = new Server();
        server.setPort(5680);
        server.setHost("127.0.0.1");
        Mockito.when(registryClient
                .getServerList(MasterActiveConfig.MasterActiveStrategy.RESOURCE.getActiveRegistryNodeType()))
                .thenReturn(Collections.singletonList(server));
        slotListener.notify(event);
        Assertions.assertEquals(0, masterActiveManager.getSlot());
        Assertions.assertEquals(1, masterActiveManager.getMasterSize());

        // remove
        event = new Event("",
                MasterActiveConfig.MasterActiveStrategy.RESOURCE.getActiveRegistryNodeType().getRegistryPath(),
                "", Event.Type.REMOVE);
        Mockito.when(registryClient
                .getServerList(MasterActiveConfig.MasterActiveStrategy.RESOURCE.getActiveRegistryNodeType()))
                .thenReturn(Collections.emptyList());
        slotListener.notify(event);
        Assertions.assertEquals(0, masterActiveManager.getMasterSize());
    }

    @Test
    void testResourceActiveHandler() {
        // mock
        MasterConfig masterConfig = Mockito.mock(MasterConfig.class);
        RegistryClient registryClient = Mockito.mock(RegistryClient.class);

        // when
        MasterActiveConfig masterActiveConfig = new MasterActiveConfig();
        masterActiveConfig.setStrategy(MasterActiveConfig.MasterActiveStrategy.RESOURCE);
        masterActiveConfig.setCpuLoadAvgActive(1);
        masterActiveConfig.setReservedMemoryActive(0.4);
        Mockito.when(masterConfig.getMasterActiveConfig()).thenReturn(masterActiveConfig);
        Mockito.when(masterConfig.getMaxCpuLoadAvg()).thenReturn(1.0);
        Mockito.when(masterConfig.getReservedMemory()).thenReturn(0.3);

        // new
        MasterActiveManager masterActiveManager = new MasterActiveManager(masterConfig, registryClient);
        MasterActiveManager.ResourceActiveHandler resourceActiveHandler =
                masterActiveManager.new ResourceActiveHandler();

        // inactive -> active
        MasterHeartBeat masterHeartBeat = new MasterHeartBeat();
        masterHeartBeat.setCpuUsage(0.7);
        masterHeartBeat.setMemoryUsage(0.5);
        resourceActiveHandler.checkSelfState(masterHeartBeat);
        Mockito.verify(registryClient, Mockito.times(1)).persistEphemeral(Mockito.anyString(), Mockito.anyString());

        // active -> inactive, more than high watch mark
        masterHeartBeat.setMemoryUsage(0.8);
        resourceActiveHandler.checkSelfState(masterHeartBeat);
        Mockito.verify(registryClient).remove(Mockito.anyString());

        // inactive, less than high watch mark, but greater than low watch mark, so should not call persist ephemeral
        masterHeartBeat.setMemoryUsage(0.65);
        resourceActiveHandler.checkSelfState(masterHeartBeat);
        Mockito.verify(registryClient, Mockito.times(1)).persistEphemeral(Mockito.anyString(), Mockito.anyString());

        // inactive -> active, less than low watch mark
        masterHeartBeat.setMemoryUsage(0.5);
        resourceActiveHandler.checkSelfState(masterHeartBeat);
        Mockito.verify(registryClient, Mockito.times(2)).persistEphemeral(Mockito.anyString(), Mockito.anyString());
    }
}
