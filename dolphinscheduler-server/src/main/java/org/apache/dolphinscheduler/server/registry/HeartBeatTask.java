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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatTask extends Thread {

    private final Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);

    private String startTime;
    private double reservedMemory;
    private double maxCpuloadAvg;
    private Set<String> heartBeatPaths;
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    public HeartBeatTask(String startTime,
                         double reservedMemory,
                         double maxCpuloadAvg,
                         Set<String> heartBeatPaths,
                         ZookeeperRegistryCenter zookeeperRegistryCenter) {
        this.startTime = startTime;
        this.reservedMemory = reservedMemory;
        this.maxCpuloadAvg = maxCpuloadAvg;
        this.heartBeatPaths = heartBeatPaths;
        this.zookeeperRegistryCenter = zookeeperRegistryCenter;
    }

    @Override
    public void run() {
        try {
            double availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();
            double loadAverage = OSUtils.loadAverage();

            int status = Constants.NORAML_NODE_STATUS;

            if (availablePhysicalMemorySize < reservedMemory
                    || loadAverage > maxCpuloadAvg) {
                logger.warn("load is too high or availablePhysicalMemorySize(G) is too low, it's availablePhysicalMemorySize(G):{},loadAvg:{}", availablePhysicalMemorySize, loadAverage);
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
            //save process id
            builder.append(OSUtils.getProcessID());

            for (String heartBeatPath : heartBeatPaths) {
                zookeeperRegistryCenter.getZookeeperCachedOperator().update(heartBeatPath, builder.toString());
            }
        } catch (Throwable ex) {
            logger.error("error write heartbeat info", ex);
        }
    }
}
