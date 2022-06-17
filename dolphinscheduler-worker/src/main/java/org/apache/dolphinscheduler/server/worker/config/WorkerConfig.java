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

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("worker")
public class WorkerConfig {
    private int listenPort;
    private int execThreads;
    private int heartbeatInterval;
    private int hostWeight;
    private boolean tenantAutoCreate;
    private boolean tenantDistributedUser;
    private int maxCpuLoadAvg;
    private double reservedMemory;
    private Set<String> groups;
    private String alertListenHost;
    private int alertListenPort;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getExecThreads() {
        return execThreads;
    }

    public void setExecThreads(int execThreads) {
        this.execThreads = execThreads;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getHostWeight() {
        return hostWeight;
    }

    public void setHostWeight(int hostWeight) {
        this.hostWeight = hostWeight;
    }

    public boolean isTenantAutoCreate() {
        return tenantAutoCreate;
    }

    public void setTenantAutoCreate(boolean tenantAutoCreate) {
        this.tenantAutoCreate = tenantAutoCreate;
    }

    public int getMaxCpuLoadAvg() {
        return maxCpuLoadAvg > 0 ? maxCpuLoadAvg : Runtime.getRuntime().availableProcessors() * 2;
    }

    public void setMaxCpuLoadAvg(int maxCpuLoadAvg) {
        this.maxCpuLoadAvg = maxCpuLoadAvg;
    }

    public double getReservedMemory() {
        return reservedMemory;
    }

    public void setReservedMemory(double reservedMemory) {
        this.reservedMemory = reservedMemory;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String getAlertListenHost() {
        return alertListenHost;
    }

    public void setAlertListenHost(String alertListenHost) {
        this.alertListenHost = alertListenHost;
    }

    public int getAlertListenPort() {
        return alertListenPort;
    }

    public void setAlertListenPort(final int alertListenPort) {
        this.alertListenPort = alertListenPort;
    }

    public boolean isTenantDistributedUser() {
        return tenantDistributedUser;
    }

    public void setTenantDistributedUser(boolean tenantDistributedUser) {
        this.tenantDistributedUser = tenantDistributedUser;
    }
}
