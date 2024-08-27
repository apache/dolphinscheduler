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
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StopWorkflowInstanceExecutorDelegate
        implements
            IExecutorDelegate<StopWorkflowInstanceExecutorDelegate.StopWorkflowInstanceOperation, Void> {

    @Autowired
    private ProcessInstanceDao workflowInstanceDao;

    @Override
    public Void execute(StopWorkflowInstanceOperation workflowInstanceControlRequest) {
        final ProcessInstance workflowInstance = workflowInstanceControlRequest.workflowInstance;
        exceptionIfWorkflowInstanceCannotStop(workflowInstance);

        if (ifWorkflowInstanceCanDirectStopInDB(workflowInstance)) {
            directStopInDB(workflowInstance);
        } else {
            stopInMaster(workflowInstance);
        }
        return null;
    }

    void exceptionIfWorkflowInstanceCannotStop(ProcessInstance workflowInstance) {
        final WorkflowExecutionStatus workflowInstanceState = workflowInstance.getState();
        if (workflowInstanceState.canStop()) {
            return;
        }
        throw new ServiceException(
                "The workflow instance: " + workflowInstance.getName() + " status is " + workflowInstanceState
                        + ", can not stop");
    }

    boolean ifWorkflowInstanceCanDirectStopInDB(ProcessInstance workflowInstance) {
        return workflowInstance.getState().canDirectStopInDB();
    }

    void directStopInDB(ProcessInstance workflowInstance) {
        workflowInstanceDao.updateWorkflowInstanceState(
                workflowInstance.getId(),
                workflowInstance.getState(),
                WorkflowExecutionStatus.STOP);
        log.info("Update workflow instance {} state from: {} to {} success",
                workflowInstance.getName(),
                workflowInstance.getState().name(),
                WorkflowExecutionStatus.STOP.name());
    }

    void stopInMaster(ProcessInstance workflowInstance) {
        try {
            final WorkflowInstanceStopResponse stopResponse = Clients
                    .withService(IWorkflowControlClient.class)
                    .withHost(workflowInstance.getHost())
                    .stopWorkflowInstance(new WorkflowInstanceStopRequest(workflowInstance.getId()));

            if (stopResponse != null && stopResponse.isSuccess()) {
                log.info("WorkflowInstance: {} stop success", workflowInstance.getName());
            } else {
                throw new ServiceException(
                        "WorkflowInstance: " + workflowInstance.getName() + " stop failed: " + stopResponse);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    String.format("WorkflowInstance: %s stop failed", workflowInstance.getName()), e);
        }
    }

    public static class StopWorkflowInstanceOperation {

        private final StopWorkflowInstanceExecutorDelegate stopWorkflowInstanceExecutorDelegate;

        private ProcessInstance workflowInstance;

        private User executeUser;

        public StopWorkflowInstanceOperation(StopWorkflowInstanceExecutorDelegate stopWorkflowInstanceExecutorDelegate) {
            this.stopWorkflowInstanceExecutorDelegate = stopWorkflowInstanceExecutorDelegate;
        }

        public StopWorkflowInstanceExecutorDelegate.StopWorkflowInstanceOperation onWorkflowInstance(ProcessInstance workflowInstance) {
            this.workflowInstance = workflowInstance;
            return this;
        }

        public StopWorkflowInstanceExecutorDelegate.StopWorkflowInstanceOperation byUser(User executeUser) {
            this.executeUser = executeUser;
            return this;
        }

        public void execute() {
            stopWorkflowInstanceExecutorDelegate.execute(this);
        }
    }
}
