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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.dto.ProcessMeta;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

@RunWith(MockitoJUnitRunner.Silent.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class ProcessDefinitionServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionServiceTest.class);

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
    private WorkerGroupMapper workerGroupMapper;

    private String sqlDependentJson = "{\"globalParams\":[]," +
            "\"tasks\":[{\"type\":\"SQL\",\"id\":\"tasks-27297\",\"name\":\"sql\"," +
            "\"params\":{\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select * from test\"," +
            "\"udfs\":\"\",\"sqlType\":\"1\",\"title\":\"\",\"receivers\":\"\",\"receiversCc\":\"\",\"showType\":\"TABLE\"" +
            ",\"localParams\":[],\"connParams\":\"\"," +
            "\"preStatements\":[],\"postStatements\":[]}," +
            "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\"," +
            "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\"," +
            "\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1," +
            "\"preTasks\":[\"dependent\"]},{\"type\":\"DEPENDENT\",\"id\":\"tasks-33787\"," +
            "\"name\":\"dependent\",\"params\":{},\"description\":\"\",\"runFlag\":\"NORMAL\"," +
            "\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\"," +
            "\"dependItemList\":[{\"projectId\":2,\"definitionId\":46,\"depTasks\":\"ALL\"," +
            "\"cycle\":\"day\",\"dateValue\":\"today\"}]}]},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\"," +
            "\"timeout\":{\"strategy\":\"\",\"enable\":false},\"taskInstancePriority\":\"MEDIUM\"," +
            "\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

    @Test
    public void queryProccessDefinitionList() throws Exception {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);

        Map<String, Object> map = processDefinitionService.queryProccessDefinitionList(loginUser,"project_test1");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));
        logger.info(JSON.toJSONString(map));
    }

    @Test
    public void queryProcessDefinitionListPagingTest() throws Exception {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);

        Map<String, Object> map = processDefinitionService.queryProcessDefinitionListPaging(loginUser, "project_test1", "",1, 5,0);

        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));
        logger.info(JSON.toJSONString(map));
    }

    @Test
    public void deleteProcessDefinitionByIdTest() throws Exception {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);

        Map<String, Object> map = processDefinitionService.deleteProcessDefinitionById(loginUser, "project_test1", 6);

        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));
        logger.info(JSON.toJSONString(map));
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
        Mockito.when(workerGroupMapper.selectById(-1)).thenReturn(null);

        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(sqlDependentJson);

        String exportProcessMetaDataStr = processDefinitionService.exportProcessMetaDataStr(46, processDefinition);
        Assert.assertNotEquals(sqlDependentJson,exportProcessMetaDataStr);
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
        workerGroup.setId(2);
        List<WorkerGroup> workerGroups = new ArrayList<>();
        workerGroups.add(workerGroup);
        Mockito.when(workerGroupMapper.queryWorkerGroupByName("ds-test")).thenReturn(workerGroups);

        processMetaCron.setScheduleWorkerGroupName("ds-test");
        int insertFlagWorker = processDefinitionService.importProcessSchedule(loginUser, currentProjectName, processMetaCron,
                processDefinitionName, processDefinitionId);
        Assert.assertEquals(0, insertFlagWorker);


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
        String topProcessJson = "{\"globalParams\":[]," +
                "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-38634\",\"name\":\"shell1\"," +
                "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"}," +
                "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\"," +
                "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false}," +
                "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}," +
                "{\"type\":\"SUB_PROCESS\",\"id\":\"tasks-44207\",\"name\":\"shell-4\"," +
                "\"params\":{\"processDefinitionId\":39},\"description\":\"\",\"runFlag\":\"NORMAL\"," +
                "\"dependence\":{},\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false}," +
                "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1," +
                "\"preTasks\":[\"shell1\"]}],\"tenantId\":1,\"timeout\":0}";

        String sub1ProcessJson = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-84090\"," +
                "\"name\":\"shell-4\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-4\\\"\"}," +
                "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\"," +
                "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false}," +
                "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]},{\"type\":\"SUB_PROCESS\"," +
                "\"id\":\"tasks-87364\",\"name\":\"shell-5\"," +
                "\"params\":{\"processDefinitionId\":46},\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{}," +
                "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\"," +
                "\"workerGroupId\":-1,\"preTasks\":[\"shell-4\"]}],\"tenantId\":1,\"timeout\":0}";

        String sub2ProcessJson = "{\"globalParams\":[]," +
                "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-52423\",\"name\":\"shell-5\"," +
                "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo \\\"shell-5\\\"\"},\"description\":\"\"," +
                "\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\"," +
                "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1," +
                "\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";


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

        processDefinitionService.importSubProcess(loginUser,testProject,jsonArray,subProcessIdMap);

        String correctSubJson = jsonArray.toString();

        Assert.assertEquals(originSubJson, correctSubJson);

    }

    @Test
    public void testImportProcessDefinitionById() throws IOException {

        String processJson = "{\"projectName\":\"testProject\",\"processDefinitionName\":\"shell-4\"," +
                "\"processDefinitionJson\":\"{\\\"tenantId\\\":1,\\\"globalParams\\\":[]," +
                "\\\"tasks\\\":[{\\\"workerGroupId\\\":-1,\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\"," +
                "\\\"type\\\":\\\"SHELL\\\",\\\"params\\\":{\\\"rawScript\\\":\\\"#!/bin/bash\\\\necho \\\\\\\"shell-4\\\\\\\"\\\"," +
                "\\\"localParams\\\":[],\\\"resourceList\\\":[]},\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}," +
                "\\\"maxRetryTimes\\\":\\\"0\\\",\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell-4\\\"," +
                "\\\"dependence\\\":{},\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[],\\\"id\\\":\\\"tasks-84090\\\"}," +
                "{\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell-5\\\",\\\"workerGroupId\\\":-1," +
                "\\\"description\\\":\\\"\\\",\\\"dependence\\\":{},\\\"preTasks\\\":[\\\"shell-4\\\"],\\\"id\\\":\\\"tasks-87364\\\"," +
                "\\\"runFlag\\\":\\\"NORMAL\\\",\\\"type\\\":\\\"SUB_PROCESS\\\",\\\"params\\\":{\\\"processDefinitionId\\\":46}," +
                "\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}}],\\\"timeout\\\":0}\"," +
                "\"processDefinitionDescription\":\"\",\"processDefinitionLocations\":\"{\\\"tasks-84090\\\":{\\\"name\\\":\\\"shell-4\\\"," +
                "\\\"targetarr\\\":\\\"\\\",\\\"x\\\":128,\\\"y\\\":114},\\\"tasks-87364\\\":{\\\"name\\\":\\\"shell-5\\\"," +
                "\\\"targetarr\\\":\\\"tasks-84090\\\",\\\"x\\\":266,\\\"y\\\":115}}\"," +
                "\"processDefinitionConnects\":\"[{\\\"endPointSourceId\\\":\\\"tasks-84090\\\"," +
                "\\\"endPointTargetId\\\":\\\"tasks-87364\\\"}]\"}";

        String subProcessJson = "{\"globalParams\":[]," +
                "\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-52423\",\"name\":\"shell-5\"," +
                "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo \\\"shell-5\\\"\"},\"description\":\"\"," +
                "\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\"," +
                "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1," +
                "\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

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

        //import process
        Map<String, Object> importProcessResult = processDefinitionService.importProcessDefinition(loginUser, multipartFile, currentProjectName);

        Assert.assertEquals(Status.SUCCESS, importProcessResult.get(Constants.STATUS));

        boolean delete = file.delete();

        Assert.assertTrue(delete);

        String processMetaJson = "";
        improssProcessCheckData(file, loginUser, currentProjectName, processMetaJson);

        processMetaJson = "{\"scheduleWorkerGroupId\":-1}";
        improssProcessCheckData(file, loginUser, currentProjectName, processMetaJson);

        processMetaJson = "{\"scheduleWorkerGroupId\":-1,\"projectName\":\"test\"}";
        improssProcessCheckData(file, loginUser, currentProjectName, processMetaJson);

        processMetaJson = "{\"scheduleWorkerGroupId\":-1,\"projectName\":\"test\",\"processDefinitionName\":\"test_definition\"}";
        improssProcessCheckData(file, loginUser, currentProjectName, processMetaJson);


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


    /**
     * get mock datasource
     * @return DataSource
     */
    private DataSource getDataSource(){
        DataSource dataSource = new DataSource();
        dataSource.setId(2);
        dataSource.setName("test");
        return  dataSource;
    }

    /**
     * get mock processDefinition
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinition(){
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(46);
        processDefinition.setName("testProject");
        processDefinition.setProjectId(2);
        return  processDefinition;
    }

    /**
     * get mock Project
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName){
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
        schedule.setWorkerGroupId(-1);
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
        processMeta.setScheduleWorkerGroupId(schedule.getWorkerGroupId());
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