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

import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

/**
 * Task Definition Log DAP implementation
 */
@Repository
public class TaskDefinitionLogDaoImpl implements TaskDefinitionLogDao {

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Override
    public List<TaskDefinitionLog> getTaskDefineLogList(List<ProcessTaskRelation> processTaskRelations) {
        Set<TaskDefinition> taskDefinitionSet = new HashSet<>();
        for (ProcessTaskRelation processTaskRelation : processTaskRelations) {
            if (processTaskRelation.getPreTaskCode() > 0) {
                taskDefinitionSet.add(new TaskDefinition(processTaskRelation.getPreTaskCode(),
                        processTaskRelation.getPreTaskVersion()));
            }
            if (processTaskRelation.getPostTaskCode() > 0) {
                taskDefinitionSet.add(new TaskDefinition(processTaskRelation.getPostTaskCode(),
                        processTaskRelation.getPostTaskVersion()));
            }
        }
        if (taskDefinitionSet.isEmpty()) {
            return Lists.newArrayList();
        }
        return taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitionSet);
    }

    @Override
    public List<TaskDefinitionLog> getTaskDefineLogListByRelation(List<ProcessTaskRelation> processTaskRelations) {
        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        Map<Long, Integer> taskCodeVersionMap = new HashMap<>();
        for (ProcessTaskRelation processTaskRelation : processTaskRelations) {
            if (processTaskRelation.getPreTaskCode() > 0) {
                taskCodeVersionMap.put(processTaskRelation.getPreTaskCode(), processTaskRelation.getPreTaskVersion());
            }
            if (processTaskRelation.getPostTaskCode() > 0) {
                taskCodeVersionMap.put(processTaskRelation.getPostTaskCode(), processTaskRelation.getPostTaskVersion());
            }
        }
        taskCodeVersionMap.forEach((code, version) -> {
            taskDefinitionLogs.add((TaskDefinitionLog) taskDefinitionDao.findTaskDefinition(code, version));
        });
        return taskDefinitionLogs;
    }

    @Override
    public void deleteByTaskDefinitionCodes(Set<Long> taskDefinitionCodes) {
        if (CollectionUtils.isEmpty(taskDefinitionCodes)) {
            return;
        }
        taskDefinitionLogMapper.deleteByTaskDefinitionCodes(taskDefinitionCodes);
    }
}
