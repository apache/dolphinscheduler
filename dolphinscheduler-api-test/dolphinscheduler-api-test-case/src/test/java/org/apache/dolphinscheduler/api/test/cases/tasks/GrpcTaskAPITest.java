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

package org.apache.dolphinscheduler.api.test.cases.tasks;

import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.LoginResponseData;
import org.apache.dolphinscheduler.api.test.pages.LoginPage;
import org.apache.dolphinscheduler.api.test.pages.project.ProjectPage;
import org.apache.dolphinscheduler.api.test.pages.workflow.ExecutorPage;
import org.apache.dolphinscheduler.api.test.pages.workflow.WorkflowDefinitionPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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

@DolphinScheduler(composeFiles = "docker/task-grpc/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class GrpcTaskAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static User loginUser;

    private static ExecutorPage executorPage;

    private static WorkflowDefinitionPage workflowDefinitionPage;

    private static ProjectPage projectPage;

    private static long projectCode;

    private static long workflowDefinitionCode;

    private static List<Integer> workflowInstanceIds;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId =
                JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        executorPage = new ExecutorPage(sessionId);
        workflowDefinitionPage = new WorkflowDefinitionPage(sessionId);
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
    public void testGrpcFailedWorkflowInstance() {
        try {
            String projectName = "project-test-" + UUID.randomUUID().toString().replace("-", "");
            String workflowDefinitionName = "test-failed-" + UUID.randomUUID().toString().replace("-", "");
            // create test project
            HttpResponse createProjectResponse = projectPage.createProject(loginUser, projectName);
            Assertions.assertTrue(createProjectResponse.getBody().getSuccess());
            projectCode = resolveProjectCode(projectName);

            // upload test workflow definition json
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("workflow-json/task-grpc/grpcFailedWorkflow.json").getFile());
            HttpResponse createWorkflowDefinitionResponse = workflowDefinitionPage
                    .createWorkflowDefinition(loginUser, projectCode, file, workflowDefinitionName);
            Assertions.assertTrue(createWorkflowDefinitionResponse.getBody().getSuccess());

            // get workflow definition code
            HttpResponse queryAllWorkflowDefinitionByProjectCodeResponse =
                    workflowDefinitionPage.queryAllWorkflowDefinitionByProjectCode(loginUser, projectCode);
            Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getSuccess());
            Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getData().toString()
                    .contains("test name"));
            workflowDefinitionCode = resolveWorkflowDefinitionCode(projectCode, workflowDefinitionName);

            // release test workflow
            HttpResponse releaseWorkflowDefinitionResponse = workflowDefinitionPage.releaseWorkflowDefinition(loginUser,
                    projectCode, workflowDefinitionCode, ReleaseState.ONLINE);
            Assertions.assertTrue(releaseWorkflowDefinitionResponse.getBody().getSuccess());

            // trigger workflow instance
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String scheduleTime = String.format("%s,%s", formatter.format(date), formatter.format(date));
            log.info("use current time {} as scheduleTime", scheduleTime);
            HttpResponse startWorkflowInstanceResponse = executorPage.startWorkflowInstance(loginUser, projectCode,
                    workflowDefinitionCode, scheduleTime, FailureStrategy.END, WarningType.NONE);
            Assertions.assertTrue(startWorkflowInstanceResponse.getBody().getSuccess());

            workflowInstanceIds = (List<Integer>) startWorkflowInstanceResponse.getBody().getData();
        } catch (Exception e) {
            log.error("failed", e);
            Assertions.fail();
        }
    }

    @Test
    @Order(10)
    public void testGrpcSuccessWorkflowInstance() {
        try {
            String projectName = "project-test-" + UUID.randomUUID().toString().replace("-", "");
            String workflowDefinitionName = "test-success-" + UUID.randomUUID().toString().replace("-", "");
            // create test project
            HttpResponse createProjectResponse = projectPage.createProject(loginUser, projectName);
            Assertions.assertTrue(createProjectResponse.getBody().getSuccess());
            projectCode = resolveProjectCode(projectName);

            // upload test workflow definition json
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("workflow-json/task-grpc/grpcSuccessWorkflow.json").getFile());
            HttpResponse createWorkflowDefinitionResponse = workflowDefinitionPage
                    .createWorkflowDefinition(loginUser, projectCode, file, workflowDefinitionName);
            Assertions.assertTrue(createWorkflowDefinitionResponse.getBody().getSuccess());

            // get workflow definition code
            HttpResponse queryAllWorkflowDefinitionByProjectCodeResponse =
                    workflowDefinitionPage.queryAllWorkflowDefinitionByProjectCode(loginUser, projectCode);
            Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getSuccess());
            Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getData().toString()
                    .contains("test name"));
            workflowDefinitionCode = resolveWorkflowDefinitionCode(projectCode, workflowDefinitionName);

            // release test workflow
            HttpResponse releaseWorkflowDefinitionResponse = workflowDefinitionPage.releaseWorkflowDefinition(loginUser,
                    projectCode, workflowDefinitionCode, ReleaseState.ONLINE);
            Assertions.assertTrue(releaseWorkflowDefinitionResponse.getBody().getSuccess());

            // trigger workflow instance
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String scheduleTime = String.format("%s,%s", formatter.format(date), formatter.format(date));
            log.info("use current time {} as scheduleTime", scheduleTime);
            HttpResponse startWorkflowInstanceResponse = executorPage.startWorkflowInstance(loginUser, projectCode,
                    workflowDefinitionCode, scheduleTime, FailureStrategy.END, WarningType.NONE);
            Assertions.assertTrue(startWorkflowInstanceResponse.getBody().getSuccess());

            workflowInstanceIds = (List<Integer>) startWorkflowInstanceResponse.getBody().getData();
        } catch (Exception e) {
            log.error("failed", e);
            Assertions.fail();
        }
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
