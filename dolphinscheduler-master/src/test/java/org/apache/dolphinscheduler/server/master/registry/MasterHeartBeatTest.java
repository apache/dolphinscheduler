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

import org.apache.dolphinscheduler.common.Constants;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * MasterHeartBeatTest
 */
public class MasterHeartBeatTest {

    @Test
    public void testAbnormalStateOfLoadAverage() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = -1 * Double.MAX_VALUE;
        double reservedMemory = -1 * Double.MAX_VALUE;
        MasterHeartBeat heartBeat = new MasterHeartBeat(startupTime, loadAverage, reservedMemory);

        heartBeat.init();
        heartBeat.fillSystemInfo();
        heartBeat.updateServerState();
        assertEquals(Constants.ABNORMAL_NODE_STATUS, heartBeat.getServerStatus());
    }

    @Test
    public void testAbnormalStateOfReservedMemory() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = Double.MAX_VALUE;
        double reservedMemory = Double.MAX_VALUE;
        MasterHeartBeat heartBeat = new MasterHeartBeat(startupTime, loadAverage, reservedMemory);

        heartBeat.init();
        heartBeat.fillSystemInfo();
        heartBeat.updateServerState();
        assertEquals(Constants.ABNORMAL_NODE_STATUS, heartBeat.getServerStatus());
    }

    @Test
    public void testNormalState() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = Double.MAX_VALUE;
        double reservedMemory = -1 * Double.MAX_VALUE;
        MasterHeartBeat heartBeat = new MasterHeartBeat(startupTime, loadAverage, reservedMemory);

        heartBeat.init();
        heartBeat.fillSystemInfo();
        heartBeat.updateServerState();
        assertEquals(Constants.NORMAL_NODE_STATUS, heartBeat.getServerStatus());
    }

    @Test
    public void testGetRealTimeHeartBeatInfo() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = 100;
        double reservedMemory = 100;
        MasterHeartBeat heartBeat = new MasterHeartBeat(startupTime, loadAverage, reservedMemory);

        String realTimeHeartBeatInfo = heartBeat.getRealTimeHeartBeatInfo();
        assertNotNull(realTimeHeartBeatInfo);

        assertNotEquals(0.0, heartBeat.getCpuUsage());
        assertNotEquals(0.0, heartBeat.getMemoryUsage());
        assertNotEquals(0.0, heartBeat.getLoadAverage());
        assertNotEquals(0.0, heartBeat.getAvailablePhysicalMemorySize());
        assertNotEquals(0.0, heartBeat.getMaxCpuloadAvg());
        assertNotEquals(0.0, heartBeat.getReservedMemory());
        assertNotEquals(0L, heartBeat.getStartupTime());
        assertNotEquals(0L, heartBeat.getReportTime());
        assertNotEquals(-1, heartBeat.getServerStatus());
        assertNotEquals(-1, heartBeat.getProcessId());
        assertNotEquals(-1.0, heartBeat.getDiskAvailable());
    }

}
