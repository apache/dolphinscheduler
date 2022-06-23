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
import org.apache.dolphinscheduler.server.registry.AbstractHeartBeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterHeartBeat extends AbstractHeartBeat {

    private static final Logger logger = LoggerFactory.getLogger(MasterHeartBeat.class);

    public MasterHeartBeat(long startupTime, double maxCpuloadAvg, double reservedMemory) {
        this.startupTime = startupTime;
        this.maxCpuloadAvg = maxCpuloadAvg;
        this.reservedMemory = reservedMemory;
    }

    @Override
    public void init() {
        this.reportTime = System.currentTimeMillis();
    }

    /**
     * update server state
     */
    @Override
    public void updateServerState() {
        if (loadAverage > maxCpuloadAvg || availablePhysicalMemorySize < reservedMemory) {
            logger.warn("current cpu load average {} is too high or available memory {}G is too low, under max.cpuload.avg={} and reserved.memory={}G",
                    loadAverage, availablePhysicalMemorySize, maxCpuloadAvg, reservedMemory);
            this.serverStatus = Constants.ABNORMAL_NODE_STATUS;
        } else {
            this.serverStatus = Constants.NORMAL_NODE_STATUS;
        }
    }

    /**
     * encode heartbeat
     */
    @Override
    public String encodeHeartBeat() {
        StringBuilder builder = new StringBuilder(100);
        builder.append(cpuUsage).append(Constants.COMMA);
        builder.append(memoryUsage).append(Constants.COMMA);
        builder.append(loadAverage).append(Constants.COMMA);
        builder.append(availablePhysicalMemorySize).append(Constants.COMMA);
        builder.append(maxCpuloadAvg).append(Constants.COMMA);
        builder.append(reservedMemory).append(Constants.COMMA);
        builder.append(startupTime).append(Constants.COMMA);
        builder.append(reportTime).append(Constants.COMMA);
        builder.append(serverStatus).append(Constants.COMMA);
        builder.append(processId).append(Constants.COMMA);
        builder.append(diskAvailable);

        return builder.toString();
    }

}
