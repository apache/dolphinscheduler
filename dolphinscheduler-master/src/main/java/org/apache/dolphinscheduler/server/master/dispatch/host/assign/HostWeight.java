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

/**
 * host weight
 */
public class HostWeight {

    private final int CPU_FACTOR = 10;

    private final int MEMORY_FACTOR = 20;

    private final int LOAD_AVERAGE_FACTOR = 70;

    private final HostWorker hostWorker;

    private final double weight;

    private double currentWeight;

    private final int waitingTaskCount;

    public HostWeight(HostWorker hostWorker, double cpu, double memory, double loadAverage, int waitingTaskCount,
                      long startTime) {
        this.hostWorker = hostWorker;
        this.weight = calculateWeight(cpu, memory, loadAverage, startTime);
        this.currentWeight = this.weight;
        this.waitingTaskCount = waitingTaskCount;
    }

    public double getWeight() {
        return weight;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public HostWorker getHostWorker() {
        return hostWorker;
    }

    public Host getHost() {
        return (Host) hostWorker;
    }

    public int getWaitingTaskCount() {
        return waitingTaskCount;
    }

    @Override
    public String toString() {
        return "HostWeight{"
                + "hostWorker=" + hostWorker
                + ", weight=" + weight
                + ", currentWeight=" + currentWeight
                + ", waitingTaskCount=" + waitingTaskCount
                + '}';
    }

    private double calculateWeight(double cpu, double memory, double loadAverage, long startTime) {
        double calculatedWeight = cpu * CPU_FACTOR + memory * MEMORY_FACTOR + loadAverage * LOAD_AVERAGE_FACTOR;
        long uptime = System.currentTimeMillis() - startTime;
        if (uptime > 0 && uptime < Constants.WARM_UP_TIME) {
            // If the warm-up is not over, add the weight
            return calculatedWeight * Constants.WARM_UP_TIME / uptime;
        }
        return calculatedWeight;
    }

}
