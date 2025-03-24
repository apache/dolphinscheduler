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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ContextType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AbstractTaskInstanceContext;
import org.apache.dolphinscheduler.dao.entity.DependentResultTaskInstanceContext;
import org.apache.dolphinscheduler.dao.entity.TaskInstanceContext;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceContextMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceContextDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class TaskInstanceContextDaoImpl extends BaseDao<TaskInstanceContext, TaskInstanceContextMapper>
        implements
            TaskInstanceContextDao {

    public TaskInstanceContextDaoImpl(TaskInstanceContextMapper taskInstanceContextMapper) {
        super(taskInstanceContextMapper);
    }

    @Override
    public List<TaskInstanceContext> queryListByTaskInstanceIdAndContextType(Integer taskInstanceId,
                                                                             ContextType contextType) {
        if (taskInstanceId == null) {
            return Collections.emptyList();
        }
        return mybatisMapper.queryListByTaskInstanceIdAndContextType(taskInstanceId, contextType);
    }

    @Override
    public int deleteByTaskInstanceIdAndContextType(Integer taskInstanceId, ContextType contextType) {
        if (taskInstanceId == null) {
            throw new IllegalArgumentException("taskInstanceId cannot be null");
        }
        return mybatisMapper.deleteByTaskInstanceIdAndContextType(taskInstanceId, contextType);
    }

    @Override
    public int upsertTaskInstanceContext(TaskInstanceContext taskInstanceContext) {
        if (taskInstanceContext == null) {
            return 0;
        }
        TaskInstanceContext dbTaskInstanceContext =
                mybatisMapper.queryListByTaskInstanceIdAndContextType(taskInstanceContext.getTaskInstanceId(),
                        taskInstanceContext.getContextType()).stream().findFirst().orElse(null);
        if (dbTaskInstanceContext == null) {
            return mybatisMapper.insert(taskInstanceContext);
        } else {
            List<AbstractTaskInstanceContext> dbDependentResultTaskInstanceContextList =
                    dbTaskInstanceContext.getTaskInstanceContext();
            dbDependentResultTaskInstanceContextList.addAll(taskInstanceContext.getTaskInstanceContext());
            List<AbstractTaskInstanceContext> deduplicatedDependentResultTaskInstanceContextList =
                    dbDependentResultTaskInstanceContextList.stream()
                            .map(DependentResultTaskInstanceContext.class::cast)
                            .collect(Collectors.collectingAndThen(
                                    Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(
                                            o -> o.getProjectCode() + Constants.UNDERLINE
                                                    + o.getWorkflowDefinitionCode() + Constants.UNDERLINE
                                                    + o.getTaskDefinitionCode() + Constants.UNDERLINE
                                                    + o.getDateCycle()))),
                                    ArrayList::new));
            taskInstanceContext.setTaskInstanceContext(deduplicatedDependentResultTaskInstanceContextList);
            return mybatisMapper.updateTaskInstanceContextByTaskInstanceIdAndContextType(
                    taskInstanceContext.getTaskInstanceId(),
                    taskInstanceContext.getContextType(), JSONUtils.toJsonString(taskInstanceContext.getContext()));
        }
    }

    @Override
    public List<TaskInstanceContext> batchQueryByTaskInstanceIdsAndContextType(List<Integer> taskInstanceIds,
                                                                               ContextType contextType) {
        if (CollectionUtils.isEmpty(taskInstanceIds)) {
            return Collections.emptyList();
        }
        return mybatisMapper.batchQueryByTaskInstanceIdsAndContextType(taskInstanceIds, contextType);
    }
}
