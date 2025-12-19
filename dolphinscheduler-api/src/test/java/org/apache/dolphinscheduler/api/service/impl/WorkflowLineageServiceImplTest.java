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
import org.apache.dolphinscheduler.dao.entity.DependentWorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
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
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workflowLineageService, "workflowTaskLineageDao", workflowTaskLineageDao);
        ReflectionTestUtils.setField(workflowLineageService, "workflowDefinitionMapper", workflowDefinitionMapper);
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
        verifyNoInteractions(workflowDefinitionMapper, taskDefinitionMapper);
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

        when(workflowDefinitionMapper.queryByCodes(Arrays.asList(200L, 201L)))
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
}
