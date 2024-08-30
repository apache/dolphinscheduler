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
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRepeatRunningRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRepeatRunningResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepeatRunningWorkflowInstanceExecutorDelegate
        implements
            IExecutorDelegate<RepeatRunningWorkflowInstanceExecutorDelegate.RepeatRunningWorkflowInstanceOperation, Void> {

    @Autowired
    private RegistryClient registryClient;

    @Override
    public Void execute(RepeatRunningWorkflowInstanceOperation workflowInstanceControlRequest) {
        final WorkflowInstance workflowInstance = workflowInstanceControlRequest.workflowInstance;
        if (workflowInstance.getState() == null || !workflowInstance.getState().isFinished()) {
            throw new ServiceException(
                    String.format("The workflow instance: %s status is %s, cannot repeat running",
                            workflowInstance.getName(), workflowInstance.getState()));
        }

        final Server masterServer = registryClient.getRandomServer(RegistryNodeType.MASTER).orElse(null);
        if (masterServer == null) {
            throw new ServiceException("no master server available");
        }
        final WorkflowInstanceRepeatRunningRequest repeatRunningRequest = WorkflowInstanceRepeatRunningRequest.builder()
                .workflowInstanceId(workflowInstance.getId())
                .userId(workflowInstanceControlRequest.executeUser.getId())
                .build();

        final WorkflowInstanceRepeatRunningResponse repeatRunningResponse = Clients
                .withService(IWorkflowControlClient.class)
                .withHost(masterServer.getHost() + ":" + masterServer.getPort())
                .repeatTriggerWorkflowInstance(repeatRunningRequest);
        if (!repeatRunningResponse.isSuccess()) {
            throw new ServiceException(
                    "Repeat running workflow instance failed: " + repeatRunningResponse.getMessage());
        }

        return null;
    }

    public static class RepeatRunningWorkflowInstanceOperation {

        private final RepeatRunningWorkflowInstanceExecutorDelegate repeatRunningWorkflowInstanceExecutorDelegate;

        private WorkflowInstance workflowInstance;

        private User executeUser;

        public RepeatRunningWorkflowInstanceOperation(RepeatRunningWorkflowInstanceExecutorDelegate repeatRunningWorkflowInstanceExecutorDelegate) {
            this.repeatRunningWorkflowInstanceExecutorDelegate = repeatRunningWorkflowInstanceExecutorDelegate;
        }

        public RepeatRunningWorkflowInstanceOperation onWorkflowInstance(WorkflowInstance workflowInstance) {
            this.workflowInstance = workflowInstance;
            return this;
        }

        public RepeatRunningWorkflowInstanceOperation byUser(User executeUser) {
            this.executeUser = executeUser;
            return this;
        }

        public void execute() {
            repeatRunningWorkflowInstanceExecutorDelegate.execute(this);
        }
    }

}
