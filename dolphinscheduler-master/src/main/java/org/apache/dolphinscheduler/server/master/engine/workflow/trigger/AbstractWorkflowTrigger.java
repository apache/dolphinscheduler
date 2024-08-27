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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class AbstractWorkflowTrigger<TriggerRequest, TriggerResponse>
        implements
            IWorkflowTrigger<TriggerRequest, TriggerResponse> {

    @Autowired
    private ProcessDefinitionLogDao workflowDefinitionDao;

    @Autowired
    private ProcessInstanceDao workflowInstanceDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommandDao commandDao;

    @Override
    @Transactional
    public TriggerResponse triggerWorkflow(final TriggerRequest triggerRequest) {
        final ProcessInstance workflowInstance = constructWorkflowInstance(triggerRequest);
        workflowInstanceDao.insert(workflowInstance);

        final Command command = constructTriggerCommand(triggerRequest, workflowInstance);
        commandDao.insert(command);

        return onTriggerSuccess(workflowInstance);
    }

    protected abstract ProcessInstance constructWorkflowInstance(final TriggerRequest triggerRequest);

    protected abstract Command constructTriggerCommand(final TriggerRequest triggerRequest,
                                                       final ProcessInstance workflowInstance);

    protected abstract TriggerResponse onTriggerSuccess(final ProcessInstance workflowInstance);

    protected ProcessDefinition getProcessDefinition(final Long workflowCode, final Integer workflowVersion) {
        final ProcessDefinitionLog workflow = workflowDefinitionDao.queryByDefinitionCodeAndVersion(
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
