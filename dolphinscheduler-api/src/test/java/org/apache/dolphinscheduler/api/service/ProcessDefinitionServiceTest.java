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

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

/**
 * process definition service test
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessDefinitionServiceTest {

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
    @InjectMocks
    private ProcessDefinitionServiceImpl processDefinitionService;
    @Mock
    private ProcessDefinitionMapper processDefineMapper;
    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;
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

    @Test
    public void testQueryProcessDefinitionList() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        List<ProcessDefinition> resourceList = new ArrayList<>();
        resourceList.add(getProcessDefinition());
        Mockito.when(processDefineMapper.queryAllDefinitionList(project.getCode())).thenReturn(resourceList);
        Map<String, Object> checkSuccessRes = processDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
        Assert.assertEquals(Status.SUCCESS, checkSuccessRes.get(Constants.STATUS));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryProcessDefinitionListPaging() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionListPaging(loginUser, projectCode, "", 1, 5, 0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        loginUser.setId(1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Page<ProcessDefinition> page = new Page<>(1, 10);
        page.setTotal(30);
        Mockito.when(processDefineMapper.queryDefineListPaging(
                Mockito.any(IPage.class)
                , Mockito.eq("")
                , Mockito.eq(loginUser.getId())
                , Mockito.eq(project.getCode())
                , Mockito.anyBoolean())).thenReturn(page);

        Map<String, Object> map1 = processDefinitionService.queryProcessDefinitionListPaging(
                loginUser, 1L, "", 1, 10, loginUser.getId());

        Assert.assertEquals(Status.SUCCESS, map1.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionByCode() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);

        //project check auth fail
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionByCode(loginUser, 1L, 1L);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        DagData dagData = new DagData(getProcessDefinition(), null, null);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(dagData);

        Map<String, Object> instanceNotexitRes = processDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, 1L);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        //instance exit
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(getProcessDefinition());
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> successRes = processDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionByName() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);

        //project check auth fail
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, "test_def");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Mockito.when(processDefineMapper.queryByDefineName(project.getCode(), "test_def")).thenReturn(null);

        Map<String, Object> instanceNotExitRes = processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, "test_def");
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotExitRes.get(Constants.STATUS));

        //instance exit
        Mockito.when(processDefineMapper.queryByDefineName(project.getCode(), "test")).thenReturn(getProcessDefinition());
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> successRes = processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, "test");
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testBatchCopyProcessDefinition() {
        long projectCode = 1L;
        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);

        // copy project definition ids empty test
        Map<String, Object> map = processDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, StringUtils.EMPTY, 2L);
        Assert.assertEquals(Status.PROCESS_DEFINITION_CODES_IS_EMPTY, map.get(Constants.STATUS));


        // project check auth fail
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> map1 = processDefinitionService.batchCopyProcessDefinition(
                loginUser, projectCode, String.valueOf(project.getId()), 2L);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map1.get(Constants.STATUS));

        // project check auth success, target project name not equal project name, check auth target project fail
        projectCode = 2L;
        Project project1 = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);

        putMsg(result, Status.SUCCESS, projectCode);
        ProcessDefinition definition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(definition);
        Set<Long> definitionCodes = Arrays.stream("46".split(Constants.COMMA)).map(Long::parseLong).collect(Collectors.toSet());
        Mockito.when(processDefineMapper.queryByCodes(definitionCodes)).thenReturn(processDefinitionList);

        Map<String, Object> map3 = processDefinitionService.batchCopyProcessDefinition(
                loginUser, projectCode, "46", 1L);
        Assert.assertEquals(Status.COPY_PROCESS_DEFINITION_ERROR, map3.get(Constants.STATUS));
    }

    @Test
    public void testBatchMoveProcessDefinition() {
        long projectCode = 1L;
        Project project1 = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project1);

        long projectCode2 = 2L;
        Project project2 = getProject(projectCode2);
        Mockito.when(projectMapper.queryByCode(projectCode2)).thenReturn(project2);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);

        Mockito.when(projectService.checkProjectAndAuth(loginUser, project1, project1.getName())).thenReturn(result);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project2, project2.getName())).thenReturn(result);

        ProcessDefinition definition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(definition);
        Set<Long> definitionCodes = Arrays.stream("46".split(Constants.COMMA)).map(Long::parseLong).collect(Collectors.toSet());
        Mockito.when(processDefineMapper.queryByCodes(definitionCodes)).thenReturn(processDefinitionList);
        Mockito.when(processTaskRelationMapper.queryByProcessCode(projectCode, 46L)).thenReturn(getProcessTaskRelation(projectCode, 46L));
        putMsg(result, Status.SUCCESS);

        Map<String, Object> successRes = processDefinitionService.batchMoveProcessDefinition(
                loginUser, projectCode, "46", projectCode2);
        Assert.assertEquals(Status.MOVE_PROCESS_DEFINITION_ERROR, successRes.get(Constants.STATUS));
    }

    @Test
    public void deleteProcessDefinitionByIdTest() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> map = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, 6);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Mockito.when(processDefineMapper.selectById(1)).thenReturn(null);
        Map<String, Object> instanceNotExitRes = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, 1);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotExitRes.get(Constants.STATUS));

        ProcessDefinition processDefinition = getProcessDefinition();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        //user no auth
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> userNoAuthRes = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, 46);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, userNoAuthRes.get(Constants.STATUS));

        //process definition online
        loginUser.setUserType(UserType.ADMIN_USER);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> dfOnlineRes = processDefinitionService.deleteProcessDefinitionById(loginUser,projectCode, 46);
        Assert.assertEquals(Status.PROCESS_DEFINE_STATE_ONLINE, dfOnlineRes.get(Constants.STATUS));

        //scheduler list elements > 1
        processDefinition.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        List<Schedule> schedules = new ArrayList<>();
        schedules.add(getSchedule());
        schedules.add(getSchedule());
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Map<String, Object> schedulerGreaterThanOneRes = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, 46);
        Assert.assertEquals(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR, schedulerGreaterThanOneRes.get(Constants.STATUS));

        //scheduler online
        schedules.clear();
        Schedule schedule = getSchedule();
        schedule.setReleaseState(ReleaseState.ONLINE);
        schedules.add(schedule);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Map<String, Object> schedulerOnlineRes = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, 46);
        Assert.assertEquals(Status.SCHEDULE_CRON_STATE_ONLINE, schedulerOnlineRes.get(Constants.STATUS));

        //delete fail
        schedules.clear();
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedules.add(schedule);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Mockito.when(processDefineMapper.deleteById(46)).thenReturn(0);
        Map<String, Object> deleteFail = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, 46);
        Assert.assertEquals(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR, deleteFail.get(Constants.STATUS));

        //delete success
        Mockito.when(processDefineMapper.deleteById(46)).thenReturn(1);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> deleteSuccess = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, 46);
        Assert.assertEquals(Status.SUCCESS, deleteSuccess.get(Constants.STATUS));
    }

    @Test
    public void testReleaseProcessDefinition() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> map = processDefinitionService.releaseProcessDefinition(loginUser, projectCode,
                6, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        // project check auth success, processs definition online
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(getProcessDefinition());
        Map<String, Object> onlineRes = processDefinitionService.releaseProcessDefinition(
                loginUser, projectCode, 46, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SUCCESS, onlineRes.get(Constants.STATUS));

        // project check auth success, processs definition online
        ProcessDefinition processDefinition1 = getProcessDefinition();
        processDefinition1.setResourceIds("1,2");
        Map<String, Object> onlineWithResourceRes = processDefinitionService.releaseProcessDefinition(
                loginUser, projectCode, 46, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SUCCESS, onlineWithResourceRes.get(Constants.STATUS));

        // release error code
        Map<String, Object> failRes = processDefinitionService.releaseProcessDefinition(
                loginUser, projectCode, 46, ReleaseState.getEnum(2));
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failRes.get(Constants.STATUS));

    }

    @Test
    public void testVerifyProcessDefinitionName() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        Map<String, Object> map = processDefinitionService.verifyProcessDefinitionName(loginUser,
                projectCode, "test_pdf");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, process not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processDefineMapper.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(null);
        Map<String, Object> processNotExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                projectCode, "test_pdf");
        Assert.assertEquals(Status.SUCCESS, processNotExistRes.get(Constants.STATUS));

        //process exist
        Mockito.when(processDefineMapper.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(getProcessDefinition());
        Map<String, Object> processExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                projectCode, "test_pdf");
        Assert.assertEquals(Status.PROCESS_DEFINITION_NAME_EXIST, processExistRes.get(Constants.STATUS));
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
        Assert.assertEquals(Status.PROCESS_DAG_IS_EMPTY, taskNotEmptyRes.get(Constants.STATUS));

        // task cycle
        String processDefinitionJsonCycle = CYCLE_SHELL_JSON;
        ProcessData processDataCycle = JSONUtils.parseObject(processDefinitionJsonCycle, ProcessData.class);
        Map<String, Object> taskCycleRes = processDefinitionService.checkProcessNodeList(processDataCycle, processDefinitionJsonCycle);
        Assert.assertEquals(Status.PROCESS_NODE_HAS_CYCLE, taskCycleRes.get(Constants.STATUS));

        //json abnormal
        String abnormalJson = processDefinitionJson.replaceAll(TaskType.SHELL.getDesc(), "");
        processData = JSONUtils.parseObject(abnormalJson, ProcessData.class);
        Map<String, Object> abnormalTaskRes = processDefinitionService.checkProcessNodeList(processData, abnormalJson);
        Assert.assertEquals(Status.PROCESS_NODE_S_PARAMETER_INVALID, abnormalTaskRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionCode() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        //process definition not exist
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(null);
        Map<String, Object> processDefinitionNullRes = processDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        //success
        ProcessDefinition processDefinition = getProcessDefinition();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(new DagData(processDefinition, null, null));
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        Map<String, Object> dataNotValidRes = processDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.SUCCESS, dataNotValidRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionCodes() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        //process definition not exist
        String defineCodes = "46";
        Set<Long> defineCodeSet = Lists.newArrayList(defineCodes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toSet());
        Mockito.when(processDefineMapper.queryByCodes(defineCodeSet)).thenReturn(null);
        Map<String, Object> processNotExistRes = processDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, defineCodes);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processNotExistRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        ProcessDefinition processDefinition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefineMapper.queryByCodes(defineCodeSet)).thenReturn(processDefinitionList);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(new DagData(processDefinition, null, null));

        Map<String, Object> successRes = processDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, defineCodes);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    private ProcessData getProcessData() {
        ProcessData processData = new ProcessData();
        List<TaskNode> taskNodeList = new ArrayList<>();
        processData.setTasks(taskNodeList);
        List<Property> properties = new ArrayList<>();
        processData.setGlobalParams(properties);
        processData.setTenantId(10);
        processData.setTimeout(100);
        return processData;
    }

    @Test
    public void testQueryAllProcessDefinitionByProjectCode() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = new HashMap<>();
        long projectCode = 2L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);
        ProcessDefinition processDefinition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefineMapper.queryAllDefinitionList(projectCode)).thenReturn(processDefinitionList);
        Map<String, Object> successRes = processDefinitionService.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testViewTree() {
        //process definition not exist
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(SHELL_JSON);
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
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setHost("192.168.xx.xx");

        //task instance not exist
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        Mockito.when(processService.genDagGraph(processDefinition)).thenReturn(new DAG<>());
        Map<String, Object> taskNullRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNullRes.get(Constants.STATUS));

        //task instance exist
        Map<String, Object> taskNotNuLLRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));

    }

    @Test
    public void testSubProcessViewTree() {

        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(SHELL_JSON);
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
        taskInstance.setTaskType(TaskType.SUB_PROCESS.getDesc());
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setHost("192.168.xx.xx");
        taskInstance.setTaskParams("\"processDefinitionId\": \"222\",\n");
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        Mockito.when(processService.genDagGraph(processDefinition)).thenReturn(new DAG<>());
        Map<String, Object> taskNotNuLLRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));

    }

    @Test
    public void testUpdateProcessDefinition() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        long projectCode = 1L;
        Project project = getProject(projectCode);

        ProcessDefinition processDefinition = getProcessDefinition();

        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);

        String taskRelationJson = "[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":123456789,"
                + "\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":123456789,"
                + "\"preTaskVersion\":1,\"postTaskCode\":123451234,\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":{}}]";
        Map<String, Object> updateResult = processDefinitionService.updateProcessDefinition(loginUser, projectCode, "test", 1,
                "", "", "", "", 0, "root", taskRelationJson);

        Assert.assertEquals(Status.DATA_IS_NOT_VALID, updateResult.get(Constants.STATUS));
    }

    @Test
    public void testBatchExportProcessDefinitionByCodes() throws IOException {
        processDefinitionService.batchExportProcessDefinitionByCodes(
                null, 1L, null, null);

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        long projectCode = 1L;
        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);

        processDefinitionService.batchExportProcessDefinitionByCodes(
                loginUser, projectCode, "1", null);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        Map<String, Object> checkResult = new HashMap<>();
        checkResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(checkResult);
        HttpServletResponse response = mock(HttpServletResponse.class);

        DagData dagData = new DagData(getProcessDefinition(), null, null);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(dagData);
        processDefinitionService.batchExportProcessDefinitionByCodes(
                loginUser, projectCode, "1", response);
        Assert.assertNotNull(processDefinitionService.exportProcessDagData(processDefinition));
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
        processDefinition.setProjectCode(1L);
        processDefinition.setName("test_pdf");
        processDefinition.setProjectId(2);
        processDefinition.setTenantId(1);
        processDefinition.setDescription("");
        processDefinition.setCode(46L);

        return processDefinition;
    }

    /**
     * get mock Project
     *
     * @param projectCode projectCode
     * @return Project
     */
    private Project getProject(long projectCode) {
        Project project = new Project();
        project.setCode(projectCode);
        project.setId(1);
        project.setName("test");
        project.setUserId(1);
        return project;
    }

    private List<ProcessTaskRelation> getProcessTaskRelation(long projectCode, long processCode) {
        List<ProcessTaskRelation> processTaskRelations = new ArrayList<>();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(projectCode);
        processTaskRelation.setProcessDefinitionCode(processCode);
        processTaskRelation.setProcessDefinitionVersion(1);
        processTaskRelations.add(processTaskRelation);
        return processTaskRelations;
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
