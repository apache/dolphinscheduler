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

import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.HeartBeatModel;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * HeartBeatUtilsTest
 */
public class HeartBeatUtilsTest {
    @Test
    public void testDecodeMasterHeartBeat() {
        String heartBeatInfo = "0.35,0.58,3.09,6.47,5.0,1.0,1634033006749,1634033006857,1,29732,65.86";
        HeartBeatModel heartBeat = HeartBeatUtils.decodeMasterHeartBeat(heartBeatInfo);

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
        assertEquals(65.86, heartBeat.getDiskAvailable(), delta);

        assertEquals(0, heartBeat.getWorkerHostWeight());
        assertEquals(0, heartBeat.getWorkerExecThreadCount());
        assertEquals(0, heartBeat.getWorkerWaitingTaskCount());

        String errorHeartBeatInfo = heartBeatInfo + ",0";
        HeartBeatModel errorHeartBeat = HeartBeatUtils.decodeMasterHeartBeat(errorHeartBeatInfo);
        assertNull(errorHeartBeat);
    }

    @Test
    public void testDecodeWorkerHeartBeat() {
        String heartBeatInfo = "0.35,0.58,3.09,6.47,5.0,1.0,1634033006749,1634033006857,1,29732,1,199,200,65.86";
        HeartBeatModel heartBeat = HeartBeatUtils.decodeWorkerHeartBeat(heartBeatInfo);

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
        assertEquals(1, heartBeat.getWorkerHostWeight());
        assertEquals(199, heartBeat.getWorkerExecThreadCount());
        assertEquals(200, heartBeat.getWorkerWaitingTaskCount());
        assertEquals(65.86, heartBeat.getDiskAvailable(), delta);

        String errorHeartBeatInfo = heartBeatInfo + ",0";
        HeartBeatModel errorHeartBeat = HeartBeatUtils.decodeWorkerHeartBeat(errorHeartBeatInfo);
        assertNull(errorHeartBeat);
    }

    @Test
    public void testDecodeHeartBeat() {

        String masterHeartBeatInfo = "0.35,0.58,3.09,6.47,5.0,1.0,1634033006749,1634033006857,1,29732,65.86";
        HeartBeatModel masterHeartBeat = HeartBeatUtils.decodeHeartBeat(masterHeartBeatInfo, NodeType.MASTER);
        assertNotNull(masterHeartBeat);

        String workerHeartBeatInfo = "0.35,0.58,3.09,6.47,5.0,1.0,1634033006749,1634033006857,1,29732,1,199,200,65.86";
        HeartBeatModel workerHeartBeat = HeartBeatUtils.decodeHeartBeat(workerHeartBeatInfo, NodeType.WORKER);
        assertNotNull(workerHeartBeat);

        try {
            HeartBeatModel errorHeartBeat = HeartBeatUtils.decodeHeartBeat(workerHeartBeatInfo, NodeType.DEAD_SERVER);
        } catch (IllegalStateException e) {
            Assert.assertThat(e.getMessage(), is("Should not reach here"));
        }
    }

}