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

package org.apache.dolphinscheduler.api.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.WorkflowLineageServiceImpl;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskLineageDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkflowTaskLineageServiceTest {

    @InjectMocks
    private WorkflowLineageServiceImpl workflowLineageService;

    @Mock
    private WorkflowTaskLineageDao workflowTaskLineageDao;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setCode(1L);
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    @Test
    public void testQueryWorkFlowLineageByName() {
        Project project = getProject("test");
        String name = "test";
        when(projectMapper.queryByCode(1L)).thenReturn(project);
        when(workflowTaskLineageDao.queryWorkFlowLineageByName(Mockito.anyLong(), Mockito.any()))
                .thenReturn(getWorkFlowLineages());
        List<WorkFlowRelationDetail> workFlowLineages = workflowLineageService.queryWorkFlowLineageByName(1L, name);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(workFlowLineages));
    }

    @Test
    public void testQueryWorkFlowLineage() {
        Project project = getProject("test");

        List<WorkflowTaskLineage> workflowTaskLineages = new ArrayList<>();
        WorkflowTaskLineage workflowTaskLineage = new WorkflowTaskLineage();
        workflowTaskLineage.setWorkflowDefinitionCode(1);
        workflowTaskLineage.setWorkflowDefinitionVersion(1);
        workflowTaskLineage.setTaskDefinitionCode(2L);
        workflowTaskLineage.setTaskDefinitionVersion(1);
        workflowTaskLineage.setDeptProjectCode(1111L);
        workflowTaskLineage.setDeptWorkflowDefinitionCode(1);
        workflowTaskLineage.setDeptTaskDefinitionCode(1111L);
        workflowTaskLineages.add(workflowTaskLineage);

        List<WorkFlowRelationDetail> workFlowRelationDetailList = new ArrayList<>();
        WorkFlowRelationDetail workFlowRelationDetail = new WorkFlowRelationDetail();
        workFlowRelationDetail.setWorkFlowCode(workflowTaskLineage.getWorkflowDefinitionCode());
        workFlowRelationDetail.setWorkFlowName("testProcessDefinitionName");
        workFlowRelationDetailList.add(workFlowRelationDetail);

        when(projectMapper.queryByCode(1L)).thenReturn(project);
        when(workflowTaskLineageDao.queryByProjectCode(project.getCode())).thenReturn(workflowTaskLineages);
        when(workflowTaskLineageDao.queryWorkFlowLineageByCode(workflowTaskLineage.getWorkflowDefinitionCode()))
                .thenReturn(workFlowRelationDetailList);

        WorkFlowLineage workFlowLineage = workflowLineageService.queryWorkFlowLineage(1L);

        List<WorkFlowRelationDetail> workFlowLineageList =
                workFlowLineage.getWorkFlowRelationDetailList();
        List<WorkFlowRelation> workFlowRelations =
                workFlowLineage.getWorkFlowRelationList();
        Assertions.assertTrue(!workFlowLineageList.isEmpty());
        Assertions.assertTrue(!workFlowRelations.isEmpty());
    }

    private List<WorkFlowRelationDetail> getWorkFlowLineages() {
        List<WorkFlowRelationDetail> workFlowLineages = new ArrayList<>();
        WorkFlowRelationDetail workFlowRelationDetail = new WorkFlowRelationDetail();
        workFlowRelationDetail.setWorkFlowCode(1);
        workFlowRelationDetail.setWorkFlowName("testdag");
        workFlowLineages.add(workFlowRelationDetail);
        return workFlowLineages;
    }

    @Test
    public void testTaskDependentMsgWithOrphanedLineageRecord() {
        // Test case: Handle dirty data scenario where taskDefinition is null
        long projectCode = 1L;
        long workflowDefinitionCode = 100L;
        long taskCode = 200L;

        // Create orphaned lineage record (taskDefinitionCode exists but taskDefinition is null)
        List<WorkflowTaskLineage> dependentWorkflowList = new ArrayList<>();
        WorkflowTaskLineage orphanedLineage = new WorkflowTaskLineage();
        orphanedLineage.setId(1);
        orphanedLineage.setDeptWorkflowDefinitionCode(50L);
        orphanedLineage.setTaskDefinitionCode(999L); // This task definition doesn't exist
        dependentWorkflowList.add(orphanedLineage);

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(50L);
        workflowDefinition.setName("TestWorkflow");

        when(workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode, workflowDefinitionCode, taskCode))
                .thenReturn(dependentWorkflowList);
        when(workflowDefinitionMapper.queryByCode(50L)).thenReturn(workflowDefinition);
        when(taskDefinitionMapper.queryByCode(999L)).thenReturn(null); // Task definition not found (dirty data)

        // Should return Optional.empty() because all records are orphaned
        Optional<String> result =
                workflowLineageService.taskDependentMsg(projectCode, workflowDefinitionCode, taskCode);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    public void testTaskDependentMsgWithMixedValidAndOrphanedRecords() {
        // Test case: Some records are valid, some are orphaned
        long projectCode = 1L;
        long workflowDefinitionCode = 100L;
        long taskCode = 200L;

        List<WorkflowTaskLineage> dependentWorkflowList = new ArrayList<>();

        // Valid lineage record
        WorkflowTaskLineage validLineage = new WorkflowTaskLineage();
        validLineage.setId(1);
        validLineage.setDeptWorkflowDefinitionCode(50L);
        validLineage.setTaskDefinitionCode(300L);
        dependentWorkflowList.add(validLineage);

        // Orphaned lineage record (dirty data)
        WorkflowTaskLineage orphanedLineage = new WorkflowTaskLineage();
        orphanedLineage.setId(2);
        orphanedLineage.setDeptWorkflowDefinitionCode(60L);
        orphanedLineage.setTaskDefinitionCode(999L); // This task definition doesn't exist
        dependentWorkflowList.add(orphanedLineage);

        WorkflowDefinition workflowDefinition1 = new WorkflowDefinition();
        workflowDefinition1.setCode(50L);
        workflowDefinition1.setName("ValidWorkflow");

        WorkflowDefinition workflowDefinition2 = new WorkflowDefinition();
        workflowDefinition2.setCode(60L);
        workflowDefinition2.setName("OrphanedWorkflow");

        TaskDefinition validTaskDefinition = new TaskDefinition();
        validTaskDefinition.setCode(300L);
        validTaskDefinition.setName("ValidTask");

        when(workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode, workflowDefinitionCode, taskCode))
                .thenReturn(dependentWorkflowList);
        when(workflowDefinitionMapper.queryByCode(50L)).thenReturn(workflowDefinition1);
        when(workflowDefinitionMapper.queryByCode(60L)).thenReturn(workflowDefinition2);
        when(taskDefinitionMapper.queryByCode(300L)).thenReturn(validTaskDefinition);
        when(taskDefinitionMapper.queryByCode(999L)).thenReturn(null); // Orphaned record

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setCode(taskCode);
        taskDefinition.setName("TestTask");
        when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(taskDefinition);

        // Should return a message with only the valid record, skipping the orphaned one
        Optional<String> result =
                workflowLineageService.taskDependentMsg(projectCode, workflowDefinitionCode, taskCode);
        Assertions.assertTrue(result.isPresent());
        String message = result.get();
        Assertions.assertTrue(message.contains("ValidWorkflow"));
        Assertions.assertTrue(message.contains("ValidTask"));
        // Orphaned record should be skipped, so it shouldn't appear in the message
        Assertions.assertFalse(message.contains("OrphanedWorkflow"));
    }

    @Test
    public void testTaskDependentMsgWithEmptyListAfterFilteringOrphanedRecords() {
        // Test case: All records are orphaned, resulting in empty list
        long projectCode = 1L;
        long workflowDefinitionCode = 100L;
        long taskCode = 0L; // No specific task code

        List<WorkflowTaskLineage> dependentWorkflowList = new ArrayList<>();
        WorkflowTaskLineage orphanedLineage = new WorkflowTaskLineage();
        orphanedLineage.setId(1);
        orphanedLineage.setDeptWorkflowDefinitionCode(50L);
        orphanedLineage.setTaskDefinitionCode(999L); // This task definition doesn't exist
        dependentWorkflowList.add(orphanedLineage);

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(50L);
        workflowDefinition.setName("TestWorkflow");

        when(workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode, workflowDefinitionCode, 0L))
                .thenReturn(dependentWorkflowList);
        when(workflowDefinitionMapper.queryByCode(50L)).thenReturn(workflowDefinition);
        when(taskDefinitionMapper.queryByCode(999L)).thenReturn(null); // Task definition not found

        // Should return Optional.empty() because all records are orphaned
        Optional<String> result =
                workflowLineageService.taskDependentMsg(projectCode, workflowDefinitionCode, taskCode);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    public void testUpdateWorkflowLineageWithNonEmptyList() {
        // Test case: Normal update with non-empty lineage list
        long workflowDefinitionCode = 100L;
        List<WorkflowTaskLineage> workflowTaskLineages = new ArrayList<>();

        WorkflowTaskLineage lineage1 = new WorkflowTaskLineage();
        lineage1.setWorkflowDefinitionCode(workflowDefinitionCode);
        lineage1.setTaskDefinitionCode(200L);
        workflowTaskLineages.add(lineage1);

        WorkflowTaskLineage lineage2 = new WorkflowTaskLineage();
        lineage2.setWorkflowDefinitionCode(workflowDefinitionCode);
        lineage2.setTaskDefinitionCode(300L);
        workflowTaskLineages.add(lineage2);

        // Mock DAO methods
        when(workflowTaskLineageDao.batchDeleteByWorkflowDefinitionCode(anyList())).thenReturn(2);
        when(workflowTaskLineageDao.batchInsert(workflowTaskLineages)).thenReturn(2);

        // Execute
        int result = workflowLineageService.updateWorkflowLineage(workflowDefinitionCode, workflowTaskLineages);

        // Verify
        Assertions.assertEquals(2, result);
        verify(workflowTaskLineageDao)
                .batchDeleteByWorkflowDefinitionCode(eq(java.util.Collections.singletonList(workflowDefinitionCode)));
        verify(workflowTaskLineageDao).batchInsert(workflowTaskLineages);
    }

    @Test
    public void testUpdateWorkflowLineageWithEmptyList() {
        // Test case: Empty list should delete historical lineage and return 0
        long workflowDefinitionCode = 100L;
        List<WorkflowTaskLineage> emptyList = new ArrayList<>();

        // Mock DAO method
        when(workflowTaskLineageDao.batchDeleteByWorkflowDefinitionCode(anyList())).thenReturn(1);

        // Execute
        int result = workflowLineageService.updateWorkflowLineage(workflowDefinitionCode, emptyList);

        // Verify
        Assertions.assertEquals(0, result);
        verify(workflowTaskLineageDao)
                .batchDeleteByWorkflowDefinitionCode(eq(java.util.Collections.singletonList(workflowDefinitionCode)));
        // batchInsert should not be called when list is empty
    }

    @Test
    public void testUpdateWorkflowLineageWithInsertFailure() {
        // Test case: Should throw exception when insert fails
        long workflowDefinitionCode = 100L;
        List<WorkflowTaskLineage> workflowTaskLineages = new ArrayList<>();

        WorkflowTaskLineage lineage1 = new WorkflowTaskLineage();
        lineage1.setWorkflowDefinitionCode(workflowDefinitionCode);
        lineage1.setTaskDefinitionCode(200L);
        workflowTaskLineages.add(lineage1);

        // Mock DAO methods
        when(workflowTaskLineageDao.batchDeleteByWorkflowDefinitionCode(anyList())).thenReturn(1);
        when(workflowTaskLineageDao.batchInsert(workflowTaskLineages)).thenReturn(0); // Insert failure

        // Execute and verify exception
        ServiceException exception = Assertions.assertThrows(ServiceException.class, () -> {
            workflowLineageService.updateWorkflowLineage(workflowDefinitionCode, workflowTaskLineages);
        });

        Assertions.assertEquals(Status.CREATE_WORKFLOW_LINEAGE_ERROR.getCode(), exception.getCode());
        verify(workflowTaskLineageDao)
                .batchDeleteByWorkflowDefinitionCode(eq(java.util.Collections.singletonList(workflowDefinitionCode)));
        verify(workflowTaskLineageDao).batchInsert(workflowTaskLineages);
    }

}
