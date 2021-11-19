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
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;

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
        ProcessTaskRelation processTaskRelationUpstream1 = new ProcessTaskRelation();
        processTaskRelationUpstream1.setPostTaskCode(taskCode);
        processTaskRelationUpstream1.setPreTaskVersion(1);
        processTaskRelationUpstream1.setPreTaskCode(123);
        processTaskRelationUpstream1.setProjectCode(projectCode);
        ProcessTaskRelation processTaskRelationUpstream2 = new ProcessTaskRelation();
        processTaskRelationUpstream2.setPostTaskCode(taskCode);
        processTaskRelationUpstream2.setPreTaskVersion(2);
        processTaskRelationUpstream1.setPreTaskCode(123);
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
}
