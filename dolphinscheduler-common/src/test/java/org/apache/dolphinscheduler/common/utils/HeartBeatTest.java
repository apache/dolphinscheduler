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

package org.apache.dolphinscheduler.common.utils;

import static org.junit.Assert.assertEquals;

import org.apache.dolphinscheduler.common.Constants;

import org.junit.Test;

/**
 * NetUtilsTest
 */
public class HeartBeatTest {

    @Test
    public void testAbnormalState() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = 100;
        double reservedMemory = 100;
        HeartBeat heartBeat = new HeartBeat(startupTime, loadAverage, reservedMemory);
        heartBeat.updateServerState();
        assertEquals(Constants.ABNORMAL_NODE_STATUS, heartBeat.getServerStatus());
    }

    @Test
    public void testBusyState() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = 0;
        double reservedMemory = 0;
        int hostWeight = 1;
        int taskCount = 200;
        int workerThreadCount = 199;
        HeartBeat heartBeat = new HeartBeat(startupTime, loadAverage, reservedMemory, hostWeight, workerThreadCount);

        heartBeat.setWorkerWaitingTaskCount(taskCount);
        heartBeat.updateServerState();
        assertEquals(Constants.BUSY_NODE_STATUE, heartBeat.getServerStatus());
    }

    @Test
    public void testDecodeHeartBeat() throws Exception {
        String heartBeatInfo = "0.35,0.58,3.09,6.47,5.0,1.0,1634033006749,1634033006857,1,29732,1,199,200";
        HeartBeat heartBeat = HeartBeat.decodeHeartBeat(heartBeatInfo);

        double delta = 0.001;
        assertEquals(0.35, heartBeat.getCpuUsage(), delta);
        assertEquals(0.58, heartBeat.getMemoryUsage(), delta);
        assertEquals(3.09, heartBeat.getLoadAverage(), delta);
        assertEquals(6.47, heartBeat.getAvailablePhysicalMemorySize(), delta);
        assertEquals(5.0, heartBeat.getMaxCpuloadAvg(), delta);
        assertEquals(1.0, heartBeat.getReservedMemory(), delta);
        assertEquals(1634033006749L, heartBeat.getStartupTime());
        assertEquals(1634033006857L, heartBeat.getReportTime());
        assertEquals(1, heartBeat.getServerStatus());
        assertEquals(29732, heartBeat.getProcessId());
        assertEquals(199, heartBeat.getWorkerExecThreadCount());
        assertEquals(200, heartBeat.getWorkerWaitingTaskCount());
    }

}
