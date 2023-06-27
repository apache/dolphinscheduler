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
import org.apache.dolphinscheduler.api.test.pages.LoginPage;
import org.apache.dolphinscheduler.api.test.pages.security.WorkerGroupPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class WorkerGroupAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static User loginUser;

    private static WorkerGroupPage workerGroupPage;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId = JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        workerGroupPage = new WorkerGroupPage(sessionId);
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
    public void testSaveWorkerGroup() {
        HttpResponse saveWorkerGroupHttpResponse = workerGroupPage
            .saveWorkerGroup(loginUser, 1, "test_worker_group", "10.5.0.5:1234", "test", null);
        Assertions.assertTrue(saveWorkerGroupHttpResponse.getBody().getSuccess());

        HttpResponse queryAllWorkerGroupsResponse = workerGroupPage.queryAllWorkerGroups(loginUser);
        List<String> workerGroupsList = (List<String>) queryAllWorkerGroupsResponse.getBody().getData();
        Set<String> workerGroupsActual = new HashSet<>(workerGroupsList);
        Set<String> workerGroupsExpected = new HashSet<>(Arrays.asList("test_worker_group", "default"));
        Assertions.assertEquals(workerGroupsExpected, workerGroupsActual);
    }

    @Test
    @Order(2)
    public void testQueryAllWorkerGroupsPaging() {
        HttpResponse queryAllWorkerGroupsPagingResponse = workerGroupPage.queryAllWorkerGroupsPaging(loginUser, 1, 2, null);
        Assertions.assertTrue(queryAllWorkerGroupsPagingResponse.getBody().getSuccess());
        String workerGroupPageInfoData =  queryAllWorkerGroupsPagingResponse.getBody().getData().toString();
        Assertions.assertTrue(workerGroupPageInfoData.contains("test_worker_group"));
    }

    @Test
    @Order(3)
    public void testQueryAllWorkerGroups() {
        HttpResponse queryAllWorkerGroupsResponse = workerGroupPage.queryAllWorkerGroups(loginUser);
        Assertions.assertTrue(queryAllWorkerGroupsResponse.getBody().getSuccess());

        String workerGroupPageInfoData =  queryAllWorkerGroupsResponse.getBody().getData().toString();
        Assertions.assertTrue(workerGroupPageInfoData.contains("test_worker_group"));
    }

    @Test
    @Order(4)
    public void queryWorkerAddressList() {
        HttpResponse queryWorkerAddressListResponse = workerGroupPage.queryWorkerAddressList(loginUser);
        Assertions.assertTrue(queryWorkerAddressListResponse.getBody().getSuccess());
        Assertions.assertTrue(queryWorkerAddressListResponse.getBody().getData().toString().contains("10.5.0.5:1234"));
    }

    @Test
    @Order(5)
    public void testDeleteWorkerGroupById() {
        HttpResponse queryAllWorkerGroupsResponse = workerGroupPage.queryAllWorkerGroups(loginUser);
        String workerGroupsBeforeDelete = queryAllWorkerGroupsResponse.getBody().getData().toString();
        Assertions.assertTrue(queryAllWorkerGroupsResponse.getBody().getSuccess());
        Assertions.assertTrue(workerGroupsBeforeDelete.contains("test_worker_group"));

        HttpResponse deleteWorkerGroupResponse = workerGroupPage.deleteWorkerGroupById(loginUser, 1);
        Assertions.assertTrue(deleteWorkerGroupResponse.getBody().getSuccess());

        queryAllWorkerGroupsResponse = workerGroupPage.queryAllWorkerGroups(loginUser);
        String workerGroupsAfterDelete = queryAllWorkerGroupsResponse.getBody().getData().toString();
        Assertions.assertTrue(!workerGroupsAfterDelete.contains("test_worker_group"));
    }
}
