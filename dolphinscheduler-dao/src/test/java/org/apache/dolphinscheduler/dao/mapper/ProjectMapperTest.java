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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.ProjectUserPermission;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.ProjectUserPermissionDao;

import java.util.Arrays;
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

    @Autowired
    private ProjectUserPermissionDao userPermissionDao;

    /**
     * insert
     *
     * @return Project
     */
    private Project insertOne() {
        // insertOne
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
        // insertOne
        Project project = insertOne();
        project.setCreateTime(new Date());
        // update
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
        // query
        List<Project> projects = projectMapper.selectList(null);
        Assertions.assertNotEquals(0, projects.size());
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

        Assertions.assertNotEquals(null, project1);
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

        Assertions.assertNotEquals(null, project1);
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
                null);
        IPage<Project> projectIPage1 = projectMapper.queryProjectListPaging(
                page,
                null,
                project.getName());
        Assertions.assertEquals(1, projectIPage.getTotal());
        Assertions.assertEquals(1, projectIPage1.getTotal());
    }

    @Test
    public void testVisibleProjectsAreFilteredBeforePagination() {
        Project owned = insertVisibleProject("visible-owned", 101);
        insertNewPermission(owned, 101, ProjectPermission.VIEW);
        insertNewPermission(owned, 101, ProjectPermission.DEVELOP);
        Project readable = insertVisibleProject("visible-readable", 102);
        insertNewPermission(readable, 101, ProjectPermission.VIEW);
        Project executable = insertVisibleProject("visible-executable", 102);
        insertNewPermission(executable, 101, ProjectPermission.EXECUTE);
        Project revoked = insertVisibleProject("visible-revoked", 102);
        insertProjectPermission(revoked, 101, 0);
        Project otherUser = insertVisibleProject("visible-other-user", 102);
        insertNewPermission(otherUser, 103, ProjectPermission.VIEW);

        IPage<Project> first = projectMapper.queryVisibleProjectListPaging(new Page<>(1, 1), 101, false, "visible-");
        Assertions.assertEquals(2, first.getTotal());
        Assertions.assertEquals(1, first.getRecords().size());
        Assertions.assertEquals(readable.getId(), first.getRecords().get(0).getId());
        Assertions.assertEquals(Constants.READ_PERMISSION, first.getRecords().get(0).getPerm());

        IPage<Project> second = projectMapper.queryVisibleProjectListPaging(new Page<>(2, 1), 101, false, "visible-");
        Assertions.assertEquals(2, second.getTotal());
        Assertions.assertEquals(owned.getId(), second.getRecords().get(0).getId());
        Assertions.assertEquals(Constants.READ_PERMISSION, second.getRecords().get(0).getPerm());

        List<Project> visibleProjects = projectMapper.queryVisibleProjects(101, false);
        Assertions.assertEquals(2, visibleProjects.size());
        Assertions.assertEquals(readable.getId(), visibleProjects.get(0).getId());
        Assertions.assertEquals(Constants.READ_PERMISSION, visibleProjects.get(0).getPerm());
        Assertions.assertEquals(owned.getId(), visibleProjects.get(1).getId());
        Assertions.assertEquals(Arrays.asList(readable.getCode(), owned.getCode()),
                projectMapper.queryVisibleProjectCodes(101, false));

        IPage<Project> beyondLast =
                projectMapper.queryVisibleProjectListPaging(new Page<>(3, 1), 101, false, "visible-");
        Assertions.assertEquals(2, beyondLast.getTotal());
        Assertions.assertTrue(beyondLast.getRecords().isEmpty());
    }

    @Test
    public void testVisibleProjectSearchDoesNotExposeUnauthorizedProjects() {
        Project readable = insertVisibleProject("search-readable", 102);
        insertNewPermission(readable, 101, ProjectPermission.VIEW);
        insertVisibleProject("search-hidden", 102);

        IPage<Project> hidden = projectMapper.queryVisibleProjectListPaging(new Page<>(1, 10), 101, false, "hidden");
        Assertions.assertEquals(0, hidden.getTotal());
        Assertions.assertTrue(hidden.getRecords().isEmpty());
        IPage<Project> absentUser = projectMapper.queryVisibleProjectListPaging(new Page<>(1, 10), 104, false, null);
        Assertions.assertEquals(0, absentUser.getTotal());
        Assertions.assertTrue(absentUser.getRecords().isEmpty());
    }

    @Test
    public void testAdminCanQueryAllProjectsWithoutMembership() {
        insertVisibleProject("admin-visible-a", 102);
        insertVisibleProject("admin-visible-b", 103);

        IPage<Project> page = projectMapper.queryVisibleProjectListPaging(new Page<>(1, 1), 101, true, "admin-visible");
        Assertions.assertEquals(2, page.getTotal());
        Assertions.assertEquals(1, page.getRecords().size());
        Assertions.assertEquals(Constants.ALL_PERMISSIONS, page.getRecords().get(0).getPerm());
    }

    @Test
    public void testProjectRevocationRemovesVisibility() {
        Project project = insertVisibleProject("revoked-project", 102);
        insertProjectPermission(project, 101, Constants.ALL_PERMISSIONS);
        insertNewPermission(project, 101, ProjectPermission.VIEW);
        Assertions.assertEquals(1,
                projectMapper.queryVisibleProjectListPaging(new Page<>(1, 10), 101, false, null).getTotal());

        userPermissionDao.revokeAllPermissions(project.getCode(), 101);
        IPage<Project> page = projectMapper.queryVisibleProjectListPaging(new Page<>(1, 10), 101, false, null);
        Assertions.assertEquals(0, page.getTotal());
        Assertions.assertTrue(page.getRecords().isEmpty());
        Assertions.assertTrue(projectMapper.queryVisibleProjects(101, false).isEmpty());
        Assertions.assertTrue(projectMapper.queryVisibleProjectCodes(101, false).isEmpty());
    }

    private Project insertVisibleProject(String name, int ownerId) {
        Project project = new Project();
        project.setName(name);
        project.setCode(name.hashCode());
        project.setUserId(ownerId);
        project.setCreateTime(new Date());
        project.setUpdateTime(new Date());
        projectMapper.insert(project);
        return project;
    }

    @Test
    public void testLegacyGrantsAndOwnershipDoNotRestoreVisibility() {
        Project project = insertVisibleProject("legacy-owner", 101);
        insertProjectPermission(project, 101, Constants.ALL_PERMISSIONS);
        Assertions.assertTrue(projectMapper.queryVisibleProjects(101, false).isEmpty());
        Assertions.assertTrue(projectMapper.queryVisibleProjectCodes(101, false).isEmpty());
        Assertions.assertEquals(0,
                projectMapper.queryVisibleProjectListPaging(new Page<>(1, 10), 101, false, null).getTotal());
    }

    private void insertNewPermission(Project project, int userId, ProjectPermission permission) {
        ProjectUserPermission grant = new ProjectUserPermission();
        grant.setProjectCode(project.getCode());
        grant.setUserId(userId);
        grant.setPermission(permission);
        userPermissionDao.insert(grant);
    }

    private void insertProjectPermission(Project project, int userId, int permission) {
        ProjectUser relation = new ProjectUser();
        relation.setProjectId(project.getId());
        relation.setUserId(userId);
        relation.setPerm(permission);
        projectUserMapper.insert(relation);
    }

    /**
     * test query project create user
     */
    @Test
    public void testQueryProjectCreatedByUser() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectCreatedByUser(project.getUserId());

        Assertions.assertNotEquals(0, projects.size());

    }

    /**
     * test query authed project list by userId
     */
    @Test
    public void testQueryAuthedProjectListByUserId() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectCreatedByUser(project.getUserId());

        Assertions.assertNotEquals(0, projects.size());
    }

    /**
     * test query project expect userId
     */
    @Test
    public void testQueryProjectExceptUserId() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectExceptUserId(
                100000);

        Assertions.assertNotEquals(0, projects.size());
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

        Assertions.assertNotEquals(0, allProject.size());
    }

    private Project insertProject(long projectCode, String projectName, int userId) {
        Project project = new Project();
        project.setName(projectName);
        project.setUserId(userId);
        project.setCode(projectCode);
        project.setCreateTime(new Date());
        project.setUpdateTime(new Date());
        projectMapper.insert(project);
        return project;
    }

    @Test
    public void testListAuthorizedProjects() {
        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);

        // project1: created by user
        insertProject(1, "project 1", user.getId());

        // project2: not created by user
        insertProject(2, "project 2", user.getId() + 1);

        // project3: not created by user but is authorized to user
        Project project3 = insertProject(3, "project 3", user.getId() + 1);
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectId(project3.getId());
        projectUser.setUserId(user.getId());
        projectUser.setCreateTime(new Date());
        projectUser.setUpdateTime(new Date());
        projectUserMapper.insert(projectUser);

        List<Project> projects = projectMapper.listAuthorizedProjects(user.getId(), null);

        Assertions.assertEquals(2, projects.size());
    }

    @Test
    public void testMemberManagementScopeAndPermissionLevelsBeforePagination() {
        Project readable = insertVisibleProject("manageable-reader", 101);
        insertNewPermission(readable, 101, ProjectPermission.MANAGE_MEMBERS);
        insertNewPermission(readable, 102, ProjectPermission.VIEW);
        Project hidden = insertVisibleProject("unmanageable-owner", 101);
        insertProjectPermission(hidden, 101, Constants.ALL_PERMISSIONS);
        Project writable = insertVisibleProject("manageable-writer", 103);
        insertNewPermission(writable, 101, ProjectPermission.MANAGE_MEMBERS);
        for (ProjectPermission permission : ProjectPermission.values()) {
            if (permission == ProjectPermission.MANAGE_MEMBERS) {
                continue;
            }
            insertNewPermission(writable, 102, permission);
        }

        IPage<Project> first = projectMapper.queryProjectAuthorizationLevelsPaging(new Page<>(1, 1),
                101, false, 102, null);
        Assertions.assertEquals(2, first.getTotal());
        Assertions.assertEquals(writable.getCode(), first.getRecords().get(0).getCode());
        Assertions.assertEquals(Constants.ALL_PERMISSIONS, first.getRecords().get(0).getPerm());
        IPage<Project> second = projectMapper.queryProjectAuthorizationLevelsPaging(new Page<>(2, 1),
                101, false, 102, null);
        Assertions.assertEquals(2, second.getTotal());
        Assertions.assertEquals(Constants.READ_PERMISSION, second.getRecords().get(0).getPerm());
        Assertions.assertEquals(0, projectMapper.queryProjectAuthorizationLevelsPaging(new Page<>(1, 10),
                101, false, 102, "unmanageable").getTotal());
        Assertions.assertEquals(3, projectMapper.queryProjectAuthorizationLevels(101, true, 102).size());
        Assertions.assertTrue(projectMapper.queryProjectAuthorizationLevels(102, false, 101).isEmpty());
    }

    @Test
    public void testProjectMemberListIgnoresLegacyRelationsAndDeduplicatesPermissions() {
        Project project = insertVisibleProject("new-members", 100);
        User member = new User();
        member.setUserName("new-member");
        userMapper.insert(member);
        User legacyMember = new User();
        legacyMember.setUserName("legacy-member");
        userMapper.insert(legacyMember);
        insertProjectPermission(project, legacyMember.getId(), Constants.ALL_PERMISSIONS);
        insertNewPermission(project, member.getId(), ProjectPermission.VIEW);
        insertNewPermission(project, member.getId(), ProjectPermission.DEVELOP);
        List<User> members = userMapper.queryAuthedUserListByProjectId(project.getId());
        Assertions.assertEquals(1, members.size());
        Assertions.assertEquals(member.getId(), members.get(0).getId());
        userPermissionDao.revokeAllPermissions(project.getCode(), member.getId());
        Assertions.assertTrue(userMapper.queryAuthedUserListByProjectId(project.getId()).isEmpty());
    }

}
