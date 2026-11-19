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

package org.apache.dolphinscheduler.api.permission;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Component
public class ProjectCodeValidator {

    private final TaskDefinitionDao taskDefinitionDao;
    private final TaskDefinitionLogMapper taskDefinitionLogMapper;

    public ProjectCodeValidator(TaskDefinitionDao taskDefinitionDao, TaskDefinitionLogMapper taskDefinitionLogMapper) {
        this.taskDefinitionDao = taskDefinitionDao;
        this.taskDefinitionLogMapper = taskDefinitionLogMapper;
    }

    public static void requireProject(long projectCode, long actualProjectCode) {
        if (projectCode != actualProjectCode) {
            throw new ServiceException(Status.PROJECT_WORKFLOW_NOT_MATCH);
        }
    }

    public void validateTasks(long projectCode, Collection<? extends TaskDefinition> tasks) {
        Set<Long> codes = new HashSet<>();
        for (TaskDefinition task : tasks) {
            // New task payloads may omit projectCode; persisted ownership is checked below.
            if (task.getProjectCode() != 0) {
                requireProject(projectCode, task.getProjectCode());
            }
            if (task.getCode() > 0) {
                codes.add(task.getCode());
            }
        }
        if (!codes.isEmpty()) {
            for (TaskDefinition task : taskDefinitionDao.queryByCodes(codes)) {
                requireProject(projectCode, task.getProjectCode());
            }
            // Check all versions, including when the request omits a version or the current row was deleted.
            if (taskDefinitionLogMapper.selectCount(new QueryWrapper<TaskDefinitionLog>()
                    .in("code", codes).ne("project_code", projectCode)) > 0) {
                throw new ServiceException(Status.PROJECT_WORKFLOW_NOT_MATCH);
            }
        }
    }

    public void validateRelations(long projectCode, long workflowCode,
                                  Collection<? extends WorkflowTaskRelation> relations) {
        List<TaskDefinition> tasks = new ArrayList<>();
        for (WorkflowTaskRelation relation : relations) {
            if (relation.getProjectCode() != 0) {
                requireProject(projectCode, relation.getProjectCode());
            }
            if (relation.getWorkflowDefinitionCode() != 0 && relation.getWorkflowDefinitionCode() != workflowCode) {
                throw new ServiceException(Status.PROJECT_WORKFLOW_NOT_MATCH);
            }
            if (relation.getPreTaskCode() > 0) {
                tasks.add(new TaskDefinition(relation.getPreTaskCode(), relation.getPreTaskVersion()));
            }
            if (relation.getPostTaskCode() > 0) {
                tasks.add(new TaskDefinition(relation.getPostTaskCode(), relation.getPostTaskVersion()));
            }
        }
        validateTasks(projectCode, tasks);
    }
}
