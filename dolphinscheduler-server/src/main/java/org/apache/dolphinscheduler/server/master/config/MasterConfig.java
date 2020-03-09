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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MasterConfig {

    @Value("${master.exec.threads}")
    private int masterExecThreads;

    @Value("${master.exec.task.num}")
    private int masterExecTaskNum;

    @Value("${master.heartbeat.interval}")
    private int masterHeartbeatInterval;

    @Value("${master.task.commit.retryTimes}")
    private int masterTaskCommitRetryTimes;

    @Value("${master.task.commit.interval}")
    private int masterTaskCommitInterval;

    @Value("${master.max.cpuload.avg}")
    private double masterMaxCpuloadAvg;

    @Value("${master.reserved.memory}")
    private double masterReservedMemory;

    @Value("${master.host.selector:lowerWeight}")
    private String hostSelector;

    @Value("${master.listen.port:5678}")
    private int listenPort;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public String getHostSelector() {
        return hostSelector;
    }

    public void setHostSelector(String hostSelector) {
        this.hostSelector = hostSelector;
    }

    public int getMasterExecThreads() {
        return masterExecThreads;
    }

    public void setMasterExecThreads(int masterExecThreads) {
        this.masterExecThreads = masterExecThreads;
    }

    public int getMasterExecTaskNum() {
        return masterExecTaskNum;
    }

    public void setMasterExecTaskNum(int masterExecTaskNum) {
        this.masterExecTaskNum = masterExecTaskNum;
    }

    public int getMasterHeartbeatInterval() {
        return masterHeartbeatInterval;
    }

    public void setMasterHeartbeatInterval(int masterHeartbeatInterval) {
        this.masterHeartbeatInterval = masterHeartbeatInterval;
    }

    public int getMasterTaskCommitRetryTimes() {
        return masterTaskCommitRetryTimes;
    }

    public void setMasterTaskCommitRetryTimes(int masterTaskCommitRetryTimes) {
        this.masterTaskCommitRetryTimes = masterTaskCommitRetryTimes;
    }

    public int getMasterTaskCommitInterval() {
        return masterTaskCommitInterval;
    }

    public void setMasterTaskCommitInterval(int masterTaskCommitInterval) {
        this.masterTaskCommitInterval = masterTaskCommitInterval;
    }

    public double getMasterMaxCpuloadAvg() {
        return masterMaxCpuloadAvg;
    }

    public void setMasterMaxCpuloadAvg(double masterMaxCpuloadAvg) {
        this.masterMaxCpuloadAvg = masterMaxCpuloadAvg;
    }

    public double getMasterReservedMemory() {
        return masterReservedMemory;
    }

    public void setMasterReservedMemory(double masterReservedMemory) {
        this.masterReservedMemory = masterReservedMemory;
    }
}
