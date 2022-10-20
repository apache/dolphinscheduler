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

package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.function.Supplier;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerHeartBeatTask extends BaseHeartBeatTask<WorkerHeartBeat> {

    private final WorkerConfig workerConfig;
    private final RegistryClient registryClient;

    private final Supplier<Integer> workerWaitingTaskCount;

    private final int processId;

    public WorkerHeartBeatTask(@NonNull WorkerConfig workerConfig,
                               @NonNull RegistryClient registryClient,
                               @NonNull Supplier<Integer> workerWaitingTaskCount) {
        super("WorkerHeartBeatTask", workerConfig.getHeartbeatInterval().toMillis());
        this.workerConfig = workerConfig;
        this.registryClient = registryClient;
        this.workerWaitingTaskCount = workerWaitingTaskCount;
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public WorkerHeartBeat getHeartBeat() {
        double loadAverage = OSUtils.loadAverage();
        double cpuUsage = OSUtils.cpuUsage();
        int maxCpuLoadAvg = workerConfig.getMaxCpuLoadAvg();
        double reservedMemory = workerConfig.getReservedMemory();
        double availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();
        int execThreads = workerConfig.getExecThreads();
        int workerWaitingTaskCount = this.workerWaitingTaskCount.get();
        int serverStatus = getServerStatus(loadAverage, maxCpuLoadAvg, availablePhysicalMemorySize, reservedMemory,
                execThreads, workerWaitingTaskCount);

        return WorkerHeartBeat.builder()
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .cpuUsage(cpuUsage)
                .loadAverage(loadAverage)
                .availablePhysicalMemorySize(availablePhysicalMemorySize)
                .maxCpuloadAvg(maxCpuLoadAvg)
                .memoryUsage(OSUtils.memoryUsage())
                .reservedMemory(reservedMemory)
                .diskAvailable(OSUtils.diskAvailable())
                .processId(processId)
                .workerHostWeight(workerConfig.getHostWeight())
                .workerWaitingTaskCount(this.workerWaitingTaskCount.get())
                .workerExecThreadCount(workerConfig.getExecThreads())
                .serverStatus(serverStatus)
                .build();
    }

    @Override
    public void writeHeartBeat(WorkerHeartBeat workerHeartBeat) {
        String workerHeartBeatJson = JSONUtils.toJsonString(workerHeartBeat);
        String workerRegistryPath = workerConfig.getWorkerRegistryPath();
        registryClient.persistEphemeral(workerRegistryPath, workerHeartBeatJson);
        log.info(
                "Success write worker group heartBeatInfo into registry, workerRegistryPath: {} workerHeartBeatInfo: {}",
                workerRegistryPath, workerHeartBeatJson);
    }

    public int getServerStatus(double loadAverage,
                               double maxCpuloadAvg,
                               double availablePhysicalMemorySize,
                               double reservedMemory,
                               int workerExecThreadCount,
                               int workerWaitingTaskCount) {
        if (loadAverage > maxCpuloadAvg || availablePhysicalMemorySize < reservedMemory) {
            log.warn(
                    "current cpu load average {} is too high or available memory {}G is too low, under max.cpuload.avg={} and reserved.memory={}G",
                    loadAverage, availablePhysicalMemorySize, maxCpuloadAvg, reservedMemory);
            return Constants.ABNORMAL_NODE_STATUS;
        } else if (workerWaitingTaskCount > workerExecThreadCount) {
            log.warn("current waiting task count {} is large than worker thread count {}, worker is busy",
                    workerWaitingTaskCount, workerExecThreadCount);
            return Constants.BUSY_NODE_STATUE;
        } else {
            return Constants.NORMAL_NODE_STATUS;
        }
    }
}
