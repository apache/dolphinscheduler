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
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Tag;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TagMapper;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RunWith(MockitoJUnitRunner.class)
public class TagServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(TagServiceTest.class);

    @InjectMocks
    TagService tagService;

    @Mock
    TagMapper tagMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProcessDefinitionServiceImpl processDefinitionService;

    @Mock
    private ProcessDefinitionMapper processDefineMapper;

    private String userName = "TagServiceTest";

    private String tagName = "TagServiceTest";

    private String projectName = "TagServiceTest";

    @Test
    public void createTag() {

        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = tagService.createTag(loginUser, "project_test1", "tag_test");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //success
        Mockito.when(tagMapper.selectById(46)).thenReturn(getTag());
        Map<String, Object> deleteSuccess = tagService.createTag(loginUser, "project_test1", tagName);
        Assert.assertEquals(Status.SUCCESS, deleteSuccess.get(Constants.STATUS));

    }

    @Test
    public void deleteTag() {

        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = tagService.deleteTag(loginUser, "project_test1", 6);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, tag not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processDefineMapper.selectById(1)).thenReturn(null);
        Map<String, Object> instanceNotexitRes = tagService.deleteTag(loginUser,
                "project_test1", 1);
        Assert.assertEquals(Status.TAG_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        Tag tag = getTag();
        //user no auth
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(tagMapper.selectById(46)).thenReturn(tag);
        Map<String, Object> userNoAuthRes = tagService.deleteTag(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, userNoAuthRes.get(Constants.STATUS));
        //success
        Mockito.when(tagMapper.deleteById(46)).thenReturn(1);
        Map<String, Object> deleteSuccess = tagService.deleteTag(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.SUCCESS, deleteSuccess.get(Constants.STATUS));
    }

    @Test
    public void updateTag() {

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        String projectName = "project_test1";
        Project project = getProject(projectName);

        Tag tag = getTag();

        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);

        Map<String, Object> updateResult = tagService.updateTag(loginUser,projectName,tag.getId(),"tag_test");
        Assert.assertEquals(Status.SUCCESS, updateResult.get(Constants.STATUS));
    }

    @Test
    public void queryTagListPaging() {

        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = tagService.queryTagListPaging(loginUser, "project_test1", 1, 5, "");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectName);
        loginUser.setId(1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Page<Tag> page = new Page<>(1, 10);
        page.setTotal(30);
        Mockito.when(tagMapper.queryTagListPaging(
                Mockito.any(IPage.class)
                , Mockito.eq(loginUser.getId())
                , Mockito.eq(project.getId())
                , Mockito.eq(""))).thenReturn(page);

        Map<String, Object> map1 = tagService.queryTagListPaging(loginUser, projectName, 1, 10, "");

        Assert.assertEquals(Status.SUCCESS, map1.get(Constants.STATUS));
    }

    @Test
    public void queryById() {
        //not exist
        Map<String, Object> result = tagService.queryById(Integer.MAX_VALUE);
        Assert.assertEquals(Status.TAG_NOT_FOUND, result.get(Constants.STATUS));
        logger.info(result.toString());

        //success
        Mockito.when(tagMapper.selectById(46)).thenReturn(getTag());
        result = tagService.queryById(46);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    /**
     * create admin user
     * @return
     */
    private User getLoginUser() {

        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName(userName);
        loginUser.setId(1);
        return  loginUser;

    }

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    private Tag getTag() {
        Tag tag = new Tag();
        tag.setId(46);
        tag.setName(tagName);
        tag.setUserId(1);
        tag.setProjectId(1);
        return tag;
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