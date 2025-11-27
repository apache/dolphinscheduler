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

package org.apache.dolphinscheduler.server.master.engine.workflow.trigger;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import org.apache.commons.lang3.tuple.ImmutablePair;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class AbstractWorkflowTrigger<TriggerRequest, TriggerResponse>
        implements
            IWorkflowTrigger<TriggerRequest, TriggerResponse> {

    @Autowired
    private WorkflowDefinitionLogDao workflowDefinitionDao;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommandDao commandDao;

    @Autowired
    private SerialCommandDao serialCommandDao;

    @Override
    @Transactional
    public TriggerResponse triggerWorkflow(final TriggerRequest triggerRequest) {
        final ImmutablePair<WorkflowDefinition, WorkflowInstance> pair = constructWorkflowInstance(triggerRequest);
        final WorkflowDefinition workflowDefinition = pair.getLeft();
        final WorkflowInstance workflowInstance = pair.getRight();
        if (workflowDefinition.getExecutionType() == WorkflowExecutionTypeEnum.PARALLEL) {
            workflowInstanceDao.insert(workflowInstance);
            final Command command = constructTriggerCommand(triggerRequest, workflowInstance);
            commandDao.insert(command);
        } else {
            workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SERIAL_WAIT, "Waiting for serial execution");
            workflowInstanceDao.insert(workflowInstance);
            final Command command = constructTriggerCommand(triggerRequest, workflowInstance);
            serialCommandDao.insert(SerialCommandDto.newSerialCommand(command).toEntity());
        }

        return onTriggerSuccess(workflowInstance);
    }

    // todo: 使用WorkflowInstanceConstructor封装
    protected abstract ImmutablePair<WorkflowDefinition, WorkflowInstance> constructWorkflowInstance(final TriggerRequest triggerRequest);

    // todo: 使用CommandConstructor封装
    protected abstract Command constructTriggerCommand(final TriggerRequest triggerRequest,
                                                       final WorkflowInstance workflowInstance);

    protected abstract TriggerResponse onTriggerSuccess(final WorkflowInstance workflowInstance);

    protected WorkflowDefinition getProcessDefinition(final Long workflowCode, final Integer workflowVersion) {
        final WorkflowDefinitionLog workflow = workflowDefinitionDao.queryByDefinitionCodeAndVersion(
                workflowCode, workflowVersion);
        if (workflow == null) {
            throw new IllegalStateException(
                    "Workflow definition not found: " + workflowCode + " version " + workflowVersion);
        }
        return workflow;
    }

    protected User getExecutorUser(final Integer userId) {
        return userDao.queryOptionalById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }

}
