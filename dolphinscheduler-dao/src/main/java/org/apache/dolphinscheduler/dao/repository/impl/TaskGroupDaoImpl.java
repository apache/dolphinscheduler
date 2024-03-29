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

import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;

import java.util.List;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class TaskGroupDaoImpl extends BaseDao<TaskGroup, TaskGroupMapper> implements TaskGroupDao {

    public TaskGroupDaoImpl(@NonNull TaskGroupMapper taskGroupMapper) {
        super(taskGroupMapper);
    }

    @Override
    public List<TaskGroup> queryAllTaskGroups() {
        return mybatisMapper.selectList(null);
    }

    @Override
    public List<TaskGroup> queryUsedTaskGroups() {
        return mybatisMapper.queryUsedTaskGroups();
    }

    @Override
    public List<TaskGroup> queryAvailableTaskGroups() {
        return mybatisMapper.queryAvailableTaskGroups();
    }

    @Override
    public boolean acquireTaskGroupSlot(Integer taskGroupId) {
        if (taskGroupId == null) {
            throw new IllegalArgumentException("taskGroupId cannot be null");
        }
        return mybatisMapper.acquireTaskGroupSlot(taskGroupId) > 0;
    }

    @Override
    public boolean releaseTaskGroupSlot(Integer taskGroupId) {
        if (taskGroupId == null) {
            throw new IllegalArgumentException("taskGroupId cannot be null");
        }
        return mybatisMapper.releaseTaskGroupSlot(taskGroupId) > 0;
    }
}
