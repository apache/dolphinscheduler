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

package org.apache.dolphinscheduler.server.master.dispatch.host.assign;

import org.apache.dolphinscheduler.extract.base.utils.Constants;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import lombok.Data;

@Data
public class HostWeight {

    private final int THREAD_USAGE_FACTOR = 10;

    private final int CPU_USAGE_FACTOR = 20;

    private final int MEMORY_USAGE_FACTOR = 20;

    private final int DISK_USAGE_FACTOR = 50;

    private final Host host;

    private final double weight;

    // if the weight is small, then is will be chosen first
    private double currentWeight;

    public HostWeight(HostWorker hostWorker,
                      double cpuUsage,
                      double memoryUsage,
                      double diskUsage,
                      double threadPoolUsage,
                      long startTime) {
        this.host = hostWorker;
        this.weight = calculateWeight(cpuUsage, memoryUsage, diskUsage, threadPoolUsage, startTime);
        this.currentWeight = this.weight;
    }

    private double calculateWeight(double cpuUsage,
                                   double memoryUsage,
                                   double diskUsage,
                                   double threadPoolUsage,
                                   long startTime) {
        double calculatedWeight = 100 - (cpuUsage * CPU_USAGE_FACTOR + memoryUsage * MEMORY_USAGE_FACTOR
                + diskUsage * DISK_USAGE_FACTOR + threadPoolUsage * THREAD_USAGE_FACTOR);
        long uptime = System.currentTimeMillis() - startTime;
        if (uptime > 0 && uptime < Constants.WARM_UP_TIME) {
            // If the warm-up is not over, add the weight
            return calculatedWeight * Constants.WARM_UP_TIME / uptime;
        }
        return calculatedWeight;
    }

}
