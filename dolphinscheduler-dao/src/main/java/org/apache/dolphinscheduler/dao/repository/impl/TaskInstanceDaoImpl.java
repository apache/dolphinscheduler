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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Task Instance DAO implementation
 */
@Repository
@Slf4j
public class TaskInstanceDaoImpl extends BaseDao<TaskInstance, TaskInstanceMapper> implements TaskInstanceDao {

    @Autowired
    private WorkflowInstanceMapper workflowInstanceMapper;

    public TaskInstanceDaoImpl(@NonNull TaskInstanceMapper taskInstanceMapper) {
        super(taskInstanceMapper);
    }

    @Override
    public boolean upsertTaskInstance(TaskInstance taskInstance) {
        if (taskInstance.getId() != null) {
            return updateById(taskInstance);
        } else {
            return insert(taskInstance) > 0;
        }
    }

    @Override
    public void markTaskInstanceInvalid(List<TaskInstance> taskInstances) {
        if (CollectionUtils.isEmpty(taskInstances)) {
            return;
        }
        for (TaskInstance taskInstance : taskInstances) {
            taskInstance.setFlag(Flag.NO);
            mybatisMapper.updateById(taskInstance);
        }
    }

    @Override
    public List<TaskInstance> queryValidTaskListByWorkflowInstanceId(Integer processInstanceId) {
        return mybatisMapper.findValidTaskListByWorkflowInstanceId(processInstanceId, Flag.YES);
    }

    @Override
    public void deleteByWorkflowInstanceId(int workflowInstanceId) {
        mybatisMapper.deleteByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public List<TaskInstance> queryByWorkflowInstanceId(Integer workflowInstanceId) {
        return mybatisMapper.findByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public List<TaskInstance> queryLastTaskInstanceListIntervalInWorkflowInstance(Integer workflowInstanceId,
                                                                                  Set<Long> taskCodes) {
        return mybatisMapper.findLastTaskInstances(workflowInstanceId, taskCodes);
    }

    @Override
    public TaskInstance queryLastTaskInstanceIntervalInWorkflowInstance(Integer workflowInstanceId, long depTaskCode) {
        return mybatisMapper.findLastTaskInstance(workflowInstanceId, depTaskCode);
    }

}
