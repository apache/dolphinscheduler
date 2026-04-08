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
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.DependentWorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private RegistryClient registryClient;

    @Override
    public List<Integer> execute(final BackfillWorkflowDTO backfillWorkflowDTO) {
        return executeWithVisitedCodes(backfillWorkflowDTO, new HashSet<>());
    }

    List<Integer> executeWithVisitedCodes(final BackfillWorkflowDTO backfillWorkflowDTO,
                                          final Set<Long> visitedCodes) {
        // todo: directly call the master api to do backfill
        if (backfillWorkflowDTO.getBackfillParams().getRunMode() == RunMode.RUN_MODE_SERIAL) {
            return doSerialBackfillWorkflow(backfillWorkflowDTO, visitedCodes);
        } else {
            return doParallelBackfillWorkflow(backfillWorkflowDTO, visitedCodes);
        }
    }

    private List<Integer> doSerialBackfillWorkflow(final BackfillWorkflowDTO backfillWorkflowDTO,
                                                   final Set<Long> visitedCodes) {
        final BackfillWorkflowDTO.BackfillParamsDTO backfillParams = backfillWorkflowDTO.getBackfillParams();
        final List<ZonedDateTime> backfillTimeList = backfillParams.getBackfillDateList();
        if (backfillParams.getExecutionOrder() == ExecutionOrder.DESC_ORDER) {
            Collections.sort(backfillTimeList, Collections.reverseOrder());
        } else {
            Collections.sort(backfillTimeList);
        }

        final Integer workflowInstanceId = doBackfillWorkflow(backfillWorkflowDTO, backfillTimeList, visitedCodes);
        return Lists.newArrayList(workflowInstanceId);
    }

    private List<Integer> doParallelBackfillWorkflow(final BackfillWorkflowDTO backfillWorkflowDTO,
                                                     final Set<Long> visitedCodes) {
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
                    backfillWorkflowDTO, stringDate, visitedCodes);
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
                                       final List<ZonedDateTime> backfillDateTimes,
                                       final Set<Long> visitedCodes) {
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
        final BackfillWorkflowDTO.BackfillParamsDTO backfillParams = backfillWorkflowDTO.getBackfillParams();
        if (backfillParams.getBackfillDependentMode() == ComplementDependentMode.ALL_DEPENDENT) {
            final Set<Long> effectiveVisitedCodes =
                    visitedCodes == null ? new HashSet<>() : new HashSet<>(visitedCodes);
            effectiveVisitedCodes.add(backfillWorkflowDTO.getWorkflowDefinition().getCode());
            doBackfillDependentWorkflow(backfillWorkflowDTO, backfillDateTimes, effectiveVisitedCodes);
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

    private void doBackfillDependentWorkflow(final BackfillWorkflowDTO backfillWorkflowDTO,
                                             final List<ZonedDateTime> backfillDateTimes,
                                             final Set<Long> visitedCodes) {
        final WorkflowDefinition upstreamWorkflow = backfillWorkflowDTO.getWorkflowDefinition();
        final long upstreamWorkflowCode = upstreamWorkflow.getCode();
        final BackfillWorkflowDTO.BackfillParamsDTO originalParams = backfillWorkflowDTO.getBackfillParams();

        final List<WorkflowDefinition> downstreamWorkflowList = resolveDownstreamWorkflows(
                upstreamWorkflowCode, visitedCodes, originalParams.isAllLevelDependent());
        if (downstreamWorkflowList.isEmpty()) {
            log.info("No downstream dependent workflows found for workflow code {}", upstreamWorkflowCode);
            return;
        }
        triggerResolvedDownstreamWorkflows(
                backfillWorkflowDTO, backfillDateTimes, visitedCodes, downstreamWorkflowList);
    }

    private List<WorkflowDefinition> resolveDownstreamWorkflows(final long upstreamWorkflowCode,
                                                                final Set<Long> visitedCodes,
                                                                final boolean allLevelDependent) {
        final Set<Long> downstreamCodes = new LinkedHashSet<>();
        if (allLevelDependent) {
            final Deque<Long> pendingWorkflows = new ArrayDeque<>();
            pendingWorkflows.add(upstreamWorkflowCode);
            while (!pendingWorkflows.isEmpty()) {
                final Long currentWorkflowCode = pendingWorkflows.removeFirst();
                for (Long directDownstreamCode : queryDirectDownstreamWorkflowCodes(currentWorkflowCode)) {
                    if (directDownstreamCode == upstreamWorkflowCode) {
                        continue;
                    }
                    if (visitedCodes.contains(directDownstreamCode)) {
                        continue;
                    }
                    if (downstreamCodes.add(directDownstreamCode)) {
                        pendingWorkflows.addLast(directDownstreamCode);
                    }
                }
            }
        } else {
            for (Long directDownstreamCode : queryDirectDownstreamWorkflowCodes(upstreamWorkflowCode)) {
                if (directDownstreamCode == upstreamWorkflowCode || visitedCodes.contains(directDownstreamCode)) {
                    continue;
                }
                downstreamCodes.add(directDownstreamCode);
            }
        }

        if (downstreamCodes.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<Long, WorkflowDefinition> downstreamWorkflowMapByCode = workflowDefinitionDao
                .queryByCodes(downstreamCodes)
                .stream()
                .collect(Collectors.toMap(WorkflowDefinition::getCode, workflowDefinition -> workflowDefinition,
                        (left, right) -> left));

        final List<WorkflowDefinition> downstreamWorkflows = new ArrayList<>();
        for (Long downstreamCode : downstreamCodes) {
            final WorkflowDefinition downstreamWorkflow = downstreamWorkflowMapByCode.get(downstreamCode);
            if (downstreamWorkflow == null) {
                log.warn("Skip dependent workflow {}, workflow definition not found", downstreamCode);
                continue;
            }
            if (downstreamWorkflow.getReleaseState() != ReleaseState.ONLINE) {
                log.warn("Skip dependent workflow {}, release state is not ONLINE", downstreamCode);
                continue;
            }
            downstreamWorkflows.add(downstreamWorkflow);
        }
        return downstreamWorkflows;
    }

    private Set<Long> queryDirectDownstreamWorkflowCodes(final long workflowCode) {
        final List<DependentWorkflowDefinition> downstreamDefinitions =
                workflowLineageService.queryDownstreamDependentWorkflowDefinitions(workflowCode);
        if (downstreamDefinitions == null || downstreamDefinitions.isEmpty()) {
            return Collections.emptySet();
        }
        return downstreamDefinitions.stream()
                .map(DependentWorkflowDefinition::getWorkflowDefinitionCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void triggerResolvedDownstreamWorkflows(final BackfillWorkflowDTO backfillWorkflowDTO,
                                                    final List<ZonedDateTime> backfillDateTimes,
                                                    final Set<Long> visitedCodes,
                                                    final List<WorkflowDefinition> downstreamWorkflows) {
        final long upstreamWorkflowCode = backfillWorkflowDTO.getWorkflowDefinition().getCode();
        final List<ZonedDateTime> upstreamBackfillDates = new ArrayList<>(backfillDateTimes);
        final BackfillWorkflowDTO.BackfillParamsDTO originalParams = backfillWorkflowDTO.getBackfillParams();
        final boolean allLevelDependent = originalParams.isAllLevelDependent();

        for (WorkflowDefinition downstreamWorkflow : downstreamWorkflows) {
            final long downstreamCode = downstreamWorkflow.getCode();
            if (visitedCodes.contains(downstreamCode)) {
                log.warn("Skip already visited dependent workflow {}", downstreamCode);
                continue;
            }

            final BackfillWorkflowDTO.BackfillParamsDTO dependentParams =
                    BackfillWorkflowDTO.BackfillParamsDTO.builder()
                            .runMode(originalParams.getRunMode() == RunMode.RUN_MODE_PARALLEL ? RunMode.RUN_MODE_SERIAL
                                    : originalParams.getRunMode())
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

            log.info("Trigger dependent workflow {} for upstream workflow {} with backfill dates {}",
                    downstreamCode, upstreamWorkflowCode,
                    backfillDateTimes.stream().map(DateUtils::dateToString).collect(Collectors.toList()));

            visitedCodes.add(downstreamCode);
            executeWithVisitedCodes(dependentBackfillDTO, visitedCodes);
        }
    }
}
