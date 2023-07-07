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
import org.apache.dolphinscheduler.api.test.pages.project.ProjectPage;
import org.apache.dolphinscheduler.api.test.pages.workflow.ProcessDefinitionPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.File;;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class ProcessDefinitionAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static User loginUser;

    private static ProcessDefinitionPage processDefinitionPage;

    private static ProjectPage projectPage;

    private static long projectCode;

    private static long processDefinitionCode;

    private static String processDefinitionName;


    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId = JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        processDefinitionPage = new ProcessDefinitionPage(sessionId);
        projectPage = new ProjectPage(sessionId);
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
    public void testImportProcessDefinition() {
        try {
            HttpResponse createProjectResponse = projectPage.createProject(loginUser, "project-test");
            HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
            Assertions.assertTrue(queryAllProjectListResponse.getBody().getSuccess());

            projectCode = (long) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllProjectListResponse.getBody().getData()).get(0)).get("code");
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("workflow-json/test.json").getFile());
            CloseableHttpResponse importProcessDefinitionResponse = processDefinitionPage
                .importProcessDefinition(loginUser, projectCode, file);
            String data = EntityUtils.toString(importProcessDefinitionResponse.getEntity());
            Assertions.assertTrue(data.contains("\"success\":true"));
        }  catch (Exception e) {
            log.error("failed", e);
            Assertions.fail();
        }
    }

    @Test
    @Order(2)
    public void testQueryAllProcessDefinitionByProjectCode() {
        HttpResponse queryAllProcessDefinitionByProjectCodeResponse = processDefinitionPage.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(queryAllProcessDefinitionByProjectCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(queryAllProcessDefinitionByProjectCodeResponse.getBody().getData().toString().contains("hello world"));
        processDefinitionCode = (long) ((LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllProcessDefinitionByProjectCodeResponse.getBody().getData()).get(0)).get("processDefinition")).get("code");
        processDefinitionName = (String) ((LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllProcessDefinitionByProjectCodeResponse.getBody().getData()).get(0)).get("processDefinition")).get("name");
    }

    @Test
    @Order(3)
    public void testQueryProcessDefinitionByCode() {
        HttpResponse queryProcessDefinitionByCodeResponse = processDefinitionPage.queryProcessDefinitionByCode(loginUser, projectCode, processDefinitionCode);
        Assertions.assertTrue(queryProcessDefinitionByCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(queryProcessDefinitionByCodeResponse.getBody().getData().toString().contains("hello world"));
    }

    @Test
    @Order(4)
    public void testgetProcessListByProjectCode() {
        HttpResponse getProcessListByProjectCodeResponse = processDefinitionPage.getProcessListByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(getProcessListByProjectCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(getProcessListByProjectCodeResponse.getBody().getData().toString().contains("test_import"));
    }

    @Test
    @Order(5)
    public void testQueryProcessDefinitionByName() {
        HttpResponse queryProcessDefinitionByNameResponse = processDefinitionPage.queryProcessDefinitionByName(loginUser, projectCode, processDefinitionName);
        Assertions.assertTrue(queryProcessDefinitionByNameResponse.getBody().getSuccess());
        Assertions.assertTrue(queryProcessDefinitionByNameResponse.getBody().getData().toString().contains("hello world"));
    }

    @Test
    @Order(6)
    public void testQueryProcessDefinitionList() {
        HttpResponse queryProcessDefinitionListResponse = processDefinitionPage.queryProcessDefinitionList(loginUser, projectCode);
        Assertions.assertTrue(queryProcessDefinitionListResponse.getBody().getSuccess());
        Assertions.assertTrue(queryProcessDefinitionListResponse.getBody().getData().toString().contains("hello world"));
    }

    @Test
    @Order(7)
    public void testReleaseProcessDefinition() {
        HttpResponse releaseProcessDefinitionResponse = processDefinitionPage.releaseProcessDefinition(loginUser, projectCode, processDefinitionCode, ReleaseState.ONLINE);
        Assertions.assertTrue(releaseProcessDefinitionResponse.getBody().getSuccess());

        HttpResponse queryProcessDefinitionByCodeResponse = processDefinitionPage.queryProcessDefinitionByCode(loginUser, projectCode, processDefinitionCode);
        Assertions.assertTrue(queryProcessDefinitionByCodeResponse.getBody().getSuccess());
        Assertions.assertTrue(queryProcessDefinitionByCodeResponse.getBody().getData().toString().contains("releaseState=ONLINE"));
    }

    @Test
    @Order(8)
    public void testDeleteProcessDefinitionByCode() {
        HttpResponse deleteProcessDefinitionByCodeResponse = processDefinitionPage.deleteProcessDefinitionByCode(loginUser, projectCode, processDefinitionCode);
        Assertions.assertFalse(deleteProcessDefinitionByCodeResponse.getBody().getSuccess());

        HttpResponse releaseProcessDefinitionResponse = processDefinitionPage.releaseProcessDefinition(loginUser, projectCode, processDefinitionCode, ReleaseState.OFFLINE);
        Assertions.assertTrue(releaseProcessDefinitionResponse.getBody().getSuccess());

        deleteProcessDefinitionByCodeResponse = processDefinitionPage.deleteProcessDefinitionByCode(loginUser, projectCode, processDefinitionCode);
        Assertions.assertTrue(deleteProcessDefinitionByCodeResponse.getBody().getSuccess());

        HttpResponse queryProcessDefinitionListResponse = processDefinitionPage.queryProcessDefinitionList(loginUser, projectCode);
        Assertions.assertTrue(queryProcessDefinitionListResponse.getBody().getSuccess());
        Assertions.assertFalse(queryProcessDefinitionListResponse.getBody().getData().toString().contains("hello world"));
    }
}


