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

import org.apache.dolphinscheduler.common.Constants;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "worker.properties")
public class WorkerConfig {

    @Value("${worker.listen.port:1234}")
    private int listenPort;

    @Value("${worker.exec.threads:100}")
    private int workerExecThreads;

    @Value("${worker.heartbeat.interval:10}")
    private int workerHeartbeatInterval;

    @Value("${worker.host.weight:100}")
    private int hostWeight;

    @Value("${worker.tenant.auto.create:false}")
    private boolean workerTenantAutoCreate;

    @Value("${worker.max.cpuload.avg:-1}")
    private int workerMaxCpuloadAvg;

    @Value("${worker.reserved.memory:0.3}")
    private double workerReservedMemory;

    @Value("#{'${worker.groups:default}'.split(',')}")
    private Set<String> workerGroups;

    @Value("${alert.listen.host:localhost}")
    private String alertListenHost;

    @Value("${task.plugin.dir:}")
    private String taskPluginDir;

    @Value("${maven.local.repository:}")
    private String mavenLocalRepository;

    @Value("${task.plugin.binding:}")
    private String taskPluginBinding;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public Set<String> getWorkerGroups() {
        return workerGroups;
    }

    public void setWorkerGroups(Set<String> workerGroups) {
        this.workerGroups = workerGroups;
    }

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

    public boolean getWorkerTenantAutoCreate() {
        return workerTenantAutoCreate;
    }

    public void setWorkerTenantAutoCreate(boolean workerTenantAutoCreate) {
        this.workerTenantAutoCreate = workerTenantAutoCreate;
    }

    public double getWorkerReservedMemory() {
        return workerReservedMemory;
    }

    public void setWorkerReservedMemory(double workerReservedMemory) {
        this.workerReservedMemory = workerReservedMemory;
    }

    public int getWorkerMaxCpuloadAvg() {
        if (workerMaxCpuloadAvg == -1) {
            return Constants.DEFAULT_WORKER_CPU_LOAD;
        }
        return workerMaxCpuloadAvg;
    }

    public void setWorkerMaxCpuloadAvg(int workerMaxCpuloadAvg) {
        this.workerMaxCpuloadAvg = workerMaxCpuloadAvg;
    }

    public int getHostWeight() {
        return hostWeight;
    }

    public void setHostWeight(int hostWeight) {
        this.hostWeight = hostWeight;
    }

    public String getAlertListenHost() {
        return alertListenHost;
    }

    public void setAlertListenHost(String alertListenHost) {
        this.alertListenHost = alertListenHost;
    }

    public String getTaskPluginDir() {
        return taskPluginDir;
    }

    public void setTaskPluginDir(String taskPluginDir) {
        this.taskPluginDir = taskPluginDir;
    }

    public String getMavenLocalRepository() {
        return mavenLocalRepository;
    }

    public void setMavenLocalRepository(String mavenLocalRepository) {
        this.mavenLocalRepository = mavenLocalRepository;
    }

    public String getTaskPluginBinding() {
        return taskPluginBinding;
    }

    public void setTaskPluginBinding(String taskPluginBinding) {
        this.taskPluginBinding = taskPluginBinding;
    }
}