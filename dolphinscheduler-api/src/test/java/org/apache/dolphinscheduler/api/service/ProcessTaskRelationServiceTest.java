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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProcessTaskRelationServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;

/**
 * process task instance relation service test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessTaskRelationServiceTest {

    @InjectMocks
    ProcessTaskRelationServiceImpl processTaskRelationService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    @Mock
    private ProcessService processService;

    /**
     * get Mock Admin User
     *
     * @return admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.GENERAL_USER);
        return loginUser;
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

    private List<ProcessTaskRelation> getProcessTaskUpstreamRelationList(long projectCode, long taskCode) {
        ProcessTaskRelation processTaskRelationUpstream0 = new ProcessTaskRelation();
        processTaskRelationUpstream0.setPostTaskCode(taskCode);
        processTaskRelationUpstream0.setPreTaskVersion(1);
        processTaskRelationUpstream0.setProjectCode(projectCode);
        processTaskRelationUpstream0.setPreTaskCode(123);
        processTaskRelationUpstream0.setProcessDefinitionCode(123);
        ProcessTaskRelation processTaskRelationUpstream1 = new ProcessTaskRelation();
        processTaskRelationUpstream1.setPostTaskCode(taskCode);
        processTaskRelationUpstream1.setPreTaskVersion(1);
        processTaskRelationUpstream1.setPreTaskCode(123);
        processTaskRelationUpstream1.setProcessDefinitionCode(124);
        processTaskRelationUpstream1.setProjectCode(projectCode);
        ProcessTaskRelation processTaskRelationUpstream2 = new ProcessTaskRelation();
        processTaskRelationUpstream2.setPostTaskCode(taskCode);
        processTaskRelationUpstream2.setPreTaskVersion(2);
        processTaskRelationUpstream2.setPreTaskCode(123);
        processTaskRelationUpstream2.setProcessDefinitionCode(125);
        processTaskRelationUpstream2.setProjectCode(projectCode);
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();
        processTaskRelationList.add(processTaskRelationUpstream0);
        processTaskRelationList.add(processTaskRelationUpstream1);
        processTaskRelationList.add(processTaskRelationUpstream2);
        return processTaskRelationList;
    }

    private List<ProcessTaskRelation> getProcessTaskDownstreamRelationList(long projectCode,long taskCode) {
        ProcessTaskRelation processTaskRelationDownstream0 = new ProcessTaskRelation();
        processTaskRelationDownstream0.setPreTaskCode(taskCode);
        processTaskRelationDownstream0.setPostTaskCode(456);
        processTaskRelationDownstream0.setPostTaskVersion(1);
        processTaskRelationDownstream0.setProjectCode(projectCode);
        ProcessTaskRelation processTaskRelationDownstream1 = new ProcessTaskRelation();
        processTaskRelationDownstream1.setPreTaskCode(taskCode);
        processTaskRelationDownstream1.setPostTaskCode(456);
        processTaskRelationDownstream1.setPostTaskVersion(1);
        processTaskRelationDownstream1.setProjectCode(projectCode);
        ProcessTaskRelation processTaskRelationDownstream2 = new ProcessTaskRelation();
        processTaskRelationDownstream2.setPreTaskCode(taskCode);
        processTaskRelationDownstream2.setPostTaskCode(4567);
        processTaskRelationDownstream2.setPostTaskVersion(1);
        processTaskRelationDownstream2.setProjectCode(projectCode);
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();
        processTaskRelationList.add(processTaskRelationDownstream0);
        processTaskRelationList.add(processTaskRelationDownstream1);
        processTaskRelationList.add(processTaskRelationDownstream2);
        return processTaskRelationList;
    }

    private ProcessDefinition getProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        processDefinition.setProjectCode(1L);
        processDefinition.setName("test_pdf");
        processDefinition.setTenantId(1);
        processDefinition.setDescription("");
        processDefinition.setCode(1L);
        processDefinition.setVersion(1);
        return processDefinition;
    }

    private TaskDefinition getTaskDefinition() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(1L);
        taskDefinition.setCode(1L);
        taskDefinition.setVersion(1);
        taskDefinition.setTaskType(TaskType.SHELL.getDesc());
        return taskDefinition;
    }

    @Test
    public void testCreateProcessTaskRelation() {
        long projectCode = 1L;
        long processDefinitionCode = 1L;
        long preTaskCode = 0L;
        long postTaskCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(getProcessDefinition());
        Mockito.when(processTaskRelationMapper.queryByCode(projectCode, processDefinitionCode, preTaskCode, postTaskCode)).thenReturn(Lists.newArrayList());
        Mockito.when(taskDefinitionMapper.queryByCode(postTaskCode)).thenReturn(getTaskDefinition());
        List<ProcessTaskRelationLog> processTaskRelationList = Lists.newArrayList();
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setProjectCode(projectCode);
        processTaskRelationLog.setProcessDefinitionCode(processDefinitionCode);
        processTaskRelationLog.setPreTaskCode(0L);
        processTaskRelationLog.setPreTaskVersion(0);
        processTaskRelationLog.setPostTaskCode(postTaskCode);
        processTaskRelationLog.setPostTaskVersion(1);
        processTaskRelationList.add(processTaskRelationLog);
        Mockito.when(processTaskRelationMapper.batchInsert(processTaskRelationList)).thenReturn(1);
        Mockito.when(processTaskRelationLogMapper.batchInsert(processTaskRelationList)).thenReturn(1);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testQueryDownstreamRelation() {
        long projectCode = 1L;
        long taskCode = 2L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);

        List<ProcessTaskRelation> processTaskRelationList = getProcessTaskDownstreamRelationList(projectCode,taskCode);

        Mockito.when(processTaskRelationMapper.queryDownstreamByCode(projectCode,taskCode))
                .thenReturn(processTaskRelationList);

        if (CollectionUtils.isNotEmpty(processTaskRelationList)) {
            Set<TaskDefinition> taskDefinitions = processTaskRelationList
                    .stream()
                    .map(processTaskRelation -> {
                        TaskDefinition taskDefinition = buildTaskDefinition(
                                processTaskRelation.getProjectCode(),
                                processTaskRelation.getPostTaskCode(),
                                processTaskRelation.getPostTaskVersion());
                        return taskDefinition;
                    })
                    .collect(Collectors.toSet());

            Set<TaskDefinitionLog> taskDefinitionLogSet = processTaskRelationList
                    .stream()
                    .map(processTaskRelation -> {
                        TaskDefinitionLog taskDefinitionLog = buildTaskDefinitionLog(
                                processTaskRelation.getProjectCode(),
                                processTaskRelation.getPostTaskCode(),
                                processTaskRelation.getPostTaskVersion()
                        );
                        return taskDefinitionLog;
                    })
                    .collect(Collectors.toSet());
            List<TaskDefinitionLog> taskDefinitionLogList = taskDefinitionLogSet.stream().collect(Collectors.toList());
            Mockito.when(taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitions))
                    .thenReturn(taskDefinitionLogList);
        }
        Map<String, Object> relation = processTaskRelationService
                .queryDownstreamRelation(loginUser, projectCode, taskCode);
        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
        Assert.assertEquals(2, ((List) relation.get("data")).size());
    }

    @Test
    public void testQueryUpstreamRelation() {
        long projectCode = 1L;
        long taskCode = 2L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        List<ProcessTaskRelation> processTaskRelationList = getProcessTaskUpstreamRelationList(projectCode,taskCode);

        Mockito.when(processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode)).thenReturn(processTaskRelationList);

        if (CollectionUtils.isNotEmpty(processTaskRelationList)) {
            Set<TaskDefinition> taskDefinitions = processTaskRelationList
                    .stream()
                    .map(processTaskRelation -> {
                        TaskDefinition taskDefinition = buildTaskDefinition(
                                processTaskRelation.getProjectCode(),
                                processTaskRelation.getPreTaskCode(),
                                processTaskRelation.getPreTaskVersion());
                        return taskDefinition;
                    })
                    .collect(Collectors.toSet());

            Set<TaskDefinitionLog> taskDefinitionLogSet = processTaskRelationList
                    .stream()
                    .map(processTaskRelation -> {
                        TaskDefinitionLog taskDefinitionLog = buildTaskDefinitionLog(
                                processTaskRelation.getProjectCode(),
                                processTaskRelation.getPreTaskCode(),
                                processTaskRelation.getPreTaskVersion());
                        return taskDefinitionLog;
                    })
                    .collect(Collectors.toSet());
            List<TaskDefinitionLog> taskDefinitionLogList = taskDefinitionLogSet.stream().collect(Collectors.toList());
            Mockito.when(taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitions))
                    .thenReturn(taskDefinitionLogList);
        }
        Map<String, Object> relation = processTaskRelationService
                .queryUpstreamRelation(loginUser, projectCode, taskCode);
        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
        Assert.assertEquals(2, ((List) relation.get("data")).size());
    }

    @Test
    public void testDeleteDownstreamRelation() {
        long projectCode = 1L;
        long taskCode = 2L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(projectCode);
        processTaskRelation.setProcessDefinitionCode(1L);
        processTaskRelation.setPreTaskCode(taskCode);
        processTaskRelation.setPostTaskCode(123L);
        processTaskRelationList.add(processTaskRelation);
        Mockito.when(processTaskRelationMapper.queryDownstreamByCode(projectCode, taskCode)).thenReturn(processTaskRelationList);
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog(processTaskRelation);
        Mockito.when(processTaskRelationMapper.deleteRelation(processTaskRelationLog)).thenReturn(1);
        Mockito.when(processTaskRelationLogMapper.deleteRelation(processTaskRelationLog)).thenReturn(1);
        ProcessDefinition processDefinition = getProcessDefinition();
        Mockito.when(processDefinitionMapper.queryByCode(1L)).thenReturn(processDefinition);
        Mockito.when(processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE)).thenReturn(1);
        Map<String, Object> result1 = processTaskRelationService.deleteDownstreamRelation(loginUser, projectCode, "123", taskCode);
        Assert.assertEquals(Status.SUCCESS, result1.get(Constants.STATUS));
    }

    @Test
    public void testDeleteUpstreamRelation() {
        long projectCode = 1L;
        long taskCode = 2L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(projectCode);
        processTaskRelation.setProcessDefinitionCode(1L);
        processTaskRelation.setPreTaskCode(0L);
        processTaskRelation.setPreTaskVersion(0);
        processTaskRelation.setPostTaskCode(taskCode);
        processTaskRelation.setPostTaskVersion(1);
        processTaskRelationList.add(processTaskRelation);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        Mockito.when(processTaskRelationMapper.queryUpstreamByCode(projectCode, taskCode)).thenReturn(processTaskRelationList);
        Mockito.when(processDefinitionMapper.queryByCode(1L)).thenReturn(getProcessDefinition());
        Mockito.when(processTaskRelationMapper.queryByProcessCode(projectCode, 1L)).thenReturn(processTaskRelationList);
        List<ProcessTaskRelationLog> relationLogs = processTaskRelationList.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());
        Mockito.when(processService.saveTaskRelation(loginUser, 1L, 1L,
            1, relationLogs, Lists.newArrayList(), Boolean.TRUE)).thenReturn(0);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testDeleteTaskProcessRelation() {
        long projectCode = 1L;
        long taskCode = 1L;
        long processDefinitionCode = 1L;
        long preTaskCode = 4L;
        long postTaskCode = 5L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        Mockito.when(processTaskRelationMapper.queryByCode(projectCode, processDefinitionCode, preTaskCode, postTaskCode)).thenReturn(Lists.newArrayList());
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(getProcessDefinition());
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(getTaskDefinition());
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskType(TaskType.CONDITIONS.getDesc());
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(taskDefinition);
        List<ProcessTaskRelation> processTaskRelationList = Lists.newArrayList();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(projectCode);
        processTaskRelation.setProcessDefinitionCode(1L);
        processTaskRelation.setPreTaskCode(0L);
        processTaskRelation.setPreTaskVersion(0);
        processTaskRelation.setPostTaskCode(taskCode);
        processTaskRelation.setPostTaskVersion(1);
        processTaskRelationList.add(processTaskRelation);
        Mockito.when(processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode)).thenReturn(processTaskRelationList);
        List<ProcessTaskRelationLog> relationLogs = processTaskRelationList.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());
        Mockito.when(processService.saveTaskRelation(loginUser, 1L, 1L,
            1, relationLogs, Lists.newArrayList(), Boolean.TRUE)).thenReturn(0);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testDeleteEdge() {
        long projectCode = 1L;
        long processDefinitionCode = 3L;
        long preTaskCode = 0L;
        long postTaskCode = 5L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(projectCode);
        processTaskRelation.setProcessDefinitionCode(processDefinitionCode);
        processTaskRelation.setProcessDefinitionVersion(1);
        processTaskRelation.setPreTaskCode(preTaskCode);
        processTaskRelation.setPostTaskCode(postTaskCode);
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog(processTaskRelation);
        processTaskRelationLog.setOperator(loginUser.getId());
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();
        processTaskRelationList.add(processTaskRelation);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        Mockito.when(processTaskRelationMapper.queryByProcessCode(projectCode, 1L)).thenReturn(processTaskRelationList);
        List<ProcessTaskRelationLog> relationLogs = processTaskRelationList.stream().map(ProcessTaskRelationLog::new).collect(Collectors.toList());
        Mockito.when(processService.saveTaskRelation(loginUser, 1L, 1L,
            1, relationLogs, Lists.newArrayList(), Boolean.TRUE)).thenReturn(0);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }
}
