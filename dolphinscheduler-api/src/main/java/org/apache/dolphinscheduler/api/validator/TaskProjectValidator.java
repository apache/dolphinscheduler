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

package org.apache.dolphinscheduler.api.validator;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Component
public class TaskProjectValidator implements IValidator<TaskProjectValidator.Context> {

    private final TaskDefinitionDao taskDefinitionDao;
    private final TaskDefinitionLogMapper taskDefinitionLogMapper;

    public TaskProjectValidator(TaskDefinitionDao taskDefinitionDao, TaskDefinitionLogMapper taskDefinitionLogMapper) {
        this.taskDefinitionDao = taskDefinitionDao;
        this.taskDefinitionLogMapper = taskDefinitionLogMapper;
    }

    @Override
    public void validate(Context context) {
        long projectCode = context.getProjectCode();
        Set<Long> codes = new HashSet<>();
        for (TaskDefinition task : context.getTasks()) {
            // New task payloads may omit projectCode; persisted ownership is checked below.
            if (task.getProjectCode() != 0 && task.getProjectCode() != projectCode) {
                throw new ServiceException(Status.PROJECT_WORKFLOW_NOT_MATCH);
            }
            if (task.getCode() > 0) {
                codes.add(task.getCode());
            }
        }
        if (!codes.isEmpty()) {
            for (TaskDefinition task : taskDefinitionDao.queryByCodes(codes)) {
                if (task.getProjectCode() != projectCode) {
                    throw new ServiceException(Status.PROJECT_WORKFLOW_NOT_MATCH);
                }
            }
            // Check all versions, including when the request omits a version or the current row was deleted.
            if (taskDefinitionLogMapper.selectCount(new QueryWrapper<TaskDefinitionLog>()
                    .in("code", codes).ne("project_code", projectCode)) > 0) {
                throw new ServiceException(Status.PROJECT_WORKFLOW_NOT_MATCH);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Context {

        private final long projectCode;
        private final Collection<? extends TaskDefinition> tasks;
    }
}
