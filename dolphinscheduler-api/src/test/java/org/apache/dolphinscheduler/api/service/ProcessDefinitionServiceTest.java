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
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

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


        String sqlDependentJson = "{\"globalParams\":[]," +
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

        String corSqlDependentJson = processDefinitionService.addTaskNodeSpecialParam(sqlDependentJson);


        JSONAssert.assertEquals(sqlDependentJson,corSqlDependentJson,false);

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

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }
}