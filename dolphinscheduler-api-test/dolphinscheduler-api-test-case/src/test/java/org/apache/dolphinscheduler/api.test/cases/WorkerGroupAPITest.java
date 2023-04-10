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

import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.LoginResponseData;
import org.apache.dolphinscheduler.api.test.entity.TenantListPagingResponseData;
import org.apache.dolphinscheduler.api.test.entity.TenantListPagingResponseTotalList;
import org.apache.dolphinscheduler.api.test.pages.LoginPage;
import org.apache.dolphinscheduler.api.test.pages.security.TenantPage;
import org.apache.dolphinscheduler.api.test.pages.security.WorkerGroupPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class WorkerGroupAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId = null;

    private static WorkerGroupPage workerGroupPage;

    private static User user;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        LOGGER.info(loginHttpResponse.toString());
        sessionId = JSONUtils.convertValue(loginHttpResponse.body().data(), LoginResponseData.class).sessionId();
        workerGroupPage = new WorkerGroupPage(sessionId);
    }

    @AfterAll
    public static void cleanup() {
        LOGGER.info("success cleanup");
    }

    @Test
    @Order(1)
    public void testSaveWorkerGroup() {
        LOGGER.info("test");
        User loginUser = new User();
        loginUser.setId(123);
        loginUser.setUserType(UserType.GENERAL_USER);
        HttpResponse saveWorkerGroupHttpResponse = workerGroupPage
            .saveWorkerGroup(loginUser, 1, "test_worker_group", "172.31.0.2:1234", "test", null);
//        (User loginUser, int id, String name, String addrList, String description, String otherParamsJson)
        Assertions.assertTrue(saveWorkerGroupHttpResponse.body().success());
    }
}
