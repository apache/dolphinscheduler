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

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.LoginResponseData;
import org.apache.dolphinscheduler.api.test.pages.LoginPage;
import org.apache.dolphinscheduler.api.test.pages.project.ProjectPage;
import org.apache.dolphinscheduler.api.test.pages.project.WorkFlowDefinitionPage;
import org.apache.dolphinscheduler.api.test.pages.security.TenantPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class WorkFlowAPITest {
    private static final Logger logger = LoggerFactory.getLogger(ProjectAPITest.class);

    private static final String projectName = "case02_wen";

    private static final String projectDesc = "123";

    private static final String workFlowName = "shell123";

    private static final String tenantName = "admin";

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId = null;

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
        HttpResponse res = flow.createWorkflow(sessionId, projectName, workFlowName);

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
        HttpResponse res = flow.runWorkflow(sessionId, projectName, workFlowName);

        logger.info("Run workflow res：%s", res);
        Assertions.assertTrue(res.body().success());

    }

}
