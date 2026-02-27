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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.service.WorkflowLineageService;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.dao.entity.DependentWorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class);
        method.setAccessible(true);

        List<String> backfillTimeList = Collections.singletonList("2026-02-01 00:00:00");

        method.invoke(backfillWorkflowExecutorDelegate, dto, backfillTimeList);

        verify(workflowDefinitionDao, never()).queryByCode(anyLong());
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
        when(workflowDefinitionDao.queryByCode(downstreamCode))
                .thenReturn(Optional.of(downstreamWorkflow));

        ArgumentCaptor<BackfillWorkflowDTO> captor = ArgumentCaptor.forClass(BackfillWorkflowDTO.class);
        doReturn(Collections.singletonList(1)).when(backfillWorkflowExecutorDelegate).execute(captor.capture());

        Method method = BackfillWorkflowExecutorDelegate.class.getDeclaredMethod(
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class);
        method.setAccessible(true);

        List<String> backfillTimeList = Arrays.asList(
                "2026-02-01 00:00:00",
                "2026-02-02 00:00:00");

        method.invoke(backfillWorkflowExecutorDelegate, dto, backfillTimeList);

        verify(workflowDefinitionDao).queryByCode(downstreamCode);

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
        Assertions.assertEquals(backfillTimeList.size(), capturedParams.getBackfillDateList().size());
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
        when(workflowDefinitionDao.queryByCode(downstreamCode))
                .thenReturn(Optional.of(downstreamWorkflow));

        ArgumentCaptor<BackfillWorkflowDTO> captor = ArgumentCaptor.forClass(BackfillWorkflowDTO.class);
        doReturn(Collections.singletonList(1)).when(backfillWorkflowExecutorDelegate).execute(captor.capture());

        Method method = BackfillWorkflowExecutorDelegate.class.getDeclaredMethod(
                "doBackfillDependentWorkflow", BackfillWorkflowDTO.class, List.class);
        method.setAccessible(true);

        List<String> backfillTimeList = Collections.singletonList("2026-02-03 00:00:00");

        method.invoke(backfillWorkflowExecutorDelegate, dto, backfillTimeList);

        verify(workflowDefinitionDao).queryByCode(downstreamCode);

        BackfillWorkflowDTO captured = captor.getValue();
        Assertions.assertNotNull(captured);

        BackfillWorkflowDTO.BackfillParamsDTO capturedParams = captured.getBackfillParams();
        Assertions.assertNotNull(capturedParams);
        Assertions.assertEquals(ComplementDependentMode.OFF_MODE, capturedParams.getBackfillDependentMode());
        Assertions.assertFalse(capturedParams.isAllLevelDependent());
        Assertions.assertEquals(backfillTimeList.size(), capturedParams.getBackfillDateList().size());
    }
}
