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

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MasterSlotManagerTest {

    @InjectMocks
    private MasterSlotManager masterSlotManager = Mockito.spy(new MasterSlotManager());

    @Mock
    private MasterConfig masterConfig;

    @Test
    void testNormalMasterSlots() {
        // on normal Master side
        Mockito.when(masterConfig.getMasterAddress()).thenReturn("127.0.0.1:7777");

        sendHeartBeat(ServerStatus.ABNORMAL, ServerStatus.NORMAL);
        Assertions.assertEquals(1, masterSlotManager.getMasterSize());
        Assertions.assertEquals(0, masterSlotManager.getSlot());

        sendHeartBeat(ServerStatus.NORMAL, ServerStatus.NORMAL);
        Assertions.assertEquals(2, masterSlotManager.getMasterSize());
        Assertions.assertEquals(1, masterSlotManager.getSlot());
    }

    @Test
    void testOverloadMasterSlots() {
        // on abnormal Master side
        Mockito.when(masterConfig.getMasterAddress()).thenReturn("127.0.0.1:6666");

        sendHeartBeat(ServerStatus.ABNORMAL, ServerStatus.NORMAL);
        Assertions.assertEquals(0, masterSlotManager.getMasterSize());
        Assertions.assertEquals(0, masterSlotManager.getSlot());

        sendHeartBeat(ServerStatus.NORMAL, ServerStatus.NORMAL);
        Assertions.assertEquals(2, masterSlotManager.getMasterSize());
        Assertions.assertEquals(0, masterSlotManager.getSlot());
    }

    public void sendHeartBeat(ServerStatus serverStatus1, ServerStatus serverStatus2) {
        MasterSlotManager.SlotChangeListener slotChangeListener = masterSlotManager.new SlotChangeListener();

        Map<String, MasterHeartBeat> masterNodeInfo = new HashMap<>();
        // generate heartbeat
        MasterHeartBeat masterHeartBeat1 = MasterHeartBeat.builder()
                .startupTime(System.currentTimeMillis())
                .serverStatus(serverStatus1)
                .host("127.0.0.1")
                .port(6666)
                .build();
        MasterHeartBeat masterHeartBeat2 = MasterHeartBeat.builder()
                .startupTime(System.currentTimeMillis())
                .serverStatus(serverStatus2)
                .host("127.0.0.1")
                .port(7777)
                .build();
        masterNodeInfo.put("127.0.0.1:6666", masterHeartBeat1);
        masterNodeInfo.put("127.0.0.1:7777", masterHeartBeat2);

        slotChangeListener.notify(masterNodeInfo);
    }
}
