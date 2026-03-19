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

package org.apache.dolphinscheduler.api.test.cases;

import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.LoginResponseData;
import org.apache.dolphinscheduler.api.test.pages.LoginPage;
import org.apache.dolphinscheduler.api.test.pages.project.ProjectPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
// TODO: Add more detailed permission control related cases after userPage test cases completed
public class ProjectAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String projectName = "project-test-" + UUID.randomUUID().toString().replace("-", "");

    private static final String renamedProjectName = projectName + "-renamed";

    private static String sessionId;

    private static User loginUser;

    private static ProjectPage projectPage;

    private static Long projectCode;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId =
                JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        projectPage = new ProjectPage(sessionId);
        loginUser = new User();
        loginUser.setUserName("admin");
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
    }

    @AfterAll
    public static void cleanup() {
        log.info("success cleanup");
    }

    @Test
    @Order(1)
    public void testQueryAllProjectList() {
        HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
        Assertions.assertTrue(queryAllProjectListResponse.getBody().getSuccess());
        List<Project> projects = (List<Project>) queryAllProjectListResponse.getBody().getData();
        Assertions.assertTrue(projects.stream().noneMatch(it -> projectName.equals(it.getName())));
        Assertions.assertTrue(projects.stream().noneMatch(it -> renamedProjectName.equals(it.getName())));
    }

    @Test
    @Order(2)
    public void testCreateProject() {
        HttpResponse createProjectResponse = projectPage.createProject(loginUser, projectName);
        Assertions.assertTrue(createProjectResponse.getBody().getSuccess());

        projectCode = resolveProjectCode(projectName);
    }

    @Test
    @Order(3)
    public void testUpdateProject() {
        HttpResponse updateProjectResponse =
                projectPage.updateProject(loginUser, resolveProjectCode(projectName), renamedProjectName,
                        loginUser.getUserName());
        Assertions.assertTrue(updateProjectResponse.getBody().getSuccess());

        HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
        String projectList = queryAllProjectListResponse.getBody().getData().toString();
        Assertions.assertFalse(projectList.contains(projectName));
        Assertions.assertTrue(projectList.contains(renamedProjectName));
    }

    @Test
    @Order(4)
    public void testQueryProjectByCode() {
        Long code = resolveProjectCode(renamedProjectName);

        HttpResponse queryProjectByCodeResponse = projectPage.queryProjectByCode(loginUser, code);
        Assertions.assertTrue(queryProjectByCodeResponse.getBody().getSuccess());

        LinkedHashMap<String, Object> project = (LinkedHashMap) queryProjectByCodeResponse.getBody().getData();
        String projectNameActual = (String) project.get("name");
        Assertions.assertEquals(renamedProjectName, projectNameActual);
    }

    @Test
    @Order(5)
    public void testQueryProjectListPaging() {
        HttpResponse queryProjectListPagingResponse = projectPage.queryProjectListPaging(loginUser, 100, 1);
        Assertions.assertTrue(queryProjectListPagingResponse.getBody().getSuccess());
        Assertions.assertTrue(queryProjectListPagingResponse.getBody().getData().toString().contains(renamedProjectName));
    }

    @Test
    @Order(6)
    public void testQueryProjectWithAuthorizedLevelListPaging() {
        HttpResponse queryProjectWithAuthorizedLevelListPagingResponse =
                projectPage.queryProjectWithAuthorizedLevelListPaging(loginUser, loginUser.getId(), 100, 1);
        Assertions.assertTrue(queryProjectWithAuthorizedLevelListPagingResponse.getBody().getSuccess());
        Assertions.assertTrue(queryProjectWithAuthorizedLevelListPagingResponse.getBody().getData().toString()
                .contains(renamedProjectName));
    }

    @Test
    @Order(7)
    public void testQueryUnauthorizedProject() {
        HttpResponse queryUnauthorizedProjectResponse =
                projectPage.queryUnauthorizedProject(loginUser, loginUser.getId());
        Assertions.assertTrue(queryUnauthorizedProjectResponse.getBody().getSuccess());
        // The test project was created by instead of authorized to this user, therefore, it should be in the
        // unauthorized list.
        Assertions.assertTrue(queryUnauthorizedProjectResponse.getBody().getData().toString().contains(renamedProjectName));
    }

    @Test
    @Order(8)
    public void testQueryAuthorizedProject() {
        HttpResponse queryAuthorizedProjectResponse = projectPage.queryAuthorizedProject(loginUser, loginUser.getId());
        Assertions.assertTrue(queryAuthorizedProjectResponse.getBody().getSuccess());
        // The test project was created by instead of authorized to this user, therefore, it should not be in the
        // authorized list.
        Assertions.assertFalse(queryAuthorizedProjectResponse.getBody().getData().toString().contains(renamedProjectName));
    }

    @Test
    @Order(9)
    public void testQueryProjectWithAuthorizedLevel() {
        HttpResponse queryProjectWithAuthorizedLevelResponse =
                projectPage.queryProjectWithAuthorizedLevel(loginUser, loginUser.getId());
        Assertions.assertTrue(queryProjectWithAuthorizedLevelResponse.getBody().getSuccess());
        // queryProjectWithAuthorizedLevel api returns a joint-set of projects both created by and authorized to the
        // user
        Assertions.assertTrue(
                queryProjectWithAuthorizedLevelResponse.getBody().getData().toString().contains(renamedProjectName));
    }

    @Test
    @Order(10)
    public void testQueryAuthorizedUser() {
        HttpResponse queryAuthorizedUserResponse =
                projectPage.queryAuthorizedUser(loginUser, resolveProjectCode(renamedProjectName));
        List<LinkedHashMap> users = (List<LinkedHashMap>) queryAuthorizedUserResponse.getBody().getData();
        Assertions.assertTrue(queryAuthorizedUserResponse.getBody().getSuccess());
        // admin has not authorized this project to any other users, therefore, the authorized user list should be empty
        Assertions.assertEquals(users.size(), 0);
    }

    @Test
    @Order(11)
    public void testQueryProjectCreatedAndAuthorizedByUser() {
        HttpResponse queryProjectCreatedAndAuthorizedByUserResponse =
                projectPage.queryProjectCreatedAndAuthorizedByUser(loginUser);
        Assertions.assertTrue(queryProjectCreatedAndAuthorizedByUserResponse.getBody().getSuccess());
        // queryProjectCreatedAndAuthorizedByUser api returns a joint-set of projects both created by and authorized to
        // the user
        Assertions.assertTrue(
                queryProjectCreatedAndAuthorizedByUserResponse.getBody().getData().toString().contains(renamedProjectName));
    }

    @Test
    @Order(12)
    public void testQueryAllProjectListForDependent() {
        HttpResponse queryAllProjectListForDependentResponse = projectPage.queryAllProjectListForDependent(loginUser);
        Assertions.assertTrue(queryAllProjectListForDependentResponse.getBody().getSuccess());
        Assertions.assertTrue(
                queryAllProjectListForDependentResponse.getBody().getData().toString().contains(renamedProjectName));
    }

    @Test
    @Order(13)
    public void testDeleteProject() {
        HttpResponse deleteProjectResponse = projectPage.deleteProject(loginUser, resolveProjectCode(renamedProjectName));
        Assertions.assertTrue(deleteProjectResponse.getBody().getSuccess());

        HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
        Assertions.assertFalse(queryAllProjectListResponse.getBody().getData().toString().contains(renamedProjectName));
    }

    private static Long resolveProjectCode(String expectedProjectName) {
        if (projectCode != null && renamedProjectName.equals(expectedProjectName)) {
            return projectCode;
        }

        HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
        Assertions.assertTrue(queryAllProjectListResponse.getBody().getSuccess());

        List<LinkedHashMap> projects = (List<LinkedHashMap>) queryAllProjectListResponse.getBody().getData();
        Long resolvedCode = projects.stream()
                .filter(it -> expectedProjectName.equals(it.get("name")))
                .findFirst()
                .map(it -> ((Number) it.get("code")).longValue())
                .orElseThrow(() -> new AssertionError("Cannot find project: " + expectedProjectName));

        if (renamedProjectName.equals(expectedProjectName)) {
            projectCode = resolvedCode;
        }
        return resolvedCode;
    }
}
