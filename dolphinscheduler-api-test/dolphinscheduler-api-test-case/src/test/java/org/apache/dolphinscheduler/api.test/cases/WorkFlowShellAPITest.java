/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.test.cases;

import org.apache.dolphinscheduler.api.test.Constants;
import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.LoginResponseData;
import org.apache.dolphinscheduler.api.test.pages.LoginPage;
import org.apache.dolphinscheduler.api.test.pages.project.ProjectPage;
import org.apache.dolphinscheduler.api.test.pages.project.WorkFlowDefinitionPage;
import org.apache.dolphinscheduler.api.test.pages.project.WorkFlowInstancesPage;
import org.apache.dolphinscheduler.api.test.pages.security.TenantPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class WorkFlowShellAPITest {
    private static final Logger logger = LoggerFactory.getLogger(ProjectAPITest.class);

    private static final String projectName = "case02_wen";

    private static final String projectDesc = "123";

    private static final String workFlowName = "shell123";

    private static final String tenantName = System.getProperty("user.name");

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId = null;

    ArrayList<Object> localParams = new ArrayList<>();

    ArrayList<Object> resourceList = new ArrayList<>();

    private static String rawScript = "echo 123";

    private static String delayTime = "0";

    private static String description = "";

    private static String environmentCode = "-1";

    private static String failRetryInterval = "1";

    private static String failRetryTimes = "0";

    private static String flag = "YES";

    private static String taskDefinitionRequestDataName = "shell123";

    private static String taskPriority = "MEDIUM";

    private static String taskType = "SHELL";

    private int timeout = 0;

    private static String timeoutFlag = "CLOSE";

    private static String timeoutNotifyStrategy = "";

    private static String workerGroup = "default";

    private static String taskRelationRequestDataName = "";

    private int preTaskCode = 0;

    private int preTaskVersion = 0;

    private static String conditionType = "NONE";
    private static String executionType = "PARALLEL";
    HashMap<String, Object> conditionParams = new HashMap<>();
    private String globalParams = "[]";

    private static String startEndTime = "2022-06-25T16:00:00.000Z";

    private static String scheduleTime = "2022-06-26 00:00:00,2022-06-26 00:00:00";

    private static String failureStrategy = "CONTINUE";

    private static String warningType = "NONE";

    private static String warningGroupId = "";

    private static String execType = "START_PROCESS";

    private static String startNodeList = "";

    private static String taskDependType = "TASK_POST";

    private static String dependentMode = "OFF_MODE";

    private static String runMode = "RUN_MODE_SERIAL";

    private static String processInstancePriority = "MEDIUM";

    private static String startParams = "";

    private static String expectedParallelismNumber = "";

    private static int dryRun = 0;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        ProjectPage projectPage = new ProjectPage();
        HttpResponse loginHttpResponse = loginPage.login(user, password);
        TenantPage tenantPage = new TenantPage();

        sessionId = JSONUtils.convertValue(loginHttpResponse.body().data(), LoginResponseData.class).sessionId();
        projectPage.createProject(sessionId, projectName, projectDesc, user);
        tenantPage.createTenant(sessionId, tenantName, 1, "");

    }

    @Test
    @Order(1)
    public void testCreateWorkflow() {
        WorkFlowDefinitionPage flow = new WorkFlowDefinitionPage();
        flow.getGenNumId(sessionId, projectName);
        HttpResponse res = flow.createWorkflow(sessionId, projectName, workFlowName, localParams, resourceList, rawScript, delayTime, description, environmentCode, failRetryInterval,
            failRetryTimes, flag, taskDefinitionRequestDataName, taskPriority, taskType, timeout, timeoutFlag, timeoutNotifyStrategy, workerGroup, taskRelationRequestDataName, preTaskCode,
            preTaskVersion, conditionType, conditionParams, executionType, globalParams);

        logger.info("Create workflow res：%s", res);
        Assertions.assertTrue(res.body().success());
    }

    @Test
    @Order(2)
    public void testOnlineWorkflow() {
        WorkFlowDefinitionPage flow = new WorkFlowDefinitionPage();
        HttpResponse res = flow.onLineWorkflow(sessionId, projectName, workFlowName);

        logger.info("Online workflow res：%s", res);
        Assertions.assertTrue(res.body().success());

    }

    @Test
    @Order(3)
    public void testQueryWorkflow() {
        WorkFlowDefinitionPage flow = new WorkFlowDefinitionPage();
        flow.getGenNumId(sessionId, projectName);
        HttpResponse res = flow.queryWorkflow(sessionId, projectName, workFlowName);

        logger.info("Query workflow res：%s", res);
        Assertions.assertTrue(res.body().success());
    }

    @Test
    @Order(4)
    public void testRunWorkflow() {
        WorkFlowDefinitionPage flow = new WorkFlowDefinitionPage();
        HttpResponse res = flow.runWorkflow(sessionId, projectName, workFlowName, startEndTime, scheduleTime, failureStrategy, warningType,
            warningGroupId, execType, startNodeList, taskDependType, dependentMode, runMode, processInstancePriority,
            workerGroup, environmentCode, startParams, expectedParallelismNumber, dryRun);

        logger.info("Run workflow res：%s", res);
        Assertions.assertTrue(res.body().success());

    }

    @Test
    @Order(5)
    public void testQueryWorkflowInstanceState() throws InterruptedException {
        WorkFlowInstancesPage instance = new WorkFlowInstancesPage();
        String state = null;

        for (int i = 0; i < Constants.SLEEP_FREQUENCY; i++) {
            state = instance.queryWorkflowInstanceState(sessionId, projectName, workFlowName);
            Thread.sleep(Constants.SLEEP_INTERVAL);
        }
        logger.info("Run workflow state：%s", state);
        Assertions.assertEquals("SUCCESS", state);

    }

}
