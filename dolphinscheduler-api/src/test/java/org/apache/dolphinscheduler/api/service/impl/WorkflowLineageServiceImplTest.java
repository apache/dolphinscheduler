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

package org.apache.dolphinscheduler.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.DependentWorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskLineageDao;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkflowLineageServiceImplTest {

    @InjectMocks
    private WorkflowLineageServiceImpl workflowLineageService;

    @Mock
    private WorkflowTaskLineageDao workflowTaskLineageDao;

    @Mock
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workflowLineageService, "workflowTaskLineageDao", workflowTaskLineageDao);
        ReflectionTestUtils.setField(workflowLineageService, "workflowDefinitionDao", workflowDefinitionDao);
        ReflectionTestUtils.setField(workflowLineageService, "taskDefinitionMapper", taskDefinitionMapper);
    }

    @Test
    void shouldReturnEmptyListWhenNoLineageExist() {
        long workflowCode = 100L;
        when(workflowTaskLineageDao
                .queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE, workflowCode, Constants.DEPENDENT_ALL_TASK))
                        .thenReturn(Collections.emptyList());

        List<DependentWorkflowDefinition> result =
                workflowLineageService.queryDownstreamDependentWorkflowDefinitions(workflowCode);

        assertThat(result).isEmpty();
        verifyNoInteractions(workflowDefinitionDao, taskDefinitionMapper);
    }

    @Test
    void shouldBuildDependentWorkflowDefinitions() {
        long upstreamWorkflowCode = 1L;

        WorkflowTaskLineage taskLineage = new WorkflowTaskLineage();
        taskLineage.setWorkflowDefinitionCode(200L);
        taskLineage.setDeptWorkflowDefinitionCode(upstreamWorkflowCode);
        taskLineage.setTaskDefinitionCode(300L);
        taskLineage.setDeptTaskDefinitionCode(0L);

        WorkflowTaskLineage workflowLineage = new WorkflowTaskLineage();
        workflowLineage.setWorkflowDefinitionCode(201L);
        workflowLineage.setDeptWorkflowDefinitionCode(upstreamWorkflowCode);
        workflowLineage.setTaskDefinitionCode(0L);

        when(workflowTaskLineageDao
                .queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE, upstreamWorkflowCode,
                        Constants.DEPENDENT_ALL_TASK))
                                .thenReturn(Arrays.asList(taskLineage, workflowLineage));

        WorkflowDefinition workflowDefinition200 = new WorkflowDefinition();
        workflowDefinition200.setCode(200L);
        workflowDefinition200.setVersion(3);

        WorkflowDefinition workflowDefinition201 = new WorkflowDefinition();
        workflowDefinition201.setCode(201L);
        workflowDefinition201.setVersion(4);

        when(workflowDefinitionDao.queryByCodes(Arrays.asList(200L, 201L)))
                .thenReturn(Arrays.asList(workflowDefinition200, workflowDefinition201));

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setCode(300L);
        taskDefinition.setTaskParams("task-params");
        taskDefinition.setWorkerGroup("test-group");

        when(taskDefinitionMapper.queryByCodeList(Collections.singletonList(300L)))
                .thenReturn(Collections.singletonList(taskDefinition));

        List<DependentWorkflowDefinition> result =
                workflowLineageService.queryDownstreamDependentWorkflowDefinitions(upstreamWorkflowCode);

        assertThat(result).hasSize(2);

        DependentWorkflowDefinition taskDependent = result.stream()
                .filter(dependent -> dependent.getWorkflowDefinitionCode() == 200L)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected DependentWorkflowDefinition with code 200 not found"));
        assertThat(taskDependent.getTaskDefinitionCode()).isEqualTo(300L);
        assertThat(taskDependent.getTaskParams()).isEqualTo("task-params");
        assertThat(taskDependent.getWorkerGroup()).isEqualTo("test-group");
        assertThat(taskDependent.getWorkflowDefinitionVersion()).isEqualTo(3);

        DependentWorkflowDefinition workflowDependent = result.stream()
                .filter(dependent -> dependent.getWorkflowDefinitionCode() == 201L)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected DependentWorkflowDefinition with code 201 not found"));
        assertThat(workflowDependent.getTaskDefinitionCode()).isEqualTo(0L);
        assertThat(workflowDependent.getTaskParams()).isNull();
        assertThat(workflowDependent.getWorkerGroup()).isNull();
        assertThat(workflowDependent.getWorkflowDefinitionVersion()).isEqualTo(4);

        verify(taskDefinitionMapper).queryByCodeList(Collections.singletonList(300L));
    }

    @Test
    void resolveDownstream_oneHop_order() {
        long root = 10L;
        long child = 20L;

        WorkflowTaskLineage edge = new WorkflowTaskLineage();
        edge.setWorkflowDefinitionCode(child);
        edge.setDeptWorkflowDefinitionCode(root);
        edge.setTaskDefinitionCode(0L);

        when(workflowTaskLineageDao
                .queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE, root, Constants.DEPENDENT_ALL_TASK))
                        .thenReturn(Collections.singletonList(edge));
        when(workflowDefinitionDao.queryByCodes(Collections.singletonList(child)))
                .thenReturn(Collections.singletonList(WorkflowDefinition.builder().code(child).version(1).build()));

        List<WorkflowDefinition> result =
                workflowLineageService.resolveDownstreamWorkflowDefinitionCodes(root, false, false);

        assertThat(result).extracting(WorkflowDefinition::getCode).containsExactly(child);
    }

    @Test
    void resolveDownstream_transitive_bfsOrder() {
        long codeA = 1L;
        long codeB = 2L;
        long codeC = 3L;

        WorkflowTaskLineage aToB = new WorkflowTaskLineage();
        aToB.setWorkflowDefinitionCode(codeB);
        aToB.setDeptWorkflowDefinitionCode(codeA);
        aToB.setTaskDefinitionCode(0L);

        WorkflowTaskLineage bToC = new WorkflowTaskLineage();
        bToC.setWorkflowDefinitionCode(codeC);
        bToC.setDeptWorkflowDefinitionCode(codeB);
        bToC.setTaskDefinitionCode(0L);

        when(workflowTaskLineageDao
                .queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE, codeA, Constants.DEPENDENT_ALL_TASK))
                        .thenReturn(Collections.singletonList(aToB));
        when(workflowTaskLineageDao
                .queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE, codeB, Constants.DEPENDENT_ALL_TASK))
                        .thenReturn(Collections.singletonList(bToC));
        when(workflowTaskLineageDao
                .queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE, codeC, Constants.DEPENDENT_ALL_TASK))
                        .thenReturn(Collections.emptyList());

        WorkflowDefinition workflowB = WorkflowDefinition.builder().code(codeB).version(1).build();
        WorkflowDefinition workflowC = WorkflowDefinition.builder().code(codeC).version(1).build();
        when(workflowDefinitionDao.queryByCodes(Collections.singletonList(codeB)))
                .thenReturn(Collections.singletonList(workflowB));
        when(workflowDefinitionDao.queryByCodes(Collections.singletonList(codeC)))
                .thenReturn(Collections.singletonList(workflowC));

        List<WorkflowDefinition> result =
                workflowLineageService.resolveDownstreamWorkflowDefinitionCodes(codeA, true, false);

        assertThat(result).extracting(WorkflowDefinition::getCode).containsExactly(codeB, codeC);
    }

    @Test
    void resolveDownstream_transitive_filterOffline_shouldSkipAndNotExpandOffline() {
        long codeA = 11L;
        long codeB = 12L;

        WorkflowTaskLineage aToB = new WorkflowTaskLineage();
        aToB.setWorkflowDefinitionCode(codeB);
        aToB.setDeptWorkflowDefinitionCode(codeA);
        aToB.setTaskDefinitionCode(0L);

        when(workflowTaskLineageDao
                .queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE, codeA, Constants.DEPENDENT_ALL_TASK))
                        .thenReturn(Collections.singletonList(aToB));

        WorkflowDefinition workflowB = new WorkflowDefinition();
        workflowB.setCode(codeB);
        workflowB.setReleaseState(ReleaseState.OFFLINE);
        when(workflowDefinitionDao.queryByCodes(Collections.singletonList(codeB)))
                .thenReturn(Collections.singletonList(workflowB));

        List<WorkflowDefinition> result =
                workflowLineageService.resolveDownstreamWorkflowDefinitionCodes(codeA, true, true);

        assertThat(result).isEmpty();
    }
}
