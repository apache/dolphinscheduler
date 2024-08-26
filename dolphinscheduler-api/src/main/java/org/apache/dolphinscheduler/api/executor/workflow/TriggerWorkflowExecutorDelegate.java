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

import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTO;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.service.process.TriggerRelationService;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TriggerWorkflowExecutorDelegate implements IExecutorDelegate<TriggerWorkflowDTO, Void> {

    @Autowired
    private CommandDao commandDao;

    @Autowired
    private TriggerRelationService triggerRelationService;

    @Override
    public Void execute(TriggerWorkflowDTO triggerWorkflowDTO) {
        final RunWorkflowCommandParam runWorkflowCommandParam =
                RunWorkflowCommandParam.builder()
                        .commandParams(triggerWorkflowDTO.getStartParamList())
                        .startNodes(triggerWorkflowDTO.getStartNodes())
                        .timeZone(DateUtils.getTimezone())
                        .build();
        final Command command = Command.builder()
                .commandType(triggerWorkflowDTO.getExecType())
                .processDefinitionCode(triggerWorkflowDTO.getWorkflowDefinition().getCode())
                .processDefinitionVersion(triggerWorkflowDTO.getWorkflowDefinition().getVersion())
                .executorId(triggerWorkflowDTO.getLoginUser().getId())
                .commandParam(JSONUtils.toJsonString(runWorkflowCommandParam))
                .taskDependType(triggerWorkflowDTO.getTaskDependType())
                .failureStrategy(triggerWorkflowDTO.getFailureStrategy())
                .warningType(triggerWorkflowDTO.getWarningType())
                .warningGroupId(triggerWorkflowDTO.getWarningGroupId())
                .startTime(new Date())
                .processInstancePriority(triggerWorkflowDTO.getWorkflowInstancePriority())
                .updateTime(new Date())
                .workerGroup(triggerWorkflowDTO.getWorkerGroup())
                .tenantCode(triggerWorkflowDTO.getTenantCode())
                .dryRun(triggerWorkflowDTO.getDryRun().getCode())
                .testFlag(triggerWorkflowDTO.getTestFlag().getCode())
                .build();
        commandDao.insert(command);
        return null;
    }
}
