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
import org.apache.dolphinscheduler.api.service.WorkflowLineageService;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Slf4j
@Component
public class BackfillWorkflowExecutorDelegate implements IExecutorDelegate<BackfillWorkflowDTO, List<Integer>> {

    @Autowired
    private WorkflowLineageService workflowLineageService;

    @Autowired
    private RegistryClient registryClient;

    @Override
    public List<Integer> execute(final BackfillWorkflowDTO backfillWorkflowDTO) {
        return executeWithDependentExpansion(backfillWorkflowDTO);
    }

    /**
     * Expands optional downstream workflows, then submits root and each downstream in list order.
     * <p>
     * {@link RunMode} (serial vs parallel date sharding) is taken only from the <strong>root</strong>
     * {@code backfillWorkflowDTO}'s {@link BackfillWorkflowDTO.BackfillParamsDTO#getRunMode()}; downstream DTOs
     * mirror the same mode in their params for consistency.
     */
    List<Integer> executeWithDependentExpansion(final BackfillWorkflowDTO backfillWorkflowDTO) {
        // todo: directly call the master api to do backfill
        List<BackfillWorkflowDTO> dependentBackfillDtos = new ArrayList<>();
        dependentBackfillDtos.add(backfillWorkflowDTO);
        if (backfillWorkflowDTO.getBackfillParams()
                .getBackfillDependentMode() == ComplementDependentMode.ALL_DEPENDENT) {

            List<WorkflowDefinition> downstreamWorkflowList =
                    workflowLineageService.resolveDownstreamWorkflowDefinitionCodes(
                            backfillWorkflowDTO.getWorkflowDefinition().getCode(),
                            backfillWorkflowDTO.getBackfillParams().isAllLevelDependent(),
                            true);
            if (downstreamWorkflowList.isEmpty()) {
                log.info("No downstream dependent workflows found for workflow code {}",
                        backfillWorkflowDTO.getWorkflowDefinition().getCode());
            } else {
                dependentBackfillDtos.addAll(buildResolvedDownstreamBackfillDtos(backfillWorkflowDTO,
                        backfillWorkflowDTO.getBackfillParams().getBackfillDateList(),
                        downstreamWorkflowList));
            }
        }
        List<Integer> workflowInstanceIdList = new ArrayList<>();
        // RunMode is defined by the root request only (not per downstream DTO).
        if (backfillWorkflowDTO.getBackfillParams().getRunMode() == RunMode.RUN_MODE_SERIAL) {
            for (BackfillWorkflowDTO dependentDto : dependentBackfillDtos) {
                workflowInstanceIdList.addAll(doSerialBackfillWorkflow(dependentDto));
            }
        } else {
            for (BackfillWorkflowDTO dependentDto : dependentBackfillDtos) {
                workflowInstanceIdList.addAll(doParallelBackfillWorkflow(dependentDto));
            }
        }
        return workflowInstanceIdList;
    }

    private List<Integer> doSerialBackfillWorkflow(final BackfillWorkflowDTO backfillWorkflowDTO) {
        final BackfillWorkflowDTO.BackfillParamsDTO backfillParams = backfillWorkflowDTO.getBackfillParams();
        final List<ZonedDateTime> backfillTimeList = backfillParams.getBackfillDateList();
        if (backfillParams.getExecutionOrder() == ExecutionOrder.DESC_ORDER) {
            Collections.sort(backfillTimeList, Collections.reverseOrder());
        } else {
            Collections.sort(backfillTimeList);
        }

        final Integer workflowInstanceId = doBackfillWorkflow(backfillWorkflowDTO, backfillTimeList);
        return Lists.newArrayList(workflowInstanceId);
    }

    private List<Integer> doParallelBackfillWorkflow(final BackfillWorkflowDTO backfillWorkflowDTO) {
        final BackfillWorkflowDTO.BackfillParamsDTO backfillParams = backfillWorkflowDTO.getBackfillParams();
        Integer expectedParallelismNumber = backfillParams.getExpectedParallelismNumber();

        List<ZonedDateTime> listDate = backfillParams.getBackfillDateList();
        if (expectedParallelismNumber != null) {
            expectedParallelismNumber = Math.min(listDate.size(), expectedParallelismNumber);
        } else {
            expectedParallelismNumber = listDate.size();
        }

        log.info("In parallel mode, current expectedParallelismNumber: {}", expectedParallelismNumber);
        final List<Integer> workflowInstanceIdList = Lists.newArrayList();
        for (List<ZonedDateTime> stringDate : splitDateTime(listDate, expectedParallelismNumber)) {
            final Integer workflowInstanceId = doBackfillWorkflow(
                    backfillWorkflowDTO, stringDate);
            workflowInstanceIdList.add(workflowInstanceId);
        }
        return workflowInstanceIdList;
    }

    /**
     * split date time list into n parts, the last part may be larger if not divisible
     */
    private List<List<ZonedDateTime>> splitDateTime(List<ZonedDateTime> dateTimeList, int numParts) {
        List<List<ZonedDateTime>> result = new ArrayList<>();
        int n = dateTimeList.size();

        int baseSize = n / numParts;
        int remainder = n % numParts;

        int start = 0;
        for (int i = 0; i < numParts; i++) {
            int currentSize = baseSize;
            if (i == numParts - 1) {
                currentSize += remainder;
            }
            List<ZonedDateTime> part = dateTimeList.subList(start, start + currentSize);
            result.add(part);
            start += currentSize;
        }

        return result;
    }

    private Integer doBackfillWorkflow(final BackfillWorkflowDTO backfillWorkflowDTO,
                                       final List<ZonedDateTime> backfillDateTimes) {
        final Server masterServer = registryClient.getRandomServer(RegistryNodeType.MASTER).orElse(null);
        if (masterServer == null) {
            throw new ServiceException("no master server available");
        }

        final List<String> backfillTimeList =
                backfillDateTimes.stream().map(DateUtils::dateToString).collect(Collectors.toList());

        final WorkflowDefinition workflowDefinition = backfillWorkflowDTO.getWorkflowDefinition();
        final WorkflowBackfillTriggerRequest backfillTriggerRequest = WorkflowBackfillTriggerRequest.builder()
                .userId(backfillWorkflowDTO.getLoginUser().getId())
                .backfillTimeList(backfillTimeList)
                .workflowCode(workflowDefinition.getCode())
                .workflowVersion(workflowDefinition.getVersion())
                .startNodes(backfillWorkflowDTO.getStartNodes())
                .failureStrategy(backfillWorkflowDTO.getFailureStrategy())
                .taskDependType(backfillWorkflowDTO.getTaskDependType())
                .warningType(backfillWorkflowDTO.getWarningType())
                .warningGroupId(backfillWorkflowDTO.getWarningGroupId())
                .workflowInstancePriority(backfillWorkflowDTO.getWorkflowInstancePriority())
                .workerGroup(backfillWorkflowDTO.getWorkerGroup())
                .tenantCode(backfillWorkflowDTO.getTenantCode())
                .environmentCode(backfillWorkflowDTO.getEnvironmentCode())
                .startParamList(backfillWorkflowDTO.getStartParamList())
                .dryRun(backfillWorkflowDTO.getDryRun())
                .build();

        final WorkflowBackfillTriggerResponse backfillTriggerResponse =
                triggerBackfillWorkflow(backfillTriggerRequest, masterServer);
        if (!backfillTriggerResponse.isSuccess()) {
            throw new ServiceException("Backfill workflow failed: " + backfillTriggerResponse.getMessage());
        }
        return backfillTriggerResponse.getWorkflowInstanceId();
    }

    protected WorkflowBackfillTriggerResponse triggerBackfillWorkflow(final WorkflowBackfillTriggerRequest request,
                                                                      final Server masterServer) {
        return Clients
                .withService(IWorkflowControlClient.class)
                .withHost(masterServer.getHost() + ":" + masterServer.getPort())
                .backfillTriggerWorkflow(request);
    }

    /**
     * Builds {@link BackfillWorkflowDTO} list for resolved downstream workflows.
     * {@link RunMode} in each downstream {@link BackfillWorkflowDTO.BackfillParamsDTO} matches the root (see
     * {@link #executeWithDependentExpansion(BackfillWorkflowDTO)}).
     */
    private List<BackfillWorkflowDTO> buildResolvedDownstreamBackfillDtos(final BackfillWorkflowDTO backfillWorkflowDTO,
                                                                          final List<ZonedDateTime> backfillDateTimes,
                                                                          final List<WorkflowDefinition> downstreamWorkflows) {
        final long upstreamWorkflowCode = backfillWorkflowDTO.getWorkflowDefinition().getCode();
        final List<ZonedDateTime> upstreamBackfillDates = new ArrayList<>(backfillDateTimes);
        final BackfillWorkflowDTO.BackfillParamsDTO originalParams = backfillWorkflowDTO.getBackfillParams();
        final boolean allLevelDependent = originalParams.isAllLevelDependent();

        final List<BackfillWorkflowDTO> result = new ArrayList<>();
        for (WorkflowDefinition downstreamWorkflow : downstreamWorkflows) {
            final long downstreamCode = downstreamWorkflow.getCode();

            final BackfillWorkflowDTO.BackfillParamsDTO dependentParams =
                    BackfillWorkflowDTO.BackfillParamsDTO.builder()
                            // Same as root; executor also branches on root RunMode only.
                            .runMode(originalParams.getRunMode())
                            .backfillDateList(upstreamBackfillDates)
                            .expectedParallelismNumber(originalParams.getExpectedParallelismNumber())
                            // Downstream expansion has already been decided in resolution stage.
                            .backfillDependentMode(ComplementDependentMode.OFF_MODE)
                            .allLevelDependent(allLevelDependent)
                            .executionOrder(originalParams.getExecutionOrder())
                            .build();

            final BackfillWorkflowDTO dependentBackfillDTO = BackfillWorkflowDTO.builder()
                    .loginUser(backfillWorkflowDTO.getLoginUser())
                    .workflowDefinition(downstreamWorkflow)
                    .startNodes(null)
                    .failureStrategy(backfillWorkflowDTO.getFailureStrategy())
                    .taskDependType(backfillWorkflowDTO.getTaskDependType())
                    .execType(backfillWorkflowDTO.getExecType())
                    .warningType(backfillWorkflowDTO.getWarningType())
                    .warningGroupId(downstreamWorkflow.getWarningGroupId())
                    .runMode(dependentParams.getRunMode())
                    .workflowInstancePriority(backfillWorkflowDTO.getWorkflowInstancePriority())
                    .workerGroup(backfillWorkflowDTO.getWorkerGroup())
                    .tenantCode(backfillWorkflowDTO.getTenantCode())
                    .environmentCode(backfillWorkflowDTO.getEnvironmentCode())
                    .startParamList(backfillWorkflowDTO.getStartParamList())
                    .dryRun(backfillWorkflowDTO.getDryRun())
                    .backfillParams(dependentParams)
                    .build();

            log.info("Built dependent backfill DTO for workflow {} (upstream {}) with backfill dates {}",
                    downstreamCode, upstreamWorkflowCode,
                    backfillDateTimes.stream().map(DateUtils::dateToString).collect(Collectors.toList()));

            result.add(dependentBackfillDTO);
        }
        return result;
    }
}
