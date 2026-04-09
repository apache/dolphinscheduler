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
import org.apache.dolphinscheduler.api.test.pages.workflow.WorkflowInstancePage;
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

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;

@DolphinScheduler(composeFiles = "docker/task-emr-serverless/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class EmrServerlessTaskAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static User loginUser;

    private static ExecutorPage executorPage;

    private static WorkflowDefinitionPage workflowDefinitionPage;

    private static WorkflowInstancePage workflowInstancePage;

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
        workflowInstancePage = new WorkflowInstancePage(sessionId);
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
    public void testEmrServerlessSuccessWorkflowInstance() throws Exception {
        String workflowDefinitionName = "test_emr_serverless_success_" + System.currentTimeMillis();
        // create test project
        projectPage.createProject(loginUser, "project-test-emr-serverless");
        HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
        Assertions.assertTrue(queryAllProjectListResponse.getBody().getSuccess());
        projectCode = (long) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllProjectListResponse
                .getBody().getData()).get(0)).get("code");

        // upload test workflow definition json
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader
                .getResource("workflow-json/task-emr-serverless/emrServerlessSuccessWorkflow.json").getFile());
        HttpResponse createWorkflowDefinitionResponse = workflowDefinitionPage
                .createWorkflowDefinition(loginUser, projectCode, file, workflowDefinitionName);
        Assertions.assertTrue(createWorkflowDefinitionResponse.getBody().getSuccess());

        // get workflow definition code
        HttpResponse queryAllWorkflowDefinitionByProjectCodeResponse =
                workflowDefinitionPage.queryAllWorkflowDefinitionByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getData().toString()
                .contains("test name"));
        workflowDefinitionCode =
                (long) ((LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllWorkflowDefinitionByProjectCodeResponse
                        .getBody().getData()).get(0)).get("workflowDefinition")).get("code");

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
        Assertions.assertFalse(workflowInstanceIds.isEmpty(), "No workflow instances were created");

        // Wait for workflow instance to complete (up to 120 seconds, polling every 2 seconds)
        int workflowInstanceId = workflowInstanceIds.get(0);
        log.info("Waiting for EMR Serverless success workflow instance: {}", workflowInstanceId);
        boolean completed = false;
        for (int i = 0; i < 60; i++) {
            Thread.sleep(2000);
            HttpResponse queryResponse = workflowInstancePage.queryWorkflowInstanceById(
                    loginUser, projectCode, workflowInstanceId);
            LinkedHashMap<String, Object> instanceData =
                    (LinkedHashMap<String, Object>) queryResponse.getBody().getData();
            String state = (String) instanceData.get("state");
            log.info("EMR Serverless success workflow instance state: {}", state);
            if ("SUCCESS".equals(state)) {
                completed = true;
                break;
            } else if ("FAILURE".equals(state) || "STOP".equals(state)) {
                Assertions.fail("EMR Serverless workflow instance expected SUCCESS but got: " + state);
            }
        }
        Assertions.assertTrue(completed, "EMR Serverless workflow instance did not complete within 120 seconds");
    }

    @Test
    @Order(2)
    public void testEmrServerlessFailedWorkflowInstance() throws Exception {
        String workflowDefinitionName = "test_emr_serverless_failed_" + System.currentTimeMillis();

        // upload failed workflow definition json
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader
                .getResource("workflow-json/task-emr-serverless/emrServerlessFailedWorkflow.json").getFile());
        HttpResponse createWorkflowDefinitionResponse = workflowDefinitionPage
                .createWorkflowDefinition(loginUser, projectCode, file, workflowDefinitionName);
        Assertions.assertTrue(createWorkflowDefinitionResponse.getBody().getSuccess());

        // get workflow definition code
        HttpResponse queryAllWorkflowDefinitionByProjectCodeResponse =
                workflowDefinitionPage.queryAllWorkflowDefinitionByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(queryAllWorkflowDefinitionByProjectCodeResponse.getBody().getSuccess());
        long failedWorkflowDefinitionCode =
                (long) ((LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllWorkflowDefinitionByProjectCodeResponse
                        .getBody().getData()).get(0)).get("workflowDefinition")).get("code");

        // release
        HttpResponse releaseWorkflowDefinitionResponse = workflowDefinitionPage.releaseWorkflowDefinition(
                loginUser, projectCode, failedWorkflowDefinitionCode, ReleaseState.ONLINE);
        Assertions.assertTrue(releaseWorkflowDefinitionResponse.getBody().getSuccess());

        // trigger
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String scheduleTime = String.format("%s,%s", formatter.format(date), formatter.format(date));
        HttpResponse startWorkflowInstanceResponse = executorPage.startWorkflowInstance(loginUser, projectCode,
                failedWorkflowDefinitionCode, scheduleTime, FailureStrategy.END, WarningType.NONE);
        Assertions.assertTrue(startWorkflowInstanceResponse.getBody().getSuccess());

        List<Integer> failedWorkflowInstanceIds =
                (List<Integer>) startWorkflowInstanceResponse.getBody().getData();
        Assertions.assertFalse(failedWorkflowInstanceIds.isEmpty(), "No workflow instances were created");

        // Wait for workflow instance to complete (up to 120 seconds, polling every 2 seconds)
        int failedWorkflowInstanceId = failedWorkflowInstanceIds.get(0);
        log.info("Waiting for EMR Serverless failed workflow instance: {}", failedWorkflowInstanceId);
        boolean completed = false;
        for (int i = 0; i < 60; i++) {
            Thread.sleep(2000);
            HttpResponse queryResponse = workflowInstancePage.queryWorkflowInstanceById(
                    loginUser, projectCode, failedWorkflowInstanceId);
            LinkedHashMap<String, Object> instanceData =
                    (LinkedHashMap<String, Object>) queryResponse.getBody().getData();
            String state = (String) instanceData.get("state");
            log.info("EMR Serverless failed workflow instance state: {}", state);
            if ("FAILURE".equals(state) || "STOP".equals(state)) {
                completed = true;
                break;
            } else if ("SUCCESS".equals(state)) {
                Assertions.fail("EMR Serverless workflow instance expected FAILURE but got SUCCESS");
            }
        }
        Assertions.assertTrue(completed, "EMR Serverless workflow instance did not complete within 120 seconds");
    }
}
