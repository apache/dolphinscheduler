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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class SensitiveWorkflowVariableAPITest {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "dolphinscheduler123";
    private static final String SECRET = "Ds_SeCrEt_2026_06_08_Case_A1";
    private static final String SENSITIVE_DATA_MASK = "******";

    private static String sessionId;
    private static User loginUser;
    private static ProjectPage projectPage;
    private static WorkflowDefinitionPage workflowDefinitionPage;
    private static WorkflowInstancePage workflowInstancePage;
    private static ExecutorPage executorPage;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(USERNAME, PASSWORD);
        sessionId =
                JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        loginUser = new User();
        loginUser.setUserName(USERNAME);
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        projectPage = new ProjectPage(sessionId);
        workflowDefinitionPage = new WorkflowDefinitionPage(sessionId);
        workflowInstancePage = new WorkflowInstancePage(sessionId);
        executorPage = new ExecutorPage(sessionId);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSensitiveGlobalParamUsesRealValueAtRuntimeAndMasksLog() {
        String projectName = "sensitive-project-" + System.currentTimeMillis();
        String workflowName = "sensitive-workflow-" + System.currentTimeMillis();

        HttpResponse createProjectResponse = projectPage.createProject(loginUser, projectName);
        assertTrue(createProjectResponse.getBody().getSuccess());
        long projectCode = ((Number) ((LinkedHashMap<String, Object>) createProjectResponse.getBody().getData())
                .get("code")).longValue();

        long taskCode = System.currentTimeMillis();
        HttpResponse createWorkflowResponse = workflowDefinitionPage.createWorkflowDefinition(
                loginUser,
                projectCode,
                sensitiveWorkflowJson(taskCode),
                workflowName);
        assertTrue(createWorkflowResponse.getBody().getSuccess());
        long workflowDefinitionCode =
                ((Number) ((LinkedHashMap<String, Object>) createWorkflowResponse.getBody().getData()).get("code"))
                        .longValue();

        HttpResponse queryWorkflowResponse =
                workflowDefinitionPage.queryWorkflowDefinitionByCode(loginUser, projectCode, workflowDefinitionCode);
        assertTrue(queryWorkflowResponse.getBody().getSuccess());
        String workflowResponseData = queryWorkflowResponse.getBody().getData().toString();
        assertTrue(workflowResponseData.contains(SENSITIVE_DATA_MASK));
        assertFalse(workflowResponseData.contains(SECRET));

        HttpResponse releaseWorkflowResponse = workflowDefinitionPage.releaseWorkflowDefinition(
                loginUser,
                projectCode,
                workflowDefinitionCode,
                ReleaseState.ONLINE);
        assertTrue(releaseWorkflowResponse.getBody().getSuccess());

        HttpResponse startWorkflowResponse = executorPage.startWorkflowInstance(
                loginUser,
                projectCode,
                workflowDefinitionCode,
                scheduleTime(),
                FailureStrategy.END,
                WarningType.NONE,
                sensitiveStartParams());
        assertTrue(startWorkflowResponse.getBody().getSuccess());
        List<Integer> workflowInstanceIds = (List<Integer>) startWorkflowResponse.getBody().getData();
        assertEquals(1, workflowInstanceIds.size());
        int workflowInstanceId = workflowInstanceIds.get(0);

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    HttpResponse instanceResponse =
                            workflowInstancePage.queryWorkflowInstanceById(loginUser, projectCode, workflowInstanceId);
                    assertTrue(instanceResponse.getBody().getSuccess());
                    Map<String, Object> workflowInstance = (Map<String, Object>) instanceResponse.getBody().getData();
                    assertEquals("SUCCESS", workflowInstance.get("state"));
                });

        HttpResponse taskListResponse =
                workflowInstancePage.queryTaskInstanceList(loginUser, projectCode, workflowInstanceId);
        assertTrue(taskListResponse.getBody().getSuccess());
        Map<String, Object> taskPage = (Map<String, Object>) taskListResponse.getBody().getData();
        List<Map<String, Object>> taskInstances = (List<Map<String, Object>>) taskPage.get("totalList");
        assertEquals(1, taskInstances.size());
        int taskInstanceId = (int) taskInstances.get(0).get("id");

        HttpResponse logResponse = workflowInstancePage.queryTaskLog(loginUser, taskInstanceId, 0, 1000);
        assertTrue(logResponse.getBody().getSuccess());
        String logContent = ((Map<String, Object>) logResponse.getBody().getData()).get("message").toString();
        assertTrue(logContent.contains(" -> " + SECRET.length()), logContent);
        assertTrue(logContent.contains(SENSITIVE_DATA_MASK), logContent);
        assertFalse(logContent.contains(SECRET), logContent);

        long plainTaskCode = System.currentTimeMillis();
        HttpResponse createPlainWorkflowResponse = workflowDefinitionPage.createWorkflowDefinition(
                loginUser,
                projectCode,
                plainWorkflowJson(plainTaskCode),
                "plain-workflow-" + System.currentTimeMillis());
        assertTrue(createPlainWorkflowResponse.getBody().getSuccess());
        long plainWorkflowDefinitionCode =
                ((Number) ((LinkedHashMap<String, Object>) createPlainWorkflowResponse.getBody().getData()).get("code"))
                        .longValue();
        HttpResponse releasePlainWorkflowResponse = workflowDefinitionPage.releaseWorkflowDefinition(
                loginUser,
                projectCode,
                plainWorkflowDefinitionCode,
                ReleaseState.ONLINE);
        assertTrue(releasePlainWorkflowResponse.getBody().getSuccess());

        HttpResponse startPlainWorkflowResponse = executorPage.startWorkflowInstance(
                loginUser,
                projectCode,
                plainWorkflowDefinitionCode,
                scheduleTime(),
                FailureStrategy.END,
                WarningType.NONE);
        assertTrue(startPlainWorkflowResponse.getBody().getSuccess());
        List<Integer> plainWorkflowInstanceIds = (List<Integer>) startPlainWorkflowResponse.getBody().getData();
        assertEquals(1, plainWorkflowInstanceIds.size());
        int plainWorkflowInstanceId = plainWorkflowInstanceIds.get(0);

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    HttpResponse instanceResponse =
                            workflowInstancePage.queryWorkflowInstanceById(loginUser, projectCode,
                                    plainWorkflowInstanceId);
                    assertTrue(instanceResponse.getBody().getSuccess());
                    Map<String, Object> workflowInstance = (Map<String, Object>) instanceResponse.getBody().getData();
                    assertEquals("SUCCESS", workflowInstance.get("state"));
                });

        HttpResponse plainTaskListResponse =
                workflowInstancePage.queryTaskInstanceList(loginUser, projectCode, plainWorkflowInstanceId);
        assertTrue(plainTaskListResponse.getBody().getSuccess());
        Map<String, Object> plainTaskPage = (Map<String, Object>) plainTaskListResponse.getBody().getData();
        List<Map<String, Object>> plainTaskInstances = (List<Map<String, Object>>) plainTaskPage.get("totalList");
        assertEquals(1, plainTaskInstances.size());
        int plainTaskInstanceId = (int) plainTaskInstances.get(0).get("id");

        HttpResponse plainLogResponse = workflowInstancePage.queryTaskLog(loginUser, plainTaskInstanceId, 0, 1000);
        assertTrue(plainLogResponse.getBody().getSuccess());
        String plainLogContent = ((Map<String, Object>) plainLogResponse.getBody().getData()).get("message").toString();
        assertTrue(plainLogContent.contains(SECRET), plainLogContent);
    }

    private String sensitiveWorkflowJson(long taskCode) {
        return "{"
                + "\"taskDefinitionJson\":[{"
                + "\"code\":" + taskCode + ","
                + "\"delayTime\":\"0\","
                + "\"description\":\"\","
                + "\"environmentCode\":-1,"
                + "\"failRetryInterval\":\"1\","
                + "\"failRetryTimes\":\"0\","
                + "\"flag\":\"YES\","
                + "\"name\":\"sensitive_shell\","
                + "\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo -n ${var} | wc -c\","
                + "\"resourceList\":[]},"
                + "\"taskPriority\":\"MEDIUM\","
                + "\"taskType\":\"SHELL\","
                + "\"timeout\":0,"
                + "\"timeoutFlag\":\"CLOSE\","
                + "\"timeoutNotifyStrategy\":\"\","
                + "\"workerGroup\":\"default\","
                + "\"cpuQuota\":-1,"
                + "\"memoryMax\":-1,"
                + "\"taskExecuteType\":\"BATCH\""
                + "}],"
                + "\"taskRelationJson\":[{"
                + "\"name\":\"\","
                + "\"preTaskCode\":0,"
                + "\"preTaskVersion\":0,"
                + "\"postTaskCode\":" + taskCode + ","
                + "\"postTaskVersion\":0,"
                + "\"conditionType\":\"NONE\","
                + "\"conditionParams\":{}"
                + "}],"
                + "\"executionType\":\"PARALLEL\","
                + "\"description\":\"\","
                + "\"globalParams\":[{\"prop\":\"var\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"" + SECRET
                + "\",\"sensitive\":true}],"
                + "\"timeout\":0"
                + "}";
    }

    private String plainWorkflowJson(long taskCode) {
        return "{"
                + "\"taskDefinitionJson\":[{"
                + "\"code\":" + taskCode + ","
                + "\"delayTime\":\"0\","
                + "\"description\":\"\","
                + "\"environmentCode\":-1,"
                + "\"failRetryInterval\":\"1\","
                + "\"failRetryTimes\":\"0\","
                + "\"flag\":\"YES\","
                + "\"name\":\"plain_shell\","
                + "\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo " + SECRET + "\","
                + "\"resourceList\":[]},"
                + "\"taskPriority\":\"MEDIUM\","
                + "\"taskType\":\"SHELL\","
                + "\"timeout\":0,"
                + "\"timeoutFlag\":\"CLOSE\","
                + "\"timeoutNotifyStrategy\":\"\","
                + "\"workerGroup\":\"default\","
                + "\"cpuQuota\":-1,"
                + "\"memoryMax\":-1,"
                + "\"taskExecuteType\":\"BATCH\""
                + "}],"
                + "\"taskRelationJson\":[{"
                + "\"name\":\"\","
                + "\"preTaskCode\":0,"
                + "\"preTaskVersion\":0,"
                + "\"postTaskCode\":" + taskCode + ","
                + "\"postTaskVersion\":0,"
                + "\"conditionType\":\"NONE\","
                + "\"conditionParams\":{}"
                + "}],"
                + "\"executionType\":\"PARALLEL\","
                + "\"description\":\"\","
                + "\"globalParams\":[],"
                + "\"timeout\":0"
                + "}";
    }

    private String sensitiveStartParams() {
        return "[{\"prop\":\"var\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"" + SENSITIVE_DATA_MASK
                + "\",\"sensitive\":true}]";
    }

    private String scheduleTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return String.format("%s,%s", formatter.format(date), formatter.format(date));
    }
}
