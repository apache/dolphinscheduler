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

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

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

    @Override
    @Transactional
    public TriggerResponse triggerWorkflow(final TriggerRequest triggerRequest) {
        final WorkflowInstance workflowInstance = constructWorkflowInstance(triggerRequest);
        workflowInstanceDao.insert(workflowInstance);

        final Command command = constructTriggerCommand(triggerRequest, workflowInstance);
        commandDao.insert(command);

        return onTriggerSuccess(workflowInstance);
    }

    protected abstract WorkflowInstance constructWorkflowInstance(final TriggerRequest triggerRequest);

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
