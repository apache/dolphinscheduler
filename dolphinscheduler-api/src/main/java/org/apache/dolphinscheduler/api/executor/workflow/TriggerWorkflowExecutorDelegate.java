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

package org.apache.dolphinscheduler.api.executor.workflow;

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTO;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TriggerWorkflowExecutorDelegate implements IExecutorDelegate<TriggerWorkflowDTO, Integer> {

    @Autowired
    private RegistryClient registryClient;

    @Override
    public Integer execute(final TriggerWorkflowDTO triggerWorkflowDTO) {
        final Server masterServer = registryClient.getRandomServer(RegistryNodeType.MASTER).orElse(null);
        if (masterServer == null) {
            throw new ServiceException("no master server available");
        }
        final WorkflowManualTriggerResponse workflowManualTriggerResponse = Clients
                .withService(IWorkflowControlClient.class)
                .withHost(masterServer.getHost() + ":" + masterServer.getPort())
                .manualTriggerWorkflow(transform2WorkflowTriggerRequest(triggerWorkflowDTO));
        if (!workflowManualTriggerResponse.isSuccess()) {
            throw new ServiceException("Trigger workflow failed: " + workflowManualTriggerResponse.getMessage());
        }
        return workflowManualTriggerResponse.getWorkflowInstanceId();

    }

    private WorkflowManualTriggerRequest transform2WorkflowTriggerRequest(TriggerWorkflowDTO triggerWorkflowDTO) {
        return WorkflowManualTriggerRequest.builder()
                .userId(triggerWorkflowDTO.getLoginUser().getId())
                .workflowDefinitionCode(triggerWorkflowDTO.getWorkflowDefinition().getCode())
                .workflowDefinitionVersion(triggerWorkflowDTO.getWorkflowDefinition().getVersion())
                .startNodes(triggerWorkflowDTO.getStartNodes())
                .failureStrategy(triggerWorkflowDTO.getFailureStrategy())
                .taskDependType(triggerWorkflowDTO.getTaskDependType())
                .warningType(triggerWorkflowDTO.getWarningType())
                .warningGroupId(triggerWorkflowDTO.getWarningGroupId())
                .workflowInstancePriority(triggerWorkflowDTO.getWorkflowInstancePriority())
                .workerGroup(triggerWorkflowDTO.getWorkerGroup())
                .tenantCode(triggerWorkflowDTO.getTenantCode())
                .environmentCode(triggerWorkflowDTO.getEnvironmentCode())
                .startParamList(triggerWorkflowDTO.getStartParamList())
                .dryRun(triggerWorkflowDTO.getDryRun())
                .testFlag(triggerWorkflowDTO.getTestFlag())
                .build();
    }
}
