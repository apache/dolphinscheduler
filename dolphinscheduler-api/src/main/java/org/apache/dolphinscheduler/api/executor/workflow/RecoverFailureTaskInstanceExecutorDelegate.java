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
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.CommandDao;

import java.util.Date;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecoverFailureTaskInstanceExecutorDelegate
        implements
            IExecutorDelegate<RecoverFailureTaskInstanceExecutorDelegate.RecoverFailureTaskInstanceOperation, Void> {

    @Autowired
    private CommandDao commandDao;

    @Override
    public Void execute(RecoverFailureTaskInstanceOperation recoverFailureTaskInstanceOperation) {
        WorkflowInstance workflowInstance = recoverFailureTaskInstanceOperation.getWorkflowInstance();
        if (!workflowInstance.getState().isFailure()) {
            throw new ServiceException(
                    String.format("The workflow instance: %s status is %s, can not be recovered",
                            workflowInstance.getName(), workflowInstance.getState()));
        }

        Command command = Command.builder()
                .commandType(CommandType.START_FAILURE_TASK_WORKFLOW)
                .processDefinitionCode(workflowInstance.getProcessDefinitionCode())
                .processDefinitionVersion(workflowInstance.getProcessDefinitionVersion())
                .processInstanceId(workflowInstance.getId())
                .executorId(recoverFailureTaskInstanceOperation.getExecuteUser().getId())
                .startTime(new Date())
                .updateTime(new Date())
                .build();
        commandDao.insert(command);
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
