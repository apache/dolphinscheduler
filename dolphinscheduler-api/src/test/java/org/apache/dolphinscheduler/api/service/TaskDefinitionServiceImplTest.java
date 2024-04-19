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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_SWITCH_TO_THIS_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.task.TaskCreateRequest;
import org.apache.dolphinscheduler.api.dto.task.TaskUpdateRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationUpdateUpstreamRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TaskDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessTaskRelationLogDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.process.ProcessServiceImpl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @InjectMocks
    private ProcessServiceImpl processServiceImpl;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessDefinitionLogMapper processDefineLogMapper;

    @Mock
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private ProcessTaskRelationService processTaskRelationService;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private ProcessTaskRelationLogDao processTaskRelationLogDao;

    @Mock
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    private static final String TASK_PARAMETER =
            "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}}";;
    private static final long PROJECT_CODE = 1L;
    private static final long PROCESS_DEFINITION_CODE = 2L;
    private static final long TASK_CODE = 3L;
    private static final String UPSTREAM_CODE = "3,5";
    private static final int VERSION = 1;
    private static final int RESOURCE_RATE = -1;
    protected User user;
    protected Exception exception;

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setTenantId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test
    public void createTaskDefinition() {
        try (
                MockedStatic<TaskPluginManager> taskPluginManagerMockedStatic =
                        Mockito.mockStatic(TaskPluginManager.class)) {
            taskPluginManagerMockedStatic.when(() -> TaskPluginManager.checkTaskParameters(Mockito.any()))
                    .thenReturn(true);
            Project project = getProject();
            when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

            Map<String, Object> result = new HashMap<>();
            when(projectService.hasProjectAndWritePerm(user, project, result))
                    .thenReturn(true);

            String createTaskDefinitionJson =
                    "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
                            + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
                            + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
                            + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
                            + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
                            + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
                            + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";
            Map<String, Object> relation = taskDefinitionService
                    .createTaskDefinition(user, PROJECT_CODE, createTaskDefinitionJson);
            assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));

        }
    }

    @Test
    public void updateTaskDefinition() {
        try (
                MockedStatic<TaskPluginManager> taskPluginManagerMockedStatic =
                        Mockito.mockStatic(TaskPluginManager.class)) {
            taskPluginManagerMockedStatic.when(() -> TaskPluginManager.checkTaskParameters(Mockito.any()))
                    .thenReturn(true);
            String taskDefinitionJson = getTaskDefinitionJson();

            Project project = getProject();
            when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

            Map<String, Object> result = new HashMap<>();
            putMsg(result, Status.SUCCESS, PROJECT_CODE);
            when(projectService.hasProjectAndWritePerm(user, project, new HashMap<>())).thenReturn(true);

            when(processService.isTaskOnline(TASK_CODE)).thenReturn(Boolean.FALSE);
            when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(new TaskDefinition());
            when(taskDefinitionMapper.updateById(Mockito.any(TaskDefinitionLog.class))).thenReturn(1);
            when(taskDefinitionLogMapper.insert(Mockito.any(TaskDefinitionLog.class))).thenReturn(1);
            when(processTaskRelationLogDao.insert(Mockito.any(ProcessTaskRelationLog.class))).thenReturn(1);
            when(processDefinitionMapper.queryByCode(2L)).thenReturn(new ProcessDefinition());
            when(processDefinitionMapper.updateById(Mockito.any(ProcessDefinition.class))).thenReturn(1);
            when(processDefinitionLogMapper.insert(Mockito.any(ProcessDefinitionLog.class))).thenReturn(1);
            when(taskDefinitionLogMapper.queryMaxVersionForDefinition(TASK_CODE)).thenReturn(1);
            when(processTaskRelationMapper.queryProcessTaskRelationByTaskCodeAndTaskVersion(TASK_CODE, 0))
                    .thenReturn(getProcessTaskRelationList2());
            when(processTaskRelationMapper
                    .updateProcessTaskRelationTaskVersion(Mockito.any(ProcessTaskRelation.class))).thenReturn(1);
            result = taskDefinitionService.updateTaskDefinition(user, PROJECT_CODE, TASK_CODE, taskDefinitionJson);
            assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
            // failure
            when(processTaskRelationMapper
                    .updateProcessTaskRelationTaskVersion(Mockito.any(ProcessTaskRelation.class))).thenReturn(2);
            exception = Assertions.assertThrows(ServiceException.class,
                    () -> taskDefinitionService.updateTaskDefinition(user, PROJECT_CODE, TASK_CODE,
                            taskDefinitionJson));
            assertEquals(Status.PROCESS_TASK_RELATION_BATCH_UPDATE_ERROR.getCode(),
                    ((ServiceException) exception).getCode());
        }

    }

    @Test
    public void queryTaskDefinitionByName() {
        String taskName = "task";
        Project project = getProject();
        when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, PROJECT_CODE);
        when(projectService.checkProjectAndAuth(user, project, PROJECT_CODE, TASK_DEFINITION))
                .thenReturn(result);

        when(taskDefinitionMapper.queryByName(project.getCode(), PROCESS_DEFINITION_CODE, taskName))
                .thenReturn(new TaskDefinition());

        Map<String, Object> relation = taskDefinitionService
                .queryTaskDefinitionByName(user, PROJECT_CODE, PROCESS_DEFINITION_CODE, taskName);

        assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    @Test
    public void deleteTaskDefinitionByCode() {
        Project project = getProject();
        when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

        // error task definition not find
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.deleteTaskDefinitionByCode(user, TASK_CODE));
        assertEquals(Status.TASK_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error delete single task definition object
        when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(getTaskDefinition());
        when(taskDefinitionMapper.deleteByCode(TASK_CODE)).thenReturn(0);
        when(projectService.hasProjectAndWritePerm(user, project, new HashMap<>())).thenReturn(true);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.deleteTaskDefinitionByCode(user, TASK_CODE));
        assertEquals(Status.DELETE_TASK_DEFINE_BY_CODE_MSG_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // success
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, project,
                TASK_DEFINITION_DELETE);
        when(processTaskRelationMapper.queryDownstreamByTaskCode(TASK_CODE)).thenReturn(new ArrayList<>());
        when(taskDefinitionMapper.deleteByCode(TASK_CODE)).thenReturn(1);
        Assertions.assertDoesNotThrow(() -> taskDefinitionService.deleteTaskDefinitionByCode(user, TASK_CODE));
    }

    @Test
    public void switchVersion() {
        Project project = getProject();
        when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();

        putMsg(result, Status.SUCCESS, PROJECT_CODE);
        when(
                projectService.checkProjectAndAuth(user, project, PROJECT_CODE, WORKFLOW_SWITCH_TO_THIS_VERSION))
                        .thenReturn(result);

        when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(TASK_CODE, VERSION))
                .thenReturn(new TaskDefinitionLog());
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(PROJECT_CODE);
        when(taskDefinitionMapper.queryByCode(TASK_CODE))
                .thenReturn(taskDefinition);
        when(taskDefinitionMapper.updateById(new TaskDefinitionLog())).thenReturn(1);
        Map<String, Object> relation = taskDefinitionService
                .switchVersion(user, PROJECT_CODE, TASK_CODE, VERSION);

        assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    @Test
    public void checkJson() {
        String taskDefinitionJson =
                "[{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
                        + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\",\\\"direct\\\":\\\"IN\\\","
                        + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":"
                        + "\\\"echo ${datetime}\\\",\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
                        + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\",\"flag\":0,\"taskPriority\":0,"
                        + "\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":0,\"timeoutFlag\":0,"
                        + "\"timeoutNotifyStrategy\":0,\"timeout\":0,\"delayTime\":0,\"resourceIds\":\"\"}]";
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        Assertions.assertFalse(taskDefinitionLogs.isEmpty());
        String taskJson =
                "[{\"name\":\"shell1\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
                        + "\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}},"
                        + "\"flag\":\"NORMAL\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":\"0\",\"failRetryInterval\":\"1\","
                        + "\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":null,\"delayTime\":\"0\"},{\"name\":\"shell2\",\"description\":\"\","
                        + "\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 2\",\"conditionResult\":{\"successNode\""
                        + ":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}},\"flag\":\"NORMAL\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\","
                        + "\"failRetryTimes\":\"0\",\"failRetryInterval\":\"1\",\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":null,\"delayTime\":\"0\"}]";
        taskDefinitionLogs = JSONUtils.toList(taskJson, TaskDefinitionLog.class);
        Assertions.assertFalse(taskDefinitionLogs.isEmpty());
        String taskParams =
                "{\"resourceList\":[],\"localParams\":[{\"prop\":\"datetime\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                        + "\"value\":\"${system.datetime}\"}],\"rawScript\":\"echo ${datetime}\",\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],"
                        + "\\\"failedNode\\\":[\\\"\\\"]}\",\"dependence\":{}}";
        Map parameters = JSONUtils.parseObject(taskParams, Map.class);
        Assertions.assertNotNull(parameters);
        String params =
                "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}}";
        Map parameters1 = JSONUtils.parseObject(params, Map.class);
        Assertions.assertNotNull(parameters1);
    }

    @Test
    public void genTaskCodeList() {
        Map<String, Object> genTaskCodeList = taskDefinitionService.genTaskCodeList(10);
        assertEquals(Status.SUCCESS, genTaskCodeList.get(Constants.STATUS));
    }

    @Test
    public void testQueryTaskDefinitionListPaging() {
        Project project = getProject();
        Map<String, Object> checkResult = new HashMap<>();
        checkResult.put(Constants.STATUS, Status.SUCCESS);
        Integer pageNo = 1;
        Integer pageSize = 10;
        IPage<TaskMainInfo> taskMainInfoIPage = new Page<>();
        TaskMainInfo taskMainInfo = new TaskMainInfo();
        taskMainInfo.setTaskCode(TASK_CODE);
        taskMainInfo.setUpstreamTaskCode(4L);
        taskMainInfo.setUpstreamTaskName("4");
        taskMainInfoIPage.setRecords(Collections.singletonList(taskMainInfo));
        taskMainInfoIPage.setTotal(10L);
        when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);
        when(projectService.checkProjectAndAuth(user, project, PROJECT_CODE, TASK_DEFINITION))
                .thenReturn(checkResult);
        when(taskDefinitionMapper.queryDefineListPaging(Mockito.any(Page.class), Mockito.anyLong(),
                Mockito.isNull(), Mockito.anyString(), Mockito.isNull()))
                        .thenReturn(taskMainInfoIPage);
        when(taskDefinitionMapper.queryDefineListByCodeList(PROJECT_CODE, Collections.singletonList(3L)))
                .thenReturn(Collections.singletonList(taskMainInfo));
        Result result = taskDefinitionService.queryTaskDefinitionListPaging(user, PROJECT_CODE,
                null, null, null, pageNo, pageSize);
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testReleaseTaskDefinition() {
        when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject());
        Project project = getProject();

        // check task dose not exist
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.TASK_DEFINE_NOT_EXIST, TASK_CODE);
        when(projectService.checkProjectAndAuth(user, project, PROJECT_CODE, null)).thenReturn(result);
        Map<String, Object> map =
                taskDefinitionService.releaseTaskDefinition(user, PROJECT_CODE, TASK_CODE, ReleaseState.OFFLINE);
        assertEquals(Status.TASK_DEFINE_NOT_EXIST, map.get(Constants.STATUS));

        // process definition offline
        putMsg(result, Status.SUCCESS);
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(PROJECT_CODE);
        taskDefinition.setVersion(1);
        taskDefinition.setCode(TASK_CODE);
        String params =
                "{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}}";
        taskDefinition.setTaskParams(params);
        taskDefinition.setTaskType("SHELL");
        when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(taskDefinition);
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog(taskDefinition);
        when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(TASK_CODE, taskDefinition.getVersion()))
                .thenReturn(taskDefinitionLog);
        Map<String, Object> offlineTaskResult =
                taskDefinitionService.releaseTaskDefinition(user, PROJECT_CODE, TASK_CODE, ReleaseState.OFFLINE);
        assertEquals(Status.SUCCESS, offlineTaskResult.get(Constants.STATUS));

        // process definition online, resource exist
        Map<String, Object> onlineTaskResult =
                taskDefinitionService.releaseTaskDefinition(user, PROJECT_CODE, TASK_CODE, ReleaseState.ONLINE);
        assertEquals(Status.SUCCESS, onlineTaskResult.get(Constants.STATUS));

        // release error code
        Map<String, Object> failResult =
                taskDefinitionService.releaseTaskDefinition(user, PROJECT_CODE, TASK_CODE, ReleaseState.getEnum(2));
        assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failResult.get(Constants.STATUS));
    }

    @Test
    public void testCreateTaskDefinitionV2() {
        TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setProjectCode(PROJECT_CODE);
        taskCreateRequest.setWorkflowCode(PROCESS_DEFINITION_CODE);

        // error process definition not find
        assertThrowsServiceException(Status.PROCESS_DEFINE_NOT_EXIST,
                () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));

        // error project not find
        when(processDefinitionMapper.queryByCode(PROCESS_DEFINITION_CODE)).thenReturn(getProcessDefinition());
        when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject());
        doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                .checkProjectAndAuthThrowException(user, getProject(), TASK_DEFINITION_CREATE);
        assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));

        // error task definition
        taskCreateRequest.setTaskParams(TASK_PARAMETER);
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, getProject(), TASK_DEFINITION_CREATE);
        assertThrowsServiceException(Status.PROCESS_NODE_S_PARAMETER_INVALID,
                () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));

        try (
                MockedStatic<TaskPluginManager> taskPluginManagerMockedStatic =
                        Mockito.mockStatic(TaskPluginManager.class)) {
            taskPluginManagerMockedStatic.when(() -> TaskPluginManager.checkTaskParameters(Mockito.any()))
                    .thenReturn(true);

            // error create task definition object
            when(taskDefinitionMapper.insert(isA(TaskDefinition.class))).thenReturn(0);
            assertThrowsServiceException(Status.CREATE_TASK_DEFINITION_ERROR,
                    () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));

            // error sync to task definition log
            when(taskDefinitionMapper.insert(isA(TaskDefinition.class))).thenReturn(1);
            when(taskDefinitionLogMapper.insert(isA(TaskDefinitionLog.class))).thenReturn(0);
            assertThrowsServiceException(Status.CREATE_TASK_DEFINITION_LOG_ERROR,
                    () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));

            // success
            when(taskDefinitionLogMapper.insert(isA(TaskDefinitionLog.class))).thenReturn(1);
            // we do not test updateUpstreamTaskDefinition, because it should be tested in processTaskRelationService
            when(
                    processTaskRelationService.updateUpstreamTaskDefinitionWithSyncDag(isA(User.class), isA(Long.class),
                            isA(Boolean.class),
                            isA(TaskRelationUpdateUpstreamRequest.class)))
                                    .thenReturn(getProcessTaskRelationList());
            when(processDefinitionService.updateSingleProcessDefinition(isA(User.class), isA(Long.class),
                    isA(WorkflowUpdateRequest.class))).thenReturn(getProcessDefinition());
            assertDoesNotThrow(() -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));
        }

    }

    @Test
    public void testUpdateTaskDefinitionV2() {
        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
        TaskDefinition taskDefinition = getTaskDefinition();
        Project project = getProject();

        // error task definition not exists
        assertThrowsServiceException(Status.TASK_DEFINITION_NOT_EXISTS,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));

        // error project not find
        when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(taskDefinition);
        when(projectMapper.queryByCode(isA(Long.class))).thenReturn(project);
        doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, TASK_DEFINITION_UPDATE);
        assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));

        // error task definition
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, project, TASK_DEFINITION_UPDATE);

        try (
                MockedStatic<TaskPluginManager> taskPluginManagerMockedStatic =
                        Mockito.mockStatic(TaskPluginManager.class)) {
            taskPluginManagerMockedStatic.when(() -> TaskPluginManager.checkTaskParameters(Mockito.any()))
                    .thenReturn(false);
            assertThrowsServiceException(Status.PROCESS_NODE_S_PARAMETER_INVALID,
                    () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        }

        try (
                MockedStatic<TaskPluginManager> taskPluginManagerMockedStatic =
                        Mockito.mockStatic(TaskPluginManager.class)) {
            taskPluginManagerMockedStatic.when(() -> TaskPluginManager.checkTaskParameters(Mockito.any()))
                    .thenReturn(true);
            // error task definition nothing update
            when(processService.isTaskOnline(TASK_CODE)).thenReturn(false);
            assertThrowsServiceException(Status.TASK_DEFINITION_NOT_CHANGE,
                    () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));

            // error task definition version invalid
            taskUpdateRequest.setTaskPriority(String.valueOf(Priority.HIGH));
            assertThrowsServiceException(Status.DATA_IS_NOT_VALID,
                    () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));

            // error task definition update effect number
            when(taskDefinitionLogMapper.queryMaxVersionForDefinition(TASK_CODE)).thenReturn(VERSION);
            when(taskDefinitionMapper.updateById(isA(TaskDefinition.class))).thenReturn(0);
            assertThrowsServiceException(Status.UPDATE_TASK_DEFINITION_ERROR,
                    () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));

            // error task definition log insert
            when(taskDefinitionMapper.updateById(isA(TaskDefinition.class))).thenReturn(1);
            when(taskDefinitionLogMapper.insert(isA(TaskDefinitionLog.class))).thenReturn(0);
            assertThrowsServiceException(Status.CREATE_TASK_DEFINITION_LOG_ERROR,
                    () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));

            // success
            when(taskDefinitionLogMapper.insert(isA(TaskDefinitionLog.class))).thenReturn(1);
            // we do not test updateUpstreamTaskDefinition, because it should be tested in processTaskRelationService
            when(
                    processTaskRelationService.updateUpstreamTaskDefinitionWithSyncDag(isA(User.class), isA(Long.class),
                            isA(Boolean.class),
                            isA(TaskRelationUpdateUpstreamRequest.class)))
                                    .thenReturn(getProcessTaskRelationList());
            Assertions.assertDoesNotThrow(
                    () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));

            taskDefinition =
                    taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest);
            assertEquals(getTaskDefinition().getVersion() + 1, taskDefinition.getVersion());
        }
    }

    @Test
    public void testUpdateDag() {
        User loginUser = getLoginUser();
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setId(null);
        List<ProcessTaskRelation> processTaskRelationList = getProcessTaskRelationList();
        TaskDefinitionLog taskDefinitionLog = getTaskDefinitionLog();
        ArrayList<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        taskDefinitionLogs.add(taskDefinitionLog);
        Integer version = 1;
        when(processDefinitionMapper.queryByCode(isA(long.class))).thenReturn(processDefinition);

        // saveProcessDefine
        when(processDefineLogMapper.queryMaxVersionForDefinition(isA(long.class))).thenReturn(version);
        when(processDefineLogMapper.insert(isA(ProcessDefinitionLog.class))).thenReturn(1);
        when(processDefinitionMapper.insert(isA(ProcessDefinitionLog.class))).thenReturn(1);
        int insertVersion =
                processServiceImpl.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        when(processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE))
                .thenReturn(insertVersion);
        assertEquals(insertVersion, version + 1);

        // saveTaskRelation
        List<ProcessTaskRelationLog> processTaskRelationLogList = getProcessTaskRelationLogList();
        when(processTaskRelationMapper.queryByProcessCode(eq(processDefinition.getCode())))
                .thenReturn(processTaskRelationList);
        when(processTaskRelationMapper.batchInsert(isA(List.class))).thenReturn(1);
        when(processTaskRelationLogMapper.batchInsert(isA(List.class))).thenReturn(1);
        int insertResult = processServiceImpl.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                processDefinition.getCode(), insertVersion, processTaskRelationLogList, taskDefinitionLogs,
                Boolean.TRUE);
        assertEquals(Constants.EXIT_CODE_SUCCESS, insertResult);
        Assertions.assertDoesNotThrow(
                () -> taskDefinitionService.updateDag(loginUser, processDefinition.getCode(), processTaskRelationList,
                        taskDefinitionLogs));
    }

    @Test
    public void testGetTaskDefinition() {
        // error task definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.getTaskDefinition(user, TASK_CODE));
        assertEquals(Status.TASK_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error task definition not exists
        when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(getTaskDefinition());
        when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject());
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, getProject(), TASK_DEFINITION);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.getTaskDefinition(user, TASK_CODE));
        assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // success
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, getProject(), TASK_DEFINITION);
        Assertions.assertDoesNotThrow(() -> taskDefinitionService.getTaskDefinition(user, TASK_CODE));
    }

    @Test
    public void testUpdateTaskWithUpstream() {
        try (
                MockedStatic<TaskPluginManager> taskPluginManagerMockedStatic =
                        Mockito.mockStatic(TaskPluginManager.class)) {
            taskPluginManagerMockedStatic.when(() -> TaskPluginManager.checkTaskParameters(Mockito.any()))
                    .thenReturn(true);
            String taskDefinitionJson = getTaskDefinitionJson();
            TaskDefinition taskDefinition = getTaskDefinition();
            taskDefinition.setFlag(Flag.NO);
            TaskDefinition taskDefinitionSecond = getTaskDefinition();
            taskDefinitionSecond.setCode(5);

            user.setUserType(UserType.ADMIN_USER);
            when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject());
            when(projectService.hasProjectAndWritePerm(user, getProject(), new HashMap<>())).thenReturn(true);
            when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(taskDefinition);
            when(taskDefinitionLogMapper.queryMaxVersionForDefinition(TASK_CODE)).thenReturn(1);
            when(taskDefinitionMapper.updateById(Mockito.any())).thenReturn(1);
            when(taskDefinitionLogMapper.insert(Mockito.any())).thenReturn(1);

            when(taskDefinitionMapper.queryByCodeList(Mockito.anySet()))
                    .thenReturn(Arrays.asList(taskDefinition, taskDefinitionSecond));

            when(processTaskRelationMapper.queryUpstreamByCode(PROJECT_CODE, TASK_CODE))
                    .thenReturn(getProcessTaskRelationListV2());
            when(processDefinitionMapper.queryByCode(PROCESS_DEFINITION_CODE))
                    .thenReturn(getProcessDefinition());
            when(processTaskRelationMapper.batchInsert(Mockito.anyList())).thenReturn(1);
            when(processTaskRelationMapper.updateById(Mockito.any())).thenReturn(1);
            when(processTaskRelationLogDao.batchInsert(Mockito.anyList())).thenReturn(2);
            // success
            Map<String, Object> successMap = taskDefinitionService.updateTaskWithUpstream(user, PROJECT_CODE, TASK_CODE,
                    taskDefinitionJson, UPSTREAM_CODE);
            assertEquals(Status.SUCCESS, successMap.get(Constants.STATUS));
            user.setUserType(UserType.GENERAL_USER);
        }
    }

    private String getTaskDefinitionJson() {
        return "{\"name\":\"detail_up\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":"
                + "\"{\\\"resourceList\\\":[],\\\"localParams\\\":[{\\\"prop\\\":\\\"datetime\\\","
                + "\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\","
                + "\\\"value\\\":\\\"${system.datetime}\\\"}],\\\"rawScript\\\":\\\"echo ${datetime}\\\","
                + "\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],"
                + "\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"dependence\\\":{}}\","
                + "\"flag\":0,\"taskPriority\":0,\"workerGroup\":\"default\",\"failRetryTimes\":0,"
                + "\"failRetryInterval\":0,\"timeoutFlag\":0,\"timeoutNotifyStrategy\":0,\"timeout\":0,"
                + "\"delayTime\":0,\"resourceIds\":\"\"}";
    }

    /**
     * create admin user
     */
    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        loginUser.setId(1);
        return loginUser;
    }

    /**
     * get mock Project
     *
     * @return Project
     */
    private Project getProject() {
        Project project = new Project();
        project.setId(1);
        project.setCode(PROJECT_CODE);
        project.setName("test");
        project.setUserId(1);
        return project;
    }

    private ProcessDefinition getProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectCode(PROJECT_CODE);
        processDefinition.setCode(PROCESS_DEFINITION_CODE);
        processDefinition.setVersion(VERSION);
        return processDefinition;
    }

    private TaskDefinition getTaskDefinition() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(PROJECT_CODE);
        taskDefinition.setCode(TASK_CODE);
        taskDefinition.setVersion(VERSION);
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setTaskParams(TASK_PARAMETER);
        taskDefinition.setFlag(Flag.YES);
        taskDefinition.setCpuQuota(RESOURCE_RATE);
        taskDefinition.setMemoryMax(RESOURCE_RATE);
        return taskDefinition;
    }

    private TaskDefinitionLog getTaskDefinitionLog() {
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setProjectCode(PROJECT_CODE);
        taskDefinitionLog.setCode(TASK_CODE);
        taskDefinitionLog.setVersion(VERSION);
        taskDefinitionLog.setTaskType("SHELL");
        taskDefinitionLog.setTaskParams(TASK_PARAMETER);
        taskDefinitionLog.setFlag(Flag.YES);
        taskDefinitionLog.setCpuQuota(RESOURCE_RATE);
        taskDefinitionLog.setMemoryMax(RESOURCE_RATE);
        return taskDefinitionLog;
    }

    private List<ProcessTaskRelation> getProcessTaskRelationList() {
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();

        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(PROJECT_CODE);
        processTaskRelation.setProcessDefinitionCode(PROCESS_DEFINITION_CODE);
        processTaskRelation.setPreTaskCode(TASK_CODE);
        processTaskRelation.setPostTaskCode(TASK_CODE + 1L);

        processTaskRelationList.add(processTaskRelation);
        return processTaskRelationList;
    }

    private List<ProcessTaskRelation> getProcessTaskRelationListV2() {
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();

        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        fillProcessTaskRelation(processTaskRelation);

        processTaskRelationList.add(processTaskRelation);
        processTaskRelation = new ProcessTaskRelation();
        fillProcessTaskRelation(processTaskRelation);
        processTaskRelation.setPreTaskCode(4L);
        processTaskRelationList.add(processTaskRelation);
        return processTaskRelationList;
    }

    private void fillProcessTaskRelation(ProcessTaskRelation processTaskRelation) {
        processTaskRelation.setProjectCode(PROJECT_CODE);
        processTaskRelation.setProcessDefinitionCode(PROCESS_DEFINITION_CODE);
        processTaskRelation.setPreTaskCode(TASK_CODE);
        processTaskRelation.setPostTaskCode(TASK_CODE + 1L);
    }

    private List<ProcessTaskRelationLog> getProcessTaskRelationLogList() {
        List<ProcessTaskRelationLog> processTaskRelationLogList = new ArrayList<>();

        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setProjectCode(PROJECT_CODE);
        processTaskRelationLog.setProcessDefinitionCode(PROCESS_DEFINITION_CODE);
        processTaskRelationLog.setPreTaskCode(TASK_CODE);
        processTaskRelationLog.setPostTaskCode(TASK_CODE + 1L);

        processTaskRelationLogList.add(processTaskRelationLog);
        return processTaskRelationLogList;
    }

    private List<ProcessTaskRelation> getProcessTaskRelationList2() {
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();

        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(PROJECT_CODE);
        processTaskRelation.setProcessDefinitionCode(PROCESS_DEFINITION_CODE);
        processTaskRelation.setPreTaskCode(TASK_CODE);
        processTaskRelation.setPostTaskCode(TASK_CODE + 1L);

        processTaskRelationList.add(processTaskRelation);

        ProcessTaskRelation processTaskRelation2 = new ProcessTaskRelation();
        processTaskRelation2.setProjectCode(PROJECT_CODE);
        processTaskRelation2.setProcessDefinitionCode(PROCESS_DEFINITION_CODE);
        processTaskRelation2.setPreTaskCode(TASK_CODE - 1);
        processTaskRelation2.setPostTaskCode(TASK_CODE);
        processTaskRelationList.add(processTaskRelation2);

        return processTaskRelationList;
    }
}
