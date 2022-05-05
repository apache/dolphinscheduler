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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * project controller test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProjectControllerTest {

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectMapper projectMapper;

    protected User user;

    @Before
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test
    public void testUpdateProject() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put("projectId", 1);

        long projectCode = 1L;
        String projectName = "test";
        String desc = "";
        String userName = "jack";
        Mockito.when(projectService.update(user, projectCode, projectName, desc, userName)).thenReturn(result);
        Result response = projectController.updateProject(user, projectCode, projectName, desc, userName);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryProjectByCode() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject());
        Mockito.when(projectService.queryByCode(user, projectCode)).thenReturn(result);
        Result response = projectController.queryProjectByCode(user, projectCode);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryProjectListPaging() {
        int pageNo = 1;
        int pageSize = 10;
        String searchVal = "";

        Result result = Result.success(new PageInfo<Resource>(1, 10));

        Mockito.when(projectService.queryProjectListPaging(user, pageSize, pageNo, searchVal)).thenReturn(result);
        Result response = projectController.queryProjectListPaging(user, searchVal, pageSize, pageNo);

        Assert.assertTrue(response != null && response.isSuccess());
    }

    @Test
    public void testQueryUnauthorizedProject() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        Mockito.when(projectService.queryUnauthorizedProject(user, 2)).thenReturn(result);
        Result response = projectController.queryUnauthorizedProject(user, 2);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryAuthorizedProject() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        Mockito.when(projectService.queryAuthorizedProject(user, 2)).thenReturn(result);
        Result response = projectController.queryAuthorizedProject(user, 2);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryAuthorizedUser() {
        Map<String, Object> result = new HashMap<>();
        this.putMsg(result, Status.SUCCESS);

        Mockito.when(this.projectService.queryAuthorizedUser(this.user, 3682329499136L)).thenReturn(result);
        Result response = this.projectController.queryAuthorizedUser(this.user, 3682329499136L);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryAllProjectList() {
        User user = new User();
        user.setId(0);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        Mockito.when(projectService.queryAllProjectList(user)).thenReturn(result);
        Result response = projectController.queryAllProjectList(user);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    private Project getProject() {
        Project project = new Project();
        project.setCode(1L);
        project.setId(1);
        project.setName("test");
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
}
