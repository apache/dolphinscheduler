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

import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostSelector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("master")
public class MasterConfig {
    private int listenPort;
    private int fetchCommandNum;
    private int preExecThreads;
    private int execThreads;
    private int dispatchTaskNumber;
    private HostSelector hostSelector;
    private int heartbeatInterval;
    private int taskCommitRetryTimes;
    private int taskCommitInterval;
    private int stateWheelInterval;
    private double maxCpuLoadAvg;
    private double reservedMemory;
    private int failoverInterval;
    private boolean killYarnJobWhenTaskFailover;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getFetchCommandNum() {
        return fetchCommandNum;
    }

    public void setFetchCommandNum(int fetchCommandNum) {
        this.fetchCommandNum = fetchCommandNum;
    }

    public int getPreExecThreads() {
        return preExecThreads;
    }

    public void setPreExecThreads(int preExecThreads) {
        this.preExecThreads = preExecThreads;
    }

    public int getExecThreads() {
        return execThreads;
    }

    public void setExecThreads(int execThreads) {
        this.execThreads = execThreads;
    }

    public int getDispatchTaskNumber() {
        return dispatchTaskNumber;
    }

    public void setDispatchTaskNumber(int dispatchTaskNumber) {
        this.dispatchTaskNumber = dispatchTaskNumber;
    }

    public HostSelector getHostSelector() {
        return hostSelector;
    }

    public void setHostSelector(HostSelector hostSelector) {
        this.hostSelector = hostSelector;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getTaskCommitRetryTimes() {
        return taskCommitRetryTimes;
    }

    public void setTaskCommitRetryTimes(int taskCommitRetryTimes) {
        this.taskCommitRetryTimes = taskCommitRetryTimes;
    }

    public int getTaskCommitInterval() {
        return taskCommitInterval;
    }

    public void setTaskCommitInterval(int taskCommitInterval) {
        this.taskCommitInterval = taskCommitInterval;
    }

    public int getStateWheelInterval() {
        return stateWheelInterval;
    }

    public void setStateWheelInterval(int stateWheelInterval) {
        this.stateWheelInterval = stateWheelInterval;
    }

    public double getMaxCpuLoadAvg() {
        return maxCpuLoadAvg > 0 ? maxCpuLoadAvg : Runtime.getRuntime().availableProcessors() * 2;
    }

    public void setMaxCpuLoadAvg(double maxCpuLoadAvg) {
        this.maxCpuLoadAvg = maxCpuLoadAvg;
    }

    public double getReservedMemory() {
        return reservedMemory;
    }

    public void setReservedMemory(double reservedMemory) {
        this.reservedMemory = reservedMemory;
    }

    public int getFailoverInterval() {
        return failoverInterval;
    }

    public void setFailoverInterval(int failoverInterval) {
        this.failoverInterval = failoverInterval;
    }

    public boolean isKillYarnJobWhenTaskFailover() {
        return killYarnJobWhenTaskFailover;
    }

    public void setKillYarnJobWhenTaskFailover(boolean killYarnJobWhenTaskFailover) {
        this.killYarnJobWhenTaskFailover = killYarnJobWhenTaskFailover;
    }
}
