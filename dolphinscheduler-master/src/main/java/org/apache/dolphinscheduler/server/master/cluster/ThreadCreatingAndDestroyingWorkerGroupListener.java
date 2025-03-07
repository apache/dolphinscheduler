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

import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.server.master.runner.WorkerGroupTaskDispatchManager;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ThreadCreatingAndDestroyingWorkerGroupListener implements WorkerGroupChangeNotifier.WorkerGroupListener {

    @Autowired
    private WorkerGroupTaskDispatchManager workerGroupTaskDispatchManager;

    @PostConstruct
    public void init() {
        // init default worker group
        workerGroupTaskDispatchManager.addWorkerGroup(WorkerGroupUtils.getDefaultWorkerGroup());
    }

    @Override
    public void onWorkerGroupAdd(List<WorkerGroup> workerGroups) {
        for (WorkerGroup workerGroup : workerGroups) {
            workerGroupTaskDispatchManager.addWorkerGroup(workerGroup.getName());
        }
    }

    @Override
    public void onWorkerGroupChange(List<WorkerGroup> workerGroups) {
        String workerGroupsString = workerGroups.stream()
                .map(WorkerGroup::getName)
                .collect(Collectors.joining(", "));
        log.info("Worker groups: {}", workerGroupsString);
    }

    @Override
    public void onWorkerGroupDelete(List<WorkerGroup> workerGroups) {
        for (WorkerGroup workerGroup : workerGroups) {
            try {
                workerGroupTaskDispatchManager.stopWorkerGroup(workerGroup.getName());
            } catch (Exception e) {
                log.error("stop worker group error", e);
            }
        }
    }
}
