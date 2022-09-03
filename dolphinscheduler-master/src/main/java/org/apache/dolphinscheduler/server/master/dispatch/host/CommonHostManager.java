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

import org.apache.commons.collections.CollectionUtils;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWorker;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * common host manager
 */
public abstract class CommonHostManager implements HostManager {

    /**
     * server node manager
     */
    @Autowired
    protected ServerNodeManager serverNodeManager;

    /**
     * select host
     *
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
                candidates = getWorkerCandidates(workerGroup);
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

    protected List<HostWorker> getWorkerCandidates(String workerGroup) {
        List<HostWorker> hostWorkers = new ArrayList<>();
        Set<String> nodes = serverNodeManager.getWorkerGroupNodes(workerGroup);
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (String node : nodes) {
                WorkerHeartBeat workerNodeInfo = serverNodeManager.getWorkerNodeInfo(node);
                hostWorkers.add(HostWorker.of(node, workerNodeInfo.getWorkerHostWeight(), workerGroup));
            }
        }
        return hostWorkers;
    }
}
