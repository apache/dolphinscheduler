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

import org.apache.dolphinscheduler.common.utils.HeartBeat;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker heart beat listener
 */
public class WorkerHeartBeatListener implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(WorkerHeartBeatListener.class);

    private final Set<String> heartBeatPaths;
    private final RegistryClient registryClient;
    private final WorkerManagerThread workerManagerThread;
    private final String serverType;
    private final HeartBeat heartBeat;
    private final int heartBeatErrorThreshold;
    private final AtomicInteger heartBeatErrorTimes = new AtomicInteger();

    public WorkerHeartBeatListener(long startupTime,
                                   double maxCpuloadAvg,
                                   double reservedMemory,
                                   int hostWeight,
                                   Set<String> heartBeatPaths,
                                   String serverType,
                                   RegistryClient registryClient,
                                   int workerThreadCount,
                                   WorkerManagerThread workerManagerThread,
                                   int heartBeatErrorThreshold) {
        this.heartBeatPaths = heartBeatPaths;
        this.registryClient = registryClient;
        this.workerManagerThread = workerManagerThread;
        this.serverType = serverType;
        this.heartBeat = new HeartBeat(startupTime, maxCpuloadAvg, reservedMemory, hostWeight, workerThreadCount);
        this.heartBeatErrorThreshold = heartBeatErrorThreshold;
    }

    public String getHeartBeatInfo() {
        return this.heartBeat.encodeHeartBeat();
    }

    @Override
    public void run() {
        try {
            // check dead or not in zookeeper
            for (String heartBeatPath : heartBeatPaths) {
                if (registryClient.checkIsDeadServer(heartBeatPath, serverType)) {
                    registryClient.getStoppable().stop("i was judged to death, release resources and stop myself");
                    return;
                }
            }

            if (workerManagerThread != null) {
                // update waiting task count
                heartBeat.setWorkerWaitingTaskCount(workerManagerThread.getThreadPoolQueueSize());
            }

            for (String heartBeatPath : heartBeatPaths) {
                registryClient.persistEphemeral(heartBeatPath, heartBeat.encodeHeartBeat());
            }
            heartBeatErrorTimes.set(0);
        } catch (Throwable ex) {
            logger.error("HeartBeat task execute failed", ex);
            if (heartBeatErrorTimes.incrementAndGet() >= heartBeatErrorThreshold) {
                registryClient.getStoppable()
                              .stop("HeartBeat task connect to zk failed too much times: " + heartBeatErrorTimes);
            }
        }
    }
}
