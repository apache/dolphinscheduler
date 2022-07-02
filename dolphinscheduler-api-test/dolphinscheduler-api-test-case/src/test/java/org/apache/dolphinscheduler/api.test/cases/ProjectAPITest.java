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
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class ProjectAPITest {
    private static final Logger logger = LoggerFactory.getLogger(ProjectAPITest.class);

    private static final String projectName = "wen";

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId = null;


    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(user, password);

        sessionId = JSONUtils.convertValue(loginHttpResponse.body().data(), LoginResponseData.class).sessionId();
    }

    @AfterAll
    public static void cleanup() {
        logger.info("success cleanup");
    }

    @Test
    @Order(1)
    public void testCreateProject() {
        ProjectPage projectPage = new ProjectPage();

        HttpResponse createProjectHttpResponse = projectPage.createProject(sessionId, projectName, "123", "admin");
        System.out.println(createProjectHttpResponse.body());
        Assertions.assertTrue(createProjectHttpResponse.body().success());
    }

    @Test
    @Order(1)
    public void testQeuryProject() {
        ProjectPage projectPage = new ProjectPage();

        String aa = projectPage.getProjectCode(sessionId, "wen");
        System.out.println(aa);
    }




}
