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
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

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

    private List<ProcessTaskRelation> getProcessTaskRelationList(long taskCode) {
        ProcessTaskRelation processTaskRelationUpstream0 = new ProcessTaskRelation();
        processTaskRelationUpstream0.setPostTaskCode(taskCode);
        ProcessTaskRelation processTaskRelationUpstream1 = new ProcessTaskRelation();
        processTaskRelationUpstream1.setPostTaskCode(taskCode);
        ProcessTaskRelation processTaskRelationUpstream2 = new ProcessTaskRelation();
        processTaskRelationUpstream2.setPostTaskCode(taskCode);
        ProcessTaskRelation processTaskRelationDownstream0 = new ProcessTaskRelation();
        processTaskRelationUpstream0.setPreTaskCode(taskCode);
        ProcessTaskRelation processTaskRelationDownstream1 = new ProcessTaskRelation();
        processTaskRelationUpstream1.setPreTaskCode(taskCode);
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();
        processTaskRelationList.add(processTaskRelationUpstream0);
        processTaskRelationList.add(processTaskRelationUpstream1);
        processTaskRelationList.add(processTaskRelationUpstream2);
        processTaskRelationList.add(processTaskRelationDownstream0);
        processTaskRelationList.add(processTaskRelationDownstream1);
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

        TaskDefinition takeDefinition = new TaskDefinition();
        takeDefinition.setProjectCode(projectCode);
        takeDefinition.setCode(taskCode);
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode))
                .thenReturn(takeDefinition);

        List<ProcessTaskRelation> processTaskRelationList = getProcessTaskRelationList(taskCode);

        Mockito.when(processTaskRelationMapper.queryByTaskCode(taskCode))
                .thenReturn(processTaskRelationList);

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

        TaskDefinition takeDefinition = new TaskDefinition();
        takeDefinition.setProjectCode(projectCode);
        takeDefinition.setCode(taskCode);
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode))
                .thenReturn(takeDefinition);

        List<ProcessTaskRelation> processTaskRelationList = getProcessTaskRelationList(taskCode);

        Mockito.when(processTaskRelationMapper.queryByTaskCode(taskCode))
                .thenReturn(processTaskRelationList);

        Map<String, Object> relation = processTaskRelationService
                .queryUpstreamRelation(loginUser, projectCode, taskCode);
        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
        Assert.assertEquals(3, ((List) relation.get("data")).size());
    }
}
