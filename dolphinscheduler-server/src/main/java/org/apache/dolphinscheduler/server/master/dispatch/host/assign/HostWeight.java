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

import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.remote.utils.Host;

/**
 * host weight
 */
public class HostWeight {

    private final int CPU_FACTOR = 10;

    private final int MEMORY_FACTOR = 20;

    private final int LOAD_AVERAGE_FACTOR = 70;

    private final Host host;

    private final double weight;

    private double currentWeight;

    public HostWeight(Host host, double cpu, double memory, double loadAverage) {
        this.weight = getWeight(cpu, memory, loadAverage, host);
        this.host = host;
        this.currentWeight = weight;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public double getWeight() {
        return weight;
    }

    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public Host getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "HostWeight{"
            + "host=" + host
            + ", weight=" + weight
            + ", currentWeight=" + currentWeight
            + '}';
    }

    private double getWeight(double cpu, double memory, double loadAverage, Host host) {
        double calculateWeight = cpu * CPU_FACTOR + memory * MEMORY_FACTOR + loadAverage * LOAD_AVERAGE_FACTOR;
        return getWarmUpWeight(host, calculateWeight);
    }

    /**
     * If the warm-up is not over, add the weight
     */
    private double getWarmUpWeight(Host host, double weight) {
        long startTime = host.getStartTime();
        long uptime = System.currentTimeMillis() - startTime;
        if (uptime > 0 && uptime < Constants.WARM_UP_TIME) {
            return weight * Constants.WARM_UP_TIME / uptime;
        }
        return weight;
    }
}
