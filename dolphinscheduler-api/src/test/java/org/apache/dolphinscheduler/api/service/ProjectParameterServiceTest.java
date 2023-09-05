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
import org.apache.dolphinscheduler.api.service.impl.ProjectParameterServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectParameter;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectParameterMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectParameterServiceTest {

    @InjectMocks
    private ProjectParameterServiceImpl projectParameterService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectParameterMapper projectParameterMapper;

    @Mock
    private ProjectServiceImpl projectService;

    protected final static long projectCode = 1L;

    @Test
    public void testCreateProjectParameter() {
        User loginUser = getGeneralUser();

        // PROJECT_PARAMETER_ALREADY_EXISTS
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectParameterMapper.selectOne(Mockito.any())).thenReturn(getProjectParameter());
        Mockito.when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(true);
        Result result = projectParameterService.createProjectParameter(loginUser, projectCode, "key", "value");
        Assertions.assertEquals(Status.PROJECT_PARAMETER_ALREADY_EXISTS.getCode(), result.getCode());

        // SUCCESS
        Mockito.when(projectParameterMapper.selectOne(Mockito.any())).thenReturn(null);
        Mockito.when(projectParameterMapper.insert(Mockito.any())).thenReturn(1);
        result = projectParameterService.createProjectParameter(loginUser, projectCode, "key1", "value");
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testUpdateProjectParameter() {
        User loginUser = getGeneralUser();

        // PROJECT_PARAMETER_NOT_EXISTS
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(true);
        Mockito.when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(null);
        Result result = projectParameterService.updateProjectParameter(loginUser, projectCode, 1, "key", "value");
        Assertions.assertEquals(Status.PROJECT_PARAMETER_NOT_EXISTS.getCode(), result.getCode());

        // PROJECT_PARAMETER_ALREADY_EXISTS
        Mockito.when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(getProjectParameter());
        Mockito.when(projectParameterMapper.selectOne(Mockito.any())).thenReturn(getProjectParameter());
        result = projectParameterService.updateProjectParameter(loginUser, projectCode, 1, "key", "value");
        Assertions.assertEquals(Status.PROJECT_PARAMETER_ALREADY_EXISTS.getCode(), result.getCode());

        // SUCCESS
        Mockito.when(projectParameterMapper.selectOne(Mockito.any())).thenReturn(null);
        Mockito.when(projectParameterMapper.updateById(Mockito.any())).thenReturn(1);
        result = projectParameterService.updateProjectParameter(loginUser, projectCode, 1, "key1", "value");
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testDeleteProjectParametersByCode() {
        User loginUser = getGeneralUser();

        // PROJECT_PARAMETER_NOT_EXISTS
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(true);
        Mockito.when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(null);
        Result result = projectParameterService.deleteProjectParametersByCode(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_PARAMETER_NOT_EXISTS.getCode(), result.getCode());

        // SUCCESS
        Mockito.when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(getProjectParameter());
        Mockito.when(projectParameterMapper.deleteById(Mockito.anyInt())).thenReturn(1);
        result = projectParameterService.deleteProjectParametersByCode(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testQueryProjectParameterByCode() {
        User loginUser = getGeneralUser();

        // PROJECT_PARAMETER_NOT_EXISTS
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class),
                Mockito.any())).thenReturn(true);
        Mockito.when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(null);
        Result result = projectParameterService.queryProjectParameterByCode(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_PARAMETER_NOT_EXISTS.getCode(), result.getCode());

        // SUCCESS
        Mockito.when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(getProjectParameter());
        result = projectParameterService.queryProjectParameterByCode(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    private User getGeneralUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("userName");
        loginUser.setId(1);
        return loginUser;
    }

    private Project getProject(long projectCode) {
        Project project = new Project();
        project.setCode(projectCode);
        project.setId(1);
        project.setName("test");
        project.setUserId(1);
        return project;
    }

    private ProjectParameter getProjectParameter() {
        ProjectParameter projectParameter = new ProjectParameter();
        projectParameter.setId(1);
        projectParameter.setCode(1);
        projectParameter.setProjectCode(1);
        projectParameter.setParamName("key");
        projectParameter.setParamValue("value");
        return projectParameter;
    }
}
