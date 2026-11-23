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

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.vo.WorkflowDefinitionCopyVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskLineageMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflow_copy_lineage;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true;"
})
@Transactional
public class WorkflowCopyLineageTest extends AbstractControllerTest {

    private static final long SOURCE_PROJECT_CODE = 8877101L;
    private static final long TARGET_PROJECT_CODE = 8877102L;
    private static final long DEPENDENT_PROJECT_CODE = 8877103L;
    private static final long SOURCE_WORKFLOW_CODE = 8877111L;
    private static final long SOURCE_TASK_CODE = 8877121L;
    private static final long EXTERNAL_TASK_CODE = 8877122L;

    @Autowired
    private WorkflowDefinitionService workflowDefinitionService;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;
    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;
    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;
    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;
    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;
    @Autowired
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;
    @Autowired
    private WorkflowTaskLineageMapper workflowTaskLineageMapper;

    @BeforeEach
    public void createSourceWorkflow() {
        for (long projectCode : Arrays.asList(SOURCE_PROJECT_CODE, TARGET_PROJECT_CODE, DEPENDENT_PROJECT_CODE)) {
            Project project = new Project();
            project.setCode(projectCode);
            project.setName("copy-lineage-" + projectCode);
            project.setUserId(user.getId());
            project.setCreateTime(new Date());
            projectMapper.insert(project);
        }
        TaskDefinitionLog task = dependentTask(SOURCE_TASK_CODE, SOURCE_PROJECT_CODE, "self-dependent",
                SOURCE_PROJECT_CODE, SOURCE_WORKFLOW_CODE);
        taskDefinitionLogMapper.insert(task);
        taskDefinitionMapper.insert(new TaskDefinitionLog(task));
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setCode(SOURCE_WORKFLOW_CODE);
        workflow.setProjectCode(SOURCE_PROJECT_CODE);
        workflow.setName("self-dependent-source");
        workflow.setVersion(1);
        workflow.setUserId(user.getId());
        workflow.setReleaseState(ReleaseState.OFFLINE);
        workflow.setFlag(Flag.YES);
        workflow.setGlobalParams("[]");
        workflow.setCreateTime(new Date());
        workflow.setUpdateTime(new Date());
        workflowDefinitionMapper.insert(workflow);
        workflowDefinitionLogMapper.insert(new WorkflowDefinitionLog(workflow));
        WorkflowTaskRelationLog relation = relation(SOURCE_PROJECT_CODE, SOURCE_WORKFLOW_CODE, SOURCE_TASK_CODE);
        workflowTaskRelationLogMapper.insert(relation);
        workflowTaskRelationMapper.insert(new WorkflowTaskRelation(relation));
    }

    @ParameterizedTest
    @ValueSource(longs = {TARGET_PROJECT_CODE, DEPENDENT_PROJECT_CODE})
    public void savedExternalDependencyBlocksDeletingCopy(long dependentProjectCode) {
        List<WorkflowDefinitionCopyVO> copies = workflowDefinitionService.batchCopyWorkflowDefinition(user,
                SOURCE_PROJECT_CODE, String.valueOf(SOURCE_WORKFLOW_CODE), TARGET_PROJECT_CODE);
        Assertions.assertEquals(1, copies.size());
        long copiedWorkflowCode = copies.get(0).getWorkflowDefinitionCode();
        long copiedTaskCode = workflowTaskRelationMapper.queryByWorkflowDefinitionCode(copiedWorkflowCode)
                .get(0).getPostTaskCode();
        WorkflowTaskLineage selfLineage =
                workflowTaskLineageMapper.queryByWorkflowDefinitionCode(copiedWorkflowCode).get(0);
        Assertions.assertEquals(copiedWorkflowCode, selfLineage.getDeptWorkflowDefinitionCode());
        Assertions.assertEquals(TARGET_PROJECT_CODE, selfLineage.getDeptProjectCode());

        TaskDefinitionLog externalTask = dependentTask(EXTERNAL_TASK_CODE, dependentProjectCode, "external-dependent",
                TARGET_PROJECT_CODE, copiedWorkflowCode);
        WorkflowDefinition externalWorkflow = workflowDefinitionService.createWorkflowDefinition(user,
                dependentProjectCode, "external-workflow", "", "[]", "[]", 0,
                JSONUtils
                        .toJsonString(Collections.singletonList(relation(dependentProjectCode, 0, EXTERNAL_TASK_CODE))),
                JSONUtils.toJsonString(Collections.singletonList(externalTask)), null,
                WorkflowExecutionTypeEnum.PARALLEL);
        WorkflowTaskLineage externalLineage =
                workflowTaskLineageMapper.queryByWorkflowDefinitionCode(externalWorkflow.getCode()).get(0);
        Assertions.assertEquals(dependentProjectCode, externalLineage.getDeptProjectCode());
        Assertions.assertEquals(copiedWorkflowCode, externalLineage.getDeptWorkflowDefinitionCode());
        Assertions.assertEquals(2, workflowTaskLineageMapper.queryWorkFlowLineageByDept(
                Constants.DEFAULT_PROJECT_CODE, copiedWorkflowCode, 0).size());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, copiedWorkflowCode));
        Assertions.assertTrue(exception.getMessage().contains("external-dependent"));
        Assertions.assertNotNull(workflowDefinitionMapper.queryByCode(copiedWorkflowCode));
        Assertions.assertNotNull(taskDefinitionMapper.queryByCode(copiedTaskCode));
        Assertions.assertFalse(workflowTaskRelationMapper.queryByWorkflowDefinitionCode(copiedWorkflowCode).isEmpty());

        workflowDefinitionService.deleteWorkflowDefinitionByCode(user, externalWorkflow.getCode());
        Assertions.assertEquals(1, workflowTaskLineageMapper.queryWorkFlowLineageByDept(
                Constants.DEFAULT_PROJECT_CODE, copiedWorkflowCode, 0).size());
        workflowDefinitionService.deleteWorkflowDefinitionByCode(user, copiedWorkflowCode);
        Assertions.assertNull(workflowDefinitionMapper.queryByCode(copiedWorkflowCode));
        Assertions.assertNull(taskDefinitionMapper.queryByCode(copiedTaskCode));
        Assertions.assertTrue(workflowTaskLineageMapper.queryWorkFlowLineageByDept(
                Constants.DEFAULT_PROJECT_CODE, copiedWorkflowCode, 0).isEmpty());
    }

    private TaskDefinitionLog dependentTask(long code, long projectCode, String name,
                                            long dependentProjectCode, long dependentWorkflowCode) {
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setCode(code);
        task.setVersion(1);
        task.setProjectCode(projectCode);
        task.setName(name);
        task.setUserId(user.getId());
        task.setTaskType("DEPENDENT");
        task.setTaskParams(String.format("{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\","
                + "\"dependTaskList\":[{\"relation\":\"AND\",\"dependItemList\":[{\"projectCode\":%d,"
                + "\"definitionCode\":%d,\"depTaskCode\":0,\"cycle\":\"day\",\"dateValue\":\"last1Days\"}]}]}}",
                dependentProjectCode, dependentWorkflowCode));
        task.setFlag(Flag.YES);
        task.setTaskPriority(Priority.MEDIUM);
        task.setTimeoutFlag(TimeoutFlag.CLOSE);
        task.setWorkerGroup("default");
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        return task;
    }

    private WorkflowTaskRelationLog relation(long projectCode, long workflowCode, long taskCode) {
        WorkflowTaskRelationLog relation = new WorkflowTaskRelationLog();
        relation.setProjectCode(projectCode);
        relation.setWorkflowDefinitionCode(workflowCode);
        relation.setWorkflowDefinitionVersion(1);
        relation.setPreTaskCode(0L);
        relation.setPreTaskVersion(0);
        relation.setPostTaskCode(taskCode);
        relation.setPostTaskVersion(1);
        relation.setCreateTime(new Date());
        relation.setUpdateTime(new Date());
        return relation;
    }
}
