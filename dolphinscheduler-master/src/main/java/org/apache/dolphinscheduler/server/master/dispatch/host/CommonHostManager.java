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

import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.WorkerGroupNotFoundException;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWorker;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * common host manager
 */
public abstract class CommonHostManager implements HostManager {

    /**
     * server node manager
     */
    @Autowired
    protected ServerNodeManager serverNodeManager;

    @Override
    public Optional<Host> select(String workerGroup) throws WorkerGroupNotFoundException {
        List<HostWorker> candidates = getWorkerCandidates(workerGroup);
        if (CollectionUtils.isEmpty(candidates)) {
            return Optional.empty();
        }
        return Optional.ofNullable(select(candidates));
    }

    protected abstract HostWorker select(Collection<HostWorker> nodes);

    protected List<HostWorker> getWorkerCandidates(String workerGroup) throws WorkerGroupNotFoundException {
        List<HostWorker> hostWorkers = new ArrayList<>();
        Set<String> nodes = serverNodeManager.getWorkerGroupNodes(workerGroup);
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (String node : nodes) {
                serverNodeManager.getWorkerNodeInfo(node).ifPresent(
                        workerNodeInfo -> hostWorkers
                                .add(HostWorker.of(node, workerNodeInfo.getWorkerHostWeight(), workerGroup)));
            }
        }
        return hostWorkers;
    }
}
