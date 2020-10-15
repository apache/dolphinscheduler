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

package org.apache.dolphinscheduler.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.dolphinscheduler.api.dto.ProcessMeta;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RunWith(MockitoJUnitRunner.class)
public class ProcessDefinitionServiceTest {

    @InjectMocks
    private ProcessDefinitionServiceImpl processDefinitionService;

    @Mock
    private ProcessDefinitionMapper processDefineMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private TaskInstanceMapper taskInstanceMapper;

    @Mock
    private ProcessDefinitionVersionService processDefinitionVersionService;

    private static final String SHELL_JSON = "{\n"
            + "    \"globalParams\": [\n"
            + "        \n"
            + "    ],\n"
            + "    \"tasks\": [\n"
            + "        {\n"
            + "            \"type\": \"SHELL\",\n"
            + "            \"id\": \"tasks-9527\",\n"
            + "            \"name\": \"shell-1\",\n"
            + "            \"params\": {\n"
            + "                \"resourceList\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"localParams\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"rawScript\": \"#!/bin/bash\\necho \\\"shell-1\\\"\"\n"
            + "            },\n"
            + "            \"description\": \"\",\n"
            + "            \"runFlag\": \"NORMAL\",\n"
            + "            \"dependence\": {\n"
            + "                \n"
            + "            },\n"
            + "            \"maxRetryTimes\": \"0\",\n"
            + "            \"retryInterval\": \"1\",\n"
            + "            \"timeout\": {\n"
            + "                \"strategy\": \"\",\n"
            + "                \"interval\": 1,\n"
            + "                \"enable\": false\n"
            + "            },\n"
            + "            \"taskInstancePriority\": \"MEDIUM\",\n"
            + "            \"workerGroupId\": -1,\n"
            + "            \"preTasks\": [\n"
            + "                \n"
            + "            ]\n"
            + "        }\n"
            + "    ],\n"
            + "    \"tenantId\": 1,\n"
            + "    \"timeout\": 0\n"
            + "}";

    private static final String CYCLE_SHELL_JSON = "{\n"
            + "    \"globalParams\": [\n"
            + "        \n"
            + "    ],\n"
            + "    \"tasks\": [\n"
            + "        {\n"
            + "            \"type\": \"SHELL\",\n"
            + "            \"id\": \"tasks-9527\",\n"
            + "            \"name\": \"shell-1\",\n"
            + "            \"params\": {\n"
            + "                \"resourceList\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"localParams\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"rawScript\": \"#!/bin/bash\\necho \\\"shell-1\\\"\"\n"
            + "            },\n"
            + "            \"description\": \"\",\n"
            + "            \"runFlag\": \"NORMAL\",\n"
            + "            \"dependence\": {\n"
            + "                \n"
            + "            },\n"
            + "            \"maxRetryTimes\": \"0\",\n"
            + "            \"retryInterval\": \"1\",\n"
            + "            \"timeout\": {\n"
            + "                \"strategy\": \"\",\n"
            + "                \"interval\": 1,\n"
            + "                \"enable\": false\n"
            + "            },\n"
            + "            \"taskInstancePriority\": \"MEDIUM\",\n"
            + "            \"workerGroupId\": -1,\n"
            + "            \"preTasks\": [\n"
            + "                \"tasks-9529\"\n"
            + "            ]\n"
            + "        },\n"
            + "        {\n"
            + "            \"type\": \"SHELL\",\n"
            + "            \"id\": \"tasks-9528\",\n"
            + "            \"name\": \"shell-1\",\n"
            + "            \"params\": {\n"
            + "                \"resourceList\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"localParams\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"rawScript\": \"#!/bin/bash\\necho \\\"shell-1\\\"\"\n"
            + "            },\n"
            + "            \"description\": \"\",\n"
            + "            \"runFlag\": \"NORMAL\",\n"
            + "            \"dependence\": {\n"
            + "                \n"
            + "            },\n"
            + "            \"maxRetryTimes\": \"0\",\n"
            + "            \"retryInterval\": \"1\",\n"
            + "            \"timeout\": {\n"
            + "                \"strategy\": \"\",\n"
            + "                \"interval\": 1,\n"
            + "                \"enable\": false\n"
            + "            },\n"
            + "            \"taskInstancePriority\": \"MEDIUM\",\n"
            + "            \"workerGroupId\": -1,\n"
            + "            \"preTasks\": [\n"
            + "                \"tasks-9527\"\n"
            + "            ]\n"
            + "        },\n"
            + "        {\n"
            + "            \"type\": \"SHELL\",\n"
            + "            \"id\": \"tasks-9529\",\n"
            + "            \"name\": \"shell-1\",\n"
            + "            \"params\": {\n"
            + "                \"resourceList\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"localParams\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"rawScript\": \"#!/bin/bash\\necho \\\"shell-1\\\"\"\n"
            + "            },\n"
            + "            \"description\": \"\",\n"
            + "            \"runFlag\": \"NORMAL\",\n"
            + "            \"dependence\": {\n"
            + "                \n"
            + "            },\n"
            + "            \"maxRetryTimes\": \"0\",\n"
            + "            \"retryInterval\": \"1\",\n"
            + "            \"timeout\": {\n"
            + "                \"strategy\": \"\",\n"
            + "                \"interval\": 1,\n"
            + "                \"enable\": false\n"
            + "            },\n"
            + "            \"taskInstancePriority\": \"MEDIUM\",\n"
            + "            \"workerGroupId\": -1,\n"
            + "            \"preTasks\": [\n"
            + "                \"tasks-9528\"\n"
            + "            ]\n"
            + "        }\n"
            + "    ],\n"
            + "    \"tenantId\": 1,\n"
            + "    \"timeout\": 0\n"
            + "}";

    @Test
    public void testQueryProcessDefinitionList() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionList(loginUser, "project_test1");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        List<ProcessDefinition> resourceList = new ArrayList<>();
        resourceList.add(getProcessDefinition());
        Mockito.when(processDefineMapper.queryAllDefinitionList(project.getId())).thenReturn(resourceList);
        Map<String, Object> checkSuccessRes = processDefinitionService.queryProcessDefinitionList(loginUser, "project_test1");
        Assert.assertEquals(Status.SUCCESS, checkSuccessRes.get(Constants.STATUS));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryProcessDefinitionListPaging() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionListPaging(loginUser, "project_test1", "", 1, 5, 0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectName);
        loginUser.setId(1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Page<ProcessDefinition> page = new Page<>(1, 10);
        page.setTotal(30);
        Mockito.when(processDefineMapper.queryDefineListPaging(
                Mockito.any(IPage.class)
                , Mockito.eq("")
                , Mockito.eq(loginUser.getId())
                , Mockito.eq(project.getId())
                , Mockito.anyBoolean())).thenReturn(page);

        Map<String, Object> map1 = processDefinitionService.queryProcessDefinitionListPaging(
                loginUser, projectName, "", 1, 10, loginUser.getId());

        Assert.assertEquals(Status.SUCCESS, map1.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionById() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project check auth fail
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionById(loginUser,
                "project_test1", 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processDefineMapper.selectById(1)).thenReturn(null);
        Map<String, Object> instanceNotexitRes = processDefinitionService.queryProcessDefinitionById(loginUser,
                "project_test1", 1);
        Assert.assertEquals(Status.PROCESS_INSTANCE_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        //instance exit
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(getProcessDefinition());
        Map<String, Object> successRes = processDefinitionService.queryProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testBatchCopyProcessDefinition() {

        String projectName = "project_test1";
        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // copy project definition ids empty test
        Map<String, Object> map = processDefinitionService.batchCopyProcessDefinition(loginUser, projectName, StringUtils.EMPTY, 0);
        Assert.assertEquals(Status.PROCESS_DEFINITION_IDS_IS_EMPTY, map.get(Constants.STATUS));

        Map<String, Object> result = new HashMap<>();

        // project check auth fail
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map1 = processDefinitionService.batchCopyProcessDefinition(
                loginUser, projectName, String.valueOf(project.getId()), 0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map1.get(Constants.STATUS));

        // project check auth success, target project is null
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(projectMapper.queryDetailById(0)).thenReturn(null);
        Map<String, Object> map2 = processDefinitionService.batchCopyProcessDefinition(
                loginUser, projectName, String.valueOf(project.getId()), 0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map2.get(Constants.STATUS));

        // project check auth success, target project name not equal project name, check auth target project fail
        Project project1 = getProject(projectName);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);

        putMsg(result, Status.SUCCESS, projectName);
        String projectName2 = "project_test2";
        Project project2 = getProject(projectName2);
        Mockito.when(projectMapper.queryByName(projectName2)).thenReturn(project2);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project2, projectName2)).thenReturn(result);
        Mockito.when(projectMapper.queryDetailById(1)).thenReturn(project2);
        // instance exit
        ProcessDefinition definition = getProcessDefinition();
        definition.setLocations("{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}");
        definition.setProcessDefinitionJson("{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\","
                + "\"name\":\"ssh_test1\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234"
                + "\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\","
                + "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}");
        definition.setConnects("[]");

        Mockito.when(processDefineMapper.selectById(46)).thenReturn(definition);

        Map<String, Object> map3 = processDefinitionService.batchCopyProcessDefinition(
                loginUser, projectName, "46", 1);
        Assert.assertEquals(Status.SUCCESS, map3.get(Constants.STATUS));

    }

    @Test
    public void testBatchMoveProcessDefinition() {
        String projectName = "project_test1";
        Project project1 = getProject(projectName);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project1);

        String projectName2 = "project_test2";
        Project project2 = getProject(projectName2);
        Mockito.when(projectMapper.queryByName(projectName2)).thenReturn(project2);

        int targetProjectId = 2;
        Mockito.when(projectMapper.queryDetailById(targetProjectId)).thenReturn(getProjectById(targetProjectId));

        Project project = getProject(projectName);
        Project targetProject = getProjectById(targetProjectId);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectName);

        Map<String, Object> result2 = new HashMap<>();
        putMsg(result2, Status.SUCCESS, targetProject.getName());

        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project2, projectName2)).thenReturn(result);

        ProcessDefinition definition = getProcessDefinition();
        definition.setLocations("{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}");
        definition.setProcessDefinitionJson("{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\""
                + ",\"name\":\"ssh_test1\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234"
                + "\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\","
                + "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}");
        definition.setConnects("[]");

        // check target project result == null
        Mockito.when(processDefineMapper.updateById(definition)).thenReturn(46);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(definition);

        putMsg(result, Status.SUCCESS);

        Map<String, Object> successRes = processDefinitionService.batchMoveProcessDefinition(
                loginUser, "project_test1", "46", 2);

        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void deleteProcessDefinitionByIdTest() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.deleteProcessDefinitionById(loginUser, "project_test1", 6);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processDefineMapper.selectById(1)).thenReturn(null);
        Map<String, Object> instanceNotexitRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 1);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        ProcessDefinition processDefinition = getProcessDefinition();
        //user no auth
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> userNoAuthRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, userNoAuthRes.get(Constants.STATUS));

        //process definition online
        loginUser.setUserType(UserType.ADMIN_USER);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> dfOnlineRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.PROCESS_DEFINE_STATE_ONLINE, dfOnlineRes.get(Constants.STATUS));

        //scheduler list elements > 1
        processDefinition.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        List<Schedule> schedules = new ArrayList<>();
        schedules.add(getSchedule());
        schedules.add(getSchedule());
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Map<String, Object> schedulerGreaterThanOneRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR, schedulerGreaterThanOneRes.get(Constants.STATUS));

        //scheduler online
        schedules.clear();
        Schedule schedule = getSchedule();
        schedule.setReleaseState(ReleaseState.ONLINE);
        schedules.add(schedule);
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Map<String, Object> schedulerOnlineRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.SCHEDULE_CRON_STATE_ONLINE, schedulerOnlineRes.get(Constants.STATUS));

        //delete fail
        schedules.clear();
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedules.add(schedule);
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Mockito.when(processDefineMapper.deleteById(46)).thenReturn(0);
        Map<String, Object> deleteFail = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR, deleteFail.get(Constants.STATUS));

        //delete success
        Mockito.when(processDefineMapper.deleteById(46)).thenReturn(1);
        Map<String, Object> deleteSuccess = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.SUCCESS, deleteSuccess.get(Constants.STATUS));
    }

    @Test
    public void testReleaseProcessDefinition() {

        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.releaseProcessDefinition(loginUser, "project_test1",
                6, ReleaseState.OFFLINE.getCode());
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        // project check auth success, processs definition online
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(getProcessDefinition());
        Map<String, Object> onlineRes = processDefinitionService.releaseProcessDefinition(
                loginUser, "project_test1", 46, ReleaseState.ONLINE.getCode());
        Assert.assertEquals(Status.SUCCESS, onlineRes.get(Constants.STATUS));

        // project check auth success, processs definition online
        ProcessDefinition processDefinition1 = getProcessDefinition();
        processDefinition1.setResourceIds("1,2");
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition1);
        Mockito.when(processService.getUserById(1)).thenReturn(loginUser);
        Map<String, Object> onlineWithResourceRes = processDefinitionService.releaseProcessDefinition(
                loginUser, "project_test1", 46, ReleaseState.ONLINE.getCode());
        Assert.assertEquals(Status.SUCCESS, onlineWithResourceRes.get(Constants.STATUS));

        // release error code
        Map<String, Object> failRes = processDefinitionService.releaseProcessDefinition(
                loginUser, "project_test1", 46, 2);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failRes.get(Constants.STATUS));

        //FIXME has function exit code 1 when exception
        //process definition offline
        //        List<Schedule> schedules = new ArrayList<>();
        //        Schedule schedule = getSchedule();
        //        schedules.add(schedule);
        //        Mockito.when(scheduleMapper.selectAllByProcessDefineArray(new int[]{46})).thenReturn(schedules);
        //        Mockito.when(scheduleMapper.updateById(schedule)).thenReturn(1);
        //        Map<String, Object> offlineRes = processDefinitionService.releaseProcessDefinition(loginUser, "project_test1",
        //                46, ReleaseState.OFFLINE.getCode());
        //        Assert.assertEquals(Status.SUCCESS, offlineRes.get(Constants.STATUS));
    }

    @Test
    public void testVerifyProcessDefinitionName() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, process not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(processDefineMapper.verifyByDefineName(project.getId(), "test_pdf")).thenReturn(null);
        Map<String, Object> processNotExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.SUCCESS, processNotExistRes.get(Constants.STATUS));

        //process exist
        Mockito.when(processDefineMapper.verifyByDefineName(project.getId(), "test_pdf")).thenReturn(getProcessDefinition());
        Map<String, Object> processExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR, processExistRes.get(Constants.STATUS));
    }

    @Test
    public void testCheckProcessNodeList() {

        Map<String, Object> dataNotValidRes = processDefinitionService.checkProcessNodeList(null, "");
        Assert.assertEquals(Status.DATA_IS_NOT_VALID, dataNotValidRes.get(Constants.STATUS));

        // task not empty
        String processDefinitionJson = SHELL_JSON;
        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        Assert.assertNotNull(processData);
        Map<String, Object> taskEmptyRes = processDefinitionService.checkProcessNodeList(processData, processDefinitionJson);
        Assert.assertEquals(Status.SUCCESS, taskEmptyRes.get(Constants.STATUS));

        // task empty
        processData.setTasks(null);
        Map<String, Object> taskNotEmptyRes = processDefinitionService.checkProcessNodeList(processData, processDefinitionJson);
        Assert.assertEquals(Status.DATA_IS_NULL, taskNotEmptyRes.get(Constants.STATUS));

        // task cycle
        String processDefinitionJsonCycle = CYCLE_SHELL_JSON;
        ProcessData processDataCycle = JSONUtils.parseObject(processDefinitionJsonCycle, ProcessData.class);
        Map<String, Object> taskCycleRes = processDefinitionService.checkProcessNodeList(processDataCycle, processDefinitionJsonCycle);
        Assert.assertEquals(Status.PROCESS_NODE_HAS_CYCLE, taskCycleRes.get(Constants.STATUS));

        //json abnormal
        String abnormalJson = processDefinitionJson.replaceAll("SHELL", "");
        processData = JSONUtils.parseObject(abnormalJson, ProcessData.class);
        Map<String, Object> abnormalTaskRes = processDefinitionService.checkProcessNodeList(processData, abnormalJson);
        Assert.assertEquals(Status.PROCESS_NODE_S_PARAMETER_INVALID, abnormalTaskRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionId() {
        //process definition not exist
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(null);
        Map<String, Object> processDefinitionNullRes = processDefinitionService.getTaskNodeListByDefinitionId(46);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        //process data null
        ProcessDefinition processDefinition = getProcessDefinition();
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> successRes = processDefinitionService.getTaskNodeListByDefinitionId(46);
        Assert.assertEquals(Status.DATA_IS_NOT_VALID, successRes.get(Constants.STATUS));

        //success
        processDefinition.setProcessDefinitionJson(SHELL_JSON);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> dataNotValidRes = processDefinitionService.getTaskNodeListByDefinitionId(46);
        Assert.assertEquals(Status.SUCCESS, dataNotValidRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionIdList() {
        //process definition not exist
        String defineIdList = "46";
        Integer[] idArray = {46};
        Mockito.when(processDefineMapper.queryDefinitionListByIdList(idArray)).thenReturn(null);
        Map<String, Object> processNotExistRes = processDefinitionService.getTaskNodeListByDefinitionIdList(defineIdList);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processNotExistRes.get(Constants.STATUS));

        //process definition exist
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(SHELL_JSON);
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefineMapper.queryDefinitionListByIdList(idArray)).thenReturn(processDefinitionList);
        Map<String, Object> successRes = processDefinitionService.getTaskNodeListByDefinitionIdList(defineIdList);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionAllByProjectId() {
        int projectId = 1;
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(SHELL_JSON);
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefineMapper.queryAllDefinitionList(projectId)).thenReturn(processDefinitionList);
        Map<String, Object> successRes = processDefinitionService.queryProcessDefinitionAllByProjectId(projectId);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testViewTree() throws Exception {
        //process definition not exist
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(SHELL_JSON);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(null);
        Map<String, Object> processDefinitionNullRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        List<ProcessInstance> processInstanceList = new ArrayList<>();
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("test_instance");
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setHost("192.168.xx.xx");
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        processInstanceList.add(processInstance);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setTaskType("SHELL");
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setHost("192.168.xx.xx");

        //task instance not exist
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Mockito.when(processInstanceService.queryByProcessDefineId(46, 10)).thenReturn(processInstanceList);
        Mockito.when(taskInstanceMapper.queryByInstanceIdAndName(processInstance.getId(), "shell-1")).thenReturn(null);
        Map<String, Object> taskNullRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNullRes.get(Constants.STATUS));

        //task instance exist
        Mockito.when(taskInstanceMapper.queryByInstanceIdAndName(processInstance.getId(), "shell-1")).thenReturn(taskInstance);
        Map<String, Object> taskNotNuLLRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));
    }

    @Test
    public void testImportProcessDefinitionById() throws IOException {

        String processJson = "[\n"
                + "    {\n"
                + "        \"projectName\": \"testProject\",\n"
                + "        \"processDefinitionName\": \"shell-4\",\n"
                + "        \"processDefinitionJson\": \"{\\\"tenantId\\\":1"
                + ",\\\"globalParams\\\":[],\\\"tasks\\\":[{\\\"workerGroupId\\\":\\\"3\\\",\\\"description\\\""
                + ":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\",\\\"type\\\":\\\"SHELL\\\",\\\"params\\\":{\\\"rawScript\\\""
                + ":\\\"#!/bin/bash\\\\necho \\\\\\\"shell-4\\\\\\\"\\\",\\\"localParams\\\":[],\\\"resourceList\\\":[]}"
                + ",\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\""
                + ",\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell-4\\\",\\\"dependence\\\":{}"
                + ",\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[],\\\"id\\\":\\\"tasks-84090\\\"}"
                + ",{\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell-5\\\",\\\"workerGroupId\\\""
                + ":\\\"3\\\",\\\"description\\\":\\\"\\\",\\\"dependence\\\":{},\\\"preTasks\\\":[\\\"shell-4\\\"]"
                + ",\\\"id\\\":\\\"tasks-87364\\\",\\\"runFlag\\\":\\\"NORMAL\\\",\\\"type\\\":\\\"SUB_PROCESS\\\""
                + ",\\\"params\\\":{\\\"processDefinitionId\\\":46},\\\"timeout\\\":{\\\"enable\\\":false"
                + ",\\\"strategy\\\":\\\"\\\"}}],\\\"timeout\\\":0}\",\n"
                + "        \"processDefinitionDescription\": \"\",\n"
                + "        \"processDefinitionLocations\": \"{\\\"tasks-84090\\\":{\\\"name\\\":\\\"shell-4\\\""
                + ",\\\"targetarr\\\":\\\"\\\",\\\"x\\\":128,\\\"y\\\":114},\\\"tasks-87364\\\":{\\\"name\\\""
                + ":\\\"shell-5\\\",\\\"targetarr\\\":\\\"tasks-84090\\\",\\\"x\\\":266,\\\"y\\\":115}}\",\n"
                + "        \"processDefinitionConnects\": \"[{\\\"endPointSourceId\\\":\\\"tasks-84090\\\""
                + ",\\\"endPointTargetId\\\":\\\"tasks-87364\\\"}]\"\n"
                + "    }\n"
                + "]";

        String subProcessJson = "{\n"
                + "    \"globalParams\": [\n"
                + "        \n"
                + "    ],\n"
                + "    \"tasks\": [\n"
                + "        {\n"
                + "            \"type\": \"SHELL\",\n"
                + "            \"id\": \"tasks-52423\",\n"
                + "            \"name\": \"shell-5\",\n"
                + "            \"params\": {\n"
                + "                \"resourceList\": [\n"
                + "                    \n"
                + "                ],\n"
                + "                \"localParams\": [\n"
                + "                    \n"
                + "                ],\n"
                + "                \"rawScript\": \"echo \\\"shell-5\\\"\"\n"
                + "            },\n"
                + "            \"description\": \"\",\n"
                + "            \"runFlag\": \"NORMAL\",\n"
                + "            \"dependence\": {\n"
                + "                \n"
                + "            },\n"
                + "            \"maxRetryTimes\": \"0\",\n"
                + "            \"retryInterval\": \"1\",\n"
                + "            \"timeout\": {\n"
                + "                \"strategy\": \"\",\n"
                + "                \"interval\": null,\n"
                + "                \"enable\": false\n"
                + "            },\n"
                + "            \"taskInstancePriority\": \"MEDIUM\",\n"
                + "            \"workerGroupId\": \"3\",\n"
                + "            \"preTasks\": [\n"
                + "                \n"
                + "            ]\n"
                + "        }\n"
                + "    ],\n"
                + "    \"tenantId\": 1,\n"
                + "    \"timeout\": 0\n"
                + "}";

        FileUtils.writeStringToFile(new File("/tmp/task.json"), processJson);

        File file = new File("/tmp/task.json");

        FileInputStream fileInputStream = new FileInputStream("/tmp/task.json");

        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        String currentProjectName = "testProject";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, currentProjectName);

        ProcessDefinition shellDefinition2 = new ProcessDefinition();
        shellDefinition2.setId(46);
        shellDefinition2.setName("shell-5");
        shellDefinition2.setProjectId(2);
        shellDefinition2.setProcessDefinitionJson(subProcessJson);

        Mockito.when(projectMapper.queryByName(currentProjectName)).thenReturn(getProject(currentProjectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, getProject(currentProjectName), currentProjectName)).thenReturn(result);
        Mockito.when(processDefineMapper.queryByDefineId(46)).thenReturn(shellDefinition2);

        Map<String, Object> importProcessResult = processDefinitionService.importProcessDefinition(loginUser, multipartFile, currentProjectName);

        Assert.assertEquals(Status.SUCCESS, importProcessResult.get(Constants.STATUS));

        boolean delete = file.delete();

        Assert.assertTrue(delete);
    }

    @Test
    public void testUpdateProcessDefinition() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        String projectName = "project_test1";
        Project project = getProject(projectName);

        ProcessDefinition processDefinition = getProcessDefinition();

        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processService.findProcessDefineById(1)).thenReturn(processDefinition);
        Mockito.when(processDefinitionVersionService.addProcessDefinitionVersion(processDefinition)).thenReturn(1L);

        String sqlDependentJson = "{\n"
                + "    \"globalParams\": [\n"
                + "        \n"
                + "    ],\n"
                + "    \"tasks\": [\n"
                + "        {\n"
                + "            \"type\": \"SQL\",\n"
                + "            \"id\": \"tasks-27297\",\n"
                + "            \"name\": \"sql\",\n"
                + "            \"params\": {\n"
                + "                \"type\": \"MYSQL\",\n"
                + "                \"datasource\": 1,\n"
                + "                \"sql\": \"select * from test\",\n"
                + "                \"udfs\": \"\",\n"
                + "                \"sqlType\": \"1\",\n"
                + "                \"title\": \"\",\n"
                + "                \"receivers\": \"\",\n"
                + "                \"receiversCc\": \"\",\n"
                + "                \"showType\": \"TABLE\",\n"
                + "                \"localParams\": [\n"
                + "                    \n"
                + "                ],\n"
                + "                \"connParams\": \"\",\n"
                + "                \"preStatements\": [\n"
                + "                    \n"
                + "                ],\n"
                + "                \"postStatements\": [\n"
                + "                    \n"
                + "                ]\n"
                + "            },\n"
                + "            \"description\": \"\",\n"
                + "            \"runFlag\": \"NORMAL\",\n"
                + "            \"dependence\": {\n"
                + "                \n"
                + "            },\n"
                + "            \"maxRetryTimes\": \"0\",\n"
                + "            \"retryInterval\": \"1\",\n"
                + "            \"timeout\": {\n"
                + "                \"strategy\": \"\",\n"
                + "                \"enable\": false\n"
                + "            },\n"
                + "            \"taskInstancePriority\": \"MEDIUM\",\n"
                + "            \"workerGroupId\": -1,\n"
                + "            \"preTasks\": [\n"
                + "                \"dependent\"\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"type\": \"DEPENDENT\",\n"
                + "            \"id\": \"tasks-33787\",\n"
                + "            \"name\": \"dependent\",\n"
                + "            \"params\": {\n"
                + "                \n"
                + "            },\n"
                + "            \"description\": \"\",\n"
                + "            \"runFlag\": \"NORMAL\",\n"
                + "            \"dependence\": {\n"
                + "                \"relation\": \"AND\",\n"
                + "                \"dependTaskList\": [\n"
                + "                    {\n"
                + "                        \"relation\": \"AND\",\n"
                + "                        \"dependItemList\": [\n"
                + "                            {\n"
                + "                                \"projectId\": 2,\n"
                + "                                \"definitionId\": 46,\n"
                + "                                \"depTasks\": \"ALL\",\n"
                + "                                \"cycle\": \"day\",\n"
                + "                                \"dateValue\": \"today\"\n"
                + "                            }\n"
                + "                        ]\n"
                + "                    }\n"
                + "                ]\n"
                + "            },\n"
                + "            \"maxRetryTimes\": \"0\",\n"
                + "            \"retryInterval\": \"1\",\n"
                + "            \"timeout\": {\n"
                + "                \"strategy\": \"\",\n"
                + "                \"enable\": false\n"
                + "            },\n"
                + "            \"taskInstancePriority\": \"MEDIUM\",\n"
                + "            \"workerGroupId\": -1,\n"
                + "            \"preTasks\": [\n"
                + "                \n"
                + "            ]\n"
                + "        }\n"
                + "    ],\n"
                + "    \"tenantId\": 1,\n"
                + "    \"timeout\": 0\n"
                + "}";
        Map<String, Object> updateResult = processDefinitionService.updateProcessDefinition(loginUser, projectName, 1, "test",
                sqlDependentJson, "", "", "");

        Assert.assertEquals(Status.UPDATE_PROCESS_DEFINITION_ERROR, updateResult.get(Constants.STATUS));
    }

    @Test
    public void testBatchExportProcessDefinitionByIds() {
        processDefinitionService.batchExportProcessDefinitionByIds(
                null, null, null, null);

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        String projectName = "project_test1";
        Project project = getProject(projectName);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);

        processDefinitionService.batchExportProcessDefinitionByIds(
                loginUser, projectName, "1", null);
    }

    @Test
    public void testGetResourceIds() throws Exception {
        // set up
        Method testMethod = ReflectionUtils.findMethod(ProcessDefinitionServiceImpl.class, "getResourceIds", ProcessData.class);
        assertThat(testMethod).isNotNull();
        testMethod.setAccessible(true);

        // when processData has empty task, then return empty string
        ProcessData input1 = new ProcessData();
        input1.setTasks(Collections.emptyList());
        String output1 = (String) testMethod.invoke(processDefinitionService, input1);
        assertThat(output1).isEmpty();

        // when task is null, then return empty string
        ProcessData input2 = new ProcessData();
        input2.setTasks(null);
        String output2 = (String) testMethod.invoke(processDefinitionService, input2);
        assertThat(output2).isEmpty();

        // when task type is incorrect mapping, then return empty string
        ProcessData input3 = new ProcessData();
        TaskNode taskNode3 = new TaskNode();
        taskNode3.setType("notExistType");
        input3.setTasks(Collections.singletonList(taskNode3));
        String output3 = (String) testMethod.invoke(processDefinitionService, input3);
        assertThat(output3).isEmpty();

        // when task parameter list is null, then return empty string
        ProcessData input4 = new ProcessData();
        TaskNode taskNode4 = new TaskNode();
        taskNode4.setType("SHELL");
        taskNode4.setParams(null);
        input4.setTasks(Collections.singletonList(taskNode4));
        String output4 = (String) testMethod.invoke(processDefinitionService, input4);
        assertThat(output4).isEmpty();

        // when resource id list is 0 1, then return 0,1
        ProcessData input5 = new ProcessData();
        TaskNode taskNode5 = new TaskNode();
        taskNode5.setType("SHELL");
        ShellParameters shellParameters5 = new ShellParameters();
        ResourceInfo resourceInfo5A = new ResourceInfo();
        resourceInfo5A.setId(0);
        ResourceInfo resourceInfo5B = new ResourceInfo();
        resourceInfo5B.setId(1);
        shellParameters5.setResourceList(Arrays.asList(resourceInfo5A, resourceInfo5B));
        taskNode5.setParams(JSONUtils.toJsonString(shellParameters5));
        input5.setTasks(Collections.singletonList(taskNode5));
        String output5 = (String) testMethod.invoke(processDefinitionService, input5);
        assertThat(output5.split(",")).hasSize(2)
                .containsExactlyInAnyOrder("0", "1");

        // when resource id list is 0 1 1 2, then return 0,1,2
        ProcessData input6 = new ProcessData();
        TaskNode taskNode6 = new TaskNode();
        taskNode6.setType("SHELL");
        ShellParameters shellParameters6 = new ShellParameters();
        ResourceInfo resourceInfo6A = new ResourceInfo();
        resourceInfo6A.setId(0);
        ResourceInfo resourceInfo6B = new ResourceInfo();
        resourceInfo6B.setId(1);
        ResourceInfo resourceInfo6C = new ResourceInfo();
        resourceInfo6C.setId(1);
        ResourceInfo resourceInfo6D = new ResourceInfo();
        resourceInfo6D.setId(2);
        shellParameters6.setResourceList(Arrays.asList(resourceInfo6A, resourceInfo6B, resourceInfo6C, resourceInfo6D));
        taskNode6.setParams(JSONUtils.toJsonString(shellParameters6));
        input6.setTasks(Collections.singletonList(taskNode6));

        String output6 = (String) testMethod.invoke(processDefinitionService, input6);

        assertThat(output6.split(",")).hasSize(3)
                .containsExactlyInAnyOrder("0", "1", "2");
    }

    /**
     * get mock datasource
     *
     * @return DataSource
     */
    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setId(2);
        dataSource.setName("test");
        return dataSource;
    }

    /**
     * get mock processDefinition
     *
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinition() {

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(46);
        processDefinition.setName("test_pdf");
        processDefinition.setProjectId(2);
        processDefinition.setTenantId(1);
        processDefinition.setDescription("");

        return processDefinition;
    }

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    /**
     * get mock Project
     *
     * @param projectId projectId
     * @return Project
     */
    private Project getProjectById(int projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setName("project_test2");
        project.setUserId(1);
        return project;
    }

    /**
     * get mock schedule
     *
     * @return schedule
     */
    private Schedule getSchedule() {
        Date date = new Date();
        Schedule schedule = new Schedule();
        schedule.setId(46);
        schedule.setProcessDefinitionId(1);
        schedule.setStartTime(date);
        schedule.setEndTime(date);
        schedule.setCrontab("0 0 5 * * ? *");
        schedule.setFailureStrategy(FailureStrategy.END);
        schedule.setUserId(1);
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedule.setProcessInstancePriority(Priority.MEDIUM);
        schedule.setWarningType(WarningType.NONE);
        schedule.setWarningGroupId(1);
        schedule.setWorkerGroup(Constants.DEFAULT_WORKER_GROUP);
        return schedule;
    }

    /**
     * get mock processMeta
     *
     * @return processMeta
     */
    private ProcessMeta getProcessMeta() {
        ProcessMeta processMeta = new ProcessMeta();
        Schedule schedule = getSchedule();
        processMeta.setScheduleCrontab(schedule.getCrontab());
        processMeta.setScheduleStartTime(DateUtils.dateToString(schedule.getStartTime()));
        processMeta.setScheduleEndTime(DateUtils.dateToString(schedule.getEndTime()));
        processMeta.setScheduleWarningType(String.valueOf(schedule.getWarningType()));
        processMeta.setScheduleWarningGroupId(schedule.getWarningGroupId());
        processMeta.setScheduleFailureStrategy(String.valueOf(schedule.getFailureStrategy()));
        processMeta.setScheduleReleaseState(String.valueOf(schedule.getReleaseState()));
        processMeta.setScheduleProcessInstancePriority(String.valueOf(schedule.getProcessInstancePriority()));
        processMeta.setScheduleWorkerGroupName("workgroup1");
        return processMeta;
    }

    private List<Schedule> getSchedulerList() {
        List<Schedule> scheduleList = new ArrayList<>();
        scheduleList.add(getSchedule());
        return scheduleList;
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }
}
