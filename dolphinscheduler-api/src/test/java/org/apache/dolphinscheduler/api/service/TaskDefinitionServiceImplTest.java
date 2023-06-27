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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_SWITCH_TO_THIS_VERSION;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;

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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
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
    private TaskPluginManager taskPluginManager;

    @Mock
    private ProcessTaskRelationService processTaskRelationService;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private ProcessTaskRelationLogDao processTaskRelationLogDao;

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
        Project project = getProject();
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        Mockito.when(projectService.hasProjectAndWritePerm(user, project, result))
                .thenReturn(true);
        Mockito.when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);

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
        Assertions.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    @Test
    public void updateTaskDefinition() {
        String taskDefinitionJson = getTaskDefinitionJson();;

        Project project = getProject();
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, PROJECT_CODE);
        Mockito.when(projectService.hasProjectAndWritePerm(user, project, new HashMap<>())).thenReturn(true);

        Mockito.when(processService.isTaskOnline(TASK_CODE)).thenReturn(Boolean.FALSE);
        Mockito.when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(new TaskDefinition());
        Mockito.when(taskDefinitionMapper.updateById(Mockito.any(TaskDefinitionLog.class))).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.insert(Mockito.any(TaskDefinitionLog.class))).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.queryMaxVersionForDefinition(TASK_CODE)).thenReturn(1);
        Mockito.when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);
        Mockito.when(processTaskRelationMapper.queryByTaskCode(3)).thenReturn(getProcessTaskRelationList2());
        Mockito.when(processTaskRelationMapper
                .updateProcessTaskRelationTaskVersion(Mockito.any(ProcessTaskRelation.class))).thenReturn(1);
        result = taskDefinitionService.updateTaskDefinition(user, PROJECT_CODE, TASK_CODE, taskDefinitionJson);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        // failure
        Mockito.when(processTaskRelationMapper
                .updateProcessTaskRelationTaskVersion(Mockito.any(ProcessTaskRelation.class))).thenReturn(2);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinition(user, PROJECT_CODE, TASK_CODE, taskDefinitionJson));
        Assertions.assertEquals(Status.PROCESS_TASK_RELATION_BATCH_UPDATE_ERROR.getCode(),
                ((ServiceException) exception).getCode());

    }

    @Test
    public void queryTaskDefinitionByName() {
        String taskName = "task";
        Project project = getProject();
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, PROJECT_CODE);
        Mockito.when(projectService.checkProjectAndAuth(user, project, PROJECT_CODE, TASK_DEFINITION))
                .thenReturn(result);

        Mockito.when(taskDefinitionMapper.queryByName(project.getCode(), PROCESS_DEFINITION_CODE, taskName))
                .thenReturn(new TaskDefinition());

        Map<String, Object> relation = taskDefinitionService
                .queryTaskDefinitionByName(user, PROJECT_CODE, PROCESS_DEFINITION_CODE, taskName);

        Assertions.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    @Test
    public void deleteTaskDefinitionByCode() {
        Project project = getProject();
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

        // error task definition not find
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.deleteTaskDefinitionByCode(user, TASK_CODE));
        Assertions.assertEquals(Status.TASK_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error delete single task definition object
        Mockito.when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(getTaskDefinition());
        Mockito.when(taskDefinitionMapper.deleteByCode(TASK_CODE)).thenReturn(0);
        Mockito.when(projectService.hasProjectAndWritePerm(user, project, new HashMap<>())).thenReturn(true);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.deleteTaskDefinitionByCode(user, TASK_CODE));
        Assertions.assertEquals(Status.DELETE_TASK_DEFINE_BY_CODE_MSG_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, project,
                TASK_DEFINITION_DELETE);
        Mockito.when(processTaskRelationMapper.queryDownstreamByTaskCode(TASK_CODE)).thenReturn(new ArrayList<>());
        Mockito.when(taskDefinitionMapper.deleteByCode(TASK_CODE)).thenReturn(1);
        Assertions.assertDoesNotThrow(() -> taskDefinitionService.deleteTaskDefinitionByCode(user, TASK_CODE));
    }

    @Test
    public void switchVersion() {
        Project project = getProject();
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);

        Map<String, Object> result = new HashMap<>();

        putMsg(result, Status.SUCCESS, PROJECT_CODE);
        Mockito.when(
                projectService.checkProjectAndAuth(user, project, PROJECT_CODE, WORKFLOW_SWITCH_TO_THIS_VERSION))
                .thenReturn(result);

        Mockito.when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(TASK_CODE, VERSION))
                .thenReturn(new TaskDefinitionLog());
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(PROJECT_CODE);
        Mockito.when(taskDefinitionMapper.queryByCode(TASK_CODE))
                .thenReturn(taskDefinition);
        Mockito.when(taskDefinitionMapper.updateById(new TaskDefinitionLog())).thenReturn(1);
        Map<String, Object> relation = taskDefinitionService
                .switchVersion(user, PROJECT_CODE, TASK_CODE, VERSION);

        Assertions.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
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
        Assertions.assertEquals(Status.SUCCESS, genTaskCodeList.get(Constants.STATUS));
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
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(project);
        Mockito.when(projectService.checkProjectAndAuth(user, project, PROJECT_CODE, TASK_DEFINITION))
                .thenReturn(checkResult);
        Mockito.when(taskDefinitionMapper.queryDefineListPaging(Mockito.any(Page.class), Mockito.anyLong(),
                Mockito.isNull(), Mockito.anyString(), Mockito.isNull()))
                .thenReturn(taskMainInfoIPage);
        Mockito.when(taskDefinitionMapper.queryDefineListByCodeList(PROJECT_CODE, Collections.singletonList(3L)))
                .thenReturn(Collections.singletonList(taskMainInfo));
        Result result = taskDefinitionService.queryTaskDefinitionListPaging(user, PROJECT_CODE,
                null, null, null, pageNo, pageSize);
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testReleaseTaskDefinition() {
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject());
        Project project = getProject();

        // check task dose not exist
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.TASK_DEFINE_NOT_EXIST, TASK_CODE);
        Mockito.when(projectService.checkProjectAndAuth(user, project, PROJECT_CODE, null)).thenReturn(result);
        Map<String, Object> map =
                taskDefinitionService.releaseTaskDefinition(user, PROJECT_CODE, TASK_CODE, ReleaseState.OFFLINE);
        Assertions.assertEquals(Status.TASK_DEFINE_NOT_EXIST, map.get(Constants.STATUS));

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
        Mockito.when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(taskDefinition);
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog(taskDefinition);
        Mockito.when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(TASK_CODE, taskDefinition.getVersion()))
                .thenReturn(taskDefinitionLog);
        Map<String, Object> offlineTaskResult =
                taskDefinitionService.releaseTaskDefinition(user, PROJECT_CODE, TASK_CODE, ReleaseState.OFFLINE);
        Assertions.assertEquals(Status.SUCCESS, offlineTaskResult.get(Constants.STATUS));

        // process definition online, resource exist
        Map<String, Object> onlineTaskResult =
                taskDefinitionService.releaseTaskDefinition(user, PROJECT_CODE, TASK_CODE, ReleaseState.ONLINE);
        Assertions.assertEquals(Status.SUCCESS, onlineTaskResult.get(Constants.STATUS));

        // release error code
        Map<String, Object> failResult =
                taskDefinitionService.releaseTaskDefinition(user, PROJECT_CODE, TASK_CODE, ReleaseState.getEnum(2));
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failResult.get(Constants.STATUS));
    }

    @Test
    public void testCreateTaskDefinitionV2() {
        TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setProjectCode(PROJECT_CODE);
        taskCreateRequest.setWorkflowCode(PROCESS_DEFINITION_CODE);

        // error process definition not find
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error project not find
        Mockito.when(processDefinitionMapper.queryByCode(PROCESS_DEFINITION_CODE)).thenReturn(getProcessDefinition());
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject());
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                .checkProjectAndAuthThrowException(user, getProject(), TASK_DEFINITION_CREATE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error task definition
        taskCreateRequest.setTaskParams(TASK_PARAMETER);
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, getProject(),
                TASK_DEFINITION_CREATE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));
        Assertions.assertEquals(Status.PROCESS_NODE_S_PARAMETER_INVALID.getCode(),
                ((ServiceException) exception).getCode());

        // error create task definition object
        Mockito.when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);
        Mockito.when(taskDefinitionMapper.insert(isA(TaskDefinition.class))).thenReturn(0);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));
        Assertions.assertEquals(Status.CREATE_TASK_DEFINITION_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // error sync to task definition log
        Mockito.when(taskDefinitionMapper.insert(isA(TaskDefinition.class))).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.insert(isA(TaskDefinitionLog.class))).thenReturn(0);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));
        Assertions.assertEquals(Status.CREATE_TASK_DEFINITION_LOG_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.when(taskDefinitionLogMapper.insert(isA(TaskDefinitionLog.class))).thenReturn(1);
        // we do not test updateUpstreamTaskDefinition, because it should be tested in processTaskRelationService
        Mockito.when(
                processTaskRelationService.updateUpstreamTaskDefinitionWithSyncDag(isA(User.class), isA(Long.class),
                        isA(Boolean.class),
                        isA(TaskRelationUpdateUpstreamRequest.class)))
                .thenReturn(getProcessTaskRelationList());
        Mockito.when(processDefinitionService.updateSingleProcessDefinition(isA(User.class), isA(Long.class),
                isA(WorkflowUpdateRequest.class))).thenReturn(getProcessDefinition());
        Assertions.assertDoesNotThrow(() -> taskDefinitionService.createTaskDefinitionV2(user, taskCreateRequest));

    }

    @Test
    public void testUpdateTaskDefinitionV2() {
        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();

        // error task definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        Assertions.assertEquals(Status.TASK_DEFINITION_NOT_EXISTS.getCode(), ((ServiceException) exception).getCode());

        // error project not find
        Mockito.when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(getTaskDefinition());
        Mockito.when(projectMapper.queryByCode(isA(Long.class))).thenReturn(getProject());
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                .checkProjectAndAuthThrowException(user, getProject(), TASK_DEFINITION_UPDATE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error task definition
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, getProject(),
                TASK_DEFINITION_UPDATE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        Assertions.assertEquals(Status.PROCESS_NODE_S_PARAMETER_INVALID.getCode(),
                ((ServiceException) exception).getCode());

        // error task definition already online
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        Assertions.assertEquals(Status.PROCESS_NODE_S_PARAMETER_INVALID.getCode(),
                ((ServiceException) exception).getCode());

        // error task definition nothing update
        Mockito.when(processService.isTaskOnline(TASK_CODE)).thenReturn(false);
        Mockito.when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        Assertions.assertEquals(Status.TASK_DEFINITION_NOT_CHANGE.getCode(), ((ServiceException) exception).getCode());

        // error task definition version invalid
        taskUpdateRequest.setTaskPriority(String.valueOf(Priority.HIGH));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        Assertions.assertEquals(Status.DATA_IS_NOT_VALID.getCode(), ((ServiceException) exception).getCode());

        // error task definition update effect number
        Mockito.when(taskDefinitionLogMapper.queryMaxVersionForDefinition(TASK_CODE)).thenReturn(VERSION);
        Mockito.when(taskDefinitionMapper.updateById(isA(TaskDefinition.class))).thenReturn(0);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        Assertions.assertEquals(Status.UPDATE_TASK_DEFINITION_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // error task definition log insert
        Mockito.when(taskDefinitionMapper.updateById(isA(TaskDefinition.class))).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.insert(isA(TaskDefinitionLog.class))).thenReturn(0);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));
        Assertions.assertEquals(Status.CREATE_TASK_DEFINITION_LOG_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.when(taskDefinitionLogMapper.insert(isA(TaskDefinitionLog.class))).thenReturn(1);
        // we do not test updateUpstreamTaskDefinition, because it should be tested in processTaskRelationService
        Mockito.when(
                processTaskRelationService.updateUpstreamTaskDefinitionWithSyncDag(isA(User.class), isA(Long.class),
                        isA(Boolean.class),
                        isA(TaskRelationUpdateUpstreamRequest.class)))
                .thenReturn(getProcessTaskRelationList());
        Assertions.assertDoesNotThrow(
                () -> taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest));

        TaskDefinition taskDefinition =
                taskDefinitionService.updateTaskDefinitionV2(user, TASK_CODE, taskUpdateRequest);
        Assertions.assertEquals(getTaskDefinition().getVersion() + 1, taskDefinition.getVersion());
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
        Mockito.when(processDefinitionMapper.queryByCode(isA(long.class))).thenReturn(processDefinition);

        // saveProcessDefine
        Mockito.when(processDefineLogMapper.queryMaxVersionForDefinition(isA(long.class))).thenReturn(version);
        Mockito.when(processDefineLogMapper.insert(isA(ProcessDefinitionLog.class))).thenReturn(1);
        Mockito.when(processDefinitionMapper.insert(isA(ProcessDefinitionLog.class))).thenReturn(1);
        int insertVersion =
                processServiceImpl.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE);
        Mockito.when(processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.TRUE))
                .thenReturn(insertVersion);
        Assertions.assertEquals(insertVersion, version + 1);

        // saveTaskRelation
        List<ProcessTaskRelationLog> processTaskRelationLogList = getProcessTaskRelationLogList();
        Mockito.when(processTaskRelationMapper.queryByProcessCode(eq(processDefinition.getProjectCode()),
                eq(processDefinition.getCode()))).thenReturn(processTaskRelationList);
        Mockito.when(processTaskRelationMapper.batchInsert(isA(List.class))).thenReturn(1);
        Mockito.when(processTaskRelationLogMapper.batchInsert(isA(List.class))).thenReturn(1);
        int insertResult = processServiceImpl.saveTaskRelation(loginUser, processDefinition.getProjectCode(),
                processDefinition.getCode(), insertVersion, processTaskRelationLogList, taskDefinitionLogs,
                Boolean.TRUE);
        Assertions.assertEquals(Constants.EXIT_CODE_SUCCESS, insertResult);
        Assertions.assertDoesNotThrow(
                () -> taskDefinitionService.updateDag(loginUser, processDefinition.getCode(), processTaskRelationList,
                        taskDefinitionLogs));
    }

    @Test
    public void testGetTaskDefinition() {
        // error task definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.getTaskDefinition(user, TASK_CODE));
        Assertions.assertEquals(Status.TASK_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error task definition not exists
        Mockito.when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(getTaskDefinition());
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject());
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, getProject(), TASK_DEFINITION);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> taskDefinitionService.getTaskDefinition(user, TASK_CODE));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, getProject(), TASK_DEFINITION);
        Assertions.assertDoesNotThrow(() -> taskDefinitionService.getTaskDefinition(user, TASK_CODE));
    }

    @Test
    public void testUpdateTaskWithUpstream() {

        String taskDefinitionJson = getTaskDefinitionJson();
        TaskDefinition taskDefinition = getTaskDefinition();
        taskDefinition.setFlag(Flag.NO);
        TaskDefinition taskDefinitionSecond = getTaskDefinition();
        taskDefinitionSecond.setCode(5);

        user.setUserType(UserType.ADMIN_USER);
        Mockito.when(projectMapper.queryByCode(PROJECT_CODE)).thenReturn(getProject());
        Mockito.when(projectService.hasProjectAndWritePerm(user, getProject(), new HashMap<>())).thenReturn(true);
        Mockito.when(taskDefinitionMapper.queryByCode(TASK_CODE)).thenReturn(taskDefinition);
        Mockito.when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);
        Mockito.when(taskDefinitionLogMapper.queryMaxVersionForDefinition(TASK_CODE)).thenReturn(1);
        Mockito.when(taskDefinitionMapper.updateById(Mockito.any())).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.insert(Mockito.any())).thenReturn(1);

        Mockito.when(taskDefinitionMapper.queryByCodeList(Mockito.anySet()))
                .thenReturn(Arrays.asList(taskDefinition, taskDefinitionSecond));

        Mockito.when(processTaskRelationMapper.queryUpstreamByCode(PROJECT_CODE, TASK_CODE))
                .thenReturn(getProcessTaskRelationListV2());
        Mockito.when(processDefinitionMapper.queryByCode(PROCESS_DEFINITION_CODE)).thenReturn(getProcessDefinition());
        Mockito.when(processTaskRelationMapper.batchInsert(Mockito.anyList())).thenReturn(1);
        Mockito.when(processTaskRelationMapper.updateById(Mockito.any())).thenReturn(1);
        Mockito.when(processTaskRelationLogDao.batchInsert(Mockito.anyList())).thenReturn(2);
        // success
        Map<String, Object> successMap = taskDefinitionService.updateTaskWithUpstream(user, PROJECT_CODE, TASK_CODE,
                taskDefinitionJson, UPSTREAM_CODE);
        Assertions.assertEquals(Status.SUCCESS, successMap.get(Constants.STATUS));
        user.setUserType(UserType.GENERAL_USER);
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
