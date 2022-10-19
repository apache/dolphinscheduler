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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class ProjectMapperTest extends BaseDaoTest {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    /**
     * insert
     *
     * @return Project
     */
    private Project insertOne() {
        //insertOne
        Project project = new Project();
        project.setName("ut project");
        project.setUserId(111);
        project.setCode(1L);
        project.setCreateTime(new Date());
        project.setUpdateTime(new Date());
        projectMapper.insert(project);
        return project;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        //insertOne
        Project project = insertOne();
        project.setCreateTime(new Date());
        //update
        int update = projectMapper.updateById(project);
        Assertions.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        Project projectMap = insertOne();
        int delete = projectMapper.deleteById(projectMap.getId());
        Assertions.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Project project = insertOne();
        //query
        List<Project> projects = projectMapper.selectList(null);
        Assertions.assertNotEquals(projects.size(), 0);
    }

    /**
     * test query detail by id
     */
    @Test
    public void testQueryDetailById() {

        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);

        Project project = insertOne();
        project.setUserId(user.getId());
        projectMapper.updateById(project);
        Project project1 = projectMapper.queryDetailById(project.getId());

        Assertions.assertNotEquals(project1, null);
        Assertions.assertEquals(project1.getUserName(), user.getUserName());
    }

    /**
     * test query project by name
     */
    @Test
    public void testQueryProjectByName() {
        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);

        Project project = insertOne();
        project.setUserId(user.getId());
        projectMapper.updateById(project);
        Project project1 = projectMapper.queryByName(project.getName());

        Assertions.assertNotEquals(project1, null);
    }

    /**
     * test page
     */
    @Test
    public void testQueryProjectListPaging() {
        Project project = insertOne();

        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);
        project.setUserId(user.getId());
        projectMapper.updateById(project);

        Page<Project> page = new Page(1, 3);
        IPage<Project> projectIPage = projectMapper.queryProjectListPaging(
            page,
            null,
            null
        );
        IPage<Project> projectIPage1 = projectMapper.queryProjectListPaging(
            page,
            null,
            project.getName()
        );
        Assertions.assertEquals(projectIPage.getTotal(), 1);
        Assertions.assertEquals(projectIPage1.getTotal(), 1);
    }

    /**
     * test query project create user
     */
    @Test
    public void testQueryProjectCreatedByUser() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectCreatedByUser(project.getUserId());

        Assertions.assertNotEquals(projects.size(), 0);

    }

    /**
     * test query authed project list by userId
     */
    @Test
    public void testQueryAuthedProjectListByUserId() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectCreatedByUser(project.getUserId());

        Assertions.assertNotEquals(projects.size(), 0);
    }

    /**
     * test query project expect userId
     */
    @Test
    public void testQueryProjectExceptUserId() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectExceptUserId(
            100000
        );

        Assertions.assertNotEquals(projects.size(), 0);
    }

    @Test
    public void testQueryAllProject() {
        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);

        Project project = insertOne();
        project.setUserId(user.getId());
        projectMapper.updateById(project);

        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectId(project.getId());
        projectUser.setUserId(user.getId());
        projectUser.setCreateTime(new Date());
        projectUser.setUpdateTime(new Date());
        projectUserMapper.insert(projectUser);

        List<Project> allProject = projectMapper.queryAllProject(user.getId());

        Assertions.assertNotEquals(allProject.size(), 0);
    }

    /**
     * test query project permission
     */
    @Test
    public void testListAuthorizedProjects() {
        Project project = insertOne();
        List<Project> projects = projectMapper.listAuthorizedProjects(1, Collections.singletonList(project.getId()));
        Assertions.assertEquals(projects.size(), 0);
    }

}