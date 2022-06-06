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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeat {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeat.class);

    private long startupTime;
    private long reportTime;
    private double cpuUsage;
    private double memoryUsage;
    private double loadAverage;
    private double availablePhysicalMemorySize;
    private double maxCpuloadAvg;
    private double reservedMemory;
    private int serverStatus;
    private int processId;

    private int workerHostWeight; // worker host weight
    private int workerWaitingTaskCount; // worker waiting task count
    private int workerExecThreadCount; // worker thread pool thread count

    private double diskAvailable;

    public double getDiskAvailable() {
        return diskAvailable;
    }

    public void setDiskAvailable(double diskAvailable) {
        this.diskAvailable = diskAvailable;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(long startupTime) {
        this.startupTime = startupTime;
    }

    public long getReportTime() {
        return reportTime;
    }

    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }

    public double getAvailablePhysicalMemorySize() {
        return availablePhysicalMemorySize;
    }

    public void setAvailablePhysicalMemorySize(double availablePhysicalMemorySize) {
        this.availablePhysicalMemorySize = availablePhysicalMemorySize;
    }

    public double getMaxCpuloadAvg() {
        return maxCpuloadAvg;
    }

    public void setMaxCpuloadAvg(double maxCpuloadAvg) {
        this.maxCpuloadAvg = maxCpuloadAvg;
    }

    public double getReservedMemory() {
        return reservedMemory;
    }

    public void setReservedMemory(double reservedMemory) {
        this.reservedMemory = reservedMemory;
    }

    public int getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(int serverStatus) {
        this.serverStatus = serverStatus;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getWorkerHostWeight() {
        return workerHostWeight;
    }

    public void setWorkerHostWeight(int workerHostWeight) {
        this.workerHostWeight = workerHostWeight;
    }

    public int getWorkerWaitingTaskCount() {
        return workerWaitingTaskCount;
    }

    public void setWorkerWaitingTaskCount(int workerWaitingTaskCount) {
        this.workerWaitingTaskCount = workerWaitingTaskCount;
    }

    public int getWorkerExecThreadCount() {
        return workerExecThreadCount;
    }

    public void setWorkerExecThreadCount(int workerExecThreadCount) {
        this.workerExecThreadCount = workerExecThreadCount;
    }

    public HeartBeat() {
        this.reportTime = System.currentTimeMillis();
        this.serverStatus = Constants.NORMAL_NODE_STATUS;
    }

    public HeartBeat(long startupTime, double maxCpuloadAvg, double reservedMemory) {
        this.reportTime = System.currentTimeMillis();
        this.serverStatus = Constants.NORMAL_NODE_STATUS;
        this.startupTime = startupTime;
        this.maxCpuloadAvg = maxCpuloadAvg;
        this.reservedMemory = reservedMemory;
    }

    public HeartBeat(long startupTime, double maxCpuloadAvg, double reservedMemory, int hostWeight, int workerExecThreadCount) {
        this.reportTime = System.currentTimeMillis();
        this.serverStatus = Constants.NORMAL_NODE_STATUS;
        this.startupTime = startupTime;
        this.maxCpuloadAvg = maxCpuloadAvg;
        this.reservedMemory = reservedMemory;
        this.workerHostWeight = hostWeight;
        this.workerExecThreadCount = workerExecThreadCount;
    }

    /**
     * fill system info
     */
    private void fillSystemInfo() {
        this.cpuUsage = OSUtils.cpuUsage();
        this.loadAverage = OSUtils.loadAverage();
        this.availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();
        this.memoryUsage = OSUtils.memoryUsage();
        this.diskAvailable = OSUtils.diskAvailable();
        this.processId = OSUtils.getProcessID();
    }

    /**
     * update server state
     */
    public void updateServerState() {
        this.reportTime = System.currentTimeMillis();
        if (loadAverage > maxCpuloadAvg || availablePhysicalMemorySize < reservedMemory) {
            logger.warn("current cpu load average {} is too high or available memory {}G is too low, under max.cpuload.avg={} and reserved.memory={}G",
                    loadAverage, availablePhysicalMemorySize, maxCpuloadAvg, reservedMemory);
            this.serverStatus = Constants.ABNORMAL_NODE_STATUS;
        } else if (workerWaitingTaskCount > workerExecThreadCount) {
            logger.warn("current waiting task count {} is large than worker thread count {}, worker is busy", workerWaitingTaskCount, workerExecThreadCount);
            this.serverStatus = Constants.BUSY_NODE_STATUE;
        } else {
            this.serverStatus = Constants.NORMAL_NODE_STATUS;
        }
    }

    /**
     * encode heartbeat
     */
    public String encodeHeartBeat() {
        this.fillSystemInfo();
        this.updateServerState();

        StringBuilder builder = new StringBuilder(100);
        builder.append(cpuUsage).append(Constants.COMMA);
        builder.append(memoryUsage).append(Constants.COMMA);
        builder.append(loadAverage).append(Constants.COMMA);
        builder.append(availablePhysicalMemorySize).append(Constants.COMMA);
        builder.append(maxCpuloadAvg).append(Constants.COMMA);
        builder.append(reservedMemory).append(Constants.COMMA);
        builder.append(startupTime).append(Constants.COMMA);
        builder.append(reportTime).append(Constants.COMMA);
        builder.append(serverStatus).append(Constants.COMMA);
        builder.append(processId).append(Constants.COMMA);
        builder.append(workerHostWeight).append(Constants.COMMA);
        builder.append(workerExecThreadCount).append(Constants.COMMA);
        builder.append(workerWaitingTaskCount).append(Constants.COMMA);
        builder.append(diskAvailable);

        return builder.toString();
    }

    /**
     * decode heartbeat
     */
    public static HeartBeat decodeHeartBeat(String heartBeatInfo) {
        String[] parts = heartBeatInfo.split(Constants.COMMA);
        if (parts.length != Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH) {
            return null;
        }
        HeartBeat heartBeat = new HeartBeat();
        heartBeat.cpuUsage = Double.parseDouble(parts[0]);
        heartBeat.memoryUsage = Double.parseDouble(parts[1]);
        heartBeat.loadAverage = Double.parseDouble(parts[2]);
        heartBeat.availablePhysicalMemorySize = Double.parseDouble(parts[3]);
        heartBeat.maxCpuloadAvg = Double.parseDouble(parts[4]);
        heartBeat.reservedMemory = Double.parseDouble(parts[5]);
        heartBeat.startupTime = Long.parseLong(parts[6]);
        heartBeat.reportTime = Long.parseLong(parts[7]);
        heartBeat.serverStatus = Integer.parseInt(parts[8]);
        heartBeat.processId = Integer.parseInt(parts[9]);
        heartBeat.workerHostWeight = Integer.parseInt(parts[10]);
        heartBeat.workerExecThreadCount = Integer.parseInt(parts[11]);
        heartBeat.workerWaitingTaskCount = Integer.parseInt(parts[12]);
        heartBeat.diskAvailable = Double.parseDouble(parts[13]);
        return heartBeat;
    }
}
