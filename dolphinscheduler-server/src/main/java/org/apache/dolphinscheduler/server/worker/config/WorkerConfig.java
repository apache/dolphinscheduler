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
package org.apache.dolphinscheduler.server.worker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorkerConfig {

    @Value("${worker.exec.threads:100}")
    private int workerExecThreads;

    @Value("${worker.heartbeat.interval:10}")
    private int workerHeartbeatInterval;

    @Value("${worker.fetch.task.num:3}")
    private int workerFetchTaskNum;

    @Value("${worker.max.cpuload.avg:10}")
    private int workerMaxCpuloadAvg;

    @Value("${master.reserved.memory:1}")
    private double workerReservedMemory;

    public int getWorkerExecThreads() {
        return workerExecThreads;
    }

    public void setWorkerExecThreads(int workerExecThreads) {
        this.workerExecThreads = workerExecThreads;
    }

    public int getWorkerHeartbeatInterval() {
        return workerHeartbeatInterval;
    }

    public void setWorkerHeartbeatInterval(int workerHeartbeatInterval) {
        this.workerHeartbeatInterval = workerHeartbeatInterval;
    }

    public int getWorkerFetchTaskNum() {
        return workerFetchTaskNum;
    }

    public void setWorkerFetchTaskNum(int workerFetchTaskNum) {
        this.workerFetchTaskNum = workerFetchTaskNum;
    }

    public double getWorkerReservedMemory() {
        return workerReservedMemory;
    }

    public void setWorkerReservedMemory(double workerReservedMemory) {
        this.workerReservedMemory = workerReservedMemory;
    }

    public int getWorkerMaxCpuloadAvg() {
        return workerMaxCpuloadAvg;
    }

    public void setWorkerMaxCpuloadAvg(int workerMaxCpuloadAvg) {
        this.workerMaxCpuloadAvg = workerMaxCpuloadAvg;
    }
}
