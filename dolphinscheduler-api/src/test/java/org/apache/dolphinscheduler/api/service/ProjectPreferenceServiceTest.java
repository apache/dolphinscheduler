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
import org.apache.dolphinscheduler.api.service.impl.ProjectPreferenceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectPreference;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectPreferenceMapper;

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
public class ProjectPreferenceServiceTest {

    @InjectMocks
    private ProjectPreferenceServiceImpl projectPreferenceService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectPreferenceMapper projectPreferenceMapper;

    @Mock
    private ProjectServiceImpl projectService;

    protected final static long projectCode = 1L;

    @Test
    public void testUpdateProjectPreference() {
        User loginUser = getGeneralUser();

        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(true);

        Mockito.when(projectPreferenceMapper.selectOne(Mockito.any())).thenReturn(null);
        Mockito.when(projectPreferenceMapper.insert(Mockito.any())).thenReturn(1);

        Result result = projectPreferenceService.updateProjectPreference(loginUser, projectCode, "value");
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testQueryProjectPreferenceByProjectCode() {
        User loginUser = getGeneralUser();

        // PROJECT_PARAMETER_NOT_EXISTS
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class),
                Mockito.any())).thenReturn(true);

        Mockito.when(projectPreferenceMapper.selectOne(Mockito.any())).thenReturn(null);
        Result result = projectPreferenceService.queryProjectPreferenceByProjectCode(loginUser, projectCode);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());

        // SUCCESS
        Mockito.when(projectPreferenceMapper.selectOne(Mockito.any())).thenReturn(getProjectPreference());
        result = projectPreferenceService.queryProjectPreferenceByProjectCode(loginUser, projectCode);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testEnableProjectPreference() {
        User loginUser = getGeneralUser();

        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(true);

        Mockito.when(projectPreferenceMapper.selectOne(Mockito.any())).thenReturn(getProjectPreference());
        Result result = projectPreferenceService.enableProjectPreference(loginUser, projectCode, 1);
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

    private ProjectPreference getProjectPreference() {
        ProjectPreference projectPreference = new ProjectPreference();
        projectPreference.setId(1);
        projectPreference.setCode(1);
        projectPreference.setProjectCode(projectCode);
        projectPreference.setPreferences("value");
        projectPreference.setState(1);
        return projectPreference;
    }
}
