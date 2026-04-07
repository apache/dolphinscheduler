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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
            final Set<Long> effectiveVisitedCodes = visitedCodes == null ? new HashSet<>() : visitedCodes;
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
        // 1) Query downstream dependent workflows for the current workflow
        final WorkflowDefinition upstreamWorkflow = backfillWorkflowDTO.getWorkflowDefinition();
        final long upstreamWorkflowCode = upstreamWorkflow.getCode();

        List<DependentWorkflowDefinition> downstreamDefinitions =
                workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamWorkflowCode);

        if (downstreamDefinitions == null || downstreamDefinitions.isEmpty()) {
            log.info("No downstream dependent workflows found for workflow code {}", upstreamWorkflowCode);
            return;
        }
        // downstreamDefinitions may contain multiple entries for the same downstream workflow code
        // (different dependent task lineage). We should only traverse each downstream workflow once
        // (visitedCodes check), but trigger all dependent nodes within that downstream workflow by
        // aggregating distinct taskDefinitionCodes into startNodes.
        final Map<Long, List<DependentWorkflowDefinition>> downstreamDefinitionsByCode =
                downstreamDefinitions.stream()
                        .collect(Collectors.groupingBy(DependentWorkflowDefinition::getWorkflowDefinitionCode));
        final Set<Long> downstreamCodes = downstreamDefinitionsByCode.keySet();
        final List<WorkflowDefinition> downstreamWorkflowList = workflowDefinitionDao.queryByCodes(downstreamCodes);
        // Each workflow code maps to a single WorkflowDefinition (code is unique in t_ds_workflow_definition).
        // We still group by code to simplify lookup and keep the code robust if this ever changes.
        final Map<Long, List<WorkflowDefinition>> downstreamWorkflowMapByCode = downstreamWorkflowList.stream()
                .collect(Collectors.groupingBy(WorkflowDefinition::getCode));

        // 2) Reuse upstream business dates for downstream backfill (same instants/zones as the chunk passed to
        // doBackfillWorkflow; avoids List<String> -> system-default parse -> dateToString drift)
        final List<ZonedDateTime> upstreamBackfillDates = new ArrayList<>(backfillDateTimes);

        // 3) Iterate downstream workflows and build/trigger corresponding BackfillWorkflowDTO
        for (Map.Entry<Long, List<DependentWorkflowDefinition>> entry : downstreamDefinitionsByCode.entrySet()) {
            long downstreamCode = entry.getKey();
            // Prevent self-dependency and circular dependency chains.
            // We only traverse each downstream workflow once.
            if (visitedCodes.contains(downstreamCode)) {
                log.warn("Skip already visited dependent workflow {}", downstreamCode);
                continue;
            }

            // Simplification: Downstream backfill is always full, startNodes=null, workerGroup uses
            // workflowDefinition's own config
            // Only grouping and deduplication are needed, all aggregation logic is omitted

            WorkflowDefinition downstreamWorkflow = null;
            List<WorkflowDefinition> workflowCandidates = downstreamWorkflowMapByCode.get(downstreamCode);
            if (workflowCandidates != null && !workflowCandidates.isEmpty()) {
                downstreamWorkflow = workflowCandidates.get(0); // code is unique, just take the first one
            }
            if (downstreamWorkflow == null) {
                log.warn("Skip dependent workflow {}, workflow definition not found", downstreamCode);
                continue;
            }
            if (downstreamWorkflow.getReleaseState() != ReleaseState.ONLINE) {
                log.warn("Skip dependent workflow {}, release state is not ONLINE", downstreamCode);
                continue;
            }

            // Currently, reuse the same business date list as upstream for downstream backfill;
            // later we can refine the dates based on dependency cycle configuration in dependentWorkflowDefinition
            // (taskParams).
            BackfillWorkflowDTO.BackfillParamsDTO originalParams = backfillWorkflowDTO.getBackfillParams();
            boolean allLevelDependent = originalParams.isAllLevelDependent();
            ComplementDependentMode downstreamDependentMode =
                    allLevelDependent ? originalParams.getBackfillDependentMode() : ComplementDependentMode.OFF_MODE;

            BackfillWorkflowDTO.BackfillParamsDTO dependentParams = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                    // If upstream runs in PARALLEL mode, force downstream to SERIAL to avoid
                    // re-chunking the already sliced date list; otherwise keep the original runMode.
                    .runMode(originalParams.getRunMode() == RunMode.RUN_MODE_PARALLEL ? RunMode.RUN_MODE_SERIAL
                            : originalParams.getRunMode())
                    .backfillDateList(upstreamBackfillDates)
                    .expectedParallelismNumber(originalParams.getExpectedParallelismNumber())
                    // Control whether downstream will continue triggering its own dependencies based on
                    // allLevelDependent flag
                    .backfillDependentMode(downstreamDependentMode)
                    .allLevelDependent(allLevelDependent)
                    .executionOrder(originalParams.getExecutionOrder())
                    .build();

            // Simplified design notes:
            // 1. Downstream backfill is always full, startNodes=null, no more aggregation of dependent nodes
            // 2. workerGroup is directly taken from the downstream workflowDefinition's own config, if null then use
            // system default workerGroup
            // 3. Only grouping deduplication and visitedCodes check, all complex aggregation logic is omitted
            // This implementation is the simplest and most controllable, suitable for full backfill and workerGroup
            // based on itself
            BackfillWorkflowDTO dependentBackfillDTO = BackfillWorkflowDTO.builder()
                    .loginUser(backfillWorkflowDTO.getLoginUser())
                    .workflowDefinition(downstreamWorkflow)
                    .startNodes(null) // Full backfill, simplified design
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

            // Mark as visited to prevent infinite recursion. Actual dependency chains are usually short, recursion is
            // safe.
            visitedCodes.add(downstreamCode);
            executeWithVisitedCodes(dependentBackfillDTO, visitedCodes);
        }
    }
}
