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

import static org.mockito.ArgumentMatchers.isA;

import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationCreateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.WorkflowTaskRelationServiceImpl;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkflowTaskRelationServiceTest {

    @InjectMocks
    WorkflowTaskRelationServiceImpl workflowTaskRelationService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;

    @Mock
    private ProcessService processService;

    private static final long PROJECT_CODE = 1L;
    private static final long WORKFLOW_DEFINITION_CODE = 2L;
    private static final long UPSTREAM_TASK_CODE = 3L;
    private static final long DOWNSTREAM_TASK_CODE = 4L;
    protected User user;
    protected Exception exception;

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setTenantId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    /**
     * get mock Project
     *
     * @param projectCode projectCode
     * @return Project
     */
    private Project getProject(long projectCode) {
        Project project = new Project();
        project.setCode(projectCode);
        project.setId(1);
        project.setName("project_test1");
        project.setUserId(1);
        return project;
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    private TaskDefinitionLog buildTaskDefinitionLog(long projectCode, long code, int version) {

        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog() {

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof TaskDefinitionLog)) {
                    return false;
                }
                TaskDefinitionLog that = (TaskDefinitionLog) o;
                return getCode() == that.getCode()
                        && getVersion() == that.getVersion()
                        && getProjectCode() == that.getProjectCode();
            }

            @Override
            public int hashCode() {
                return Objects.hash(getCode(), getVersion(), getProjectCode());
            }
        };
        taskDefinitionLog.setProjectCode(projectCode);
        taskDefinitionLog.setCode(code);
        taskDefinitionLog.setVersion(version);
        return taskDefinitionLog;
    }

    private TaskDefinition buildTaskDefinition(long projectCode, long code, int version) {

        TaskDefinition taskDefinition = new TaskDefinition() {

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof TaskDefinition)) {
                    return false;
                }
                TaskDefinition that = (TaskDefinition) o;
                return getCode() == that.getCode()
                        && getVersion() == that.getVersion()
                        && getProjectCode() == that.getProjectCode();
            }

            @Override
            public int hashCode() {
                return Objects.hash(getCode(), getVersion(), getProjectCode());
            }
        };
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setCode(code);
        taskDefinition.setVersion(version);
        return taskDefinition;
    }

    private List<WorkflowTaskRelation> getWorkflowTaskUpstreamRelationList(long projectCode, long taskCode) {
        WorkflowTaskRelation workflowTaskRelationUpstream0 = new WorkflowTaskRelation();
        workflowTaskRelationUpstream0.setPostTaskCode(taskCode);
        workflowTaskRelationUpstream0.setPreTaskVersion(1);
        workflowTaskRelationUpstream0.setProjectCode(projectCode);
        workflowTaskRelationUpstream0.setPreTaskCode(123);
        workflowTaskRelationUpstream0.setWorkflowDefinitionCode(123);
        WorkflowTaskRelation workflowTaskRelationUpstream1 = new WorkflowTaskRelation();
        workflowTaskRelationUpstream1.setPostTaskCode(taskCode);
        workflowTaskRelationUpstream1.setPreTaskVersion(1);
        workflowTaskRelationUpstream1.setPreTaskCode(123);
        workflowTaskRelationUpstream1.setWorkflowDefinitionCode(124);
        workflowTaskRelationUpstream1.setProjectCode(projectCode);
        WorkflowTaskRelation workflowTaskRelationUpstream2 = new WorkflowTaskRelation();
        workflowTaskRelationUpstream2.setPostTaskCode(taskCode);
        workflowTaskRelationUpstream2.setPreTaskVersion(2);
        workflowTaskRelationUpstream2.setPreTaskCode(123);
        workflowTaskRelationUpstream2.setWorkflowDefinitionCode(125);
        workflowTaskRelationUpstream2.setProjectCode(projectCode);
        List<WorkflowTaskRelation> workflowTaskRelationList = new ArrayList<>();
        workflowTaskRelationList.add(workflowTaskRelationUpstream0);
        workflowTaskRelationList.add(workflowTaskRelationUpstream1);
        workflowTaskRelationList.add(workflowTaskRelationUpstream2);
        return workflowTaskRelationList;
    }

    private List<WorkflowTaskRelation> getWorkflowTaskDownstreamRelationList(long projectCode, long taskCode) {
        WorkflowTaskRelation workflowTaskRelationDownstream0 = new WorkflowTaskRelation();
        workflowTaskRelationDownstream0.setPreTaskCode(taskCode);
        workflowTaskRelationDownstream0.setPostTaskCode(456);
        workflowTaskRelationDownstream0.setPostTaskVersion(1);
        workflowTaskRelationDownstream0.setProjectCode(projectCode);
        WorkflowTaskRelation workflowTaskRelationDownstream1 = new WorkflowTaskRelation();
        workflowTaskRelationDownstream1.setPreTaskCode(taskCode);
        workflowTaskRelationDownstream1.setPostTaskCode(456);
        workflowTaskRelationDownstream1.setPostTaskVersion(1);
        workflowTaskRelationDownstream1.setProjectCode(projectCode);
        WorkflowTaskRelation workflowTaskRelationDownstream2 = new WorkflowTaskRelation();
        workflowTaskRelationDownstream2.setPreTaskCode(taskCode);
        workflowTaskRelationDownstream2.setPostTaskCode(4567);
        workflowTaskRelationDownstream2.setPostTaskVersion(1);
        workflowTaskRelationDownstream2.setProjectCode(projectCode);
        List<WorkflowTaskRelation> workflowTaskRelationList = new ArrayList<>();
        workflowTaskRelationList.add(workflowTaskRelationDownstream0);
        workflowTaskRelationList.add(workflowTaskRelationDownstream1);
        workflowTaskRelationList.add(workflowTaskRelationDownstream2);
        return workflowTaskRelationList;
    }

    private WorkflowDefinition getWorkflowDefinition() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setId(1);
        workflowDefinition.setProjectCode(1L);
        workflowDefinition.setName("test_pdf");
        workflowDefinition.setDescription("");
        workflowDefinition.setCode(1L);
        workflowDefinition.setVersion(1);
        return workflowDefinition;
    }

    private TaskDefinition getTaskDefinition() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(1L);
        taskDefinition.setCode(1L);
        taskDefinition.setVersion(1);
        taskDefinition.setTaskType("SHELL");
        return taskDefinition;
    }

    @Test
    public void testCreateWorkflowTaskRelation() {
        long projectCode = 1L;
        long workflowDefinitionCode = 1L;
        long preTaskCode = 0L;
        long postTaskCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        Mockito.when(workflowDefinitionMapper.queryByCode(workflowDefinitionCode)).thenReturn(getWorkflowDefinition());
        Mockito.when(
                workflowTaskRelationMapper.queryByCode(projectCode, workflowDefinitionCode, preTaskCode, postTaskCode))
                .thenReturn(Lists.newArrayList());
        Mockito.when(taskDefinitionMapper.queryByCode(postTaskCode)).thenReturn(getTaskDefinition());

        List<WorkflowTaskRelation> workflowTaskRelationList = Lists.newArrayList();
        List<WorkflowTaskRelationLog> workflowTaskRelationLogList = Lists.newArrayList();
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setProjectCode(projectCode);
        workflowTaskRelation.setWorkflowDefinitionCode(workflowDefinitionCode);
        workflowTaskRelation.setPreTaskCode(0L);
        workflowTaskRelation.setPreTaskVersion(0);
        workflowTaskRelation.setPostTaskCode(postTaskCode);
        workflowTaskRelation.setPostTaskVersion(1);
        workflowTaskRelationList.add(workflowTaskRelation);
        workflowTaskRelationLogList.add(new WorkflowTaskRelationLog(workflowTaskRelation));

        Mockito.when(workflowTaskRelationMapper.batchInsert(workflowTaskRelationList)).thenReturn(1);
        Mockito.when(workflowTaskRelationLogMapper.batchInsert(workflowTaskRelationLogList)).thenReturn(1);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testQueryDownstreamRelation() {
        long projectCode = 1L;
        long taskCode = 2L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);

        List<WorkflowTaskRelation> workflowTaskRelationList =
                getWorkflowTaskDownstreamRelationList(projectCode, taskCode);

        Mockito.when(workflowTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode))
                .thenReturn(workflowTaskRelationList);

        if (CollectionUtils.isNotEmpty(workflowTaskRelationList)) {
            Set<TaskDefinition> taskDefinitions = workflowTaskRelationList
                    .stream()
                    .map(workflowTaskRelation -> buildTaskDefinition(
                            workflowTaskRelation.getProjectCode(),
                            workflowTaskRelation.getPostTaskCode(),
                            workflowTaskRelation.getPostTaskVersion()))
                    .collect(Collectors.toSet());

            Set<TaskDefinitionLog> taskDefinitionLogSet = workflowTaskRelationList
                    .stream()
                    .map(workflowTaskRelation -> buildTaskDefinitionLog(
                            workflowTaskRelation.getProjectCode(),
                            workflowTaskRelation.getPostTaskCode(),
                            workflowTaskRelation.getPostTaskVersion()))
                    .collect(Collectors.toSet());
            List<TaskDefinitionLog> taskDefinitionLogList = taskDefinitionLogSet.stream().collect(Collectors.toList());
            Mockito.when(taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitions))
                    .thenReturn(taskDefinitionLogList);
        }
        Map<String, Object> relation = workflowTaskRelationService
                .queryDownstreamRelation(user, projectCode, taskCode);
        Assertions.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
        Assertions.assertEquals(2, ((List) relation.get("data")).size());
    }

    @Test
    public void testQueryUpstreamRelation() {
        long projectCode = 1L;
        long taskCode = 2L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        List<WorkflowTaskRelation> workflowTaskRelationList =
                getWorkflowTaskUpstreamRelationList(projectCode, taskCode);

        Mockito.when(workflowTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode))
                .thenReturn(workflowTaskRelationList);

        if (CollectionUtils.isNotEmpty(workflowTaskRelationList)) {
            Set<TaskDefinition> taskDefinitions = workflowTaskRelationList
                    .stream()
                    .map(workflowTaskRelation -> buildTaskDefinition(
                            workflowTaskRelation.getProjectCode(),
                            workflowTaskRelation.getPreTaskCode(),
                            workflowTaskRelation.getPreTaskVersion()))
                    .collect(Collectors.toSet());

            Set<TaskDefinitionLog> taskDefinitionLogSet = workflowTaskRelationList
                    .stream()
                    .map(workflowTaskRelation -> buildTaskDefinitionLog(
                            workflowTaskRelation.getProjectCode(),
                            workflowTaskRelation.getPreTaskCode(),
                            workflowTaskRelation.getPreTaskVersion()))
                    .collect(Collectors.toSet());
            List<TaskDefinitionLog> taskDefinitionLogList = taskDefinitionLogSet.stream().collect(Collectors.toList());
            Mockito.when(taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitions))
                    .thenReturn(taskDefinitionLogList);
        }
        Map<String, Object> relation = workflowTaskRelationService
                .queryUpstreamRelation(user, projectCode, taskCode);
        Assertions.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
        Assertions.assertEquals(2, ((List) relation.get("data")).size());
    }

    @Test
    public void testDeleteDownstreamRelation() {
        long projectCode = 1L;
        long taskCode = 2L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        List<WorkflowTaskRelation> workflowTaskRelationList = new ArrayList<>();
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setProjectCode(projectCode);
        workflowTaskRelation.setWorkflowDefinitionCode(1L);
        workflowTaskRelation.setPreTaskCode(taskCode);
        workflowTaskRelation.setPostTaskCode(123L);
        workflowTaskRelationList.add(workflowTaskRelation);
        Mockito.when(workflowTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode))
                .thenReturn(workflowTaskRelationList);
        WorkflowTaskRelationLog workflowTaskRelationLog = new WorkflowTaskRelationLog(workflowTaskRelation);
        Mockito.when(workflowTaskRelationMapper.deleteRelation(workflowTaskRelationLog)).thenReturn(1);
        Mockito.when(workflowTaskRelationLogMapper.deleteRelation(workflowTaskRelationLog)).thenReturn(1);
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        Mockito.when(workflowDefinitionMapper.queryByCode(1L)).thenReturn(workflowDefinition);
        Mockito.when(processService.saveWorkflowDefine(user, workflowDefinition, Boolean.TRUE, Boolean.TRUE))
                .thenReturn(1);
        Map<String, Object> result1 =
                workflowTaskRelationService.deleteDownstreamRelation(user, projectCode, "123", taskCode);
        Assertions.assertEquals(Status.SUCCESS, result1.get(Constants.STATUS));
    }

    @Test
    public void testDeleteUpstreamRelation() {
        long projectCode = 1L;
        long taskCode = 2L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        List<WorkflowTaskRelation> workflowTaskRelationList = Lists.newArrayList();
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setProjectCode(projectCode);
        workflowTaskRelation.setWorkflowDefinitionCode(1L);
        workflowTaskRelation.setPreTaskCode(0L);
        workflowTaskRelation.setPreTaskVersion(0);
        workflowTaskRelation.setPostTaskCode(taskCode);
        workflowTaskRelation.setPostTaskVersion(1);
        workflowTaskRelationList.add(workflowTaskRelation);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        Mockito.when(workflowTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode))
                .thenReturn(workflowTaskRelationList);
        Mockito.when(workflowDefinitionMapper.queryByCode(1L)).thenReturn(getWorkflowDefinition());
        Mockito.when(workflowTaskRelationMapper.queryByWorkflowDefinitionCode(1L)).thenReturn(workflowTaskRelationList);
        List<WorkflowTaskRelationLog> relationLogs =
                workflowTaskRelationList.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());
        Mockito.when(processService.saveTaskRelation(user, 1L, 1L,
                1, relationLogs, Lists.newArrayList(), Boolean.TRUE)).thenReturn(0);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testDeleteTaskWorkflowRelation() {
        long projectCode = 1L;
        long taskCode = 1L;
        long workflowDefinitionCode = 1L;
        long preTaskCode = 4L;
        long postTaskCode = 5L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        Mockito.when(
                workflowTaskRelationMapper.queryByCode(projectCode, workflowDefinitionCode, preTaskCode, postTaskCode))
                .thenReturn(Lists.newArrayList());
        Mockito.when(workflowDefinitionMapper.queryByCode(workflowDefinitionCode)).thenReturn(getWorkflowDefinition());
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(getTaskDefinition());
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskType("CONDITIONS");
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(taskDefinition);
        List<WorkflowTaskRelation> workflowTaskRelationList = Lists.newArrayList();
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setProjectCode(projectCode);
        workflowTaskRelation.setWorkflowDefinitionCode(1L);
        workflowTaskRelation.setPreTaskCode(0L);
        workflowTaskRelation.setPreTaskVersion(0);
        workflowTaskRelation.setPostTaskCode(taskCode);
        workflowTaskRelation.setPostTaskVersion(1);
        workflowTaskRelationList.add(workflowTaskRelation);
        Mockito.when(workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode))
                .thenReturn(workflowTaskRelationList);
        List<WorkflowTaskRelationLog> relationLogs =
                workflowTaskRelationList.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());

        Mockito.when(processService.saveTaskRelation(user, 1L, 1L,
                1, relationLogs, Lists.newArrayList(), Boolean.TRUE)).thenReturn(0);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testDeleteEdge() {
        long projectCode = 1L;
        long workflowDefinitionCode = 3L;
        long preTaskCode = 0L;
        long postTaskCode = 5L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setProjectCode(projectCode);
        workflowTaskRelation.setWorkflowDefinitionCode(workflowDefinitionCode);
        workflowTaskRelation.setWorkflowDefinitionVersion(1);
        workflowTaskRelation.setPreTaskCode(preTaskCode);
        workflowTaskRelation.setPostTaskCode(postTaskCode);
        WorkflowTaskRelationLog processTaskRelationLog = new WorkflowTaskRelationLog(workflowTaskRelation);
        processTaskRelationLog.setOperator(user.getId());
        List<WorkflowTaskRelation> workflowTaskRelationList = new ArrayList<>();
        workflowTaskRelationList.add(workflowTaskRelation);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        Mockito.when(workflowTaskRelationMapper.queryByWorkflowDefinitionCode(1L)).thenReturn(workflowTaskRelationList);
        List<WorkflowTaskRelationLog> relationLogs =
                workflowTaskRelationList.stream().map(WorkflowTaskRelationLog::new).collect(Collectors.toList());
        Mockito.when(processService.saveTaskRelation(user, 1L, 1L,
                1, relationLogs, Lists.newArrayList(), Boolean.TRUE)).thenReturn(0);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCreateWorkflowTaskRelationV2() {
        TaskRelationCreateRequest taskRelationCreateRequest = new TaskRelationCreateRequest();
        taskRelationCreateRequest.setWorkflowCode(WORKFLOW_DEFINITION_CODE);

        // error workflow definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowTaskRelationService.createWorkflowTaskRelationV2(user, taskRelationCreateRequest));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // error project without permissions
        Mockito.when(workflowDefinitionMapper.queryByCode(WORKFLOW_DEFINITION_CODE))
                .thenReturn(getWorkflowDefinition());
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject(PROJECT_CODE));
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, getProject(PROJECT_CODE), null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowTaskRelationService.createWorkflowTaskRelationV2(user, taskRelationCreateRequest));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // error insert workflow task relation
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, getProject(PROJECT_CODE),
                null);
        Mockito.when(workflowTaskRelationMapper.insert(isA(WorkflowTaskRelation.class))).thenReturn(0);
        Mockito.when(taskDefinitionMapper.queryByCode(isA(Long.class))).thenReturn(getTaskDefinition());
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowTaskRelationService.createWorkflowTaskRelationV2(user, taskRelationCreateRequest));
        Assertions.assertEquals(Status.CREATE_WORKFLOW_TASK_RELATION_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // error insert workflow task relation log
        Mockito.when(workflowTaskRelationMapper.insert(isA(WorkflowTaskRelation.class))).thenReturn(1);
        Mockito.when(workflowTaskRelationLogMapper.insert(isA(WorkflowTaskRelationLog.class))).thenReturn(0);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowTaskRelationService.createWorkflowTaskRelationV2(user, taskRelationCreateRequest));
        Assertions.assertEquals(Status.CREATE_WORKFLOW_TASK_RELATION_LOG_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.when(workflowTaskRelationLogMapper.insert(isA(WorkflowTaskRelationLog.class))).thenReturn(1);
        Assertions.assertDoesNotThrow(
                () -> workflowTaskRelationService.createWorkflowTaskRelationV2(user, taskRelationCreateRequest));
    }

    @Test
    public void testDeleteTaskWorkflowRelationV2() {
        TaskRelationCreateRequest taskRelationCreateRequest = new TaskRelationCreateRequest();
        taskRelationCreateRequest.setWorkflowCode(WORKFLOW_DEFINITION_CODE);

        // error task relation size
        Mockito.when(
                workflowTaskRelationMapper.filterWorkflowTaskRelation(isA(Page.class), isA(WorkflowTaskRelation.class)))
                .thenReturn(getMultiWorkflowTaskRelations());
        exception = Assertions.assertThrows(ServiceException.class, () -> workflowTaskRelationService
                .deleteTaskWorkflowRelationV2(user, UPSTREAM_TASK_CODE, DOWNSTREAM_TASK_CODE));
        Assertions.assertEquals(Status.WORKFLOW_TASK_RELATION_NOT_EXPECT.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.when(
                workflowTaskRelationMapper.filterWorkflowTaskRelation(isA(Page.class), isA(WorkflowTaskRelation.class)))
                .thenReturn(getOneWorkflowTaskRelation());
        Assertions.assertDoesNotThrow(() -> workflowTaskRelationService.deleteTaskWorkflowRelationV2(user,
                UPSTREAM_TASK_CODE, DOWNSTREAM_TASK_CODE));
    }

    private IPage<WorkflowTaskRelation> getOneWorkflowTaskRelation() {
        IPage<WorkflowTaskRelation> workflowTaskRelationIPage = new Page<>();
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setWorkflowDefinitionCode(WORKFLOW_DEFINITION_CODE);
        workflowTaskRelation.setPreTaskCode(UPSTREAM_TASK_CODE);
        workflowTaskRelation.setPostTaskCode(DOWNSTREAM_TASK_CODE);
        workflowTaskRelationIPage.setRecords(Collections.singletonList(workflowTaskRelation));
        return workflowTaskRelationIPage;
    }

    private IPage<WorkflowTaskRelation> getMultiWorkflowTaskRelations() {
        IPage<WorkflowTaskRelation> workflowTaskRelationIPage = new Page<>();
        List<WorkflowTaskRelation> workflowTaskRelations = new ArrayList<>();

        WorkflowTaskRelation workflowTaskRelation0 = new WorkflowTaskRelation();
        workflowTaskRelation0.setWorkflowDefinitionCode(WORKFLOW_DEFINITION_CODE);
        workflowTaskRelation0.setPreTaskCode(UPSTREAM_TASK_CODE);
        workflowTaskRelation0.setPostTaskCode(DOWNSTREAM_TASK_CODE);
        workflowTaskRelations.add(workflowTaskRelation0);

        WorkflowTaskRelation workflowTaskRelation1 = new WorkflowTaskRelation();
        workflowTaskRelation1.setWorkflowDefinitionCode(WORKFLOW_DEFINITION_CODE);
        workflowTaskRelation1.setPreTaskCode(UPSTREAM_TASK_CODE);
        workflowTaskRelation1.setPostTaskCode(DOWNSTREAM_TASK_CODE);
        workflowTaskRelations.add(workflowTaskRelation1);

        workflowTaskRelationIPage.setRecords(workflowTaskRelations);
        return workflowTaskRelationIPage;
    }
}
