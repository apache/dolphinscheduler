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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.service.WorkflowLineageService;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.DependentWorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BackfillWorkflowExecutorDelegateTest {

    @Spy
    @InjectMocks
    private BackfillWorkflowExecutorDelegate backfillWorkflowExecutorDelegate;

    @Mock
    private WorkflowLineageService workflowLineageService;

    @Mock
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Mock
    private RegistryClient registryClient;

    @Test
    public void testDoParallelBackfillWorkflow_ShouldIsolateVisitedCodesAcrossChunks() {
        long upstreamCode = 500L;
        long downstreamCode = 600L;
        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder().code(upstreamCode).releaseState(ReleaseState.ONLINE).build();
        User loginUser = new User();
        loginUser.setId(1);
        WorkflowDefinition downstreamWorkflow =
                WorkflowDefinition.builder().code(downstreamCode).releaseState(ReleaseState.ONLINE).build();
        DependentWorkflowDefinition dep = new DependentWorkflowDefinition();
        dep.setWorkflowDefinitionCode(downstreamCode);
        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Collections.singletonList(dep));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(downstreamCode)))
                .thenReturn(Collections.singletonList(downstreamWorkflow));

        List<ZonedDateTime> dates = Arrays.asList(
                ZonedDateTime.parse("2026-02-01T00:00:00Z"),
                ZonedDateTime.parse("2026-02-02T00:00:00Z"),
                ZonedDateTime.parse("2026-02-03T00:00:00Z"));
        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_PARALLEL)
                .backfillDateList(dates)
                .expectedParallelismNumber(2)
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(true)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();
        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(upstreamWorkflow)
                .backfillParams(params)
                .build();
        Set<Long> baseVisitedCodes = new HashSet<>(Collections.singleton(upstreamCode));
        List<Set<Long>> dependentVisitedCodes = new java.util.ArrayList<>();

        Server masterServer = new Server();
        masterServer.setHost("127.0.0.1");
        masterServer.setPort(1234);
        when(registryClient.getRandomServer(RegistryNodeType.MASTER)).thenReturn(Optional.of(masterServer));
        doReturn(WorkflowBackfillTriggerResponse.success(1)).when(backfillWorkflowExecutorDelegate)
                .triggerBackfillWorkflow(any(), any());

        doAnswer(invocation -> {
            Set<Long> visitedCodes = invocation.getArgument(1);
            dependentVisitedCodes.add(visitedCodes);
            return Collections.singletonList(1);
        }).when(backfillWorkflowExecutorDelegate)
                .executeWithVisitedCodes(
                        org.mockito.ArgumentMatchers
                                .argThat(dtoValue -> dtoValue.getWorkflowDefinition().getCode() == downstreamCode),
                        any());

        List<Integer> result = backfillWorkflowExecutorDelegate.executeWithVisitedCodes(dto, baseVisitedCodes);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(2, dependentVisitedCodes.size());
        Assertions.assertEquals(Collections.singleton(upstreamCode), baseVisitedCodes);
        Assertions.assertFalse(baseVisitedCodes.contains(downstreamCode));
        Assertions.assertNotSame(dependentVisitedCodes.get(0), dependentVisitedCodes.get(1));
        Assertions.assertTrue(dependentVisitedCodes.get(0).contains(downstreamCode));
        Assertions.assertTrue(dependentVisitedCodes.get(1).contains(downstreamCode));
    }

    @Test
    public void testDoBackfillDependentWorkflow_UseDependentRoutingFields() throws Exception {
        long upstreamCode = 1L;
        int upstreamVersion = 1;
        long downstreamCode = 2L;
        int downstreamVersion = 3;
        long dependentTaskDefinitionCode = 999L;
        String dependentWorkerGroup = "wg-dep";

        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder()
                        .code(upstreamCode)
                        .version(upstreamVersion)
                        .releaseState(ReleaseState.ONLINE)
                        .build();

        WorkflowDefinition downstreamWrongVersion =
                WorkflowDefinition.builder()
                        .code(downstreamCode)
                        .version(1)
                        .releaseState(ReleaseState.ONLINE)
                        .build();

        WorkflowDefinition downstreamCorrectVersion =
                WorkflowDefinition.builder()
                        .code(downstreamCode)
                        .version(downstreamVersion)
                        .releaseState(ReleaseState.ONLINE)
                        .build();

        DependentWorkflowDefinition dependentWorkflowDefinition = new DependentWorkflowDefinition();
        dependentWorkflowDefinition.setWorkflowDefinitionCode(downstreamCode);
        dependentWorkflowDefinition.setWorkflowDefinitionVersion(downstreamVersion);
        dependentWorkflowDefinition.setTaskDefinitionCode(dependentTaskDefinitionCode);
        dependentWorkflowDefinition.setWorkerGroup(dependentWorkerGroup);

        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Collections.singletonList(dependentWorkflowDefinition));

        when(workflowDefinitionDao.queryByCodes(Collections.singleton(downstreamCode)))
                .thenReturn(Arrays.asList(downstreamWrongVersion, downstreamCorrectVersion));

        User loginUser = new User();
        loginUser.setId(10);

        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.singletonList(ZonedDateTime.parse("2026-02-01T00:00:00Z")))
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                // ensure dependent workflow itself will NOT trigger its own dependencies
                .allLevelDependent(false)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();

        BackfillWorkflowDTO upstreamDto = BackfillWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(upstreamWorkflow)
                .workerGroup("wg-upstream")
                .backfillParams(params)
                .build();

        Server masterServer = new Server();
        masterServer.setHost("127.0.0.1");
        masterServer.setPort(1234);
        when(registryClient.getRandomServer(RegistryNodeType.MASTER)).thenReturn(Optional.of(masterServer));

        List<WorkflowBackfillTriggerRequest> capturedRequests = new ArrayList<>();
        doAnswer(invocation -> {
            WorkflowBackfillTriggerRequest request = invocation.getArgument(0);
            capturedRequests.add(request);
            return WorkflowBackfillTriggerResponse.success(1);
        }).when(backfillWorkflowExecutorDelegate)
                .triggerBackfillWorkflow(any(), any());

        backfillWorkflowExecutorDelegate.executeWithVisitedCodes(
                upstreamDto,
                new HashSet<>());

        Assertions.assertEquals(2, capturedRequests.size());
        WorkflowBackfillTriggerRequest dependentRequest =
                capturedRequests.stream().filter(r -> r.getWorkflowCode() == downstreamCode).findFirst()
                        .orElseThrow();

        Assertions.assertEquals(downstreamVersion, dependentRequest.getWorkflowVersion());
        Assertions.assertEquals(Collections.singletonList(dependentTaskDefinitionCode), dependentRequest.getStartNodes());
        Assertions.assertEquals(dependentWorkerGroup, dependentRequest.getWorkerGroup());
    }
}
