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

package org.apache.dolphinscheduler.common.model;

public class HeartBeatModel {

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
    private double diskAvailable;
    private int workerHostWeight;
    private int workerWaitingTaskCount;
    private int workerExecThreadCount;

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

    public double getDiskAvailable() {
        return diskAvailable;
    }

    public void setDiskAvailable(double diskAvailable) {
        this.diskAvailable = diskAvailable;
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
}
