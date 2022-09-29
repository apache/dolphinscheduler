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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWeight;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWorker;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.LowerWeightRoundRobin;
import org.apache.dolphinscheduler.server.master.registry.WorkerInfoChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * lower weight host manager
 */
public class LowerWeightHostManager extends CommonHostManager {

    private final Logger logger = LoggerFactory.getLogger(LowerWeightHostManager.class);

    /**
     * selector
     */
    private LowerWeightRoundRobin selector;

    /**
     * worker host weights
     */
    private ConcurrentHashMap<String, Set<HostWeight>> workerHostWeightsMap;

    /**
     * worker group host lock
     */
    private Lock lock;

    @PostConstruct
    public void init() {
        this.selector = new LowerWeightRoundRobin();
        this.workerHostWeightsMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
        serverNodeManager.addWorkerInfoChangeListener(new WorkerWeightListener());
    }

    /**
     * select host
     *
     * @param context context
     * @return host
     */
    @Override
    public Host select(ExecutionContext context) {
        Set<HostWeight> workerHostWeights = getWorkerHostWeights(context.getWorkerGroup());
        if (CollectionUtils.isNotEmpty(workerHostWeights)) {
            return selector.select(workerHostWeights).getHost();
        }
        return new Host();
    }

    @Override
    public HostWorker select(Collection<HostWorker> nodes) {
        throw new UnsupportedOperationException("not support");
    }


    private class WorkerWeightListener implements WorkerInfoChangeListener {
        @Override
        public void notify(Map<String, Set<String>> workerGroups, Map<String, WorkerHeartBeat> workerNodeInfo) {
            syncWorkerResources(workerGroups, workerNodeInfo);
        }
    }

    /**
     * Sync worker resource.
     *
     * @param workerGroupNodes  worker group nodes, key is worker group, value is worker group nodes.
     * @param workerNodeInfoMap worker node info map, key is worker node, value is worker info.
     */
    private void syncWorkerResources(final Map<String, Set<String>> workerGroupNodes,
                                     final Map<String, WorkerHeartBeat> workerNodeInfoMap) {
        try {
            Map<String, Set<HostWeight>> workerHostWeights = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : workerGroupNodes.entrySet()) {
                String workerGroup = entry.getKey();
                Set<String> nodes = entry.getValue();
                Set<HostWeight> hostWeights = new HashSet<>(nodes.size());
                for (String node : nodes) {
                    WorkerHeartBeat heartbeat = workerNodeInfoMap.getOrDefault(node, null);
                    Optional<HostWeight> hostWeightOpt = getHostWeight(node, workerGroup, heartbeat);
                    hostWeightOpt.ifPresent(hostWeights::add);
                }
                if (!hostWeights.isEmpty()) {
                    workerHostWeights.put(workerGroup, hostWeights);
                }
            }
            syncWorkerHostWeight(workerHostWeights);
        } catch (Throwable ex) {
            logger.error("Sync worker resource error", ex);
        }
    }

    public Optional<HostWeight> getHostWeight(String addr, String workerGroup, WorkerHeartBeat heartBeat) {
        if (heartBeat == null) {
            logger.warn("worker {} in work group {} have not received the heartbeat", addr, workerGroup);
            return Optional.empty();
        }
        if (Constants.ABNORMAL_NODE_STATUS == heartBeat.getServerStatus()) {
            logger.warn("worker {} current cpu load average {} is too high or available memory {}G is too low",
                    addr, heartBeat.getLoadAverage(), heartBeat.getAvailablePhysicalMemorySize());
            return Optional.empty();
        }
        if (Constants.BUSY_NODE_STATUE == heartBeat.getServerStatus()) {
            logger.warn("worker {} is busy, current waiting task count {} is large than worker thread count {}",
                    addr, heartBeat.getWorkerWaitingTaskCount(), heartBeat.getWorkerExecThreadCount());
            return Optional.empty();
        }
        return Optional.of(
                new HostWeight(
                        HostWorker.of(addr, heartBeat.getWorkerHostWeight(), workerGroup),
                        heartBeat.getCpuUsage(),
                        heartBeat.getMemoryUsage(),
                        heartBeat.getLoadAverage(),
                        heartBeat.getWorkerWaitingTaskCount(),
                        heartBeat.getStartupTime()));
    }

    private void syncWorkerHostWeight(Map<String, Set<HostWeight>> workerHostWeights) {
        lock.lock();
        try {
            workerHostWeightsMap.clear();
            workerHostWeightsMap.putAll(workerHostWeights);
        } finally {
            lock.unlock();
        }
    }

    private Set<HostWeight> getWorkerHostWeights(String workerGroup) {
        lock.lock();
        try {
            return workerHostWeightsMap.get(workerGroup);
        } finally {
            lock.unlock();
        }
    }

}
