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

package org.apache.dolphinscheduler.server.master.dispatch.host;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.ResInfo;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWorker;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.ZKMasterClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 *  round robin host manager
 */
public abstract class CommonHostManager implements HostManager {

    /**
     * zookeeper registry center
     */
    @Autowired
    protected ZookeeperRegistryCenter registryCenter;

    /**
     * zookeeper node manager
     */
    @Autowired
    protected ZookeeperNodeManager zookeeperNodeManager;

    /**
     * zk master client
     */
    @Autowired
    protected ZKMasterClient zkMasterClient;

    /**
     * worker group mapper
     */
    @Autowired
    protected WorkerGroupMapper workerGroupMapper;

    /**
     * select host
     * @param context context
     * @return host
     */
    @Override
    public Host select(ExecutionContext context) {
        List<HostWorker> candidates = null;
        String workerGroup = context.getWorkerGroup();
        ExecutorType executorType = context.getExecutorType();
        switch (executorType) {
            case WORKER:
                candidates = getHostWorkersFromDatabase(workerGroup);
                if (candidates.isEmpty()) {
                    candidates = getHostWorkersFromZookeeper(workerGroup);
                }
                break;
            case CLIENT:
                break;
            default:
                throw new IllegalArgumentException("invalid executorType : " + executorType);
        }

        if (CollectionUtils.isEmpty(candidates)) {
            return new Host();
        }
        return select(candidates);
    }

    protected abstract HostWorker select(Collection<HostWorker> nodes);

    protected List<HostWorker> getHostWorkersFromDatabase(String workerGroup) {
        List<HostWorker> hostWorkers = new ArrayList<>();
        List<WorkerGroup> workerGroups = workerGroupMapper.queryWorkerGroupByName(workerGroup);
        if (CollectionUtils.isNotEmpty(workerGroups)) {
            Map<String, String> serverMaps = zkMasterClient.getServerMaps(ZKNodeType.WORKER, true);
            for (WorkerGroup wg : workerGroups) {
                for (String addr : wg.getAddrList().split(Constants.COMMA)) {
                    if (serverMaps.containsKey(addr)) {
                        String heartbeat = serverMaps.get(addr);
                        int hostWeight = getWorkerHostWeightFromHeartbeat(heartbeat);
                        hostWorkers.add(HostWorker.of(addr, hostWeight, workerGroup));
                    }
                }
            }
        }
        return hostWorkers;
    }

    protected List<HostWorker> getHostWorkersFromZookeeper(String workerGroup) {
        List<HostWorker> hostWorkers = new ArrayList<>();
        Collection<String> nodes = zookeeperNodeManager.getWorkerGroupNodes(workerGroup);
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (String node : nodes) {
                String workerGroupPath = registryCenter.getWorkerGroupPath(workerGroup);
                String heartbeat = registryCenter.getRegisterOperator().get(workerGroupPath + "/" + node);
                int hostWeight = getWorkerHostWeightFromHeartbeat(heartbeat);
                hostWorkers.add(HostWorker.of(node, hostWeight, workerGroup));
            }
        }
        return hostWorkers;
    }

    protected int getWorkerHostWeightFromHeartbeat(String heartbeat) {
        int hostWeight = Constants.DEFAULT_WORKER_HOST_WEIGHT;
        if (StringUtils.isNotEmpty(heartbeat)) {
            String[] parts = heartbeat.split(Constants.COMMA);
            if (ResInfo.isNewHeartbeatWithWeight(parts)) {
                hostWeight = Integer.parseInt(parts[10]);
            }
        }
        return hostWeight;
    }

    public void setZookeeperNodeManager(ZookeeperNodeManager zookeeperNodeManager) {
        this.zookeeperNodeManager = zookeeperNodeManager;
    }

    public ZookeeperNodeManager getZookeeperNodeManager() {
        return zookeeperNodeManager;
    }

}
