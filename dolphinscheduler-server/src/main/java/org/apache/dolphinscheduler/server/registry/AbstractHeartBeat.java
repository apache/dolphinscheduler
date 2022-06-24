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

import lombok.Getter;
import lombok.Setter;
import org.apache.dolphinscheduler.common.utils.OSUtils;

@Getter
@Setter
public abstract class AbstractHeartBeat {

    protected long startupTime;
    protected long reportTime;
    protected double cpuUsage;
    protected double memoryUsage;
    protected double loadAverage;
    protected double availablePhysicalMemorySize;
    protected double maxCpuloadAvg;
    protected double reservedMemory;
    protected int serverStatus;
    protected int processId;
    protected double diskAvailable;

    /**
     * init
     */
    public abstract void init();

    /**
     * updateServerState
     */
    public abstract void updateServerState();

    /**
     * encode HeartBeat
     *
     * @return
     */
    public abstract String encodeHeartBeat();

    /**
     * get real heartbeat info
     *
     * @return
     */
    public String getRealTimeHeartBeatInfo() {
        this.init();
        this.fillSystemInfo();
        this.updateServerState();
        return this.encodeHeartBeat();
    }

    /**
     * fill system info
     */
    public void fillSystemInfo() {
        this.cpuUsage = OSUtils.cpuUsage();
        this.loadAverage = OSUtils.loadAverage();
        this.availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();
        this.memoryUsage = OSUtils.memoryUsage();
        this.diskAvailable = OSUtils.diskAvailable();
        this.processId = OSUtils.getProcessID();
    }

}
