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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class DependentTaskAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String projectName = "dependent-test-project-" + System.currentTimeMillis();

    private static String sessionId;

    private static User loginUser;

    private static ExecutorPage executorPage;

    private static WorkflowDefinitionPage workflowDefinitionPage;

    private static WorkflowInstancePage workflowInstancePage;

    private static ProjectPage projectPage;

    private static long projectCode;

    private static long upstreamWorkflowDefinitionCode;

    private static long dependentWorkflowDefinitionCode;

    private static long failedDependentWorkflowDefinitionCode;

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
    public void testCreateUpstreamWorkflow() throws Exception {
        // create test project
        HttpResponse createProjectResponse = projectPage.createProject(loginUser, projectName);
        Assertions.assertTrue(createProjectResponse.getBody().getSuccess());

        HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
        Assertions.assertTrue(queryAllProjectListResponse.getBody().getSuccess());

        // find the project by name
        List<LinkedHashMap> projectList = (List<LinkedHashMap>) queryAllProjectListResponse.getBody().getData();
        for (LinkedHashMap<String, Object> project : projectList) {
            if (projectName.equals(project.get("name"))) {
                projectCode = ((Number) project.get("code")).longValue();
                break;
            }
        }
        Assertions.assertNotEquals(0, projectCode, "project should be found by name");
        log.info("project code: {}", projectCode);

        // create upstream workflow definition (shell task: echo hello)
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("workflow-json/test.json").getFile());
        String upstreamWorkflowName = "upstream_shell_workflow_" + System.currentTimeMillis();
        HttpResponse createWorkflowResponse = workflowDefinitionPage
                .createWorkflowDefinition(loginUser, projectCode, file, upstreamWorkflowName);
        Assertions.assertTrue(createWorkflowResponse.getBody().getSuccess());

        // get upstream workflow definition code
        HttpResponse queryAllWorkflowResponse =
                workflowDefinitionPage.queryAllWorkflowDefinitionByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(queryAllWorkflowResponse.getBody().getSuccess());
        upstreamWorkflowDefinitionCode =
                (long) ((LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllWorkflowResponse
                        .getBody().getData()).get(0)).get("workflowDefinition")).get("code");
        log.info("upstream workflow definition code: {}", upstreamWorkflowDefinitionCode);

        // release upstream workflow
        HttpResponse releaseResponse = workflowDefinitionPage.releaseWorkflowDefinition(loginUser,
                projectCode, upstreamWorkflowDefinitionCode, ReleaseState.ONLINE);
        Assertions.assertTrue(releaseResponse.getBody().getSuccess());

        // trigger upstream workflow instance
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String scheduleTime = String.format("%s,%s", formatter.format(date), formatter.format(date));
        log.info("use current time {} as scheduleTime", scheduleTime);
        HttpResponse startWorkflowResponse = executorPage.startWorkflowInstance(loginUser, projectCode,
                upstreamWorkflowDefinitionCode, scheduleTime, FailureStrategy.END, WarningType.NONE);
        Assertions.assertTrue(startWorkflowResponse.getBody().getSuccess());

        List<Integer> workflowInstanceIds = (List<Integer>) startWorkflowResponse.getBody().getData();
        Assertions.assertFalse(workflowInstanceIds.isEmpty());
        log.info("upstream workflow instance ids: {}", workflowInstanceIds);

        // wait for upstream workflow to complete
        int workflowInstanceId = workflowInstanceIds.get(0);
        boolean completed = false;
        for (int i = 0; i < 60; i++) {
            Thread.sleep(2000);
            HttpResponse queryResponse = workflowInstancePage.queryWorkflowInstanceById(loginUser,
                    projectCode, workflowInstanceId);
            if (queryResponse.getBody().getSuccess() && queryResponse.getBody().getData() != null) {
                LinkedHashMap<String, Object> instanceData =
                        (LinkedHashMap<String, Object>) queryResponse.getBody().getData();
                String state = (String) instanceData.get("state");
                log.info("upstream workflow instance state: {}", state);
                if ("SUCCESS".equals(state)) {
                    completed = true;
                    break;
                } else if ("FAILURE".equals(state) || "STOP".equals(state)) {
                    Assertions.fail("upstream workflow instance failed with state: " + state);
                }
            }
        }
        Assertions.assertTrue(completed, "upstream workflow instance should complete within 120 seconds");
    }

    @Test
    @Order(10)
    public void testDependentSuccessWorkflowInstance() throws Exception {
        // read dependent success workflow json template and replace placeholders
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(
                classLoader.getResource("workflow-json/task-dependent/dependentSuccessWorkflow.json").getFile());
        String jsonContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        jsonContent = jsonContent
                .replace("${projectCode}", String.valueOf(projectCode))
                .replace("${definitionCode}", String.valueOf(upstreamWorkflowDefinitionCode));

        String dependentWorkflowName = "dependent_success_" + System.currentTimeMillis();
        HttpResponse createResponse = workflowDefinitionPage
                .createWorkflowDefinition(loginUser, projectCode, jsonContent, dependentWorkflowName);
        Assertions.assertTrue(createResponse.getBody().getSuccess());

        // get dependent workflow definition code from create response
        LinkedHashMap<String, Object> createData =
                (LinkedHashMap<String, Object>) createResponse.getBody().getData();
        dependentWorkflowDefinitionCode = ((Number) createData.get("code")).longValue();
        log.info("dependent workflow definition code: {}", dependentWorkflowDefinitionCode);

        // release dependent workflow
        HttpResponse releaseResponse = workflowDefinitionPage.releaseWorkflowDefinition(loginUser,
                projectCode, dependentWorkflowDefinitionCode, ReleaseState.ONLINE);
        Assertions.assertTrue(releaseResponse.getBody().getSuccess());

        // trigger dependent workflow instance
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String scheduleTime = String.format("%s,%s", formatter.format(date), formatter.format(date));
        log.info("use current time {} as scheduleTime", scheduleTime);
        HttpResponse startWorkflowResponse = executorPage.startWorkflowInstance(loginUser, projectCode,
                dependentWorkflowDefinitionCode, scheduleTime, FailureStrategy.END, WarningType.NONE);
        Assertions.assertTrue(startWorkflowResponse.getBody().getSuccess());

        List<Integer> workflowInstanceIds = (List<Integer>) startWorkflowResponse.getBody().getData();
        Assertions.assertFalse(workflowInstanceIds.isEmpty());
        log.info("dependent workflow instance ids: {}", workflowInstanceIds);

        // wait for dependent workflow to complete
        int workflowInstanceId = workflowInstanceIds.get(0);
        boolean completed = false;
        for (int i = 0; i < 60; i++) {
            Thread.sleep(2000);
            HttpResponse queryResponse = workflowInstancePage.queryWorkflowInstanceById(loginUser,
                    projectCode, workflowInstanceId);
            if (queryResponse.getBody().getSuccess() && queryResponse.getBody().getData() != null) {
                LinkedHashMap<String, Object> instanceData =
                        (LinkedHashMap<String, Object>) queryResponse.getBody().getData();
                String state = (String) instanceData.get("state");
                log.info("dependent success workflow instance state: {}", state);
                if ("SUCCESS".equals(state)) {
                    completed = true;
                    break;
                } else if ("FAILURE".equals(state) || "STOP".equals(state)) {
                    Assertions.fail("dependent workflow instance failed with state: " + state);
                }
            }
        }
        Assertions.assertTrue(completed, "dependent workflow instance should complete within 120 seconds");

        // query task instances and verify DEPENDENT task type
        HttpResponse queryTaskListResponse = workflowInstancePage.queryTaskListByWorkflowInstanceId(loginUser,
                projectCode, workflowInstanceId);
        Assertions.assertTrue(queryTaskListResponse.getBody().getSuccess());

        List<LinkedHashMap<String, Object>> taskList;
        Object taskData = queryTaskListResponse.getBody().getData();
        if (taskData instanceof List) {
            taskList = (List<LinkedHashMap<String, Object>>) taskData;
        } else {
            LinkedHashMap<String, Object> pageData = (LinkedHashMap<String, Object>) taskData;
            taskList = (List<LinkedHashMap<String, Object>>) pageData.get("taskList");
        }
        Assertions.assertNotNull(taskList);
        Assertions.assertFalse(taskList.isEmpty());

        LinkedHashMap<String, Object> dependentTask = taskList.get(0);
        Assertions.assertEquals("DEPENDENT", dependentTask.get("taskType"));
        Assertions.assertEquals("SUCCESS", dependentTask.get("state"));
        log.info("dependent task instance verified: taskType={}, state={}",
                dependentTask.get("taskType"), dependentTask.get("state"));
    }

    @Test
    @Order(20)
    public void testDependentFailedWorkflowInstance() throws Exception {
        // read dependent failed workflow json template and replace placeholders
        // this workflow depends on a non-existent definition code, so the dependent task should fail
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(
                classLoader.getResource("workflow-json/task-dependent/dependentFailedWorkflow.json").getFile());
        String jsonContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        jsonContent = jsonContent.replace("${projectCode}", String.valueOf(projectCode));

        String dependentWorkflowName = "dependent_failed_" + System.currentTimeMillis();
        HttpResponse createResponse = workflowDefinitionPage
                .createWorkflowDefinition(loginUser, projectCode, jsonContent, dependentWorkflowName);
        Assertions.assertTrue(createResponse.getBody().getSuccess());

        // get failed dependent workflow definition code from create response
        LinkedHashMap<String, Object> createData =
                (LinkedHashMap<String, Object>) createResponse.getBody().getData();
        failedDependentWorkflowDefinitionCode = ((Number) createData.get("code")).longValue();
        log.info("failed dependent workflow definition code: {}", failedDependentWorkflowDefinitionCode);

        // release failed dependent workflow
        HttpResponse releaseResponse = workflowDefinitionPage.releaseWorkflowDefinition(loginUser,
                projectCode, failedDependentWorkflowDefinitionCode, ReleaseState.ONLINE);
        Assertions.assertTrue(releaseResponse.getBody().getSuccess());

        // trigger failed dependent workflow instance
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String scheduleTime = String.format("%s,%s", formatter.format(date), formatter.format(date));
        log.info("use current time {} as scheduleTime", scheduleTime);
        HttpResponse startWorkflowResponse = executorPage.startWorkflowInstance(loginUser, projectCode,
                failedDependentWorkflowDefinitionCode, scheduleTime, FailureStrategy.END, WarningType.NONE);
        Assertions.assertTrue(startWorkflowResponse.getBody().getSuccess());

        List<Integer> workflowInstanceIds = (List<Integer>) startWorkflowResponse.getBody().getData();
        Assertions.assertFalse(workflowInstanceIds.isEmpty());
        log.info("failed dependent workflow instance ids: {}", workflowInstanceIds);

        // wait for dependent workflow to fail
        int workflowInstanceId = workflowInstanceIds.get(0);
        boolean failed = false;
        for (int i = 0; i < 60; i++) {
            Thread.sleep(2000);
            HttpResponse queryResponse = workflowInstancePage.queryWorkflowInstanceById(loginUser,
                    projectCode, workflowInstanceId);
            if (queryResponse.getBody().getSuccess() && queryResponse.getBody().getData() != null) {
                LinkedHashMap<String, Object> instanceData =
                        (LinkedHashMap<String, Object>) queryResponse.getBody().getData();
                String state = (String) instanceData.get("state");
                log.info("failed dependent workflow instance state: {}", state);
                if ("FAILURE".equals(state)) {
                    failed = true;
                    break;
                } else if ("SUCCESS".equals(state)) {
                    Assertions.fail("dependent workflow instance should not succeed");
                }
            }
        }
        Assertions.assertTrue(failed, "dependent workflow referencing non-existent definition should fail");

        // query task instances and verify DEPENDENT task type with FAILURE state
        HttpResponse queryTaskListResponse = workflowInstancePage.queryTaskListByWorkflowInstanceId(loginUser,
                projectCode, workflowInstanceId);
        Assertions.assertTrue(queryTaskListResponse.getBody().getSuccess());

        List<LinkedHashMap<String, Object>> failedTaskList;
        Object failedTaskData = queryTaskListResponse.getBody().getData();
        if (failedTaskData instanceof List) {
            failedTaskList = (List<LinkedHashMap<String, Object>>) failedTaskData;
        } else {
            LinkedHashMap<String, Object> pageData = (LinkedHashMap<String, Object>) failedTaskData;
            failedTaskList = (List<LinkedHashMap<String, Object>>) pageData.get("taskList");
        }
        Assertions.assertNotNull(failedTaskList);
        Assertions.assertFalse(failedTaskList.isEmpty());

        LinkedHashMap<String, Object> dependentTask = failedTaskList.get(0);
        Assertions.assertEquals("DEPENDENT", dependentTask.get("taskType"));
        Assertions.assertEquals("FAILURE", dependentTask.get("state"));
        log.info("failed dependent task instance verified: taskType={}, state={}",
                dependentTask.get("taskType"), dependentTask.get("state"));
    }
}
