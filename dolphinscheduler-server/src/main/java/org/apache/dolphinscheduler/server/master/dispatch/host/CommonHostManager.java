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
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWorker;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     * zookeeperNodeManager
     */
    @Autowired
    protected ZookeeperNodeManager zookeeperNodeManager;

    /**
     * select host
     * @param context context
     * @return host
     */
    @Override
    public Host select(ExecutionContext context) {
        Host host = new Host();
        Collection<String> nodes = null;
        String workerGroup = context.getWorkerGroup();
        // executor type
        ExecutorType executorType = context.getExecutorType();
        switch (executorType) {
            case WORKER:
                nodes = zookeeperNodeManager.getWorkerGroupNodes(workerGroup);
                break;
            case CLIENT:
                break;
            default:
                throw new IllegalArgumentException("invalid executorType : " + executorType);

        }
        if (nodes == null || nodes.isEmpty()) {
            return host;
        }
        List<HostWorker> candidateHosts = new ArrayList<>();
        nodes.forEach(node -> {
            String workerGroupPath = registryCenter.getWorkerGroupPath(workerGroup);
            String heartbeat = registryCenter.getRegisterOperator().get(workerGroupPath + "/" + node);
            int weight = Constants.DEFAULT_WORKER_WEIGHT;
            if (StringUtils.isNotEmpty(heartbeat)) {
                String[] parts = heartbeat.split(Constants.COMMA);
                if (parts.length == Constants.HEARTBEAT_WITH_WEIGHT_FOR_ZOOKEEPER_INFO_LENGTH) {
                    weight = Integer.parseInt(parts[10]);
                }
            }
            candidateHosts.add(HostWorker.of(node, weight, workerGroup));
        });
        return select(candidateHosts);
    }

    protected abstract HostWorker select(Collection<HostWorker> nodes);

    public void setZookeeperNodeManager(ZookeeperNodeManager zookeeperNodeManager) {
        this.zookeeperNodeManager = zookeeperNodeManager;
    }

    public ZookeeperNodeManager getZookeeperNodeManager() {
        return zookeeperNodeManager;
    }
}
