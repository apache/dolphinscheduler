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

import org.apache.commons.lang3.StringUtils;

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

        if (StringUtils.isNotEmpty(workerGroup)
                && !projectWorkerGroupRelationService.isWorkerGroupAssignedToProject(projectCode, workerGroup)) {
            log.warn("Worker group {} is not assigned to project {}", workerGroup, projectCode);
            throw new ServiceException(Status.WORKER_GROUP_NOT_ASSIGNED_TO_PROJECT, workerGroup);
        }
    }
}
