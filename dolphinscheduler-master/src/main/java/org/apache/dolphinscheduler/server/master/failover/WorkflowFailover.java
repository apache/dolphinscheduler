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

package org.apache.dolphinscheduler.server.master.failover;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.WorkflowFailoverCommandParam;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class WorkflowFailover {

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private CommandDao commandDao;

    @Transactional
    public void failoverWorkflow(final WorkflowInstance workflowInstance) {
        workflowInstanceDao.updateWorkflowInstanceState(
                workflowInstance.getId(),
                workflowInstance.getState(),
                WorkflowExecutionStatus.FAILOVER);

        final WorkflowFailoverCommandParam failoverWorkflowCommandParam = WorkflowFailoverCommandParam.builder()
                .workflowExecutionStatus(workflowInstance.getState())
                .build();

        final Command failoverCommand = Command.builder()
                .commandParam(JSONUtils.toJsonString(failoverWorkflowCommandParam))
                .commandType(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS)
                .workflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode())
                .workflowDefinitionVersion(workflowInstance.getWorkflowDefinitionVersion())
                .workflowInstanceId(workflowInstance.getId())
                .build();
        commandDao.insert(failoverCommand);
        log.info("Success failover workflowInstance: [id={}, name={}, state={}]",
                workflowInstance.getId(),
                workflowInstance.getName(),
                workflowInstance.getState().name());
    }

}
