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

package org.apache.dolphinscheduler.api.validator.workflow;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowTriggerRequest;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.utils.WorkflowUtils;
import org.apache.dolphinscheduler.api.validator.ITransformer;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertyUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TriggerWorkflowRequestTransformer implements ITransformer<WorkflowTriggerRequest, TriggerWorkflowDTO> {

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Override
    public TriggerWorkflowDTO transform(WorkflowTriggerRequest workflowTriggerRequest) {
        TriggerWorkflowDTO triggerWorkflowDTO = TriggerWorkflowDTO.builder()
                .loginUser(workflowTriggerRequest.getLoginUser())
                .startNodes(WorkflowUtils.parseStartNodeList(workflowTriggerRequest.getStartNodes()))
                .failureStrategy(workflowTriggerRequest.getFailureStrategy())
                .taskDependType(workflowTriggerRequest.getTaskDependType())
                .execType(workflowTriggerRequest.getExecType())
                .warningType(workflowTriggerRequest.getWarningType())
                .warningGroupId(workflowTriggerRequest.getWarningGroupId())
                .workflowInstancePriority(workflowTriggerRequest.getWorkflowInstancePriority())
                .workerGroup(workflowTriggerRequest.getWorkerGroup())
                .tenantCode(workflowTriggerRequest.getTenantCode())
                .environmentCode(workflowTriggerRequest.getEnvironmentCode())
                .startParamList(
                        PropertyUtils.startParamsTransformPropertyList(workflowTriggerRequest.getStartParamList()))
                .dryRun(workflowTriggerRequest.getDryRun())
                .testFlag(workflowTriggerRequest.getTestFlag())
                .build();

        WorkflowDefinition workflowDefinition = workflowDefinitionDao
                .queryByCode(workflowTriggerRequest.getWorkflowDefinitionCode())
                .orElseThrow(() -> new ServiceException(
                        "Cannot find the workflow: " + workflowTriggerRequest.getWorkflowDefinitionCode()));

        triggerWorkflowDTO.setWorkflowDefinition(workflowDefinition);
        return triggerWorkflowDTO;
    }
}
