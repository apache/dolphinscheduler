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

package org.apache.dolphinscheduler.server.registry;

import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.utils.HeartBeat;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Heart beat task
 */
public class HeartBeatTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);

    private final Set<String> heartBeatPaths;
    private final RegistryClient registryClient;
    private int workerWaitingTaskCount;
    private final HeartBeat heartBeat;

    private final AtomicInteger heartBeatErrorTimes = new AtomicInteger();

    public HeartBeatTask(long startupTime,
                         double maxCpuloadAvg,
                         double reservedMemory,
                         Set<String> heartBeatPaths,
                         RegistryClient registryClient) {
        this.heartBeatPaths = heartBeatPaths;
        this.registryClient = registryClient;
        this.heartBeat = new HeartBeat(startupTime, maxCpuloadAvg, reservedMemory);
    }

    public HeartBeatTask(long startupTime,
                         double maxCpuloadAvg,
                         double reservedMemory,
                         int hostWeight,
                         Set<String> heartBeatPaths,
                         RegistryClient registryClient,
                         int workerThreadCount,
                         int workerWaitingTaskCount) {
        this.heartBeatPaths = heartBeatPaths;
        this.registryClient = registryClient;
        this.workerWaitingTaskCount = workerWaitingTaskCount;
        this.heartBeat = new HeartBeat(startupTime, maxCpuloadAvg, reservedMemory, hostWeight, workerThreadCount);
    }

    public String getHeartBeatInfo() {
        return this.heartBeat.encodeHeartBeat();
    }

    @Override
    public void run() {
        try {
            if (!ServerLifeCycleManager.isRunning()) {
                return;
            }
            // update waiting task count
            heartBeat.setWorkerWaitingTaskCount(workerWaitingTaskCount);

            for (String heartBeatPath : heartBeatPaths) {
                registryClient.persistEphemeral(heartBeatPath, heartBeat.encodeHeartBeat());
            }
            heartBeatErrorTimes.set(0);
        } catch (Throwable ex) {
            logger.error("HeartBeat task execute failed, errorTimes: {}", heartBeatErrorTimes.get(), ex);
        }
    }
}
