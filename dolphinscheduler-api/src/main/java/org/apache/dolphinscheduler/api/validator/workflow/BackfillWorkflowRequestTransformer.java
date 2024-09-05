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

package org.apache.dolphinscheduler.api.validator.workflow;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowBackFillRequest;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.utils.WorkflowUtils;
import org.apache.dolphinscheduler.api.validator.ITransformer;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertyUtils;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BackfillWorkflowRequestTransformer implements ITransformer<WorkflowBackFillRequest, BackfillWorkflowDTO> {

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Override
    public BackfillWorkflowDTO transform(WorkflowBackFillRequest workflowBackFillRequest) {

        final BackfillWorkflowDTO.BackfillParamsDTO backfillParams =
                transformBackfillParamsDTO(workflowBackFillRequest);
        final BackfillWorkflowDTO backfillWorkflowDTO = BackfillWorkflowDTO.builder()
                .loginUser(workflowBackFillRequest.getLoginUser())
                .startNodes(WorkflowUtils.parseStartNodeList(workflowBackFillRequest.getStartNodes()))
                .failureStrategy(workflowBackFillRequest.getFailureStrategy())
                .taskDependType(workflowBackFillRequest.getTaskDependType())
                .execType(workflowBackFillRequest.getExecType())
                .warningType(workflowBackFillRequest.getWarningType())
                .warningGroupId(workflowBackFillRequest.getWarningGroupId())
                .runMode(workflowBackFillRequest.getBackfillRunMode())
                .workflowInstancePriority(workflowBackFillRequest.getWorkflowInstancePriority())
                .workerGroup(workflowBackFillRequest.getWorkerGroup())
                .tenantCode(workflowBackFillRequest.getTenantCode())
                .environmentCode(workflowBackFillRequest.getEnvironmentCode())
                .startParamList(
                        PropertyUtils.startParamsTransformPropertyList(workflowBackFillRequest.getStartParamList()))
                .dryRun(workflowBackFillRequest.getDryRun())
                .testFlag(workflowBackFillRequest.getTestFlag())
                .triggerCode(CodeGenerateUtils.genCode())
                .backfillParams(backfillParams)
                .build();

        WorkflowDefinition workflowDefinition = workflowDefinitionDao
                .queryByCode(workflowBackFillRequest.getWorkflowDefinitionCode())
                .orElseThrow(() -> new ServiceException(
                        "Cannot find the workflow: " + workflowBackFillRequest.getWorkflowDefinitionCode()));

        backfillWorkflowDTO.setWorkflowDefinition(workflowDefinition);
        return backfillWorkflowDTO;
    }

    private BackfillWorkflowDTO.BackfillParamsDTO transformBackfillParamsDTO(WorkflowBackFillRequest workflowBackFillRequest) {
        final List<ZonedDateTime> backfillDateList = parseBackfillDateList(workflowBackFillRequest);
        return BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(workflowBackFillRequest.getBackfillRunMode())
                .expectedParallelismNumber(workflowBackFillRequest.getExpectedParallelismNumber())
                .backfillDateList(backfillDateList)
                .backfillDependentMode(workflowBackFillRequest.getBackfillDependentMode())
                .allLevelDependent(workflowBackFillRequest.isAllLevelDependent())
                .executionOrder(workflowBackFillRequest.getExecutionOrder())
                .build();
    }

    @SneakyThrows
    private List<ZonedDateTime> parseBackfillDateList(WorkflowBackFillRequest workflowBackFillRequest) {
        final WorkflowBackFillRequest.BackfillTime backfillTime = workflowBackFillRequest.getBackfillTime();
        List<Schedule> schedules = processService.queryReleaseSchedulerListByWorkflowDefinitionCode(
                workflowBackFillRequest.getWorkflowDefinitionCode());

        if (StringUtils.isNotEmpty(backfillTime.getComplementStartDate())
                && StringUtils.isNotEmpty(backfillTime.getComplementEndDate())) {
            // todo: why we need to filter the schedules here?
            return CronUtils.getSelfFireDateList(
                    DateUtils.stringToZoneDateTime(backfillTime.getComplementStartDate()),
                    DateUtils.stringToZoneDateTime(backfillTime.getComplementEndDate()),
                    schedules);
        }
        if (StringUtils.isNotEmpty(backfillTime.getComplementScheduleDateList())) {
            return Arrays.stream(backfillTime.getComplementScheduleDateList().split(","))
                    .distinct()
                    .map(DateUtils::stringToZoneDateTime)
                    .collect(Collectors.toList());
        }
        throw new ServiceException("backfillTime: " + backfillTime + " is invalid");
    }
}
