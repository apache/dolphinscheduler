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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.service.WorkflowLineageService;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.DependentWorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    public void testDoBackfillDependentWorkflow_NoDownstreamDefinitions() throws Exception {
        long upstreamCode = 1L;
        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder()
                        .code(upstreamCode)
                        .releaseState(ReleaseState.ONLINE)
                        .build();

        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.<ZonedDateTime>emptyList())
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(true)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();

        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .workflowDefinition(upstreamWorkflow)
                .backfillParams(params)
                .build();

        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Collections.emptyList());

        Method method = BackfillWorkflowExecutorDelegate.class.getDeclaredMethod(
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class, Set.class);
        method.setAccessible(true);

        List<ZonedDateTime> backfillDates =
                Collections.singletonList(DateUtils.stringToZoneDateTime("2026-02-01 00:00:00"));

        Set<Long> visitedCodes = new HashSet<>();
        visitedCodes.add(dto.getWorkflowDefinition().getCode());
        method.invoke(backfillWorkflowExecutorDelegate, dto, backfillDates, visitedCodes);

        verify(workflowDefinitionDao, never()).queryByCodes(any());
    }

    @Test
    public void testDoBackfillDependentWorkflow_WithDownstream_AllLevelDependent() throws Exception {
        long upstreamCode = 10L;
        long downstreamCode = 20L;

        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder()
                        .code(upstreamCode)
                        .releaseState(ReleaseState.ONLINE)
                        .build();

        WorkflowDefinition downstreamWorkflow =
                WorkflowDefinition.builder()
                        .code(downstreamCode)
                        .releaseState(ReleaseState.ONLINE)
                        .warningGroupId(100)
                        .build();

        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.<ZonedDateTime>emptyList())
                .expectedParallelismNumber(2)
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(true)
                .executionOrder(ExecutionOrder.DESC_ORDER)
                .build();

        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .workflowDefinition(upstreamWorkflow)
                .backfillParams(params)
                .build();

        DependentWorkflowDefinition selfDependent = new DependentWorkflowDefinition();
        selfDependent.setWorkflowDefinitionCode(upstreamCode);

        DependentWorkflowDefinition validDependent = new DependentWorkflowDefinition();
        validDependent.setWorkflowDefinitionCode(downstreamCode);

        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Arrays.asList(selfDependent, validDependent));
        when(workflowDefinitionDao.queryByCodes(new LinkedHashSet<>(Arrays.asList(upstreamCode, downstreamCode))))
                .thenReturn(Collections.singletonList(downstreamWorkflow));

        ArgumentCaptor<BackfillWorkflowDTO> captor = ArgumentCaptor.forClass(BackfillWorkflowDTO.class);
        doReturn(Collections.singletonList(1)).when(backfillWorkflowExecutorDelegate)
                .executeWithVisitedCodes(captor.capture(), any());

        Method method = BackfillWorkflowExecutorDelegate.class.getDeclaredMethod(
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class, Set.class);
        method.setAccessible(true);

        List<ZonedDateTime> backfillDates = Arrays.asList(
                DateUtils.stringToZoneDateTime("2026-02-01 00:00:00"),
                DateUtils.stringToZoneDateTime("2026-02-02 00:00:00"));

        Set<Long> visitedCodes = new HashSet<>();
        visitedCodes.add(dto.getWorkflowDefinition().getCode());
        method.invoke(backfillWorkflowExecutorDelegate, dto, backfillDates, visitedCodes);

        verify(workflowDefinitionDao)
                .queryByCodes(new LinkedHashSet<>(Arrays.asList(upstreamCode, downstreamCode)));

        BackfillWorkflowDTO captured = captor.getValue();
        Assertions.assertNotNull(captured);
        Assertions.assertEquals(downstreamCode, captured.getWorkflowDefinition().getCode());
        Assertions.assertEquals(downstreamWorkflow.getWarningGroupId(), captured.getWarningGroupId());

        BackfillWorkflowDTO.BackfillParamsDTO capturedParams = captured.getBackfillParams();
        Assertions.assertNotNull(capturedParams);
        Assertions.assertEquals(params.getRunMode(), capturedParams.getRunMode());
        Assertions.assertEquals(params.getExpectedParallelismNumber(), capturedParams.getExpectedParallelismNumber());
        Assertions.assertEquals(params.getExecutionOrder(), capturedParams.getExecutionOrder());
        Assertions.assertEquals(ComplementDependentMode.ALL_DEPENDENT, capturedParams.getBackfillDependentMode());
        Assertions.assertTrue(capturedParams.isAllLevelDependent());
        Assertions.assertEquals(backfillDates.size(), capturedParams.getBackfillDateList().size());
        Assertions.assertNull(captured.getStartNodes());
    }

    @Test
    public void testDoBackfillDependentWorkflow_WithDownstream_SingleLevelDependent() throws Exception {
        long upstreamCode = 100L;
        long downstreamCode = 200L;

        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder()
                        .code(upstreamCode)
                        .releaseState(ReleaseState.ONLINE)
                        .build();

        WorkflowDefinition downstreamWorkflow =
                WorkflowDefinition.builder()
                        .code(downstreamCode)
                        .releaseState(ReleaseState.ONLINE)
                        .warningGroupId(200)
                        .build();

        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.<ZonedDateTime>emptyList())
                .expectedParallelismNumber(3)
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(false)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();

        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .workflowDefinition(upstreamWorkflow)
                .backfillParams(params)
                .build();

        DependentWorkflowDefinition validDependent = new DependentWorkflowDefinition();
        validDependent.setWorkflowDefinitionCode(downstreamCode);

        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Collections.singletonList(validDependent));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(downstreamCode)))
                .thenReturn(Collections.singletonList(downstreamWorkflow));

        ArgumentCaptor<BackfillWorkflowDTO> captor = ArgumentCaptor.forClass(BackfillWorkflowDTO.class);
        doReturn(Collections.singletonList(1)).when(backfillWorkflowExecutorDelegate)
                .executeWithVisitedCodes(captor.capture(), any());

        Method method = BackfillWorkflowExecutorDelegate.class.getDeclaredMethod(
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class, Set.class);
        method.setAccessible(true);

        List<ZonedDateTime> backfillDates =
                Collections.singletonList(DateUtils.stringToZoneDateTime("2026-02-03 00:00:00"));

        Set<Long> visitedCodes = new HashSet<>();
        visitedCodes.add(dto.getWorkflowDefinition().getCode());
        method.invoke(backfillWorkflowExecutorDelegate, dto, backfillDates, visitedCodes);

        verify(workflowDefinitionDao).queryByCodes(Collections.singleton(downstreamCode));

        BackfillWorkflowDTO captured = captor.getValue();
        Assertions.assertNotNull(captured);

        BackfillWorkflowDTO.BackfillParamsDTO capturedParams = captured.getBackfillParams();
        Assertions.assertNotNull(capturedParams);
        Assertions.assertEquals(ComplementDependentMode.OFF_MODE, capturedParams.getBackfillDependentMode());
        Assertions.assertFalse(capturedParams.isAllLevelDependent());
        Assertions.assertEquals(backfillDates.size(), capturedParams.getBackfillDateList().size());
        Assertions.assertNull(captured.getStartNodes());
    }

    @Test
    public void testDoBackfillDependentWorkflow_SkipWorkflowNotFound() throws Exception {
        long upstreamCode = 1000L;
        long downstreamCode = 2000L;

        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder().code(upstreamCode).releaseState(ReleaseState.ONLINE).build();

        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.<ZonedDateTime>emptyList())
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(true)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();

        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .workflowDefinition(upstreamWorkflow)
                .backfillParams(params)
                .build();

        DependentWorkflowDefinition dep = new DependentWorkflowDefinition();
        dep.setWorkflowDefinitionCode(downstreamCode);

        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Collections.singletonList(dep));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(downstreamCode)))
                .thenReturn(Collections.emptyList());

        Method method = BackfillWorkflowExecutorDelegate.class.getDeclaredMethod(
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class, Set.class);
        method.setAccessible(true);

        Set<Long> visitedCodes = new HashSet<>();
        visitedCodes.add(dto.getWorkflowDefinition().getCode());
        method.invoke(
                backfillWorkflowExecutorDelegate,
                dto,
                Collections.singletonList(DateUtils.stringToZoneDateTime("2026-02-01 00:00:00")),
                visitedCodes);

        verify(backfillWorkflowExecutorDelegate, never()).executeWithVisitedCodes(any(), any());
    }

    @Test
    public void testDoBackfillDependentWorkflow_SkipOfflineWorkflow() throws Exception {
        long upstreamCode = 3000L;
        long downstreamCode = 4000L;

        WorkflowDefinition upstreamWorkflow =
                WorkflowDefinition.builder().code(upstreamCode).releaseState(ReleaseState.ONLINE).build();

        WorkflowDefinition offlineDownstream =
                WorkflowDefinition.builder().code(downstreamCode).releaseState(ReleaseState.OFFLINE).build();

        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.<ZonedDateTime>emptyList())
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(true)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();

        BackfillWorkflowDTO dto = BackfillWorkflowDTO.builder()
                .workflowDefinition(upstreamWorkflow)
                .backfillParams(params)
                .build();

        DependentWorkflowDefinition dep = new DependentWorkflowDefinition();
        dep.setWorkflowDefinitionCode(downstreamCode);

        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamCode))
                .thenReturn(Collections.singletonList(dep));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(downstreamCode)))
                .thenReturn(Collections.singletonList(offlineDownstream));

        Method method = BackfillWorkflowExecutorDelegate.class.getDeclaredMethod(
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class, Set.class);
        method.setAccessible(true);

        Set<Long> visitedCodes = new HashSet<>();
        visitedCodes.add(dto.getWorkflowDefinition().getCode());
        method.invoke(
                backfillWorkflowExecutorDelegate,
                dto,
                Collections.singletonList(DateUtils.stringToZoneDateTime("2026-02-01 00:00:00")),
                visitedCodes);

        verify(backfillWorkflowExecutorDelegate, never()).executeWithVisitedCodes(any(), any());
    }

    @Test
    public void testDoBackfillDependentWorkflow_MultiLevelAndCycle() throws Exception {
        long workflowA = 10L;
        long workflowB = 20L;
        long workflowC = 30L;

        WorkflowDefinition upstreamA =
                WorkflowDefinition.builder().code(workflowA).releaseState(ReleaseState.ONLINE).build();
        WorkflowDefinition downstreamB =
                WorkflowDefinition.builder().code(workflowB).releaseState(ReleaseState.ONLINE).warningGroupId(1)
                        .build();
        WorkflowDefinition downstreamC =
                WorkflowDefinition.builder().code(workflowC).releaseState(ReleaseState.ONLINE).warningGroupId(2)
                        .build();

        BackfillWorkflowDTO.BackfillParamsDTO params = BackfillWorkflowDTO.BackfillParamsDTO.builder()
                .runMode(RunMode.RUN_MODE_SERIAL)
                .backfillDateList(Collections.<ZonedDateTime>emptyList())
                .backfillDependentMode(ComplementDependentMode.ALL_DEPENDENT)
                .allLevelDependent(true)
                .executionOrder(ExecutionOrder.ASC_ORDER)
                .build();

        BackfillWorkflowDTO dtoA = BackfillWorkflowDTO.builder()
                .workflowDefinition(upstreamA)
                .backfillParams(params)
                .build();

        DependentWorkflowDefinition depToB = new DependentWorkflowDefinition();
        depToB.setWorkflowDefinitionCode(workflowB);
        DependentWorkflowDefinition depToA = new DependentWorkflowDefinition();
        depToA.setWorkflowDefinitionCode(workflowA);
        DependentWorkflowDefinition depToC = new DependentWorkflowDefinition();
        depToC.setWorkflowDefinitionCode(workflowC);

        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(workflowA))
                .thenReturn(Collections.singletonList(depToB));
        when(workflowLineageService.queryDownstreamDependentWorkflowDefinitions(workflowB))
                .thenReturn(Arrays.asList(depToA, depToC));
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(workflowB)))
                .thenReturn(Collections.singletonList(downstreamB));
        when(workflowDefinitionDao.queryByCodes(new LinkedHashSet<>(Arrays.asList(workflowA, workflowC))))
                .thenReturn(Collections.singletonList(downstreamC));

        ArgumentCaptor<BackfillWorkflowDTO> captor = ArgumentCaptor.forClass(BackfillWorkflowDTO.class);
        doReturn(Collections.singletonList(1)).when(backfillWorkflowExecutorDelegate)
                .executeWithVisitedCodes(captor.capture(), any());

        Method method = BackfillWorkflowExecutorDelegate.class.getDeclaredMethod(
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class, Set.class);
        method.setAccessible(true);

        List<ZonedDateTime> backfillDates =
                Collections.singletonList(DateUtils.stringToZoneDateTime("2026-02-01 00:00:00"));
        Set<Long> visitedCodes = new HashSet<>();
        visitedCodes.add(workflowA);

        // Level 1: A -> B
        method.invoke(backfillWorkflowExecutorDelegate, dtoA, backfillDates, visitedCodes);
        BackfillWorkflowDTO dtoB = captor.getAllValues().get(0);

        // Level 2: B -> A(cycle, should skip) and B -> C(should trigger)
        method.invoke(backfillWorkflowExecutorDelegate, dtoB, backfillDates, visitedCodes);

        verify(backfillWorkflowExecutorDelegate, times(2)).executeWithVisitedCodes(any(), any());
        verify(workflowDefinitionDao, times(2)).queryByCodes(any());

        List<Long> triggeredCodes = captor.getAllValues().stream()
                .map(it -> it.getWorkflowDefinition().getCode())
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(workflowB, workflowC), triggeredCodes);
        Assertions.assertTrue(visitedCodes.contains(workflowB));
        Assertions.assertTrue(visitedCodes.contains(workflowC));
    }

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
}
