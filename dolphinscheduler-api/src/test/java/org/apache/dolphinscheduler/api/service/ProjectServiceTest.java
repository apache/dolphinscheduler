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

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class ProjectServiceTest {


    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceTest.class);

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProjectUserMapper projectUserMapper;



    private String projectName = "ProjectServiceTest";

    private String userName = "ProjectServiceTest";

    @Before
    public void setUp() {
        remove();
        removeUser();
    }


    @After
    public void after(){
        remove();
        removeUser();
    }

    @Test
    public void testCreateProject(){

        User loginUser  = getLoginUser();
        Map<String, Object> result = projectService.createProject(loginUser, projectName, projectName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

        //project name exist
        result = projectService.createProject(loginUser, projectName, projectName);
        logger.info(result.toString());
        Assert.assertEquals(Status.PROJECT_ALREADY_EXISTS,result.get(Constants.STATUS));

    }
    @Test
    public void testQueryById(){

        // id not exist
        Map<String, Object> result = projectService.queryById(Integer.MAX_VALUE);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT,result.get(Constants.STATUS));
        User user = getLoginUser();
        //add
        add(user);
        //get
        Project project = getProject();
        result = projectService.queryById(project.getId());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }
    @Test
    public void testCheckProjectAndAuth(){

        User loginUser = getLoginUser();
        Project project = null;
        // project null
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser,project,projectName);
        logger.info(result.toString());
        Status status = (Status)result.get(Constants.STATUS);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(),status.getCode());
        //add
        add(loginUser);
        project =getProject();
        result = projectService.checkProjectAndAuth(loginUser,project,projectName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }
    @Test
    public void testHasProjectAndPerm(){

        User loginUser = getLoginUser();
        add(loginUser);
        Project project = getProject();
        Map<String, Object> result = new HashMap<>();
        // not exist user
        User tempUser = new User();
        tempUser.setId(Integer.MAX_VALUE);
        boolean checkResult = projectService.hasProjectAndPerm(tempUser,project,result);
        logger.info(result.toString());
        Assert.assertFalse(checkResult);
        checkResult = projectService.hasProjectAndPerm(loginUser,project,result);
        Assert.assertTrue(checkResult);
    }
    @Test
    public void testQueryProjectListPaging(){
        User loginUser = getLoginUser();
        add(loginUser);
        Map<String, Object> result  =  projectService.queryProjectListPaging(loginUser,10,1,projectName);
        logger.info(result.toString());
        PageInfo<Project> pageInfo = (PageInfo<Project>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(pageInfo.getTotalCount()>0);
    }
    @Test
    public void testDeleteProject(){
        User loginUser = getLoginUser();
        add(loginUser);
        Project project = getProject();
        Map<String, Object> result= projectService.deleteProject(loginUser,project.getId());
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }

    @Test
    public void testUpdate(){

        User loginUser = getLoginUser();
        add(loginUser);
        Project project = getProject();
        // update desc
        Map<String, Object> result = projectService.update(loginUser,project.getId(),projectName,"desc");
        logger.info(result.toString());
        //check update desc field
        project = getProject();
        Assert.assertEquals("desc",project.getDescription());
    }
    @Test
    public void testQueryAuthorizedProject(){
        //admin
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        // creater
        User createUser = getLoginUser();
        add(createUser);
        // not exist user
        Map<String, Object> result = projectService.queryAuthorizedProject(loginUser,Integer.MAX_VALUE);
        List<Project> projects = (List<Project>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isEmpty(projects));
        //add projectuser
        addProjectUser(getProject().getId(),createUser.getId());
        result = projectService.queryAuthorizedProject(loginUser,createUser.getId());
        logger.info(result.toString());
        projects = (List<Project>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(projects));
        //remove projectUser
        removeProjectUser(getProject().getId(),createUser.getId());
    }
    @Test
    public void testQueryAllProjectList(){

        User loginUser = getLoginUser();
        add(loginUser);

        Map<String, Object> result = projectService.queryAllProjectList();
        logger.info(result.toString());
        List<Project> projects  = (List<Project>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isEmpty(projects));

    }
    @Test
    public void testQueryUnauthorizedProject(){
        //admin
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        // creater
        User createUser = getLoginUser();
        add(createUser);

        Map<String, Object> result = projectService.queryUnauthorizedProject(loginUser,Integer.MAX_VALUE);
        logger.info(result.toString());
        List<Project> projects = (List<Project>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(projects));
    }


    /**
     * add project
     * @param user
     */
    private void add(User user){

        projectService.createProject(user, projectName, projectName);
    }

    private Project getProject(){

        return  projectMapper.queryByName(projectName);
    }


    /**
     * create admin user
     * @return
     */
    private User getLoginUser(){

        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName(userName);
        userMapper.insert(loginUser);

      return  userMapper.queryByUserNameAccurately(userName);

    }

    /**
     * remove users
     */
    private void removeUser(){
        Map<String,Object> map = new HashMap<>(1);
        map.put("user_name",userName);
        userMapper.deleteByMap(map);
    }

    /**
     * remove project
     */
    private void remove(){
        Map<String,Object> map = new HashMap<>(1);
        map.put("name",projectName);
        projectMapper.deleteByMap(map);
    }

    /**
     * add project user
     * @param projectId
     * @param userId
     */
    private void addProjectUser(int projectId , int userId){
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectId(projectId);
        projectUser.setUserId(userId);
        projectUserMapper.insert(projectUser);
    }

    /**
     * remove project user
     * @param projectId
     * @param userId
     */
    private void removeProjectUser(int projectId , int userId){
        projectUserMapper.deleteProjectRelation(projectId,userId);
    }



}
