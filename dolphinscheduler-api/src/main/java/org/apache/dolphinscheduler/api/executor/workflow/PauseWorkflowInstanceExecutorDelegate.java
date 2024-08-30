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
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PauseWorkflowInstanceExecutorDelegate
        implements
            IExecutorDelegate<PauseWorkflowInstanceExecutorDelegate.PauseWorkflowInstanceOperation, Void> {

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Override
    public Void execute(PauseWorkflowInstanceOperation workflowInstanceControlRequest) {
        final WorkflowInstance workflowInstance = workflowInstanceControlRequest.workflowInstance;
        exceptionIfWorkflowInstanceCannotPause(workflowInstance);
        if (ifWorkflowInstanceCanDirectPauseInDB(workflowInstance)) {
            directPauseInDB(workflowInstance);
        } else {
            pauseInMaster(workflowInstance);
        }
        return null;
    }

    private void exceptionIfWorkflowInstanceCannotPause(WorkflowInstance workflowInstance) {
        WorkflowExecutionStatus workflowInstanceState = workflowInstance.getState();
        if (workflowInstanceState.canPause()) {
            return;
        }
        throw new ServiceException(
                "The workflow instance: " + workflowInstance.getName() + " status is " + workflowInstanceState
                        + ", can not pause");
    }

    private boolean ifWorkflowInstanceCanDirectPauseInDB(WorkflowInstance workflowInstance) {
        return workflowInstance.getState().canDirectPauseInDB();
    }

    private void directPauseInDB(WorkflowInstance workflowInstance) {
        workflowInstanceDao.updateWorkflowInstanceState(
                workflowInstance.getId(),
                workflowInstance.getState(),
                WorkflowExecutionStatus.PAUSE);
        log.info("Update workflow instance {} state from: {} to {} success",
                workflowInstance.getName(),
                workflowInstance.getState().name(),
                WorkflowExecutionStatus.PAUSE.name());
    }

    private void pauseInMaster(WorkflowInstance workflowInstance) {
        try {
            final WorkflowInstancePauseResponse pauseResponse = Clients
                    .withService(IWorkflowControlClient.class)
                    .withHost(workflowInstance.getHost())
                    .pauseWorkflowInstance(new WorkflowInstancePauseRequest(workflowInstance.getId()));

            if (pauseResponse != null && pauseResponse.isSuccess()) {
                log.info("WorkflowInstance: {} pause success", workflowInstance.getName());
            } else {
                throw new ServiceException(
                        "WorkflowInstance: " + workflowInstance.getName() + " pause failed: " + pauseResponse);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    String.format("WorkflowInstance: %s pause failed", workflowInstance.getName()), e);
        }
    }

    public static class PauseWorkflowInstanceOperation {

        private final PauseWorkflowInstanceExecutorDelegate pauseWorkflowInstanceExecutorDelegate;

        private WorkflowInstance workflowInstance;

        private User executeUser;

        public PauseWorkflowInstanceOperation(PauseWorkflowInstanceExecutorDelegate pauseWorkflowInstanceExecutorDelegate) {
            this.pauseWorkflowInstanceExecutorDelegate = pauseWorkflowInstanceExecutorDelegate;
        }

        public PauseWorkflowInstanceExecutorDelegate.PauseWorkflowInstanceOperation onWorkflowInstance(WorkflowInstance workflowInstance) {
            this.workflowInstance = workflowInstance;
            return this;
        }

        public PauseWorkflowInstanceExecutorDelegate.PauseWorkflowInstanceOperation byUser(User executeUser) {
            this.executeUser = executeUser;
            return this;
        }

        public void execute() {
            pauseWorkflowInstanceExecutorDelegate.execute(this);
        }
    }
}
