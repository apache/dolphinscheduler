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
import org.apache.dolphinscheduler.api.test.pages.workflow.ProcessDefinitionPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.com.google.common.io.Resources;

import lombok.extern.slf4j.Slf4j;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class ProcessDefinitionAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static User loginUser;

    private static ProcessDefinitionPage processDefinitionPage;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId = JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        processDefinitionPage = new ProcessDefinitionPage(sessionId);
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
            MultipartFile file = getMultipartFile("workflow-json/test.json");
            HttpResponse importProcessDefinitionResponse = processDefinitionPage
                .importProcessDefinition(loginUser, 1, file);
            log.info("[debug111] {}", importProcessDefinitionResponse.toString());
            log.info(importProcessDefinitionResponse.getBody().getSuccess().toString());
            log.info(importProcessDefinitionResponse.getBody().getData().toString());
        }  catch (Exception e) {
            log.error("failed", e);
            Assertions.fail();
        }
    }

    private MultipartFile getMultipartFile(String filePath) throws URISyntaxException {
        URL urlPath = Resources.getResource(filePath);
        Path path = Paths.get(urlPath.toURI());
//        String contentType = "text/plain";
        String contentType = "multipart/form-data";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
        }
        MultipartFile result = new MockMultipartFile(filePath,
            filePath, contentType, content);
        log.info("workflow json size: {}", result.getSize());
        return result;
    }
}


