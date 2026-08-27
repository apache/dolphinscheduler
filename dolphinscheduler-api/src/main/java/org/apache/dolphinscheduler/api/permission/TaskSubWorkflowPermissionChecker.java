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
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubWorkflowParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskSubWorkflowPermissionChecker {

    private final WorkflowDefinitionDao workflowDefinitionDao;
    private final ProjectService projectService;

    public TaskSubWorkflowPermissionChecker(WorkflowDefinitionDao workflowDefinitionDao,
                                            ProjectService projectService) {
        this.workflowDefinitionDao = workflowDefinitionDao;
        this.projectService = projectService;
    }

    public void checkPermission(User loginUser, Collection<? extends TaskDefinition> taskDefinitions) {
        if (taskDefinitions == null || taskDefinitions.isEmpty()) {
            return;
        }

        Set<Long> subWorkflowDefinitionCodes = new HashSet<>();
        for (TaskDefinition taskDefinition : taskDefinitions) {
            if (!TaskTypeUtils.isSubWorkflowTask(taskDefinition.getTaskType())) {
                continue;
            }
            SubWorkflowParameters subWorkflowParameters =
                    JSONUtils.parseObject(taskDefinition.getTaskParams(), SubWorkflowParameters.class);
            if (subWorkflowParameters.getWorkflowDefinitionCode() > 0) {
                subWorkflowDefinitionCodes.add(subWorkflowParameters.getWorkflowDefinitionCode());
            }
        }

        if (subWorkflowDefinitionCodes.isEmpty()) {
            return;
        }

        List<WorkflowDefinition> subWorkflowDefinitions =
                workflowDefinitionDao.queryByCodes(subWorkflowDefinitionCodes);
        Set<Long> existingSubWorkflowDefinitionCodes = subWorkflowDefinitions == null
                ? new HashSet<>()
                : subWorkflowDefinitions.stream().map(WorkflowDefinition::getCode).collect(Collectors.toSet());
        if (!existingSubWorkflowDefinitionCodes.containsAll(subWorkflowDefinitionCodes)) {
            log.warn("Referenced sub workflow is unavailable, userId:{}.", loginUser.getId());
            throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
        }

        Set<Long> subWorkflowProjectCodes = subWorkflowDefinitions.stream()
                .map(WorkflowDefinition::getProjectCode)
                .collect(Collectors.toSet());
        try {
            for (Long projectCode : subWorkflowProjectCodes) {
                projectService.checkHasProjectWritePermissionThrowException(loginUser, projectCode);
            }
        } catch (ServiceException ex) {
            log.warn("Referenced sub workflow is unavailable, userId:{}.", loginUser.getId());
            throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
        }
    }
}
