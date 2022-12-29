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

package org.apache.dolphinscheduler.api.controller;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import java.text.MessageFormat;

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

/**
 * project controller test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectControllerTest {

    protected User user;

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectMapper projectMapper;

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test
    public void testUpdateProject() {
        Result result = new Result();
        putMsg(result, Status.SUCCESS);

        long projectCode = 1L;
        String projectName = "test";
        String desc = "";
        String userName = "jack";
        Mockito.when(projectService.update(user, projectCode, projectName, desc, userName)).thenReturn(result);
        Result response = projectController.updateProject(user, projectCode, projectName, desc, userName);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryProjectByCode() {
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject());
        Mockito.when(projectService.queryByCode(user, projectCode)).thenReturn(result);
        Result response = projectController.queryProjectByCode(user, projectCode);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryProjectListPaging() {
        int pageNo = 1;
        int pageSize = 10;
        String searchVal = "";

        Result result = Result.success(new PageInfo<Resource>(1, 10));

        Mockito.when(projectService.queryProjectListPaging(user, pageSize, pageNo, searchVal)).thenReturn(result);
        Result response = projectController.queryProjectListPaging(user, searchVal, pageSize, pageNo);

        Assertions.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryUnauthorizedProject() {
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        Mockito.when(projectService.queryUnauthorizedProject(user, 2)).thenReturn(result);
        Result response = projectController.queryUnauthorizedProject(user, 2);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryAuthorizedProject() {
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        Mockito.when(projectService.queryAuthorizedProject(user, 2)).thenReturn(result);
        Result response = projectController.queryAuthorizedProject(user, 2);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryAuthorizedUser() {
        Result result = new Result();
        this.putMsg(result, Status.SUCCESS);

        Mockito.when(this.projectService.queryAuthorizedUser(this.user, 3682329499136L)).thenReturn(result);
        Result response = this.projectController.queryAuthorizedUser(this.user, 3682329499136L);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryAllProjectList() {
        User user = new User();
        user.setId(0);
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        Mockito.when(projectService.queryAllProjectList(user)).thenReturn(result);
        Result response = projectController.queryAllProjectList(user);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }
    @Test
    public void testQueryAllProjectListForDependent() {
        User user = new User();
        user.setId(0);
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        Mockito.when(projectService.queryAllProjectListForDependent()).thenReturn(result);
        Result response = projectController.queryAllProjectListForDependent(user);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }
    private Project getProject() {
        Project project = new Project();
        project.setCode(1L);
        project.setId(1);
        project.setName("test");
        project.setUserId(1);
        return project;
    }

    private void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }
}
