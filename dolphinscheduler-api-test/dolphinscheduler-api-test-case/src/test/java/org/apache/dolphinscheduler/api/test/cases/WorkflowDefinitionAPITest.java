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
import org.apache.dolphinscheduler.api.test.pages.workflow.WorkflowDefinitionPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.File;
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
public class WorkflowDefinitionAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static User loginUser;

    private static WorkflowDefinitionPage workflowDefinitionPage;

    private static ProjectPage projectPage;

    private static long projectCode;

    private static long workflowDefinitionCode;

    private static final String projectName = "project-test-" + UUID.randomUUID().toString().replace("-", "");

    private static final String workflowDefinitionName = "test-" + UUID.randomUUID().toString().replace("-", "");

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId =
                JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        workflowDefinitionPage = new WorkflowDefinitionPage(sessionId);
        projectPage = new ProjectPage(sessionId);
        loginUser = new User();
        loginUser.setId(123);
        loginUser.setUserType(UserType.GENERAL_USER);
    }

    @AfterAll
    public static void cleanup() {
        log.info("success cleanup");
    }

    @Test
    @Order(1)
    public void testCreateWorkflowDefinition() {
        try {
            HttpResponse createProjectResponse = projectPage.createProject(loginUser, projectName);
            Assertions.assertTrue(createProjectResponse.getBody().getSuccess());
            projectCode = resolveProjectCode(projectName);
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("workflow-json/test.json").getFile());
            HttpResponse createWorkflowDefinitionResponse = workflowDefinitionPage
                    .createWorkflowDefinition(loginUser, projectCode, file, workflowDefinitionName);
            Boolean successFlag = createWorkflowDefinitionResponse.getBody().getSuccess();
            Assertions.assertTrue(successFlag);
        } catch (Exception e) {
            log.error("failed", e);
            Assertions.fail();
        }
    }

    @Test
    @Order(2)
    public void testQueryAllWorkflowDefinitionByProjectCode() {
        HttpResponse queryAllWorkflowDefinitionByProjectCodeResponse =
                workflowDefinitionPage.queryAllWorkflowDefinitionByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(
                queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getData().toString().contains("hello world"));
        workflowDefinitionCode = resolveWorkflowDefinitionCode(projectCode, workflowDefinitionName);
    }

    @Test
    @Order(3)
    public void testQueryWorkflowDefinitionByCode() {
        HttpResponse queryWorkflowDefinitionByCodeResponse =
                workflowDefinitionPage.queryWorkflowDefinitionByCode(loginUser, projectCode, workflowDefinitionCode);
        Assertions.assertTrue(queryWorkflowDefinitionByCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(
                queryWorkflowDefinitionByCodeResponse.getBody().getData().toString().contains("hello world"));
    }

    @Test
    @Order(4)
    public void testGetWorkflowListByProjectCode() {
        HttpResponse getWorkflowListByProjectCodeResponse =
                workflowDefinitionPage.getWorkflowListByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(getWorkflowListByProjectCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(getWorkflowListByProjectCodeResponse.getBody().getData().toString()
                .contains(workflowDefinitionName));
    }

    @Test
    @Order(5)
    public void testQueryWorkflowDefinitionByName() {
        HttpResponse queryWorkflowDefinitionByNameResponse =
                workflowDefinitionPage.queryWorkflowDefinitionByName(loginUser, projectCode, workflowDefinitionName);
        Assertions.assertTrue(queryWorkflowDefinitionByNameResponse.getBody().getSuccess());
        Assertions.assertTrue(
                queryWorkflowDefinitionByNameResponse.getBody().getData().toString().contains(workflowDefinitionName));
    }

    @Test
    @Order(6)
    public void testQueryWorkflowDefinitionList() {
        HttpResponse queryWorkflowDefinitionListResponse =
                workflowDefinitionPage.queryWorkflowDefinitionList(loginUser, projectCode);
        Assertions.assertTrue(queryWorkflowDefinitionListResponse.getBody().getSuccess());
        Assertions.assertTrue(queryWorkflowDefinitionListResponse.getBody().getData().toString()
                .contains(workflowDefinitionName));
    }

    @Test
    @Order(7)
    public void testReleaseWorkflowDefinition() {
        HttpResponse releaseWorkflowDefinitionResponse = workflowDefinitionPage.releaseWorkflowDefinition(loginUser,
                projectCode, workflowDefinitionCode, ReleaseState.ONLINE);
        Assertions.assertTrue(releaseWorkflowDefinitionResponse.getBody().getSuccess());

        HttpResponse queryWorkflowDefinitionByCodeResponse =
                workflowDefinitionPage.queryWorkflowDefinitionByCode(loginUser, projectCode, workflowDefinitionCode);
        Assertions.assertTrue(queryWorkflowDefinitionByCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(
                queryWorkflowDefinitionByCodeResponse.getBody().getData().toString().contains("releaseState=ONLINE"));
    }

    @Test
    @Order(8)
    public void testDeleteWorkflowDefinitionByCode() {
        HttpResponse deleteWorkflowDefinitionByCodeResponse =
                workflowDefinitionPage.deleteWorkflowDefinitionByCode(loginUser, projectCode, workflowDefinitionCode);
        Assertions.assertFalse(deleteWorkflowDefinitionByCodeResponse.getBody().getSuccess());

        HttpResponse releaseWorkflowDefinitionResponse = workflowDefinitionPage.releaseWorkflowDefinition(loginUser,
                projectCode, workflowDefinitionCode, ReleaseState.OFFLINE);
        Assertions.assertTrue(releaseWorkflowDefinitionResponse.getBody().getSuccess());

        deleteWorkflowDefinitionByCodeResponse =
                workflowDefinitionPage.deleteWorkflowDefinitionByCode(loginUser, projectCode, workflowDefinitionCode);
        Assertions.assertTrue(deleteWorkflowDefinitionByCodeResponse.getBody().getSuccess());

        HttpResponse queryWorkflowDefinitionListResponse =
                workflowDefinitionPage.queryWorkflowDefinitionList(loginUser, projectCode);
        Assertions.assertTrue(queryWorkflowDefinitionListResponse.getBody().getSuccess());
        Assertions.assertFalse(queryWorkflowDefinitionListResponse.getBody().getData().toString()
                .contains(workflowDefinitionName));
    }

    private static long resolveProjectCode(String expectedProjectName) {
        HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
        Assertions.assertTrue(queryAllProjectListResponse.getBody().getSuccess());

        List<LinkedHashMap<String, Object>> projects =
                (List<LinkedHashMap<String, Object>>) queryAllProjectListResponse.getBody().getData();
        return projects.stream()
                .filter(it -> expectedProjectName.equals(it.get("name")))
                .findFirst()
                .map(it -> ((Number) it.get("code")).longValue())
                .orElseThrow(() -> new AssertionError("Cannot find project: " + expectedProjectName));
    }

    private static long resolveWorkflowDefinitionCode(long projectCode, String expectedWorkflowDefinitionName) {
        HttpResponse queryAllWorkflowDefinitionByProjectCodeResponse =
                workflowDefinitionPage.queryAllWorkflowDefinitionByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getSuccess());

        List<LinkedHashMap<String, Object>> workflows =
                (List<LinkedHashMap<String, Object>>) queryAllWorkflowDefinitionByProjectCodeResponse.getBody()
                        .getData();
        return workflows.stream()
                .map(it -> (LinkedHashMap<String, Object>) it.get("workflowDefinition"))
                .filter(it -> expectedWorkflowDefinitionName.equals(it.get("name")))
                .findFirst()
                .map(it -> ((Number) it.get("code")).longValue())
                .orElseThrow(
                        () -> new AssertionError("Cannot find workflow definition: " + expectedWorkflowDefinitionName));
    }
}
