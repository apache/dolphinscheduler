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

import static org.apache.dolphinscheduler.remote.utils.Constants.COMMA;

import java.util.Date;
import java.util.Set;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heart beat task
 */
public class HeartBeatTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);

    private String startTime;
    private double maxCpuloadAvg;
    private double reservedMemory;
    private int hostWeight; // worker host weight
    private Set<String> heartBeatPaths;
    private String serverType;
    private RegistryClient registryClient;

    public HeartBeatTask(String startTime,
                         double maxCpuloadAvg,
                         double reservedMemory,
                         Set<String> heartBeatPaths,
                         String serverType,
                         RegistryClient registryClient) {
        this.startTime = startTime;
        this.maxCpuloadAvg = maxCpuloadAvg;
        this.reservedMemory = reservedMemory;
        this.heartBeatPaths = heartBeatPaths;
        this.serverType = serverType;
        this.registryClient = registryClient;
    }

    public HeartBeatTask(String startTime,
                         double maxCpuloadAvg,
                         double reservedMemory,
                         int hostWeight,
                         Set<String> heartBeatPaths,
                         String serverType,
                         RegistryClient registryClient) {
        this.startTime = startTime;
        this.maxCpuloadAvg = maxCpuloadAvg;
        this.reservedMemory = reservedMemory;
        this.hostWeight = hostWeight;
        this.heartBeatPaths = heartBeatPaths;
        this.serverType = serverType;
        this.registryClient = registryClient;
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

            double loadAverage = OSUtils.loadAverage();
            double availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();
            int status = Constants.NORMAL_NODE_STATUS;
            if (loadAverage > maxCpuloadAvg || availablePhysicalMemorySize < reservedMemory) {
                logger.warn("current cpu load average {} is too high or available memory {}G is too low, under max.cpuload.avg={} and reserved.memory={}G",
                        loadAverage, availablePhysicalMemorySize, maxCpuloadAvg, reservedMemory);
                status = Constants.ABNORMAL_NODE_STATUS;
            }

            StringBuilder builder = new StringBuilder(100);
            builder.append(OSUtils.cpuUsage()).append(COMMA);
            builder.append(OSUtils.memoryUsage()).append(COMMA);
            builder.append(OSUtils.loadAverage()).append(COMMA);
            builder.append(OSUtils.availablePhysicalMemorySize()).append(Constants.COMMA);
            builder.append(maxCpuloadAvg).append(Constants.COMMA);
            builder.append(reservedMemory).append(Constants.COMMA);
            builder.append(startTime).append(Constants.COMMA);
            builder.append(DateUtils.dateToString(new Date())).append(Constants.COMMA);
            builder.append(status).append(COMMA);
            // save process id
            builder.append(OSUtils.getProcessID());
            // worker host weight
            if (Constants.WORKER_TYPE.equals(serverType)) {
                builder.append(Constants.COMMA).append(hostWeight);
            }

            for (String heartBeatPath : heartBeatPaths) {
                registryClient.update(heartBeatPath, builder.toString());
            }
        } catch (Throwable ex) {
            logger.error("error write heartbeat info", ex);
        }
    }

}
