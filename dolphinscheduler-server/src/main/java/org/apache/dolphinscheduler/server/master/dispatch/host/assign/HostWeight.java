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

import org.apache.dolphinscheduler.remote.utils.Host;

/**
 * host weight
 */
public class HostWeight {

    private final int CPU_FACTOR = 10;

    private final int MEMORY_FACTOR = 20;

    private final int LOAD_AVERAGE_FACTOR = 70;

    private final Host host;

    private final int weight;

    private int currentWeight;

    public HostWeight(Host host, double cpu, double memory, double loadAverage) {
        this.weight = calculateWeight(cpu, memory, loadAverage);
        this.host = host ;
        this.currentWeight = weight ;
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public int getWeight() {
        return weight;
    }

    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }

    public Host getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "HostWeight{" +
                "host=" + host +
                ", weight=" + weight +
                ", currentWeight=" + currentWeight +
                '}';
    }

    private int calculateWeight(double cpu, double memory, double loadAverage){
        return (int)(cpu * CPU_FACTOR + memory * MEMORY_FACTOR + loadAverage * LOAD_AVERAGE_FACTOR);
    }
}
