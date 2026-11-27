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

package org.apache.dolphinscheduler.tools.lineage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskLineageDao;
import org.apache.dolphinscheduler.plugin.task.api.task.DependentLogicTaskChannelFactory;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrateLineageServiceTest {

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Mock
    private WorkflowTaskLineageDao workflowTaskLineageDao;

    @InjectMocks
    private MigrateLineageService migrateLineageService;

    @Captor
    private ArgumentCaptor<List<WorkflowTaskLineage>> lineageCaptor;

    @Test
    void migratesEachDependencyWithItsOwnWorkflowAndTask() {
        TaskDefinition firstTask = dependentTask(11L, 2, 300L, 301L, 302L);
        TaskDefinition secondTask = dependentTask(22L, 3, 400L, 401L, 402L);
        when(taskDefinitionMapper.queryDefinitionsByTaskType(DependentLogicTaskChannelFactory.NAME))
                .thenReturn(List.of(firstTask, secondTask));

        WorkflowTaskRelation firstRelation = new WorkflowTaskRelation();
        firstRelation.setPostTaskCode(11L);
        firstRelation.setWorkflowDefinitionCode(100L);
        firstRelation.setWorkflowDefinitionVersion(4);

        WorkflowTaskRelation secondRelation = new WorkflowTaskRelation();
        secondRelation.setPreTaskCode(22L);
        secondRelation.setWorkflowDefinitionCode(200L);
        secondRelation.setWorkflowDefinitionVersion(5);

        when(workflowTaskRelationMapper.queryByTaskCodes(new Long[]{11L, 22L}))
                .thenReturn(List.of(firstRelation, secondRelation));

        migrateLineageService.migrateLineageOnce();

        verify(workflowTaskLineageDao).batchInsert(lineageCaptor.capture());
        assertThat(lineageCaptor.getValue())
                .extracting(WorkflowTaskLineage::getWorkflowDefinitionCode,
                        WorkflowTaskLineage::getWorkflowDefinitionVersion,
                        WorkflowTaskLineage::getTaskDefinitionCode,
                        WorkflowTaskLineage::getTaskDefinitionVersion,
                        WorkflowTaskLineage::getDeptProjectCode,
                        WorkflowTaskLineage::getDeptWorkflowDefinitionCode,
                        WorkflowTaskLineage::getDeptTaskDefinitionCode)
                .containsExactlyInAnyOrder(
                        tuple(100L, 4, 11L, 2, 300L, 301L, 302L),
                        tuple(200L, 5, 22L, 3, 400L, 401L, 402L));
    }

    @Test
    void skipsDependencyWithoutAWorkflowRelation() {
        TaskDefinition task = dependentTask(11L, 2, 300L, 301L, 302L);
        when(taskDefinitionMapper.queryDefinitionsByTaskType(DependentLogicTaskChannelFactory.NAME))
                .thenReturn(List.of(task));
        when(workflowTaskRelationMapper.queryByTaskCodes(new Long[]{11L})).thenReturn(List.of());

        migrateLineageService.migrateLineageOnce();

        verify(workflowTaskLineageDao).batchInsert(lineageCaptor.capture());
        assertThat(lineageCaptor.getValue()).isEmpty();
    }

    private TaskDefinition dependentTask(long code, int version, long upstreamProjectCode,
                                         long upstreamWorkflowCode, long upstreamTaskCode) {
        TaskDefinition taskDefinition = new TaskDefinition(code, version);
        taskDefinition.setTaskParams(String.format(
                "{\"dependence\":{\"dependTaskList\":[{\"dependItemList\":["
                        + "{\"projectCode\":%d,\"definitionCode\":%d,\"depTaskCode\":%d}]}]}}",
                upstreamProjectCode, upstreamWorkflowCode, upstreamTaskCode));
        return taskDefinition;
    }
}
