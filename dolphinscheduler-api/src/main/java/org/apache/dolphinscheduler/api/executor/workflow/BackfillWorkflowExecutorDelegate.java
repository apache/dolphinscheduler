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

import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Slf4j
@Component
public class BackfillWorkflowExecutorDelegate implements IExecutorDelegate<BackfillWorkflowDTO, Void> {

    @Autowired
    private CommandDao commandDao;

    @Autowired
    private ProcessService processService;

    @Override
    public Void execute(final BackfillWorkflowDTO backfillWorkflowDTO) {
        // todo: directly call the master api to do backfill
        if (backfillWorkflowDTO.getBackfillParams().getRunMode() == RunMode.RUN_MODE_SERIAL) {
            doSerialBackfillWorkflow(backfillWorkflowDTO);
        } else {
            doParallemBackfillWorkflow(backfillWorkflowDTO);
        }
        return null;
    }

    private void doSerialBackfillWorkflow(final BackfillWorkflowDTO backfillWorkflowDTO) {
        final BackfillWorkflowDTO.BackfillParamsDTO backfillParams = backfillWorkflowDTO.getBackfillParams();
        final List<ZonedDateTime> backfillTimeList = backfillParams.getBackfillDateList();
        if (backfillParams.getExecutionOrder() == ExecutionOrder.DESC_ORDER) {
            Collections.sort(backfillTimeList, Collections.reverseOrder());
        } else {
            Collections.sort(backfillTimeList);
        }

        final BackfillWorkflowCommandParam backfillWorkflowCommandParam = BackfillWorkflowCommandParam.builder()
                .commandParams(backfillWorkflowDTO.getStartParamList())
                .startNodes(backfillWorkflowDTO.getStartNodes())
                .backfillTimeList(backfillTimeList.stream().map(DateUtils::dateToString).collect(Collectors.toList()))
                .timeZone(DateUtils.getTimezone())
                .build();

        doCreateCommand(backfillWorkflowDTO, backfillWorkflowCommandParam);
    }

    private void doParallemBackfillWorkflow(final BackfillWorkflowDTO backfillWorkflowDTO) {
        final BackfillWorkflowDTO.BackfillParamsDTO backfillParams = backfillWorkflowDTO.getBackfillParams();
        Integer expectedParallelismNumber = backfillParams.getExpectedParallelismNumber();

        List<ZonedDateTime> listDate = backfillParams.getBackfillDateList();
        if (expectedParallelismNumber != null) {
            expectedParallelismNumber = Math.min(listDate.size(), expectedParallelismNumber);
        } else {
            expectedParallelismNumber = listDate.size();
        }

        log.info("In parallel mode, current expectedParallelismNumber:{}", expectedParallelismNumber);
        for (List<ZonedDateTime> stringDate : Lists.partition(listDate, expectedParallelismNumber)) {
            final BackfillWorkflowCommandParam backfillWorkflowCommandParam = BackfillWorkflowCommandParam.builder()
                    .commandParams(backfillWorkflowDTO.getStartParamList())
                    .startNodes(backfillWorkflowDTO.getStartNodes())
                    .backfillTimeList(stringDate.stream().map(DateUtils::dateToString).collect(Collectors.toList()))
                    .timeZone(DateUtils.getTimezone())
                    .build();
            doCreateCommand(backfillWorkflowDTO, backfillWorkflowCommandParam);
        }
    }

    private void doCreateCommand(final BackfillWorkflowDTO backfillWorkflowDTO,
                                 final BackfillWorkflowCommandParam backfillWorkflowCommandParam) {
        List<String> backfillTimeList = backfillWorkflowCommandParam.getBackfillTimeList();
        final Command command = Command.builder()
                .commandType(backfillWorkflowDTO.getExecType())
                .processDefinitionCode(backfillWorkflowDTO.getWorkflowDefinition().getCode())
                .processDefinitionVersion(backfillWorkflowDTO.getWorkflowDefinition().getVersion())
                .executorId(backfillWorkflowDTO.getLoginUser().getId())
                .scheduleTime(DateUtils.stringToDate(backfillTimeList.get(0)))
                .commandParam(JSONUtils.toJsonString(backfillWorkflowCommandParam))
                .taskDependType(backfillWorkflowDTO.getTaskDependType())
                .failureStrategy(backfillWorkflowDTO.getFailureStrategy())
                .warningType(backfillWorkflowDTO.getWarningType())
                .warningGroupId(backfillWorkflowDTO.getWarningGroupId())
                .startTime(new Date())
                .processInstancePriority(backfillWorkflowDTO.getWorkflowInstancePriority())
                .updateTime(new Date())
                .workerGroup(backfillWorkflowDTO.getWorkerGroup())
                .tenantCode(backfillWorkflowDTO.getTenantCode())
                .dryRun(backfillWorkflowDTO.getDryRun().getCode())
                .testFlag(backfillWorkflowDTO.getTestFlag().getCode())
                .build();
        commandDao.insert(command);
        final BackfillWorkflowDTO.BackfillParamsDTO backfillParams = backfillWorkflowDTO.getBackfillParams();
        if (backfillParams.getBackfillDependentMode() == ComplementDependentMode.ALL_DEPENDENT) {
            doBackfillDependentWorkflow(backfillWorkflowCommandParam, command);
        }
    }

    private void doBackfillDependentWorkflow(final BackfillWorkflowCommandParam backfillWorkflowCommandParam,
                                             final Command backfillCommand) {
    }
}
