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
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRecoverFailureTasksResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecoverFailureTaskInstanceExecutorDelegate
        implements
            IExecutorDelegate<RecoverFailureTaskInstanceExecutorDelegate.RecoverFailureTaskInstanceOperation, Void> {

    @Autowired
    private RegistryClient registryClient;

    @Override
    public Void execute(RecoverFailureTaskInstanceOperation recoverFailureTaskInstanceOperation) {
        WorkflowInstance workflowInstance = recoverFailureTaskInstanceOperation.getWorkflowInstance();
        if (!workflowInstance.getState().isFailure()) {
            throw new ServiceException(
                    String.format("The workflow instance: %s status is %s, can not be recovered",
                            workflowInstance.getName(), workflowInstance.getState()));
        }

        final Server masterServer = registryClient.getRandomServer(RegistryNodeType.MASTER).orElse(null);
        if (masterServer == null) {
            throw new ServiceException("no master server available");
        }
        final WorkflowInstanceRecoverFailureTasksRequest recoverFailureTaskRequest =
                WorkflowInstanceRecoverFailureTasksRequest.builder()
                        .workflowInstanceId(workflowInstance.getId())
                        .userId(recoverFailureTaskInstanceOperation.executeUser.getId())
                        .build();

        final WorkflowInstanceRecoverFailureTasksResponse recoverFailureTaskResponse = Clients
                .withService(IWorkflowControlClient.class)
                .withHost(masterServer.getHost() + ":" + masterServer.getPort())
                .triggerFromFailureTasks(recoverFailureTaskRequest);
        if (!recoverFailureTaskResponse.isSuccess()) {
            throw new ServiceException("Recover workflow instance failed: " + recoverFailureTaskResponse.getMessage());
        }
        return null;
    }

    @Getter
    public static class RecoverFailureTaskInstanceOperation {

        private final RecoverFailureTaskInstanceExecutorDelegate recoverFailureTaskInstanceExecutorDelegate;

        private WorkflowInstance workflowInstance;

        private User executeUser;

        public RecoverFailureTaskInstanceOperation(RecoverFailureTaskInstanceExecutorDelegate recoverFailureTaskInstanceExecutorDelegate) {
            this.recoverFailureTaskInstanceExecutorDelegate = recoverFailureTaskInstanceExecutorDelegate;
        }

        public RecoverFailureTaskInstanceOperation onWorkflowInstance(WorkflowInstance workflowInstance) {
            this.workflowInstance = workflowInstance;
            return this;
        }

        public RecoverFailureTaskInstanceOperation byUser(User executeUser) {
            this.executeUser = executeUser;
            return this;
        }

        public void execute() {
            recoverFailureTaskInstanceExecutorDelegate.execute(this);
        }
    }
}
