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
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.AbstractResourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskDatasourcePermissionChecker {

    private final ResourcePermissionCheckService resourcePermissionCheckService;

    public TaskDatasourcePermissionChecker(ResourcePermissionCheckService resourcePermissionCheckService) {
        this.resourcePermissionCheckService = resourcePermissionCheckService;
    }

    public void checkPermission(User loginUser, Collection<? extends TaskDefinition> taskDefinitions) {
        if (taskDefinitions == null || taskDefinitions.isEmpty()) {
            return;
        }

        Set<Integer> datasourceIds = new HashSet<>();
        for (TaskDefinition taskDefinition : taskDefinitions) {
            AbstractParameters taskParameters = TaskPluginManager.parseTaskParameters(
                    taskDefinition.getTaskType(), taskDefinition.getTaskParams());
            ResourceParametersHelper resources = taskParameters.getResources();
            if (resources == null) {
                continue;
            }
            Map<Integer, AbstractResourceParameters> datasourceResources =
                    resources.getResourceMap(ResourceType.DATASOURCE);
            if (datasourceResources == null) {
                continue;
            }
            datasourceResources.keySet().stream()
                    .filter(datasourceId -> datasourceId != null && datasourceId > 0)
                    .forEach(datasourceIds::add);
        }

        if (datasourceIds.isEmpty()) {
            return;
        }

        int userId = loginUser.getUserType() == UserType.ADMIN_USER ? 0 : loginUser.getId();
        Integer[] datasourceIdArray = datasourceIds.toArray(new Integer[0]);
        if (!resourcePermissionCheckService.resourcePermissionCheck(
                AuthorizationType.DATASOURCE, datasourceIdArray, userId, log)) {
            log.warn("User does not have permission to use datasource referenced by task, userId:{}.",
                    loginUser.getId());
            throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
        }
    }
}
