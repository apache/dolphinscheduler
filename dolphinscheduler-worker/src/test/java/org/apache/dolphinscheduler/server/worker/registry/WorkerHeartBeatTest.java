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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * WorkerHeartBeatTest
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkerHeartBeatTest {

    @Mock
    private WorkerManagerThread workerManagerThread;

    @Test
    public void testAbnormalState() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = 100;
        double reservedMemory = 100;
        int hostWeight = 1;
        int workerThreadCount = 199;
        WorkerHeartBeat heartBeat = new WorkerHeartBeat(startupTime, loadAverage, reservedMemory, hostWeight, workerThreadCount, workerManagerThread);

        heartBeat.init();
        heartBeat.fillSystemInfo();
        heartBeat.updateServerState();
        assertEquals(Constants.ABNORMAL_NODE_STATUS, heartBeat.getServerStatus());
    }

    @Test
    public void testBusyState() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = 0;
        double reservedMemory = 0;
        int hostWeight = 1;
        int workerThreadCount = 199;
        WorkerHeartBeat heartBeat = new WorkerHeartBeat(startupTime, loadAverage, reservedMemory, hostWeight, workerThreadCount, workerManagerThread);

        heartBeat.init();
        heartBeat.setWorkerWaitingTaskCount(200);
        heartBeat.fillSystemInfo();
        heartBeat.updateServerState();
        assertEquals(Constants.BUSY_NODE_STATUE, heartBeat.getServerStatus());
    }

    @Test
    public void testNormalState() {
        long startupTime = System.currentTimeMillis();
        double loadAverage = 0;
        double reservedMemory = 0;
        int hostWeight = 1;
        int workerThreadCount = 199;
        WorkerHeartBeat heartBeat = new WorkerHeartBeat(startupTime, loadAverage, reservedMemory, hostWeight, workerThreadCount, workerManagerThread);

        heartBeat.init();
        heartBeat.setWorkerWaitingTaskCount(198);
        heartBeat.fillSystemInfo();
        heartBeat.updateServerState();
        assertEquals(Constants.NORMAL_NODE_STATUS, heartBeat.getServerStatus());
    }

}
