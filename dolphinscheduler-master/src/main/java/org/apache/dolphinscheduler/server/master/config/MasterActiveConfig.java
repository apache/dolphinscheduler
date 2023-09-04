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

package org.apache.dolphinscheduler.server.master.config;

import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class MasterActiveConfig {

    private MasterActiveStrategy strategy = MasterActiveStrategy.REGISTER;

    private double cpuLoadAvgActive = 1;

    private double reservedMemoryActive = 0.4;

    /**
     * when cpu greater than it, the master is inactive
     */
    public double cpuHighWatchMark(MasterConfig masterConfig) {
        return masterConfig.getMaxCpuLoadAvg();
    }

    /**
     * when cpu less than it, the master is active
     */
    public double cpuLowWatchMark() {
        return cpuLoadAvgActive;
    }

    /**
     * when memory greater than it, the master is inactive
     */
    public double memoryHighWatchMark(MasterConfig masterConfig) {
        return 1 - masterConfig.getReservedMemory();
    }

    /**
     * when memory less than it, the master is active
     */
    public double memoryLowWatchMark() {
        return 1 - reservedMemoryActive;
    }

    @AllArgsConstructor
    @Getter
    public enum MasterActiveStrategy {

        /**
         * All registered masters are active
         */
        REGISTER(RegistryNodeType.MASTER),

        /**
         * Only masters with sufficient resources are active
         */
        RESOURCE(RegistryNodeType.ACTIVE),

        ;

        private final RegistryNodeType activeRegistryNodeType;
    }
}
