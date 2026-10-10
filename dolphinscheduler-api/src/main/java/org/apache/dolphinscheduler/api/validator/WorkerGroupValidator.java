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
import org.apache.dolphinscheduler.api.service.ProjectWorkerGroupRelationService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator for workerGroup validation
 * Checks if the workerGroup is assigned to the project
 */
@Slf4j
@Component
public class WorkerGroupValidator implements IValidator<WorkerGroupValidationContext> {

    @Autowired
    private ProjectWorkerGroupRelationService projectWorkerGroupRelationService;

    @Override
    public void validate(final WorkerGroupValidationContext context) {
        String workerGroup = context.getWorkerGroup();
        long projectCode = context.getProjectCode();

        if (StringUtils.isEmpty(workerGroup)) {
            log.warn("Worker group is empty or null for project {}", projectCode);
            throw new ServiceException(Status.WORKER_GROUP_NOT_ASSIGNED_TO_PROJECT, workerGroup);
        }

        Set<String> assignedWorkerGroupNames =
                projectWorkerGroupRelationService.getAllAssignedWorkerGroupNames(projectCode);

        if (assignedWorkerGroupNames == null || !assignedWorkerGroupNames.contains(workerGroup)) {
            log.warn("Worker group {} is not assigned to project {}", workerGroup, projectCode);
            throw new ServiceException(Status.WORKER_GROUP_NOT_ASSIGNED_TO_PROJECT, workerGroup);
        }
    }

    /**
     * Validate a list of workerGroups are assigned to the project
     * This method queries the assigned workerGroups once and then checks all workerGroups against it
     *
     * @param workerGroups the list of workerGroups to validate
     * @param projectCode the project code
     */
    public void validate(final List<String> workerGroups, final long projectCode) {
        if (CollectionUtils.isEmpty(workerGroups)) {
            return;
        }

        List<String> distinctWorkerGroups = workerGroups.stream()
                .distinct()
                .collect(Collectors.toList());

        Set<String> assignedWorkerGroupNames = projectWorkerGroupRelationService
                .getAllAssignedWorkerGroupNames(projectCode);

        if (assignedWorkerGroupNames == null) {
            assignedWorkerGroupNames = new java.util.HashSet<>();
        }

        Set<String> finalAssignedWorkerGroupNames = assignedWorkerGroupNames;
        List<String> unassignedWorkerGroups = distinctWorkerGroups.stream()
                .filter(wg -> StringUtils.isEmpty(wg) || !finalAssignedWorkerGroupNames.contains(wg))
                .collect(Collectors.toList());

        if (!unassignedWorkerGroups.isEmpty()) {
            log.warn("Worker groups {} are not assigned to project {}", unassignedWorkerGroups, projectCode);
            throw new ServiceException(Status.WORKER_GROUP_NOT_ASSIGNED_TO_PROJECT,
                    String.join(",", unassignedWorkerGroups));
        }
    }
}
