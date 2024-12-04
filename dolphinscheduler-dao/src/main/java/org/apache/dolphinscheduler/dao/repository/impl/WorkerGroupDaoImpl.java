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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.common.enums.WorkerGroupSource;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;

import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
public class WorkerGroupDaoImpl extends BaseDao<WorkerGroup, WorkerGroupMapper> implements WorkerGroupDao {

    public WorkerGroupDaoImpl(@NonNull WorkerGroupMapper workerGroupMapper) {
        super(workerGroupMapper);
    }

    @Override
    public boolean upsertAddrListByWorkerGroupName(String workerGroupName, String addrList, WorkerGroupSource source) {
        List<WorkerGroup> currentWorkerGroups = mybatisMapper.queryWorkerGroupByName(workerGroupName);
        if (CollectionUtils.isEmpty(currentWorkerGroups)) {
            WorkerGroup newWorkerGroup = new WorkerGroup();
            newWorkerGroup.setName(workerGroupName);
            newWorkerGroup.setAddrList(addrList);
            newWorkerGroup.setSource(source);
            newWorkerGroup.setCreateTime(new Date());
            newWorkerGroup.setUpdateTime(newWorkerGroup.getCreateTime());
            int inserted = mybatisMapper.insert(newWorkerGroup);
            return inserted > 0;
        } else {
            int updated = mybatisMapper.updateAddrListByWorkerGroupName(workerGroupName, addrList, source);
            return updated > 0;
        }
    }

    @Override
    public boolean deleteByWorkerGroupName(String workerGroupName) {
        int deleted = mybatisMapper.deleteByWorkerGroupName(workerGroupName);
        return deleted > 0;
    }

    @Override
    public List<String> queryAllWorkerGroupNames() {
        return mybatisMapper.queryAllWorkerGroup().stream()
                .map(WorkerGroup::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkerGroup> queryAllWorkerGroup() {
        return mybatisMapper.queryAllWorkerGroup();
    }

    @Override
    public List<WorkerGroup> queryWorkerGroupByName(String name) {
        return mybatisMapper.queryWorkerGroupByName(name);
    }
}
