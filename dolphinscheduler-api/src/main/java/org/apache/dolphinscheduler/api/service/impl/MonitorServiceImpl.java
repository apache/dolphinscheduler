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

import org.apache.dolphinscheduler.api.enums.FuncPermissionEnum;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerServerModel;
import org.apache.dolphinscheduler.dao.MonitorDBDao;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.service.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

/**
 * monitor service impl
 */
@Service
public class MonitorServiceImpl extends BaseServiceImpl implements MonitorService {

    public static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    @Autowired
    private MonitorDBDao monitorDBDao;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private ResourcePermissionCheckService resourcePermissionCheckService;

    /**
     * query database state
     *
     * @param loginUser login user
     * @return data base state
     */
    @Override
    public Map<String, Object> queryDatabaseState(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        if (!operationPermissionCheck(loginUser.getId(), FuncPermissionEnum.MONITOR_DB.getKey(), logger)) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        List<MonitorRecord> monitorRecordList = monitorDBDao.queryDatabaseState();
        result.put(Constants.DATA_LIST, monitorRecordList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query master list
     *
     * @param loginUser login user
     * @return master information list
     */
    @Override
    public Map<String, Object> queryMaster(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        if (!operationPermissionCheck(loginUser.getId(), FuncPermissionEnum.MONITOR_MASTER.getKey(), logger)) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        List<Server> masterServers = getServerListFromRegistry(true);
        result.put(Constants.DATA_LIST, masterServers);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query worker list
     *
     * @param loginUser login user
     * @return worker information list
     */
    @Override
    public Map<String, Object> queryWorker(User loginUser) {

        Map<String, Object> result = new HashMap<>();

        if (!operationPermissionCheck(loginUser.getId(), FuncPermissionEnum.MONITOR_WORKER.getKey(), logger)) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        List<WorkerServerModel> workerServers = getServerListFromRegistry(false)
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

        Map<String, WorkerServerModel> workerHostPortServerMapping = workerServers
            .stream()
            .collect(Collectors.toMap(
                (WorkerServerModel worker) -> {
                    String[] s = worker.getZkDirectories().iterator().next().split("/");
                    return s[s.length - 1];
                }
                , Function.identity()
                , (WorkerServerModel oldOne, WorkerServerModel newOne) -> {
                    oldOne.getZkDirectories().addAll(newOne.getZkDirectories());
                    return oldOne;
                }));

        result.put(Constants.DATA_LIST, workerHostPortServerMapping.values());
        putMsg(result, Status.SUCCESS);

        return result;
    }

    @Override
    public List<Server> getServerListFromRegistry(boolean isMaster) {
        return isMaster
            ? registryClient.getServerList(NodeType.MASTER)
            : registryClient.getServerList(NodeType.WORKER);
    }

    private boolean operationPermissionCheck(Integer userId, String permissionKey, Logger logger) {
        return resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.MONITOR, userId, permissionKey, logger);
    }

}
