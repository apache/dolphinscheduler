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

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.dto.ProcessMeta;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@RunWith(MockitoJUnitRunner.Silent.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class ProcessDefinitionServiceTest {

    @InjectMocks
    ProcessDefinitionService processDefinitionService;

    @Mock
    private DataSourceMapper dataSourceMapper;

    @Mock
    private ProcessDefinitionMapper processDefineMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private ScheduleMapper scheduleMapper;



    @Mock
    private ProcessService processService;

    @Mock
    private ProcessInstanceMapper processInstanceMapper;

    @Mock
    private TaskInstanceMapper taskInstanceMapper;

    private String sqlDependentJson = "{\"globalParams\":[],"
            + "\"tasks\":[{\"type\":\"SQL\",\"id\":\"tasks-27297\",\"name\":\"sql\","
            + "\"params\":{\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select * from test\","
            + "\"udfs\":\"\",\"sqlType\":\"1\",\"title\":\"\",\"receivers\":\"\",\"receiversCc\":\"\",\"showType\":\"TABLE\""
            + ",\"localParams\":[],\"connParams\":\"\","
            + "\"preStatements\":[],\"postStatements\":[]},"
            + "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\","
            + "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\","
            + "\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,"
            + "\"preTasks\":[\"dependent\"]},{\"type\":\"DEPENDENT\",\"id\":\"tasks-33787\","
            + "\"name\":\"dependent\",\"params\":{},\"description\":\"\",\"runFlag\":\"NORMAL\","
            + "\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\","
            + "\"dependItemList\":[{\"projectId\":2,\"definitionId\":46,\"depTasks\":\"ALL\","
            + "\"cycle\":\"day\",\"dateValue\":\"today\"}]}]},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\","
            + "\"timeout\":{\"strategy\":\"\",\"enable\":false},\"taskInstancePriority\":\"MEDIUM\","
            + "\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

    private String shellJson = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-9527\",\"name\":\"shell-1\","
            + "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"},"
            + "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\","
            + "\"timeout\":{\"strategy\":\"\",\"interval\":1,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\","
            + "\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

    @Test
    public void testQueryProcessDefinitionList() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionList(loginUser,"project_test1");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        List<ProcessDefinition> resourceList = new ArrayList<>();
        resourceList.add(getProcessDefinition());
        Mockito.when(processDefineMapper.queryAllDefinitionList(project.getId())).thenReturn(resourceList);
        Map<String, Object> checkSuccessRes = processDefinitionService.queryProcessDefinitionList(loginUser,"project_test1");
        Assert.assertEquals(Status.SUCCESS, checkSuccessRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionListPaging() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionListPaging(loginUser, "project_test1", "",1, 5,0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

    }

    @Test
    public void testQueryProcessDefinitionById() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project check auth fail
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionById(loginUser,
                "project_test1", 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
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
    public void testCopyProcessDefinition()  throws Exception {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>(5);
        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);

        ProcessDefinition definition = getProcessDefinition();
        definition.setLocations("{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}");
        definition.setProcessDefinitionJson("{\"globalParams\":[],"
                + "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\":\"ssh_test1\","
                + "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},"
                + "\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}");
        definition.setConnects("[]");
        //instance exit
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(definition);

        Map<String, Object> createProcessResult = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);

        Mockito.when(processDefinitionService.createProcessDefinition(
                loginUser,
                definition.getProjectName(),
                definition.getName(),
                definition.getProcessDefinitionJson(),
                definition.getDescription(),
                definition.getLocations(),
                definition.getConnects())).thenReturn(createProcessResult);

        Map<String, Object> successRes = processDefinitionService.copyProcessDefinition(loginUser,
                "project_test1", 46);

        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void deleteProcessDefinitionByIdTest() throws Exception {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.deleteProcessDefinitionById(loginUser, "project_test1", 6);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
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
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.releaseProcessDefinition(loginUser, "project_test1",
                6, ReleaseState.OFFLINE.getCode());
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, processs definition online
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(getProcessDefinition());
        Mockito.when(processDefineMapper.updateById(getProcessDefinition())).thenReturn(1);
        Map<String, Object> onlineRes = processDefinitionService.releaseProcessDefinition(loginUser, "project_test1",
                46, ReleaseState.ONLINE.getCode());
        Assert.assertEquals(Status.SUCCESS, onlineRes.get(Constants.STATUS));

        //release error code
        Map<String, Object> failRes = processDefinitionService.releaseProcessDefinition(loginUser, "project_test1",
                46, 2);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failRes.get(Constants.STATUS));

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
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, process not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(processDefineMapper.verifyByDefineName(project.getId(),"test_pdf")).thenReturn(null);
        Map<String, Object> processNotExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.SUCCESS, processNotExistRes.get(Constants.STATUS));

        //process exist
        Mockito.when(processDefineMapper.verifyByDefineName(project.getId(),"test_pdf")).thenReturn(getProcessDefinition());
        Map<String, Object> processExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR, processExistRes.get(Constants.STATUS));
    }

    @Test
    public void testCheckProcessNodeList() {

        Map<String, Object> dataNotValidRes = processDefinitionService.checkProcessNodeList(null, "");
        Assert.assertEquals(Status.DATA_IS_NOT_VALID, dataNotValidRes.get(Constants.STATUS));

        //task not empty
        String processDefinitionJson = shellJson;
        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        assert processData != null;
        Map<String, Object> taskEmptyRes = processDefinitionService.checkProcessNodeList(processData, processDefinitionJson);
        Assert.assertEquals(Status.SUCCESS, taskEmptyRes.get(Constants.STATUS));

        //task empty
        processData.setTasks(null);
        Map<String, Object> taskNotEmptyRes = processDefinitionService.checkProcessNodeList(processData, processDefinitionJson);
        Assert.assertEquals(Status.DATA_IS_NULL, taskNotEmptyRes.get(Constants.STATUS));

        //json abnormal
        String abnormalJson = processDefinitionJson.replaceAll("SHELL","");
        processData = JSONUtils.parseObject(abnormalJson, ProcessData.class);
        Map<String, Object> abnormalTaskRes = processDefinitionService.checkProcessNodeList(processData, abnormalJson);
        Assert.assertEquals(Status.PROCESS_NODE_S_PARAMETER_INVALID, abnormalTaskRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionId() throws Exception {
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
        processDefinition.setProcessDefinitionJson(shellJson);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> dataNotValidRes = processDefinitionService.getTaskNodeListByDefinitionId(46);
        Assert.assertEquals(Status.SUCCESS, dataNotValidRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionIdList() throws Exception {
        //process definition not exist
        String defineIdList = "46";
        Integer[] idArray = {46};
        Mockito.when(processDefineMapper.queryDefinitionListByIdList(idArray)).thenReturn(null);
        Map<String, Object> processNotExistRes = processDefinitionService.getTaskNodeListByDefinitionIdList(defineIdList);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processNotExistRes.get(Constants.STATUS));

        //process definition exist
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(shellJson);
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
        processDefinition.setProcessDefinitionJson(shellJson);
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
        processDefinition.setProcessDefinitionJson(shellJson);
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(null);
        Map<String, Object> processDefinitionNullRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        List<ProcessInstance> processInstanceList = new ArrayList<>();
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("test_instance");
        processInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
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
        taskInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
        taskInstance.setHost("192.168.xx.xx");

        //task instance not exist
        Mockito.when(processDefineMapper.selectById(46)).thenReturn(processDefinition);
        Mockito.when(processInstanceMapper.queryByProcessDefineId(46, 10)).thenReturn(processInstanceList);
        Mockito.when(taskInstanceMapper.queryByInstanceIdAndName(processInstance.getId(), "shell-1")).thenReturn(null);
        Map<String, Object> taskNullRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNullRes.get(Constants.STATUS));

        //task instance exist
        Mockito.when(taskInstanceMapper.queryByInstanceIdAndName(processInstance.getId(), "shell-1")).thenReturn(taskInstance);
        Map<String, Object> taskNotNuLLRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));
    }

    /**
     * add datasource param and dependent when export process
     * @throws JSONException
     */
    @Test
    public void testAddTaskNodeSpecialParam() throws JSONException {

        Mockito.when(dataSourceMapper.selectById(1)).thenReturn(getDataSource());
        Mockito.when(processDefineMapper.queryByDefineId(2)).thenReturn(getProcessDefinition());

        String corSqlDependentJson = processDefinitionService.addExportTaskNodeSpecialParam(sqlDependentJson);

        JSONAssert.assertEquals(sqlDependentJson,corSqlDependentJson,false);

    }

    @Test
    public void testExportProcessMetaDataStr() {
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(getSchedulerList());

        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(sqlDependentJson);

        String exportProcessMetaDataStr = processDefinitionService.exportProcessMetaDataStr(46, processDefinition);
        Assert.assertNotEquals(sqlDependentJson,exportProcessMetaDataStr);
    }

    @Test
    public void testAddExportTaskNodeSpecialParam() throws JSONException {
        String shellData = shellJson;

        String resultStr = processDefinitionService.addExportTaskNodeSpecialParam(shellData);
        JSONAssert.assertEquals(shellJson, resultStr, false);
    }

    @Test
    public void testImportProcessSchedule() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);

        String currentProjectName = "test";
        String processDefinitionName = "test_process";
        Integer processDefinitionId = 1;
        Schedule schedule = getSchedule();

        ProcessMeta processMeta = getProcessMeta();

        int insertFlag = processDefinitionService.importProcessSchedule(loginUser, currentProjectName, processMeta,
                processDefinitionName, processDefinitionId);
        Assert.assertEquals(0, insertFlag);

        ProcessMeta processMetaCron = new ProcessMeta();
        processMetaCron.setScheduleCrontab(schedule.getCrontab());

        int insertFlagCron = processDefinitionService.importProcessSchedule(loginUser, currentProjectName, processMetaCron,
                processDefinitionName, processDefinitionId);
        Assert.assertEquals(0, insertFlagCron);

        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName("ds-test-workergroup");
        List<WorkerGroup> workerGroups = new ArrayList<>();
        workerGroups.add(workerGroup);

        processMetaCron.setScheduleWorkerGroupName("ds-test");
        int insertFlagWorker = processDefinitionService.importProcessSchedule(loginUser, currentProjectName, processMetaCron,
                processDefinitionName, processDefinitionId);
        Assert.assertEquals(0, insertFlagWorker);

        int workerNullFlag = processDefinitionService.importProcessSchedule(loginUser, currentProjectName, processMetaCron,
                processDefinitionName, processDefinitionId);
        Assert.assertEquals(0, workerNullFlag);

    }

    /**
     * import sub process test
     */
    @Test
    public void testImportSubProcess() {

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        Project testProject = getProject("test");

        //Recursive subprocess sub2 process in sub1 process and sub1process in top process
        String topProcessJson = "{\"globalParams\":[],"
                + "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-38634\",\"name\":\"shell1\","
                + "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"},"
                + "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\","
                + "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]},"
                + "{\"type\":\"SUB_PROCESS\",\"id\":\"tasks-44207\",\"name\":\"shell-4\","
                + "\"params\":{\"processDefinitionId\":39},\"description\":\"\",\"runFlag\":\"NORMAL\","
                + "\"dependence\":{},\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,"
                + "\"preTasks\":[\"shell1\"]}],\"tenantId\":1,\"timeout\":0}";

        String sub1ProcessJson = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-84090\","
                + "\"name\":\"shell-4\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-4\\\"\"},"
                + "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\","
                + "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]},{\"type\":\"SUB_PROCESS\","
                + "\"id\":\"tasks-87364\",\"name\":\"shell-5\","
                + "\"params\":{\"processDefinitionId\":46},\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},"
                + "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\","
                + "\"workerGroupId\":-1,\"preTasks\":[\"shell-4\"]}],\"tenantId\":1,\"timeout\":0}";

        String sub2ProcessJson = "{\"globalParams\":[],"
                + "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-52423\",\"name\":\"shell-5\","
                + "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo \\\"shell-5\\\"\"},\"description\":\"\","
                + "\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\","
                + "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,"
                + "\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

        JSONObject jsonObject = JSONUtils.parseObject(topProcessJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("tasks");

        String originSubJson = jsonArray.toString();

        Map<Integer, Integer> subProcessIdMap = new HashMap<>(20);

        ProcessDefinition shellDefinition1 = new ProcessDefinition();
        shellDefinition1.setId(39);
        shellDefinition1.setName("shell-4");
        shellDefinition1.setProjectId(2);
        shellDefinition1.setProcessDefinitionJson(sub1ProcessJson);

        ProcessDefinition shellDefinition2 = new ProcessDefinition();
        shellDefinition2.setId(46);
        shellDefinition2.setName("shell-5");
        shellDefinition2.setProjectId(2);
        shellDefinition2.setProcessDefinitionJson(sub2ProcessJson);

        Mockito.when(processDefineMapper.queryByDefineId(39)).thenReturn(shellDefinition1);
        Mockito.when(processDefineMapper.queryByDefineId(46)).thenReturn(shellDefinition2);
        Mockito.when(processDefineMapper.queryByDefineName(testProject.getId(), "shell-5")).thenReturn(null);
        Mockito.when(processDefineMapper.queryByDefineName(testProject.getId(), "shell-4")).thenReturn(null);
        Mockito.when(processDefineMapper.queryByDefineName(testProject.getId(), "testProject")).thenReturn(shellDefinition2);

        processDefinitionService.importSubProcess(loginUser,testProject, jsonArray, subProcessIdMap);

        String correctSubJson = jsonArray.toString();

        Assert.assertEquals(originSubJson, correctSubJson);

    }

    @Test
    public void testImportProcessDefinitionById() throws IOException {

        String processJson = "[{\"projectName\":\"testProject\",\"processDefinitionName\":\"shell-4\","
                + "\"processDefinitionJson\":\"{\\\"tenantId\\\":1,\\\"globalParams\\\":[],"
                + "\\\"tasks\\\":[{\\\"workerGroupId\\\":\\\"default\\\",\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\","
                + "\\\"type\\\":\\\"SHELL\\\",\\\"params\\\":{\\\"rawScript\\\":\\\"#!/bin/bash\\\\necho \\\\\\\"shell-4\\\\\\\"\\\","
                + "\\\"localParams\\\":[],\\\"resourceList\\\":[]},\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},"
                + "\\\"maxRetryTimes\\\":\\\"0\\\",\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell-4\\\","
                + "\\\"dependence\\\":{},\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[],\\\"id\\\":\\\"tasks-84090\\\"},"
                + "{\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell-5\\\",\\\"workerGroupId\\\":\\\"default\\\\,"
                + "\\\"description\\\":\\\"\\\",\\\"dependence\\\":{},\\\"preTasks\\\":[\\\"shell-4\\\"],\\\"id\\\":\\\"tasks-87364\\\","
                + "\\\"runFlag\\\":\\\"NORMAL\\\",\\\"type\\\":\\\"SUB_PROCESS\\\",\\\"params\\\":{\\\"processDefinitionId\\\":46},"
                + "\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}}],\\\"timeout\\\":0}\","
                + "\"processDefinitionDescription\":\"\",\"processDefinitionLocations\":\"{\\\"tasks-84090\\\":{\\\"name\\\":\\\"shell-4\\\","
                + "\\\"targetarr\\\":\\\"\\\",\\\"x\\\":128,\\\"y\\\":114},\\\"tasks-87364\\\":{\\\"name\\\":\\\"shell-5\\\","
                + "\\\"targetarr\\\":\\\"tasks-84090\\\",\\\"x\\\":266,\\\"y\\\":115}}\","
                + "\"processDefinitionConnects\":\"[{\\\"endPointSourceId\\\":\\\"tasks-84090\\\","
                + "\\\"endPointTargetId\\\":\\\"tasks-87364\\\"}]\"}]";

        String subProcessJson = "{\"globalParams\":[],"
                + "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-52423\",\"name\":\"shell-5\","
                + "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo \\\"shell-5\\\"\"},\"description\":\"\","
                + "\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\","
                + "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":\\\"default\\\\,"
                + "\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

        FileUtils.writeStringToFile(new File("/tmp/task.json"),processJson);

        File file = new File("/tmp/task.json");

        FileInputStream fileInputStream = new FileInputStream("/tmp/task.json");

        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        String currentProjectName = "testProject";
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS, currentProjectName);

        ProcessDefinition shellDefinition2 = new ProcessDefinition();
        shellDefinition2.setId(46);
        shellDefinition2.setName("shell-5");
        shellDefinition2.setProjectId(2);
        shellDefinition2.setProcessDefinitionJson(subProcessJson);

        Mockito.when(projectMapper.queryByName(currentProjectName)).thenReturn(getProject(currentProjectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, getProject(currentProjectName), currentProjectName)).thenReturn(result);
        Mockito.when(processDefineMapper.queryByDefineId(46)).thenReturn(shellDefinition2);

    }

    /**
     * check import process metadata
     * @param file file
     * @param loginUser login user
     * @param currentProjectName current project name
     * @param processMetaJson process meta json
     * @throws IOException IO exception
     */
    private void improssProcessCheckData(File file, User loginUser, String currentProjectName, String processMetaJson) throws IOException {
        //check null
        FileUtils.writeStringToFile(new File("/tmp/task.json"),processMetaJson);

        File fileEmpty = new File("/tmp/task.json");

        FileInputStream fileEmptyInputStream = new FileInputStream("/tmp/task.json");

        MultipartFile multiFileEmpty = new MockMultipartFile(fileEmpty.getName(), fileEmpty.getName(),
                ContentType.APPLICATION_OCTET_STREAM.toString(), fileEmptyInputStream);

        Map<String, Object> resEmptyProcess = processDefinitionService.importProcessDefinition(loginUser, multiFileEmpty, currentProjectName);

        Assert.assertEquals(Status.DATA_IS_NULL, resEmptyProcess.get(Constants.STATUS));

        boolean deleteFlag = file.delete();

        Assert.assertTrue(deleteFlag);
    }

    @Test
    public void testUpdateProcessDefinition () {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);

        String projectName = "project_test1";
        Project project = getProject(projectName);

        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processService.findProcessDefineById(1)).thenReturn(getProcessDefinition());

        Map<String, Object> updateResult = processDefinitionService.updateProcessDefinition(loginUser, projectName, 1, "test",
                sqlDependentJson, "", "", "");

        Assert.assertEquals(Status.UPDATE_PROCESS_DEFINITION_ERROR, updateResult.get(Constants.STATUS));
    }

    /**
     * get mock datasource
     * @return DataSource
     */
    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setId(2);
        dataSource.setName("test");
        return  dataSource;
    }

    /**
     * get mock processDefinition
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinition() {

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(46);
        processDefinition.setName("test_pdf");
        processDefinition.setProjectId(2);
        processDefinition.setTenantId(1);
        processDefinition.setDescription("");

        return  processDefinition;
    }

    /**
     * get mock Project
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return  project;
    }

    /**
     * get mock schedule
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