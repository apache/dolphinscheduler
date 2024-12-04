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

package org.apache.dolphinscheduler.server.master.cluster;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.WorkerGroupSource;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.MapComparator;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.entity.WorkerGroupInfo;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Use to watch the worker group from database and notify the change.
 */
@Slf4j
@Component
public class WorkerGroupChangeNotifier {

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private RegistryClient registryClient;

    private final WorkerGroupDao workerGroupDao;
    private final List<WorkerGroupListener> listeners = new CopyOnWriteArrayList<>();

    private Map<String, WorkerGroup> workerGroupMap = new HashMap<>();

    public WorkerGroupChangeNotifier(WorkerGroupDao workerGroupDao) {
        this.workerGroupDao = workerGroupDao;
    }

    public void start() {
        detectWorkerGroupChanges();
        final long workerGroupRefreshIntervalSeconds = masterConfig.getWorkerGroupRefreshInterval().getSeconds();
        MasterThreadFactory.getDefaultSchedulerThreadExecutor().scheduleWithFixedDelay(
                this::detectWorkerGroupChanges,
                workerGroupRefreshIntervalSeconds,
                workerGroupRefreshIntervalSeconds,
                TimeUnit.SECONDS);
    }

    public void subscribeWorkerGroupsChange(WorkerGroupListener listener) {
        listeners.add(listener);
    }

    public synchronized void detectWorkerGroupChanges() {
        try {
            final MapComparator<String, WorkerGroup> mapComparator = detectChangedWorkerGroups();
            triggerListeners(mapComparator);
            workerGroupMap = mapComparator.getNewMap();
        } catch (Exception ex) {
            log.error("Detect WorkerGroup changes failed", ex);
        }
    }

    Map<String, WorkerGroup> getWorkerGroupMap() {
        return workerGroupMap;
    }

    private MapComparator<String, WorkerGroup> detectChangedWorkerGroups() {
        Map<String, WorkerGroup> tmpWorkerGroupMap = workerGroupDao.queryAll()
                .stream()
                .collect(Collectors.toMap(WorkerGroup::getName, workerGroup -> workerGroup));

        List<Server> activeWorkerNodes = registryClient.getServerList(RegistryNodeType.WORKER);
        Map<String, String> configWorkerGroups = activeWorkerNodes.stream()
                .collect(Collectors.groupingBy(activeWorkerNode -> JSONUtils.parseObject(activeWorkerNode.getHeartBeatInfo(), WorkerHeartBeat.class).getWorkerGroup(),
                        Collectors.mapping(Server::getHost, Collectors.joining(Constants.COMMA))));
        for (Server activeWorkerNode : activeWorkerNodes) {
            WorkerGroup configWorkerGroup = new WorkerGroup();
            WorkerHeartBeat workerHeartBeat = JSONUtils.parseObject(activeWorkerNode.getHeartBeatInfo(), WorkerHeartBeat.class);
            configWorkerGroup.setName(workerHeartBeat.getWorkerGroup());
            configWorkerGroup.setAddrList(configWorkerGroups.get(workerHeartBeat.getWorkerGroup()));
            configWorkerGroup.setCreateTime(DateUtils.timeStampToDate(workerHeartBeat.getStartupTime()));
            configWorkerGroup.setUpdateTime(DateUtils.timeStampToDate(workerHeartBeat.getReportTime()));
            tmpWorkerGroupMap.put(configWorkerGroup.getName(), configWorkerGroup);
        }

        return new MapComparator<>(workerGroupMap, tmpWorkerGroupMap);
    }

    private void triggerListeners(MapComparator<String, WorkerGroup> mapComparator) {
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }
        final List<WorkerGroup> workerGroupsAdded = mapComparator.getValuesToAdd();
        if (CollectionUtils.isNotEmpty(workerGroupsAdded)) {
            listeners.forEach(listener -> listener.onWorkerGroupAdd(workerGroupsAdded));
        }

        final List<WorkerGroup> workerGroupsRemoved = mapComparator.getValuesToRemove();
        if (CollectionUtils.isNotEmpty(workerGroupsRemoved)) {
            listeners.forEach(listener -> listener.onWorkerGroupDelete(workerGroupsRemoved));
        }

        final List<WorkerGroup> workerGroupsUpdated = mapComparator.getNewValuesToUpdate();
        if (CollectionUtils.isNotEmpty(workerGroupsUpdated)) {
            listeners.forEach(listener -> listener.onWorkerGroupChange(workerGroupsUpdated));
        }
    }

    public interface WorkerGroupListener {

        void onWorkerGroupDelete(List<WorkerGroup> workerGroups);

        void onWorkerGroupAdd(List<WorkerGroup> workerGroups);

        void onWorkerGroupChange(List<WorkerGroup> workerGroups);
    }
}
