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
import org.apache.dolphinscheduler.api.test.pages.project.TaskInstancePage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class TaskInstanceAPITest {
    private static final Logger logger = LoggerFactory.getLogger(ProjectAPITest.class);

    private static final String projectName = "case03_wen";

    private static final String workFlowName = "shell123";

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId = null;

    private static String genNumId = null;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(user, password);

        sessionId = JSONUtils.convertValue(loginHttpResponse.body().data(), LoginResponseData.class).sessionId();
    }

    @Test
    @Order(1)
    public void testQueryTaskInstance() {
        TaskInstancePage taskInstance = new TaskInstancePage();

        String res = taskInstance.queryTaskInstance(sessionId, projectName, workFlowName);

        Assertions.assertTrue(res.equals("SUCCESS"));

    }

    @Test
    @Order(2)
    public void testQueryTaskInstanceLog() {
        TaskInstancePage taskInstance = new TaskInstancePage();
        HttpResponse res = taskInstance.queryTaskInstanceLog(sessionId, projectName, workFlowName);
        logger.info(String.valueOf(res.body()));
        Assertions.assertTrue(res.body().success());

    }

}
