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
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import java.text.MessageFormat;
import java.util.ArrayList;
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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskDefinitionServiceImplTest {

    @InjectMocks
    private TaskDefinitionServiceImpl taskDefinitionService;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private TaskPluginManager taskPluginManager;

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
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_DEFINITION_CREATE)).thenReturn(result);

        String createTaskDefinitionJson = "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
            + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
            + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
            + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
            + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
            + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
            + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";
        List<TaskDefinitionLog> taskDefinitions = JSONUtils.toList(createTaskDefinitionJson, TaskDefinitionLog.class);
        Mockito.when(processService.saveTaskDefine(loginUser, projectCode, taskDefinitions, Boolean.TRUE)).thenReturn(1);
        Mockito.when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);
        Map<String, Object> relation = taskDefinitionService
            .createTaskDefinition(loginUser, projectCode, createTaskDefinitionJson);
        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));

    }

    @Test
    public void updateTaskDefinition() {
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
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode,TASK_DEFINITION_UPDATE)).thenReturn(result);

        Mockito.when(processService.isTaskOnline(taskCode)).thenReturn(Boolean.FALSE);
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(new TaskDefinition());
        Mockito.when(taskDefinitionMapper.updateById(Mockito.any(TaskDefinitionLog.class))).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.insert(Mockito.any(TaskDefinitionLog.class))).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.queryMaxVersionForDefinition(taskCode)).thenReturn(1);
        Mockito.when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);
        result = taskDefinitionService.updateTaskDefinition(loginUser, projectCode, taskCode, taskDefinitionJson);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void queryTaskDefinitionByName() {
        String taskName = "task";
        long projectCode = 1L;
        long processCode = 1L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode,TASK_DEFINITION )).thenReturn(result);

        Mockito.when(taskDefinitionMapper.queryByName(project.getCode(), processCode, taskName))
            .thenReturn(new TaskDefinition());

        Map<String, Object> relation = taskDefinitionService
            .queryTaskDefinitionByName(loginUser, projectCode, processCode, taskName);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    @Test
    public void deleteTaskDefinitionByCode() {
        long projectCode = 1L;
        long taskCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode,TASK_DEFINITION_DELETE )).thenReturn(result);
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(getTaskDefinition());
        Mockito.when(processTaskRelationMapper.queryDownstreamByTaskCode(taskCode))
            .thenReturn(new ArrayList<>());
        Mockito.when(taskDefinitionMapper.deleteByCode(taskCode))
            .thenReturn(1);

        Map<String, Object> relation = taskDefinitionService
            .deleteTaskDefinitionByCode(loginUser, projectCode, taskCode);

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
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_SWITCH_TO_THIS_VERSION)).thenReturn(result);

        Mockito.when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, version))
            .thenReturn(new TaskDefinitionLog());
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode))
            .thenReturn(taskDefinition);
        Mockito.when(taskDefinitionMapper.updateById(new TaskDefinitionLog())).thenReturn(1);
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

    private TaskDefinition getTaskDefinition() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(1L);
        taskDefinition.setCode(1L);
        taskDefinition.setVersion(1);
        taskDefinition.setTaskType("SHELL");
        return taskDefinition;
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
        String taskJson = "[{\"name\":\"shell1\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
            + "\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}},"
            + "\"flag\":\"NORMAL\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":\"0\",\"failRetryInterval\":\"1\","
            + "\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":null,\"delayTime\":\"0\"},{\"name\":\"shell2\",\"description\":\"\","
            + "\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 2\",\"conditionResult\":{\"successNode\""
            + ":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}},\"flag\":\"NORMAL\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\","
            + "\"failRetryTimes\":\"0\",\"failRetryInterval\":\"1\",\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":null,\"delayTime\":\"0\"}]";
        taskDefinitionLogs = JSONUtils.toList(taskJson, TaskDefinitionLog.class);
        Assert.assertFalse(taskDefinitionLogs.isEmpty());
        String taskParams = "{\"resourceList\":[],\"localParams\":[{\"prop\":\"datetime\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
            + "\"value\":\"${system.datetime}\"}],\"rawScript\":\"echo ${datetime}\",\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],"
            + "\\\"failedNode\\\":[\\\"\\\"]}\",\"dependence\":{}}";
        Map parameters = JSONUtils.parseObject(taskParams, Map.class);
        Assert.assertNotNull(parameters);
        String params = "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}}";
        Map parameters1 = JSONUtils.parseObject(params, Map.class);
        Assert.assertNotNull(parameters1);
    }

    @Test
    public void genTaskCodeList() {
        Map<String, Object> genTaskCodeList = taskDefinitionService.genTaskCodeList(10);
        Assert.assertEquals(Status.SUCCESS, genTaskCodeList.get(Constants.STATUS));
    }

    @Test
    public void testReleaseTaskDefinition() {
        long projectCode = 1L;
        long taskCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // check task dose not exist
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode,null)).thenReturn(result);
        Map<String, Object> map = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.TASK_DEFINE_NOT_EXIST, map.get(Constants.STATUS));

        // process definition offline
        putMsg(result, Status.SUCCESS);
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setVersion(1);
        taskDefinition.setCode(taskCode);
        String params = "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}}";
        taskDefinition.setTaskParams(params);
        taskDefinition.setTaskType("SHELL");
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(taskDefinition);
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog(taskDefinition);
        Mockito.when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, taskDefinition.getVersion())).thenReturn(taskDefinitionLog);
        Map<String, Object> offlineTaskResult = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.SUCCESS, offlineTaskResult.get(Constants.STATUS));

        // process definition online, resource exist
        Map<String, Object> onlineTaskResult = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SUCCESS, onlineTaskResult.get(Constants.STATUS));

        // release error code
        Map<String, Object> failResult = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.getEnum(2));
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failResult.get(Constants.STATUS));
    }
}
