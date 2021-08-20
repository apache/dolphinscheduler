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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TaskDefinitionServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.MessageFormat;
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

@RunWith(MockitoJUnitRunner.class)
public class TaskDefinitionServiceImplTest {
    String taskDefinitionJson = "{\n"
            + "    \"type\": \"SQL\",\n"
            + "    \"id\": \"tasks-27297\",\n"
            + "    \"name\": \"SQL\",\n"
            + "    \"params\": {\n"
            + "        \"type\": \"MYSQL\",\n"
            + "        \"datasource\": 1,\n"
            + "        \"sql\": \"select * from test\",\n"
            + "        \"udfs\": \"\",\n"
            + "        \"sqlType\": \"1\",\n"
            + "        \"title\": \"\",\n"
            + "        \"receivers\": \"\",\n"
            + "        \"receiversCc\": \"\",\n"
            + "        \"showType\": \"TABLE\",\n"
            + "        \"localParams\": [\n"
            + "            \n"
            + "        ],\n"
            + "        \"connParams\": \"\",\n"
            + "        \"preStatements\": [\n"
            + "            \n"
            + "        ],\n"
            + "        \"postStatements\": [\n"
            + "            \n"
            + "        ]\n"
            + "    },\n"
            + "    \"description\": \"\",\n"
            + "    \"runFlag\": \"NORMAL\",\n"
            + "    \"dependence\": {\n"
            + "        \n"
            + "    },\n"
            + "    \"maxRetryTimes\": \"0\",\n"
            + "    \"retryInterval\": \"1\",\n"
            + "    \"timeout\": {\n"
            + "        \"strategy\": \"\",\n"
            + "        \"enable\": false\n"
            + "    },\n"
            + "    \"taskInstancePriority\": \"MEDIUM\",\n"
            + "    \"workerGroupId\": -1,\n"
            + "    \"preTasks\": [\n"
            + "        \"dependent\"\n"
            + "    ]\n"
            + "}\n";

    @InjectMocks
    private TaskDefinitionServiceImpl taskDefinitionService;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private ProcessDefinitionMapper processDefineMapper;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ProcessService processService;

    @Test
    public void createTaskDefinition() {
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);

        String createTaskDefinitionJson = "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
            + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
            + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
            + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
            + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
            + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
            + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";
        List<TaskDefinition> taskDefinitions = JSONUtils.toList(createTaskDefinitionJson, TaskDefinition.class);
        Mockito.when(taskDefinitionMapper.batchInsert(Mockito.anyList())).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.batchInsert(Mockito.anyList())).thenReturn(1);
        Map<String, Object> relation = taskDefinitionService
                .createTaskDefinition(loginUser, projectCode, createTaskDefinitionJson);
        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));

    }

    @Test
    public void updateTaskDefinition () {
        String taskDefinitionJson = "{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
            + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
            + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
            + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
            + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
            + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
            + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}";
        long projectCode = 1L;
        long taskCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);

        Mockito.when(processService.isTaskOnline(taskCode)).thenReturn(Boolean.FALSE);
        Mockito.when(taskDefinitionMapper.queryByDefinitionCode(taskCode)).thenReturn(new TaskDefinition());
        Mockito.when(taskDefinitionMapper.updateById(Mockito.any(TaskDefinitionLog.class))).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.insert(Mockito.any(TaskDefinitionLog.class))).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.queryMaxVersionForDefinition(taskCode)).thenReturn(1);
        result = taskDefinitionService.updateTaskDefinition(loginUser, projectCode, taskCode, taskDefinitionJson);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void queryTaskDefinitionByName() {
        String taskName = "task";
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);

        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);

        Mockito.when(taskDefinitionMapper.queryByDefinitionName(project.getCode(), taskName))
                .thenReturn(new TaskDefinition());

        Map<String, Object> relation = taskDefinitionService
                .queryTaskDefinitionByName(loginUser, projectCode, taskName);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    @Test
    public void deleteTaskDefinitionByCode() {
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);

        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);

        Mockito.when(taskDefinitionMapper.deleteByCode(Mockito.anyLong()))
                .thenReturn(1);

        Map<String, Object> relation = taskDefinitionService
                .deleteTaskDefinitionByCode(loginUser, projectCode, 11L);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));

    }

    @Test
    public void switchVersion() {
        int version = 1;
        long taskCode = 11L;
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);

        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);

        Mockito.when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, version))
                .thenReturn(new TaskDefinitionLog());

        Mockito.when(taskDefinitionMapper.queryByDefinitionCode(taskCode))
                .thenReturn(new TaskDefinition());
        
        Map<String, Object> relation = taskDefinitionService
                .switchVersion(loginUser, projectCode, taskCode, version);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    /**
     * get mock Project
     *
     * @param projectCode projectCode
     * @return Project
     */
    private Project getProject(long projectCode) {
        Project project = new Project();
        project.setId(1);
        project.setCode(projectCode);
        project.setName("test");
        project.setUserId(1);
        return project;
    }

    @Test
    public void checkJson() {
        String taskDefinitionJson = "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
            + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
            + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
            + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
            + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
            + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
            + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        Assert.assertFalse(taskDefinitionLogs.isEmpty());
        String taskParams = "{\"resourceList\":[],\"localParams\":[{\"prop\":\"datetime\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
            + "\"value\":\"${system.datetime}\"}],\"rawScript\":\"echo ${datetime}\",\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],"
            + "\\\"failedNode\\\":[\\\"\\\"]}\",\"dependence\":{}}";
        ShellParameters parameters = JSONUtils.parseObject(taskParams, ShellParameters.class);
        Assert.assertNotNull(parameters);
    }

    @Test
    public void genTaskCodeList() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> genTaskCodeList = taskDefinitionService.genTaskCodeList(loginUser, 10);
        Assert.assertEquals(Status.SUCCESS, genTaskCodeList.get(Constants.STATUS));
    }
}