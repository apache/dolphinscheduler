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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflow_version_restore;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true;"
})
public class WorkflowVersionRestoreTest extends AbstractControllerTest {

    private static final long PROJECT_CODE = 9876501L;
    private static final long WORKFLOW_CODE = 9876502L;
    private static final long EXISTING_TASK_CODE = 9876503L;
    private static final long NEW_TASK_CODE = 9876504L;

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkflowDefinitionService workflowDefinitionService;
    @Autowired
    private ProjectResourceAuthorizationService projectResourceAuthorizationService;
    @Autowired
    private TaskDefinitionService taskDefinitionService;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;
    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;
    @Autowired
    private WorkflowInstanceMapper workflowInstanceMapper;
    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;
    @Autowired
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;
    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;
    @SpyBean
    private TaskDefinitionMapper taskDefinitionMapper;
    @MockBean
    private SchedulerService schedulerService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TaskDefinitionLog existingTask;
    private WorkflowInstance instance;

    @BeforeEach
    public void createWorkflow() {
        for (String table : Arrays.asList("t_ds_project_resource_authorization", "t_ds_workflow_instance",
                "t_ds_workflow_task_relation",
                "t_ds_workflow_task_relation_log", "t_ds_workflow_definition", "t_ds_workflow_definition_log",
                "t_ds_task_definition", "t_ds_task_definition_log")) {
            jdbcTemplate.update("DELETE FROM " + table + " WHERE project_code = ?", PROJECT_CODE);
        }
        jdbcTemplate.update("DELETE FROM t_ds_project WHERE code = ?", PROJECT_CODE);
        Project project = new Project();
        project.setCode(PROJECT_CODE);
        project.setName("workflow-version-restore");
        project.setUserId(user.getId());
        project.setCreateTime(new Date());
        projectMapper.insert(project);
        projectResourceAuthorizationService.grantResource(user, PROJECT_CODE, ProjectResourceType.WORKER_GROUP,
                "default");

        existingTask = task(EXISTING_TASK_CODE, "existing");
        taskDefinitionLogMapper.insert(existingTask);
        taskDefinitionMapper.insert(new TaskDefinitionLog(existingTask));
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setCode(WORKFLOW_CODE);
        definition.setProjectCode(PROJECT_CODE);
        definition.setName("restore-history-only-task");
        definition.setVersion(1);
        definition.setUserId(user.getId());
        definition.setReleaseState(ReleaseState.OFFLINE);
        definition.setFlag(Flag.YES);
        definition.setGlobalParams("[]");
        definition.setCreateTime(new Date());
        definition.setUpdateTime(new Date());
        workflowDefinitionMapper.insert(definition);
        workflowDefinitionLogMapper.insert(new WorkflowDefinitionLog(definition));
        WorkflowTaskRelationLog relation = relation(0L, EXISTING_TASK_CODE);
        workflowTaskRelationLogMapper.insert(relation);
        workflowTaskRelationMapper.insert(new WorkflowTaskRelation(relation));

        instance = new WorkflowInstance();
        instance.setName("finished-instance");
        instance.setProjectCode(PROJECT_CODE);
        instance.setWorkflowDefinitionCode(WORKFLOW_CODE);
        instance.setWorkflowDefinitionVersion(1);
        instance.setState(WorkflowExecutionStatus.SUCCESS);
        instance.setCommandType(CommandType.START_PROCESS);
        instance.setGlobalParams("[]");
        instance.setExecutorId(user.getId());
        workflowInstanceMapper.insert(instance);
    }

    @Test
    public void restoresTasksCreatedOnlyInInstanceHistory() throws Exception {
        int version = saveInstanceWithNewTask();
        ServiceException beforeRestore = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.queryTaskDefinitionDetail(user, PROJECT_CODE, NEW_TASK_CODE));
        Assertions.assertEquals(Status.TASK_DEFINE_NOT_EXIST.getCode(), beforeRestore.getCode());

        workflowDefinitionService.switchWorkflowDefinitionVersion(user, PROJECT_CODE, WORKFLOW_CODE, version);

        Assertions.assertEquals(version, workflowDefinitionMapper.queryByCode(WORKFLOW_CODE).getVersion());
        List<WorkflowTaskRelation> restoredRelations =
                workflowTaskRelationMapper.queryByWorkflowDefinitionCode(WORKFLOW_CODE);
        Assertions.assertEquals(2, restoredRelations.size());
        for (WorkflowTaskRelation relation : restoredRelations) {
            mockMvc.perform(get("/projects/{projectCode}/task-definition/{code}", PROJECT_CODE,
                    relation.getPostTaskCode()).header(SESSION_ID, sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(Status.SUCCESS.getCode()))
                    .andExpect(jsonPath("$.data.projectCode").value(PROJECT_CODE))
                    .andExpect(jsonPath("$.data.version").value(relation.getPostTaskVersion()));
        }
        Mockito.verifyNoInteractions(schedulerService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void rollsBackWorkflowRestoreIfTaskWriteFails(boolean failInsert) {
        int version = saveInstanceWithNewTask();
        TaskDefinition currentTask = taskDefinitionMapper.queryByCode(EXISTING_TASK_CODE);
        currentTask.setVersion(9);
        taskDefinitionMapper.updateById(currentTask);
        taskDefinitionLogMapper.insert(new TaskDefinitionLog(currentTask));
        if (failInsert) {
            Mockito.doReturn(0).when(taskDefinitionMapper)
                    .insert(Mockito.argThat(task -> task.getCode() == NEW_TASK_CODE));
        } else {
            Mockito.doReturn(0).when(taskDefinitionMapper)
                    .updateById(Mockito.argThat(task -> task.getCode() == EXISTING_TASK_CODE));
        }

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(user, PROJECT_CODE, WORKFLOW_CODE,
                        version));

        Assertions.assertEquals(Status.SWITCH_WORKFLOW_DEFINITION_VERSION_ERROR.getCode(), error.getCode());
        Assertions.assertEquals(1, workflowDefinitionMapper.queryByCode(WORKFLOW_CODE).getVersion());
        List<WorkflowTaskRelation> currentRelations =
                workflowTaskRelationMapper.queryByWorkflowDefinitionCode(WORKFLOW_CODE);
        Assertions.assertEquals(1, currentRelations.size());
        Assertions.assertEquals(EXISTING_TASK_CODE, currentRelations.get(0).getPostTaskCode());
        Assertions.assertEquals(1, currentRelations.get(0).getWorkflowDefinitionVersion());
        Assertions.assertEquals(9, taskDefinitionService.getTaskDefinition(user, EXISTING_TASK_CODE).getVersion());
        Assertions.assertNull(taskDefinitionMapper.queryByCode(NEW_TASK_CODE));
        Mockito.verifyNoInteractions(schedulerService);
    }

    private int saveInstanceWithNewTask() {
        TaskDefinitionLog newTask = task(NEW_TASK_CODE, "new");
        workflowInstanceService.updateWorkflowInstance(user, PROJECT_CODE, instance.getId(),
                JSONUtils.toJsonString(Arrays.asList(relation(0L, EXISTING_TASK_CODE),
                        relation(EXISTING_TASK_CODE, NEW_TASK_CODE))),
                JSONUtils.toJsonString(Arrays.asList(existingTask, newTask)), null, false, "[]", "[]", 0);
        int version = workflowInstanceMapper.selectById(instance.getId()).getWorkflowDefinitionVersion();
        Assertions.assertTrue(version > 1);
        Assertions.assertEquals(1, workflowDefinitionMapper.queryByCode(WORKFLOW_CODE).getVersion());
        Assertions.assertNotNull(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(NEW_TASK_CODE, 1));
        Assertions.assertNull(taskDefinitionMapper.queryByCode(NEW_TASK_CODE));
        return version;
    }

    private TaskDefinitionLog task(long code, String name) {
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setCode(code);
        task.setVersion(1);
        task.setName(name);
        task.setProjectCode(PROJECT_CODE);
        task.setUserId(user.getId());
        task.setTaskType("SHELL");
        task.setTaskParams("{\"rawScript\":\"echo test\",\"localParams\":[],\"resourceList\":[]}");
        task.setFlag(Flag.YES);
        task.setTaskPriority(Priority.MEDIUM);
        task.setTimeoutFlag(TimeoutFlag.CLOSE);
        task.setWorkerGroup("default");
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        return task;
    }

    private WorkflowTaskRelationLog relation(long preTaskCode, long postTaskCode) {
        WorkflowTaskRelationLog relation = new WorkflowTaskRelationLog();
        relation.setProjectCode(PROJECT_CODE);
        relation.setWorkflowDefinitionCode(WORKFLOW_CODE);
        relation.setWorkflowDefinitionVersion(1);
        relation.setPreTaskCode(preTaskCode);
        relation.setPreTaskVersion(preTaskCode == 0 ? 0 : 1);
        relation.setPostTaskCode(postTaskCode);
        relation.setPostTaskVersion(1);
        relation.setCreateTime(new Date());
        relation.setUpdateTime(new Date());
        return relation;
    }
}
