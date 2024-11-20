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

package org.apache.dolphinscheduler.server.master.cluster;

import org.apache.dolphinscheduler.common.model.MasterHeartBeat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MasterServerMetadata extends BaseServerMetadata implements Comparable<MasterServerMetadata> {

    public static MasterServerMetadata parseFromHeartBeat(final MasterHeartBeat masterHeartBeat) {
        return MasterServerMetadata.builder()
                .serverStartupTime(masterHeartBeat.getStartupTime())
                .address(masterHeartBeat.getHost() + ":" + masterHeartBeat.getPort())
                .cpuUsage(masterHeartBeat.getCpuUsage())
                .memoryUsage(masterHeartBeat.getMemoryUsage())
                .serverStatus(masterHeartBeat.getServerStatus())
                .build();
    }

    // Use the master address to sort the master server
    @Override
    public int compareTo(final MasterServerMetadata o) {
        return this.getAddress().compareTo(o.getAddress());
    }

}
