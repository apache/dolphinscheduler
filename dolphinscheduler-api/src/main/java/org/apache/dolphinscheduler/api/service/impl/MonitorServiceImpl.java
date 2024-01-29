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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerServerModel;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMetrics;
import org.apache.dolphinscheduler.dao.plugin.api.monitor.DatabaseMonitor;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * monitor service impl
 */
@Service
@Slf4j
public class MonitorServiceImpl extends BaseServiceImpl implements MonitorService {

    @Autowired
    private DatabaseMonitor databaseMonitor;

    @Autowired
    private RegistryClient registryClient;

    /**
     * query database state
     *
     * @param loginUser login user
     * @return data base state
     */
    @Override
    public List<DatabaseMetrics> queryDatabaseState(User loginUser) {
        return Lists.newArrayList(databaseMonitor.getDatabaseMetrics());
    }

    /**
     * query master list
     *
     * @param loginUser login user
     * @return master information list
     */
    @Override
    public List<Server> queryMaster(User loginUser) {
        return registryClient.getServerList(RegistryNodeType.MASTER);
    }

    /**
     * query worker list
     *
     * @param loginUser login user
     * @return worker information list
     */
    @Override
    public List<WorkerServerModel> queryWorker(User loginUser) {

        return registryClient.getServerList(RegistryNodeType.WORKER)
                .stream()
                .map((Server server) -> {
                    WorkerServerModel model = new WorkerServerModel();
                    model.setId(server.getId());
                    model.setHost(server.getHost());
                    model.setPort(server.getPort());
                    model.setZkDirectories(Sets.newHashSet(server.getZkDirectory()));
                    model.setResInfo(server.getResInfo());
                    model.setCreateTime(server.getCreateTime());
                    model.setLastHeartbeatTime(server.getLastHeartbeatTime());
                    return model;
                })
                .collect(Collectors.toList());

    }

    @Override
    public List<Server> getServerListFromRegistry(boolean isMaster) {
        return isMaster
                ? registryClient.getServerList(RegistryNodeType.MASTER)
                : registryClient.getServerList(RegistryNodeType.WORKER);
    }

}
