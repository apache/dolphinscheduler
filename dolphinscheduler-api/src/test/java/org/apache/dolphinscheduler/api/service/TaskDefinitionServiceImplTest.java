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
import org.apache.dolphinscheduler.api.service.impl.TaskGroupServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
    ;

    @InjectMocks
    private TaskGroupServiceImpl taskGroupService;

    @Mock
    private TaskGroupQueueService taskGroupQueueService;

    @Mock
    private TaskGroupMapper taskGroupMapper;

    @Mock
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Mock
    private UserMapper userMapper;

    private String taskGroupName = "TaskGroupServiceTest";

    private String taskGroupDesc = "this is a task group";

    private String userName = "taskGroupServiceTest";

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
        List<TaskDefinitionLog> taskDefinitions = JSONUtils.toList(createTaskDefinitionJson, TaskDefinitionLog.class);
        Mockito.when(processService.saveTaskDefine(loginUser, projectCode, taskDefinitions)).thenReturn(1);
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
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);

        Mockito.when(processService.isTaskOnline(taskCode)).thenReturn(Boolean.FALSE);
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(new TaskDefinition());
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

        Mockito.when(taskDefinitionMapper.queryByName(project.getCode(), taskName))
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
        Mockito.when(processTaskRelationMapper.queryByTaskCode(Mockito.anyLong()))
                .thenReturn(new ArrayList<>());
        Mockito.when(taskDefinitionMapper.deleteByCode(Mockito.anyLong()))
                .thenReturn(1);

        Map<String, Object> relation = taskDefinitionService
                .deleteTaskDefinitionByCode(loginUser, projectCode, Mockito.anyLong());

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

        Mockito.when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, version))
                .thenReturn(new TaskDefinitionLog());

        Mockito.when(taskDefinitionMapper.queryByCode(taskCode))
                .thenReturn(new TaskDefinition());
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
        ShellParameters parameters = JSONUtils.parseObject(taskParams, ShellParameters.class);
        Assert.assertNotNull(parameters);
        String params = "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}}";
        ShellParameters parameters1 = JSONUtils.parseObject(params, ShellParameters.class);
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
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(result);
        Map<String, Object> map = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.TASK_DEFINE_NOT_EXIST, map.get(Constants.STATUS));

        // process definition offline
        putMsg(result, Status.SUCCESS);
        Mockito.when(taskDefinitionMapper.queryByCode(taskCode)).thenReturn(new TaskDefinition());
        Map<String, Object> offlineTaskResult = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.SUCCESS, offlineTaskResult.get(Constants.STATUS));

        // process definition online, resource exist
        Map<String, Object> onlineTaskResult = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SUCCESS, onlineTaskResult.get(Constants.STATUS));

        // process definition online, resource does not exist
        Map<String, Object> onlineResResult = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.ONLINE);
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION, onlineResResult.get(Constants.STATUS));

        // release error code
        Map<String, Object> failResult = taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, taskCode, ReleaseState.getEnum(2));
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failResult.get(Constants.STATUS));
    }

    /**
     * create admin user
     */
    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setUserName(userName);
        loginUser.setId(1);
        return loginUser;
    }

    private TaskGroup getTaskGroup() {
        TaskGroup taskGroup = new TaskGroup(taskGroupName, taskGroupDesc,
                100, 1);
        return taskGroup;
    }

    private List<TaskGroup> getList() {
        List<TaskGroup> list = new ArrayList<>();
        list.add(getTaskGroup());
        return list;
    }

    @Test
    public void testCreate() {
        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(taskGroupMapper.insert(taskGroup)).thenReturn(1);
        Mockito.when(taskGroupMapper.queryByName(loginUser.getId(), taskGroupName)).thenReturn(null);
        Map<String, Object> result = taskGroupService.createTaskGroup(loginUser, taskGroupName, taskGroupDesc, 100);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    @Test
    public void testQueryById() {
        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        Map<String, Object> result = taskGroupService.queryTaskGroupById(loginUser, 1);
        Assert.assertNotNull(result.get(Constants.DATA_LIST));
    }

    @Test
    public void testQueryProjectListPaging() {

        IPage<TaskGroup> page = new Page<>(1, 10);
        page.setRecords(getList());
        User loginUser = getLoginUser();
        Mockito.when(taskGroupMapper.queryTaskGroupPaging(Mockito.any(Page.class), Mockito.eq(10),
                Mockito.eq(null), Mockito.eq(0))).thenReturn(page);

        // query all
        Map<String, Object> result = taskGroupService.queryAllTaskGroup(loginUser, 1, 10);
        PageInfo<TaskGroup> pageInfo = (PageInfo<TaskGroup>) result.get(Constants.DATA_LIST);
        Assert.assertNotNull(pageInfo.getTotalList());
    }

    @Test
    public void testUpdate() {

        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        taskGroup.setStatus(Flag.NO.getCode());
        // Task group status error
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        Map<String, Object> result = taskGroupService.updateTaskGroup(loginUser, 1, "newName", "desc", 100);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        taskGroup.setStatus(0);
    }

    @Test
    public void testCloseAndStart() {

        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);

        //close failed
        Map<String, Object> result1 = taskGroupService.closeTaskGroup(loginUser, 1);
        Assert.assertEquals(Status.SUCCESS, result1.get(Constants.STATUS));
    }

    @Test
    public void testWakeTaskFroceManually() {

        TreeMap<Integer, Integer> tm = new TreeMap<>();
        tm.put(1, 1);
        Map<String, Object> map1 = taskGroupService.wakeTaskcompulsively(getLoginUser(), 1);
        Assert.assertEquals(Status.SUCCESS, map1.get(Constants.STATUS));

    }

}
