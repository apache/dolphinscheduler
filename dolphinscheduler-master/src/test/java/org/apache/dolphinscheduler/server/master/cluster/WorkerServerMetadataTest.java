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

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class WorkerServerMetadataTest {

    @Test
    void parseFromHeartBeat() {
        WorkerHeartBeat workerHeartBeat = WorkerHeartBeat.builder()
                .processId(30001)
                .startupTime(System.currentTimeMillis())
                .reportTime(System.currentTimeMillis())
                .jvmCpuUsage(0.1)
                .cpuUsage(0.2)
                .jvmMemoryUsage(0.3)
                .memoryUsage(0.4)
                .diskUsage(0.5)
                .serverStatus(ServerStatus.NORMAL)
                .host("localhost")
                .port(12345)
                .workerHostWeight(2)
                .threadPoolUsage(0.6)
                .build();
        WorkerServerMetadata workerServerMetadata = WorkerServerMetadata.parseFromHeartBeat(workerHeartBeat);
        Truth.assertThat(workerServerMetadata.getCpuUsage()).isEqualTo(0.2);
        Truth.assertThat(workerServerMetadata.getMemoryUsage()).isEqualTo(0.4);
        Truth.assertThat(workerServerMetadata.getServerStatus()).isEqualTo(ServerStatus.NORMAL);
        Truth.assertThat(workerServerMetadata.getAddress()).isEqualTo("localhost:12345");
        Truth.assertThat(workerServerMetadata.getWorkerWeight()).isEqualTo(2);
        Truth.assertThat(workerServerMetadata.getTaskThreadPoolUsage()).isEqualTo(0.6);
    }
}
