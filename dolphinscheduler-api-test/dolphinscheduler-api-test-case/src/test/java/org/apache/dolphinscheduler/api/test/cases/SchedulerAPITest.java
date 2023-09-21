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
import org.apache.dolphinscheduler.api.test.pages.workflow.SchedulerPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@Slf4j
public class SchedulerAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static User loginUser;

    private static SchedulerPage schedulerPage;

    private static ProcessDefinitionPage processDefinitionPage;

    private static ProjectPage projectPage;

    private static long projectCode;

    private static long processDefinitionCode;

    private static int scheduleId;


    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(username, password);
        sessionId = JSONUtils.convertValue(loginHttpResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        projectPage = new ProjectPage(sessionId);
        schedulerPage = new SchedulerPage(sessionId);
        processDefinitionPage = new ProcessDefinitionPage(sessionId);
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
    public void testCreateSchedule() {
        projectPage.createProject(loginUser, "project-test");
        HttpResponse queryAllProjectListResponse = projectPage.queryAllProjectList(loginUser);
        Assertions.assertTrue(queryAllProjectListResponse.getBody().getSuccess());

        projectCode = (long) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllProjectListResponse.getBody().getData()).get(0)).get("code");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("workflow-json/test.json").getFile());
        processDefinitionPage.importProcessDefinition(loginUser, projectCode, file);
        HttpResponse queryAllProcessDefinitionByProjectCodeResponse = processDefinitionPage.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
        Assertions.assertTrue(queryAllProcessDefinitionByProjectCodeResponse.getBody().getSuccess());
        processDefinitionCode = (long) ((LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryAllProcessDefinitionByProjectCodeResponse.getBody().getData()).get(0)).get("processDefinition")).get("code");

        processDefinitionPage.releaseProcessDefinition(loginUser, projectCode, processDefinitionCode, ReleaseState.ONLINE);
        final String schedule = "{\"startTime\":\"2019-08-08 00:00:00\",\"endTime\":\"2100-08-08 00:00:00\",\"timezoneId\":\"America/Phoenix\",\"crontab\":\"0 0 3/6 * * ? *\"}" ;
        HttpResponse createScheduleResponse = schedulerPage.createSchedule(loginUser, projectCode, processDefinitionCode, schedule);
        Assertions.assertTrue(createScheduleResponse.getBody().getSuccess());
        Assertions.assertTrue(createScheduleResponse.getBody().getData().toString().contains("2019-08-08"));
    }

    @Test
    @Order(2)
    public void testQueryScheduleList() {
        HttpResponse queryScheduleListResponse = schedulerPage.queryScheduleList(loginUser, projectCode);
        Assertions.assertTrue(queryScheduleListResponse.getBody().getSuccess());
        Assertions.assertTrue(queryScheduleListResponse.getBody().getData().toString().contains("2019-08-08"));
        scheduleId = (int) ((LinkedHashMap<String, Object>) ((List<LinkedHashMap>) queryScheduleListResponse.getBody().getData()).get(0)).get("id");
    }

    @Test
    @Order(3)
    public void testPublishScheduleOnline() {
        HttpResponse publishScheduleOnlineResponse = schedulerPage.publishScheduleOnline(loginUser, projectCode, scheduleId);
        Assertions.assertTrue(publishScheduleOnlineResponse.getBody().getSuccess());

        HttpResponse queryScheduleListResponse = schedulerPage.queryScheduleList(loginUser, projectCode);
        Assertions.assertTrue(queryScheduleListResponse.getBody().getSuccess());
        Assertions.assertTrue(queryScheduleListResponse.getBody().getData().toString().contains("releaseState=ONLINE"));
    }

    @Test
    @Order(4)
    public void testOfflineSchedule() {
        HttpResponse offlineScheduleResponse = schedulerPage.offlineSchedule(loginUser, projectCode, scheduleId);
        Assertions.assertTrue(offlineScheduleResponse.getBody().getSuccess());

        HttpResponse queryScheduleListResponse = schedulerPage.queryScheduleList(loginUser, projectCode);
        Assertions.assertTrue(queryScheduleListResponse.getBody().getSuccess());
        Assertions.assertTrue(queryScheduleListResponse.getBody().getData().toString().contains("releaseState=OFFLINE"));
    }

    @Test
    @Order(5)
    public void testUpdateSchedule() {
        final String schedule = "{\"startTime\":\"1996-08-08 00:00:00\",\"endTime\":\"2200-08-08 00:00:00\",\"timezoneId\":\"America/Phoenix\",\"crontab\":\"0 0 3/6 * * ? *\"}";
        HttpResponse updateScheduleResponse = schedulerPage.updateSchedule(loginUser, projectCode, scheduleId, schedule);
        Assertions.assertTrue(updateScheduleResponse.getBody().getSuccess());

        HttpResponse queryScheduleListResponse = schedulerPage.queryScheduleList(loginUser, projectCode);
        Assertions.assertTrue(queryScheduleListResponse.getBody().getSuccess());
        Assertions.assertTrue(queryScheduleListResponse.getBody().getData().toString().contains("1996-08-08"));
    }

    @Test
    @Order(6)
    public void testDeleteScheduleById() {
        HttpResponse deleteScheduleByIdResponse = schedulerPage.deleteScheduleById(loginUser, projectCode, scheduleId);
        Assertions.assertTrue(deleteScheduleByIdResponse.getBody().getSuccess());

        HttpResponse queryScheduleListResponse = schedulerPage.queryScheduleList(loginUser, projectCode);
        Assertions.assertTrue(queryScheduleListResponse.getBody().getSuccess());
        Assertions.assertFalse(queryScheduleListResponse.getBody().getData().toString().contains("1996-08-08"));
    }
}


