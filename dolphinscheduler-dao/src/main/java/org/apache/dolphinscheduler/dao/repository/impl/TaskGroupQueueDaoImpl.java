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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupQueueDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

@Repository
public class TaskGroupQueueDaoImpl extends BaseDao<TaskGroupQueue, TaskGroupQueueMapper> implements TaskGroupQueueDao {

    public TaskGroupQueueDaoImpl(@NonNull TaskGroupQueueMapper taskGroupQueueMapper) {
        super(taskGroupQueueMapper);
    }

    @Override
    public void deleteByWorkflowInstanceIds(List<Integer> workflowInstanceIds) {
        if (CollectionUtils.isEmpty(workflowInstanceIds)) {
            return;
        }
        mybatisMapper.deleteByWorkflowInstanceIds(workflowInstanceIds);
    }

    @Override
    public List<TaskGroupQueue> queryAllInQueueTaskGroupQueue() {
        return mybatisMapper.queryAllTaskGroupQueueByInQueue(Flag.YES.getCode());
    }

    @Override
    public List<TaskGroupQueue> queryInQueueTaskGroupQueue(int minTaskGroupQueueId, int limit) {
        return mybatisMapper.queryInQueueTaskGroupQueue(Flag.YES.getCode(), minTaskGroupQueueId, limit);
    }

    @Override
    public List<TaskGroupQueue> queryAllInQueueTaskGroupQueueByGroupId(Integer taskGroupId) {
        return mybatisMapper.queryAllInQueueTaskGroupQueueByGroupId(taskGroupId, Flag.YES.getCode());
    }

    @Override
    public List<TaskGroupQueue> queryByTaskInstanceId(Integer taskInstanceId) {
        return mybatisMapper.queryByTaskInstanceId(taskInstanceId);
    }

    @Override
    public List<TaskGroupQueue> queryAcquiredTaskGroupQueueByGroupId(Integer taskGroupId) {
        return mybatisMapper.queryUsingTaskGroupQueueByGroupId(
                taskGroupId,
                TaskGroupQueueStatus.ACQUIRE_SUCCESS.getCode(),
                Flag.YES.getCode(),
                Flag.NO.getCode());
    }

    @Override
    public int countUsingTaskGroupQueueByGroupId(Integer taskGroupId) {
        return mybatisMapper.countUsingTaskGroupQueueByGroupId(taskGroupId,
                TaskGroupQueueStatus.ACQUIRE_SUCCESS.getCode(),
                Flag.YES.ordinal(),
                Flag.NO.getCode());
    }

    @Override
    public List<TaskGroupQueue> queryWaitNotifyForceStartTaskGroupQueue(int minTaskGroupQueueId, int limit) {
        return mybatisMapper.queryWaitNotifyForceStartTaskGroupQueue(
                Flag.YES.getCode(),
                Flag.YES.getCode(),
                minTaskGroupQueueId,
                limit);
    }
}
