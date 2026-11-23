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
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.stereotype.Component;

@Component
public class WorkflowRelationValidator implements IValidator<WorkflowRelationValidator.Context> {

    private final TaskProjectValidator taskProjectValidator;

    public WorkflowRelationValidator(TaskProjectValidator taskProjectValidator) {
        this.taskProjectValidator = taskProjectValidator;
    }

    @Override
    public void validate(Context context) {
        List<TaskDefinition> tasks = new ArrayList<>();
        for (WorkflowTaskRelation relation : context.getRelations()) {
            if (relation.getProjectCode() != 0 && relation.getProjectCode() != context.getProjectCode()) {
                throw new ServiceException(Status.PROJECT_WORKFLOW_NOT_MATCH);
            }
            if (relation.getWorkflowDefinitionCode() != 0
                    && relation.getWorkflowDefinitionCode() != context.getWorkflowCode()) {
                throw new ServiceException(Status.PROJECT_WORKFLOW_NOT_MATCH);
            }
            if (relation.getPreTaskCode() > 0) {
                tasks.add(new TaskDefinition(relation.getPreTaskCode(), relation.getPreTaskVersion()));
            }
            if (relation.getPostTaskCode() > 0) {
                tasks.add(new TaskDefinition(relation.getPostTaskCode(), relation.getPostTaskVersion()));
            }
        }
        taskProjectValidator.validate(new TaskProjectValidator.Context(context.getProjectCode(), tasks));
    }

    @Getter
    @AllArgsConstructor
    public static class Context {

        private final long projectCode;
        private final long workflowCode;
        private final Collection<? extends WorkflowTaskRelation> relations;
    }
}
