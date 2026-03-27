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
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

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

    // No need to mock WorkerGroupService, not used in actual implementation

    @Test
    public void testDownstreamFullBackfill_startNodesAlwaysNull_workerGroupFromWorkflow() {
        long upstreamCode = 1L;
        long downstreamCode = 2L;
        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder().code(upstreamCode).releaseState(ReleaseState.ONLINE).build();
        WorkflowDefinition downstreamWorkflow =
                WorkflowDefinition.builder().code(downstreamCode).releaseState(ReleaseState.ONLINE).build();
        DependentWorkflowDefinition dep = new DependentWorkflowDefinition();
        dep.setWorkflowDefinitionCode(downstreamCode);
        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Collections.singletonList(dep));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(downstreamCode)))
                .thenReturn(Collections.singletonList(downstreamWorkflow));
        User loginUser = new User();
        loginUser.setId(1);
        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.singletonList(ZonedDateTime.parse("2026-02-01T00:00:00Z")))
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(false)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();
        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(upstreamWorkflow)
                .workerGroup("upstreamGroup")
                .backfillParams(params)
                .build();
        Server masterServer = new Server();
        masterServer.setHost("127.0.0.1");
        masterServer.setPort(1234);
        when(registryClient.getRandomServer(RegistryNodeType.MASTER)).thenReturn(Optional.of(masterServer));
        List<WorkflowBackfillTriggerRequest> requests = new ArrayList<>();
        doAnswer(invocation -> {
            WorkflowBackfillTriggerRequest req = invocation.getArgument(0);
            requests.add(req);
            return WorkflowBackfillTriggerResponse.success(1);
        }).when(backfillWorkflowExecutorDelegate).triggerBackfillWorkflow(any(), any());
        backfillWorkflowExecutorDelegate.executeWithVisitedCodes(dto, new HashSet<>());
        // upstream + downstream
        Assertions.assertEquals(2, requests.size());
        WorkflowBackfillTriggerRequest downstreamReq =
                requests.stream().filter(r -> r.getWorkflowCode() == downstreamCode).findFirst().orElseThrow();
        Assertions.assertNull(downstreamReq.getStartNodes());
        Assertions.assertEquals("upstreamGroup", downstreamReq.getWorkerGroup());
    }

    @Test
    public void testDownstreamWorkerGroupNull_fallbackToDefault() {
        long upstreamCode = 1L;
        long downstreamCode = 2L;
        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder().code(upstreamCode).releaseState(ReleaseState.ONLINE).build();
        WorkflowDefinition downstreamWorkflow =
                WorkflowDefinition.builder().code(downstreamCode).releaseState(ReleaseState.ONLINE).build();
        DependentWorkflowDefinition dep = new DependentWorkflowDefinition();
        dep.setWorkflowDefinitionCode(downstreamCode);
        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Collections.singletonList(dep));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(downstreamCode)))
                .thenReturn(Collections.singletonList(downstreamWorkflow));
        User loginUser = new User();
        loginUser.setId(1);
        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.singletonList(ZonedDateTime.parse("2026-02-01T00:00:00Z")))
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(false)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();
        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(upstreamWorkflow)
                .workerGroup("upstreamGroup")
                .backfillParams(params)
                .build();
        Server masterServer = new Server();
        masterServer.setHost("127.0.0.1");
        masterServer.setPort(1234);
        when(registryClient.getRandomServer(RegistryNodeType.MASTER)).thenReturn(Optional.of(masterServer));
        List<WorkflowBackfillTriggerRequest> requests = new ArrayList<>();
        doAnswer(invocation -> {
            WorkflowBackfillTriggerRequest req = invocation.getArgument(0);
            requests.add(req);
            return WorkflowBackfillTriggerResponse.success(1);
        }).when(backfillWorkflowExecutorDelegate).triggerBackfillWorkflow(any(), any());
        backfillWorkflowExecutorDelegate.executeWithVisitedCodes(dto, new HashSet<>());
        WorkflowBackfillTriggerRequest downstreamReq =
                requests.stream().filter(r -> r.getWorkflowCode() == downstreamCode).findFirst().orElseThrow();
        Assertions.assertEquals("upstreamGroup", downstreamReq.getWorkerGroup());
    }

    @Test
    public void testDownstreamDeduplication_onlyOncePerWorkflow() {
        long upstreamCode = 1L;
        long downstreamCode = 2L;
        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder().code(upstreamCode).releaseState(ReleaseState.ONLINE).build();
        WorkflowDefinition downstreamWorkflow =
                WorkflowDefinition.builder().code(downstreamCode).releaseState(ReleaseState.ONLINE).build();
        DependentWorkflowDefinition dep1 = new DependentWorkflowDefinition();
        dep1.setWorkflowDefinitionCode(downstreamCode);
        DependentWorkflowDefinition dep2 = new DependentWorkflowDefinition();
        dep2.setWorkflowDefinitionCode(downstreamCode);
        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Arrays.asList(dep1, dep2));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(downstreamCode)))
                .thenReturn(Collections.singletonList(downstreamWorkflow));
        User loginUser = new User();
        loginUser.setId(1);
        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.singletonList(ZonedDateTime.parse("2026-02-01T00:00:00Z")))
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(false)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();
        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(upstreamWorkflow)
                .workerGroup("upstreamGroup")
                .backfillParams(params)
                .build();
        Server masterServer = new Server();
        masterServer.setHost("127.0.0.1");
        masterServer.setPort(1234);
        when(registryClient.getRandomServer(RegistryNodeType.MASTER)).thenReturn(Optional.of(masterServer));
        List<WorkflowBackfillTriggerRequest> requests = new ArrayList<>();
        doAnswer(invocation -> {
            WorkflowBackfillTriggerRequest req = invocation.getArgument(0);
            requests.add(req);
            return WorkflowBackfillTriggerResponse.success(1);
        }).when(backfillWorkflowExecutorDelegate).triggerBackfillWorkflow(any(), any());
        backfillWorkflowExecutorDelegate.executeWithVisitedCodes(dto, new HashSet<>());
        // downstream is only triggered once
        long count = requests.stream().filter(r -> r.getWorkflowCode() == downstreamCode).count();
        Assertions.assertEquals(1, count);
    }

    @Test
    public void testCycleDependency_noInfiniteRecursion() {
        long codeA = 1L, codeB = 2L;
        WorkflowDefinition workflowA =
                WorkflowDefinition.builder().code(codeA).releaseState(ReleaseState.ONLINE).build();
        WorkflowDefinition workflowB =
                WorkflowDefinition.builder().code(codeB).releaseState(ReleaseState.ONLINE).build();
        DependentWorkflowDefinition depAtoB = new DependentWorkflowDefinition();
        depAtoB.setWorkflowDefinitionCode(codeB);
        DependentWorkflowDefinition depBtoA = new DependentWorkflowDefinition();
        depBtoA.setWorkflowDefinitionCode(codeA);
        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(codeA))
                .thenReturn(Collections.singletonList(depAtoB));
        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(codeB))
                .thenReturn(Collections.singletonList(depBtoA));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(codeB)))
                .thenReturn(Collections.singletonList(workflowB));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(codeA)))
                .thenReturn(Collections.singletonList(workflowA));
        User loginUser = new User();
        loginUser.setId(1);
        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.singletonList(ZonedDateTime.parse("2026-02-01T00:00:00Z")))
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(true)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();
        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(workflowA)
                .workerGroup("a")
                .backfillParams(params)
                .build();
        Server masterServer = new Server();
        masterServer.setHost("127.0.0.1");
        masterServer.setPort(1234);
        when(registryClient.getRandomServer(RegistryNodeType.MASTER)).thenReturn(Optional.of(masterServer));
        List<WorkflowBackfillTriggerRequest> requests = new ArrayList<>();
        doAnswer(invocation -> {
            WorkflowBackfillTriggerRequest req = invocation.getArgument(0);
            requests.add(req);
            return WorkflowBackfillTriggerResponse.success(1);
        }).when(backfillWorkflowExecutorDelegate).triggerBackfillWorkflow(any(), any());
        backfillWorkflowExecutorDelegate.executeWithVisitedCodes(dto, new HashSet<>());
        // Only A and B will be triggered once each
        Assertions.assertEquals(2, requests.size());
    }

    @Test
    public void testOfflineOrMissingDownstream_skipped() {
        long upstreamCode = 1L, offlineCode = 2L, missingCode = 3L;
        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder().code(upstreamCode).releaseState(ReleaseState.ONLINE).build();
        WorkflowDefinition offlineWorkflow =
                WorkflowDefinition.builder().code(offlineCode).releaseState(ReleaseState.OFFLINE).build();
        DependentWorkflowDefinition depOffline = new DependentWorkflowDefinition();
        depOffline.setWorkflowDefinitionCode(offlineCode);
        DependentWorkflowDefinition depMissing = new DependentWorkflowDefinition();
        depMissing.setWorkflowDefinitionCode(missingCode);
        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Arrays.asList(depOffline, depMissing));
        when(workflowDefinitionDao.queryByCodes(new LinkedHashSet<>(Arrays.asList(offlineCode, missingCode))))
                .thenReturn(Collections.singletonList(offlineWorkflow));
        User loginUser = new User();
        loginUser.setId(1);
        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.singletonList(ZonedDateTime.parse("2026-02-01T00:00:00Z")))
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(true)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();
        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .loginUser(loginUser)
                .workflowDefinition(upstreamWorkflow)
                .workerGroup("g")
                .backfillParams(params)
                .build();
        Server masterServer = new Server();
        masterServer.setHost("127.0.0.1");
        masterServer.setPort(1234);
        when(registryClient.getRandomServer(RegistryNodeType.MASTER)).thenReturn(Optional.of(masterServer));
        List<WorkflowBackfillTriggerRequest> requests = new ArrayList<>();
        doAnswer(invocation -> {
            WorkflowBackfillTriggerRequest req = invocation.getArgument(0);
            requests.add(req);
            return WorkflowBackfillTriggerResponse.success(1);
        }).when(backfillWorkflowExecutorDelegate).triggerBackfillWorkflow(any(), any());
        backfillWorkflowExecutorDelegate.executeWithVisitedCodes(dto, new HashSet<>());
        // Only upstream will be triggered, offline and missing will be skipped
        Assertions.assertEquals(1, requests.size());
    }
}
