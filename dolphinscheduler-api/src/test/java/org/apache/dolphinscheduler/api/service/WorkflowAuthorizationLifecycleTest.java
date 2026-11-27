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
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflow_authorization_lifecycle;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true;"
})
@Transactional
public class WorkflowAuthorizationLifecycleTest extends AbstractControllerTest {

    private static final long PROJECT_CODE = 8877201L;
    private static final long WORKFLOW_CODE = 8877211L;
    private static final long CHILD_WORKFLOW_CODE = 8877212L;
    private static final long SHELL_TASK_CODE = 8877221L;
    private static final long DEPENDENT_TASK_CODE = 8877222L;

    @Autowired
    private WorkflowDefinitionService workflowDefinitionService;
    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private ProjectResourceAuthorizationService projectResourceAuthorizationService;
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
    private WorkflowInstanceMapper workflowInstanceMapper;
    @Autowired
    private WorkflowInstanceRelationMapper workflowInstanceRelationMapper;

    @BeforeEach
    public void createProject() {
        Project project = new Project();
        project.setCode(PROJECT_CODE);
        project.setName("authorization-lifecycle");
        project.setUserId(user.getId());
        project.setCreateTime(new Date());
        projectMapper.insert(project);
        projectResourceAuthorizationService.grantResource(user, PROJECT_CODE, ProjectResourceType.WORKER_GROUP,
                "default");
    }

    @Test
    public void restoreVersionUsesItsOwnTaskSetForSelfReferences() {
        createHistoricalTasks(WORKFLOW_CODE, SHELL_TASK_CODE);

        workflowDefinitionService.switchWorkflowDefinitionVersion(user, PROJECT_CODE, WORKFLOW_CODE, 1);

        Assertions.assertEquals(1, workflowDefinitionMapper.queryByCode(WORKFLOW_CODE).getVersion());
        Assertions.assertEquals(2, workflowTaskRelationMapper.queryByWorkflowDefinitionCode(WORKFLOW_CODE).size());
        Assertions.assertNotNull(taskDefinitionMapper.queryByCode(SHELL_TASK_CODE));
        Assertions.assertNotNull(taskDefinitionMapper.queryByCode(DEPENDENT_TASK_CODE));
    }

    @ParameterizedTest
    @CsvSource({"8877211,8877223", "8877212,8877221"})
    public void restoreVersionRejectsMissingSelfTaskOrExternalCurrentTask(long referencedWorkflow,
                                                                          long referencedTask) {
        createHistoricalTasks(referencedWorkflow, referencedTask);

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(user, PROJECT_CODE, WORKFLOW_CODE, 1));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), error.getCode());
        Assertions.assertEquals(2, workflowDefinitionMapper.queryByCode(WORKFLOW_CODE).getVersion());
        Assertions.assertTrue(workflowTaskRelationMapper.queryByWorkflowDefinitionCode(WORKFLOW_CODE).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    public void parentRemainsDeletableAfterChildDeletion(boolean historicalDanglingLink, boolean deleteDefinition) {
        WorkflowDefinition parentDefinition = createWorkflow(WORKFLOW_CODE, 1);
        WorkflowDefinition childDefinition = createWorkflow(CHILD_WORKFLOW_CODE, 1);
        WorkflowInstance parent = createInstance(parentDefinition, Flag.NO);
        WorkflowInstance child = createInstance(childDefinition, Flag.YES);
        WorkflowInstanceRelation relation = new WorkflowInstanceRelation();
        relation.setParentWorkflowInstanceId(parent.getId());
        relation.setParentTaskInstanceId(8877231);
        relation.setWorkflowInstanceId(child.getId());
        workflowInstanceRelationMapper.insert(relation);

        if (historicalDanglingLink) {
            workflowInstanceMapper.deleteById(child.getId());
        } else {
            workflowInstanceService.deleteWorkflowInstanceById(user, child.getId());
            Assertions.assertNull(workflowInstanceRelationMapper.selectById(relation.getId()));
        }
        Assertions.assertNotNull(workflowInstanceMapper.selectById(parent.getId()));

        if (deleteDefinition) {
            workflowDefinitionService.deleteWorkflowDefinitionByCode(user, WORKFLOW_CODE);
            Assertions.assertNull(workflowDefinitionMapper.queryByCode(WORKFLOW_CODE));
        } else {
            workflowInstanceService.deleteWorkflowInstanceById(user, parent.getId());
        }

        Assertions.assertNull(workflowInstanceMapper.selectById(parent.getId()));
        Assertions.assertNull(workflowInstanceMapper.selectById(child.getId()));
        Assertions.assertNull(workflowInstanceRelationMapper.selectById(relation.getId()));
        Assertions.assertNotNull(workflowDefinitionMapper.queryByCode(CHILD_WORKFLOW_CODE));
    }

    private void createHistoricalTasks(long referencedWorkflow, long referencedTask) {
        WorkflowDefinition workflow = createWorkflow(WORKFLOW_CODE, 1);
        TaskDefinitionLog shell = historicalTask(SHELL_TASK_CODE, "SHELL",
                "{\"localParams\":[],\"resourceList\":[],\"rawScript\":\"echo test\"}");
        TaskDefinitionLog dependent = historicalTask(DEPENDENT_TASK_CODE, "DEPENDENT",
                String.format("{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\","
                        + "\"dependTaskList\":[{\"relation\":\"AND\",\"dependItemList\":[{\"projectCode\":%d,"
                        + "\"definitionCode\":%d,\"depTaskCode\":%d,\"cycle\":\"day\",\"dateValue\":\"last1Days\"}]}]}}",
                        PROJECT_CODE, referencedWorkflow, referencedTask));
        for (TaskDefinitionLog task : new TaskDefinitionLog[]{shell, dependent}) {
            taskDefinitionLogMapper.insert(task);
            WorkflowTaskRelationLog relation = new WorkflowTaskRelationLog();
            relation.setProjectCode(PROJECT_CODE);
            relation.setWorkflowDefinitionCode(WORKFLOW_CODE);
            relation.setWorkflowDefinitionVersion(1);
            relation.setPreTaskCode(0L);
            relation.setPreTaskVersion(0);
            relation.setPostTaskCode(task.getCode());
            relation.setPostTaskVersion(1);
            relation.setCreateTime(new Date());
            relation.setUpdateTime(new Date());
            workflowTaskRelationLogMapper.insert(relation);
        }
        workflow.setVersion(2);
        workflowDefinitionMapper.updateById(workflow);
        workflowDefinitionLogMapper.insert(new WorkflowDefinitionLog(workflow));
        if (referencedWorkflow != WORKFLOW_CODE) {
            createWorkflow(referencedWorkflow, 2);
        }
    }

    private TaskDefinitionLog historicalTask(long code, String type, String parameters) {
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setCode(code);
        task.setProjectCode(PROJECT_CODE);
        task.setVersion(1);
        task.setName("historical-" + code);
        task.setUserId(user.getId());
        task.setTaskType(type);
        task.setTaskParams(parameters);
        task.setWorkerGroup("default");
        task.setFlag(Flag.YES);
        task.setTaskPriority(Priority.MEDIUM);
        task.setTimeoutFlag(TimeoutFlag.CLOSE);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        return task;
    }

    private WorkflowDefinition createWorkflow(long code, int version) {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setCode(code);
        workflow.setProjectCode(PROJECT_CODE);
        workflow.setVersion(version);
        workflow.setName("lifecycle-" + code);
        workflow.setUserId(user.getId());
        workflow.setReleaseState(ReleaseState.OFFLINE);
        workflow.setFlag(Flag.YES);
        workflow.setGlobalParams("[]");
        workflow.setCreateTime(new Date());
        workflow.setUpdateTime(new Date());
        workflowDefinitionMapper.insert(workflow);
        workflowDefinitionLogMapper.insert(new WorkflowDefinitionLog(workflow));
        return workflow;
    }

    private WorkflowInstance createInstance(WorkflowDefinition workflow, Flag subWorkflow) {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setName(workflow.getName());
        instance.setProjectCode(workflow.getProjectCode());
        instance.setWorkflowDefinitionCode(workflow.getCode());
        instance.setWorkflowDefinitionVersion(workflow.getVersion());
        instance.setState(WorkflowExecutionStatus.SUCCESS);
        instance.setIsSubWorkflow(subWorkflow);
        instance.setExecutorId(user.getId());
        workflowInstanceMapper.insert(instance);
        return instance;
    }
}
