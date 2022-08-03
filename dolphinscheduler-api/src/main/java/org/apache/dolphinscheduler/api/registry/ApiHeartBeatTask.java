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

import org.apache.dolphinscheduler.common.utils.HeartBeat;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiHeartBeatTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ApiHeartBeatTask.class);

    private final Set<String> heartBeatPaths;
    private final RegistryClient registryClient;
    private final String serverType;
    private final HeartBeat heartBeat;

    private final int heartBeatErrorThreshold;

    private final AtomicInteger heartBeatErrorTimes = new AtomicInteger();

    public ApiHeartBeatTask(long startupTime,
                         Set<String> heartBeatPaths,
                         String serverType,
                         RegistryClient registryClient,
                         int heartBeatErrorThreshold) {
        this.heartBeatPaths = heartBeatPaths;
        this.registryClient = registryClient;
        this.serverType = serverType;
        this.heartBeat = new HeartBeat(startupTime);
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
