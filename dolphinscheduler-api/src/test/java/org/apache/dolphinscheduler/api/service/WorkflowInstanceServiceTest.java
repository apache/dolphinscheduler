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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.INSTANCE_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.INSTANCE_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_INSTANCE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.WorkflowInstanceServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ContextType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.DependentResultTaskInstanceContext;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstanceContext;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceContextDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceMapDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkflowInstanceServiceTest {

    @InjectMocks
    WorkflowInstanceServiceImpl workflowInstanceService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectServiceImpl projectService;

    @Mock
    ProcessService processService;

    @Mock
    TaskInstanceDao taskInstanceDao;

    @Mock
    WorkflowInstanceDao workflowInstanceDao;

    @Mock
    WorkflowInstanceMapper workflowInstanceMapper;

    @Mock
    WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Mock
    WorkflowDefinitionMapper workflowDefinitionMapper;

    @Mock
    WorkflowDefinitionService workflowDefinitionService;

    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Mock
    LoggerServiceImpl loggerService;

    @Mock
    UsersService usersService;

    @Mock
    TenantMapper tenantMapper;
    @Mock
    TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    ScheduleMapper scheduleMapper;

    @Mock
    CuringParamsService curingGlobalParamsService;

    @Mock
    AlertDao alertDao;

    @Mock
    private TaskInstanceService taskInstanceService;

    @Mock
    private WorkflowInstanceMapDao workflowInstanceMapDao;

    @Mock
    private TaskInstanceContextDao taskInstanceContextDao;

    private String shellJson = "[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":123456789,"
            + "\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"},{\"name\":\"\",\"preTaskCode\":123456789,"
            + "\"preTaskVersion\":1,\"postTaskCode\":123451234,\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"}]";

    private String taskJson =
            "[{\"name\":\"shell1\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
                    + "\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}},"
                    + "\"flag\":\"NORMAL\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":\"0\",\"failRetryInterval\":\"1\","
                    + "\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":null,\"delayTime\":\"0\"},{\"name\":\"shell2\",\"description\":\"\","
                    + "\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 2\",\"conditionResult\":{\"successNode\""
                    + ":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}},\"flag\":\"NORMAL\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\","
                    + "\"failRetryTimes\":\"0\",\"failRetryInterval\":\"1\",\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":null,\"delayTime\":\"0\"}]";

    private String taskRelationJson =
            "[{\"name\":\"\",\"preTaskCode\":4254865123776,\"preTaskVersion\":1,\"postTaskCode\":4254862762304,\"postTaskVersion\":1,\"conditionType\":0,"
                    + "\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":4254865123776,\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":{}}]";

    private String taskDefinitionJson =
            "[{\"code\":4254862762304,\"name\":\"test1\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
                    + "\"localParams\":[],\"rawScript\":\"echo 1\",\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},\"switchResult\":{}},\"flag\":\"YES\","
                    + "\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":null,\"timeout\":0,"
                    + "\"environmentCode\":-1},{\"code\":4254865123776,\"name\":\"test2\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
                    + "\"localParams\":[],\"rawScript\":\"echo 2\",\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},\"switchResult\":{}},\"flag\":\"YES\","
                    + "\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"WARN\",\"timeout\":0,"
                    + "\"environmentCode\":-1}]";

    @Test
    public void testQueryWorkflowInstanceList() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        doThrow(new ServiceException())
                .when(projectService)
                .checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_INSTANCE);
        assertThrows(ServiceException.class, () -> {
            workflowInstanceService.queryWorkflowInstanceList(
                    loginUser,
                    projectCode,
                    46,
                    "2020-01-01 00:00:00",
                    "2020-01-02 00:00:00",
                    "",
                    "test_user",
                    WorkflowExecutionStatus.RUNNING_EXECUTION,
                    "192.168.xx.xx",
                    "",
                    1,
                    10);
        });

        Date start = DateUtils.stringToDate("2020-01-01 00:00:00");
        Date end = DateUtils.stringToDate("2020-01-02 00:00:00");
        WorkflowInstance workflowInstance = getProcessInstance();
        List<WorkflowInstance> workflowInstanceList = new ArrayList<>();
        Page<WorkflowInstance> pageReturn = new Page<>(1, 10);
        workflowInstanceList.add(workflowInstance);
        pageReturn.setRecords(workflowInstanceList);

        // data parameter check
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(Mockito.any(),
                Mockito.any(Project.class),
                Mockito.any());
        when(workflowDefinitionMapper.selectById(Mockito.anyInt())).thenReturn(getProcessDefinition());
        when(workflowInstanceMapper.queryWorkflowInstanceListPaging(Mockito.any(Page.class), Mockito.any(),
                Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                eq("192.168.xx.xx"), Mockito.any(), Mockito.any())).thenReturn(pageReturn);
        assertThrows(ServiceException.class, () -> workflowInstanceService.queryWorkflowInstanceList(
                loginUser,
                projectCode,
                1,
                "20200101 00:00:00",
                "20200102 00:00:00",
                "",
                loginUser.getUserName(),
                WorkflowExecutionStatus.RUNNING_EXECUTION,
                "192.168.xx.xx",
                "",
                1,
                10));

        // project auth success
        putMsg(result, Status.SUCCESS, projectCode);

        doNothing().when(projectService).checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_INSTANCE);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(workflowInstanceMapper.queryWorkflowInstanceListPaging(
                Mockito.any(Page.class),
                eq(project.getCode()),
                eq(1L),
                eq(""),
                eq(""),
                Mockito.any(),
                eq("192.168.xx.xx"),
                eq(start),
                eq(end)))
                        .thenReturn(pageReturn);
        when(usersService.queryUser(workflowInstance.getExecutorId())).thenReturn(loginUser);

        Result successRes =
                workflowInstanceService.queryWorkflowInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
                        "2020-01-02 00:00:00", "", loginUser.getUserName(), WorkflowExecutionStatus.RUNNING_EXECUTION,
                        "192.168.xx.xx", "", 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());

        // data parameter empty
        when(workflowInstanceMapper.queryWorkflowInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()),
                eq(1L), eq(""), eq(""), Mockito.any(),
                eq("192.168.xx.xx"), eq(null), eq(null))).thenReturn(pageReturn);
        successRes = workflowInstanceService.queryWorkflowInstanceList(loginUser, projectCode, 1, "",
                "", "", loginUser.getUserName(), WorkflowExecutionStatus.RUNNING_EXECUTION,
                "192.168.xx.xx", "", 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());

        // executor null
        when(usersService.queryUser(loginUser.getId())).thenReturn(null);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(-1);
        Result executorExistRes =
                workflowInstanceService.queryWorkflowInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
                        "2020-01-02 00:00:00", "", "admin", WorkflowExecutionStatus.RUNNING_EXECUTION,
                        "192.168.xx.xx", "", 1, 10);

        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) executorExistRes.getCode());

        // executor name empty
        when(workflowInstanceMapper.queryWorkflowInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()),
                eq(1L), eq(""), eq("admin"), Mockito.any(),
                eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        Result executorEmptyRes =
                workflowInstanceService.queryWorkflowInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
                        "2020-01-02 00:00:00", "", "", WorkflowExecutionStatus.RUNNING_EXECUTION,
                        "192.168.xx.xx", "", 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) executorEmptyRes.getCode());

    }

    @Test
    public void queryByTriggerCode() {
        long projectCode = 666L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> proejctAuthFailMap =
                workflowInstanceService.queryByTriggerCode(loginUser, projectCode, 999L);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, proejctAuthFailMap.get(Constants.STATUS));
        // project auth sucess
        putMsg(result, Status.SUCCESS, projectCode);
        when(workflowInstanceMapper.queryByTriggerCode(projectCode)).thenReturn(new ArrayList());
        proejctAuthFailMap =
                workflowInstanceService.queryByTriggerCode(loginUser, projectCode, 999L);
        Assertions.assertEquals(Status.SUCCESS, proejctAuthFailMap.get(Constants.STATUS));
    }

    @Test
    public void testQueryTopNLongestRunningWorkflowInstance() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        int size = 10;
        String startTime = "2020-01-01 00:00:00";
        String endTime = "2020-08-02 00:00:00";
        Date start = DateUtils.stringToDate(startTime);
        Date end = DateUtils.stringToDate(endTime);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes = workflowInstanceService
                .queryTopNLongestRunningWorkflowInstance(loginUser, projectCode, size, startTime, endTime);

        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // project auth success
        putMsg(result, Status.SUCCESS, projectCode);
        WorkflowInstance workflowInstance = getProcessInstance();
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        projectAuthFailRes = workflowInstanceService
                .queryTopNLongestRunningWorkflowInstance(loginUser, projectCode, -1, startTime, endTime);
        Assertions.assertEquals(Status.NEGTIVE_SIZE_NUMBER_ERROR, projectAuthFailRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(usersService.queryUser(workflowInstance.getExecutorId())).thenReturn(loginUser);
        Map<String, Object> successRes = workflowInstanceService.queryTopNLongestRunningWorkflowInstance(loginUser,
                projectCode, size, startTime, endTime);

        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testTopNLongestRunningProcessInstanceFailure() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        int size = 10;
        String startTime = "2020-01-01 00:00:00";
        String endTime = "2020-08-02 00:00:00";

        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> startTimeBiggerFailRes = workflowInstanceService
                .queryTopNLongestRunningWorkflowInstance(loginUser, projectCode, size, endTime, startTime);
        Assertions.assertEquals(Status.START_TIME_BIGGER_THAN_END_TIME_ERROR,
                startTimeBiggerFailRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> dataNullFailRes = workflowInstanceService
                .queryTopNLongestRunningWorkflowInstance(loginUser, projectCode, size, null, endTime);
        Assertions.assertEquals(Status.DATA_IS_NULL, dataNullFailRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        dataNullFailRes = workflowInstanceService
                .queryTopNLongestRunningWorkflowInstance(loginUser, projectCode, size, startTime, null);
        Assertions.assertEquals(Status.DATA_IS_NULL, dataNullFailRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryWorkflowInstanceById() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes =
                workflowInstanceService.queryWorkflowInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // project auth success
        WorkflowInstance workflowInstance = getProcessInstance();
        putMsg(result, Status.SUCCESS, projectCode);
        WorkflowDefinition workflowDefinition = getProcessDefinition();
        workflowDefinition.setProjectCode(projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findWorkflowInstanceDetailById(workflowInstance.getId()))
                .thenReturn(Optional.of(workflowInstance));
        when(processService.findWorkflowDefinition(workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getWorkflowDefinitionVersion())).thenReturn(workflowDefinition);
        Map<String, Object> successRes = workflowInstanceService.queryWorkflowInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        // worker group null
        Map<String, Object> workerNullRes =
                workflowInstanceService.queryWorkflowInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, workerNullRes.get(Constants.STATUS));

        // worker group exist
        WorkerGroup workerGroup = getWorkGroup();
        Map<String, Object> workerExistRes =
                workflowInstanceService.queryWorkflowInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, workerExistRes.get(Constants.STATUS));

        when(processService.findWorkflowDefinition(workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getWorkflowDefinitionVersion())).thenReturn(null);;
        workerExistRes = workflowInstanceService.queryWorkflowInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST, workerExistRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryTaskListByWorkflowInstanceId() throws IOException {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes =
                workflowInstanceService.queryTaskListByWorkflowInstanceId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // project auth success
        putMsg(result, Status.SUCCESS, projectCode);
        WorkflowInstance workflowInstance = getProcessInstance();
        workflowInstance.setState(WorkflowExecutionStatus.SUCCESS);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(0);
        taskInstance.setTaskType("SHELL");
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);
        List<DependentResultTaskInstanceContext> dependentResultTaskInstanceContextList = new ArrayList<>();
        TaskInstanceContext taskInstanceContext = new TaskInstanceContext();
        taskInstanceContext.setTaskInstanceId(0);
        taskInstanceContext.setContextType(ContextType.DEPENDENT_RESULT_CONTEXT);
        DependentResultTaskInstanceContext dependentResultTaskInstanceContext =
                new DependentResultTaskInstanceContext();
        dependentResultTaskInstanceContext.setProjectCode(projectCode);
        dependentResultTaskInstanceContext.setDependentResult(DependResult.SUCCESS);
        taskInstanceContext.setTaskInstanceContext(
                Lists.asList(dependentResultTaskInstanceContext, new DependentResultTaskInstanceContext[0]));
        List<Integer> taskInstanceIdList = new ArrayList<>();
        taskInstanceIdList.add(0);
        Result res = new Result();
        res.setCode(Status.SUCCESS.ordinal());
        res.setData("xxx");
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findWorkflowInstanceDetailById(workflowInstance.getId()))
                .thenReturn(Optional.of(workflowInstance));
        when(taskInstanceDao.queryValidTaskListByWorkflowInstanceId(workflowInstance.getId(),
                workflowInstance.getTestFlag()))
                        .thenReturn(taskInstanceList);
        when(loggerService.queryLog(loginUser, taskInstance.getId(), 0, 4098)).thenReturn(res);
        when(taskInstanceContextDao.batchQueryByTaskInstanceIdsAndContextType(taskInstanceIdList,
                ContextType.DEPENDENT_RESULT_CONTEXT))
                        .thenReturn(Lists.asList(taskInstanceContext, new TaskInstanceContext[0]));
        Map<String, Object> successRes =
                workflowInstanceService.queryTaskListByWorkflowInstanceId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQuerySubWorkflowInstanceByTaskId() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes =
                workflowInstanceService.querySubWorkflowInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // task null
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(taskInstanceDao.queryById(1)).thenReturn(null);
        Map<String, Object> taskNullRes =
                workflowInstanceService.querySubWorkflowInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_EXISTS, taskNullRes.get(Constants.STATUS));

        // task not sub process
        TaskInstance taskInstance = getTaskInstance();
        taskInstance.setTaskType("HTTP");
        taskInstance.setWorkflowInstanceId(1);
        putMsg(result, Status.SUCCESS, projectCode);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        Map<String, Object> notSubprocessRes =
                workflowInstanceService.querySubWorkflowInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, notSubprocessRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        taskDefinition.setProjectCode(0L);
        notSubprocessRes = workflowInstanceService.querySubWorkflowInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_EXISTS, notSubprocessRes.get(Constants.STATUS));

        taskDefinition.setProjectCode(projectCode);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        // sub process not exist
        TaskInstance subTask = getTaskInstance();
        subTask.setTaskType("SUB_WORKFLOW");
        subTask.setWorkflowInstanceId(1);
        putMsg(result, Status.SUCCESS, projectCode);
        when(taskInstanceDao.queryById(subTask.getId())).thenReturn(subTask);
        when(processService.findSubWorkflowInstance(subTask.getWorkflowInstanceId(), subTask.getId())).thenReturn(null);
        Map<String, Object> subprocessNotExistRes =
                workflowInstanceService.querySubWorkflowInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST, subprocessNotExistRes.get(Constants.STATUS));

        // sub process exist
        WorkflowInstance workflowInstance = getProcessInstance();
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findSubWorkflowInstance(taskInstance.getWorkflowInstanceId(), taskInstance.getId()))
                .thenReturn(workflowInstance);
        Map<String, Object> subprocessExistRes =
                workflowInstanceService.querySubWorkflowInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, subprocessExistRes.get(Constants.STATUS));
    }

    @Test
    public void testUpdateWorkflowInstance() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        doThrow(new ServiceException(Status.PROJECT_NOT_FOUND, projectCode))
                .when(projectService)
                .checkProjectAndAuthThrowException(loginUser, projectCode, INSTANCE_UPDATE);
        Assertions.assertThrows(ServiceException.class,
                () -> workflowInstanceService.updateWorkflowInstance(loginUser, projectCode, 1,
                        shellJson, taskJson, "2020-02-21 00:00:00", true, "", "", 0));

        // process instance null
        putMsg(result, Status.SUCCESS, projectCode);
        WorkflowInstance workflowInstance = getProcessInstance();
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        doNothing()
                .when(projectService)
                .checkProjectAndAuthThrowException(loginUser, projectCode, INSTANCE_UPDATE);
        when(processService.findWorkflowInstanceDetailById(1)).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> {
            workflowInstanceService.updateWorkflowInstance(loginUser, projectCode, 1,
                    shellJson, taskJson, "2020-02-21 00:00:00", true, "", "", 0);
        });
        // process instance not finish
        when(processService.findWorkflowInstanceDetailById(1)).thenReturn(Optional.ofNullable(workflowInstance));
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        putMsg(result, Status.SUCCESS, projectCode);
        Map<String, Object> processInstanceNotFinishRes =
                workflowInstanceService.updateWorkflowInstance(loginUser, projectCode, 1,
                        shellJson, taskJson, "2020-02-21 00:00:00", true, "", "", 0);
        Assertions.assertEquals(Status.WORKFLOW_INSTANCE_STATE_OPERATION_ERROR,
                processInstanceNotFinishRes.get(Constants.STATUS));

        // process instance finish
        workflowInstance.setState(WorkflowExecutionStatus.SUCCESS);
        workflowInstance.setTimeout(3000);
        workflowInstance.setCommandType(CommandType.STOP);
        workflowInstance.setWorkflowDefinitionCode(46L);
        workflowInstance.setWorkflowDefinitionVersion(1);
        WorkflowDefinition workflowDefinition = getProcessDefinition();
        workflowDefinition.setId(1);
        workflowDefinition.setUserId(1);
        workflowDefinition.setProjectCode(projectCode);
        Tenant tenant = getTenant();
        when(workflowDefinitionMapper.queryByCode(46L)).thenReturn(workflowDefinition);
        when(tenantMapper.queryByTenantCode("root")).thenReturn(tenant);
        when(processService.getTenantForWorkflow(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(tenant.getTenantCode());
        when(workflowInstanceDao.updateById(workflowInstance)).thenReturn(true);
        when(processService.saveWorkflowDefine(loginUser, workflowDefinition, Boolean.TRUE, Boolean.FALSE))
                .thenReturn(1);

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        when(workflowDefinitionService.checkWorkflowNodeList(taskRelationJson, taskDefinitionLogs)).thenReturn(result);
        putMsg(result, Status.SUCCESS, projectCode);

        try (
                MockedStatic<TaskPluginManager> taskPluginManagerMockedStatic =
                        Mockito.mockStatic(TaskPluginManager.class)) {
            taskPluginManagerMockedStatic
                    .when(() -> TaskPluginManager.checkTaskParameters(Mockito.any(), Mockito.any()))
                    .thenReturn(true);
            Map<String, Object> processInstanceFinishRes =
                    workflowInstanceService.updateWorkflowInstance(loginUser, projectCode, 1,
                            taskRelationJson, taskDefinitionJson, "2020-02-21 00:00:00", true, "", "", 0);
            Assertions.assertEquals(Status.SUCCESS, processInstanceFinishRes.get(Constants.STATUS));

            // success
            when(workflowDefinitionMapper.queryByCode(46L)).thenReturn(workflowDefinition);
            putMsg(result, Status.SUCCESS, projectCode);

            when(processService.saveWorkflowDefine(loginUser, workflowDefinition, Boolean.FALSE, Boolean.FALSE))
                    .thenReturn(1);
            Map<String, Object> successRes = workflowInstanceService.updateWorkflowInstance(loginUser, projectCode, 1,
                    taskRelationJson, taskDefinitionJson, "2020-02-21 00:00:00", Boolean.FALSE, "", "", 0);
            Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
        }
    }

    @Test
    public void testQueryParentInstanceBySubId() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes =
                workflowInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // process instance null
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findWorkflowInstanceDetailById(1)).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> {
            workflowInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        });

        // not sub process
        WorkflowInstance workflowInstance = getProcessInstance();
        workflowInstance.setIsSubWorkflow(Flag.NO);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findWorkflowInstanceDetailById(1)).thenReturn(Optional.ofNullable(workflowInstance));
        Map<String, Object> notSubProcessRes =
                workflowInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.WORKFLOW_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE,
                notSubProcessRes.get(Constants.STATUS));

        // sub process
        workflowInstance.setIsSubWorkflow(Flag.YES);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findParentWorkflowInstance(1)).thenReturn(null);
        Map<String, Object> subProcessNullRes =
                workflowInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUB_WORKFLOW_INSTANCE_NOT_EXIST, subProcessNullRes.get(Constants.STATUS));

        // success
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findParentWorkflowInstance(1)).thenReturn(workflowInstance);
        Map<String, Object> successRes = workflowInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testDeleteWorkflowInstanceById() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, INSTANCE_DELETE)).thenReturn(result);

        assertThrows(ServiceException.class,
                () -> workflowInstanceService.deleteWorkflowInstanceById(loginUser, 1));

        // not sub process
        WorkflowInstance workflowInstance = getProcessInstance();
        workflowInstance.setIsSubWorkflow(Flag.NO);
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findWorkflowInstanceDetailById(1)).thenReturn(Optional.ofNullable(workflowInstance));
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(new WorkflowDefinitionLog());
        assertThrows(ServiceException.class,
                () -> workflowInstanceService.deleteWorkflowInstanceById(loginUser, 1));

        workflowInstance.setState(WorkflowExecutionStatus.SUCCESS);
        workflowInstance.setState(WorkflowExecutionStatus.SUCCESS);
        workflowInstance.setTimeout(3000);
        workflowInstance.setCommandType(CommandType.STOP);
        workflowInstance.setWorkflowDefinitionCode(46L);
        workflowInstance.setWorkflowDefinitionVersion(1);
        WorkflowDefinition workflowDefinition = getProcessDefinition();
        workflowDefinition.setId(1);
        workflowDefinition.setUserId(1);
        workflowDefinition.setProjectCode(0L);
        when(workflowDefinitionMapper.queryByCode(46L)).thenReturn(workflowDefinition);
        when(processService.findWorkflowInstanceDetailById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(ServiceException.class,
                () -> workflowInstanceService.deleteWorkflowInstanceById(loginUser, 1));

        workflowDefinition.setProjectCode(projectCode);
        when(processService.findWorkflowInstanceDetailById(Mockito.anyInt())).thenReturn(Optional.of(workflowInstance));
        when(processService.deleteWorkflowInstanceById(1)).thenReturn(1);
        workflowInstanceService.deleteWorkflowInstanceById(loginUser, 1);

        when(processService.deleteWorkflowInstanceById(1)).thenReturn(0);
        Assertions.assertDoesNotThrow(() -> workflowInstanceService.deleteWorkflowInstanceById(loginUser, 1));
    }

    @Test
    public void testViewVariables() {
        // process instance not null
        WorkflowInstance workflowInstance = getProcessInstance();
        workflowInstance.setCommandType(CommandType.SCHEDULER);
        workflowInstance.setScheduleTime(new Date());
        workflowInstance.setGlobalParams("");
        when(workflowInstanceMapper.queryDetailById(1)).thenReturn(workflowInstance);
        Map<String, Object> successRes = workflowInstanceService.viewVariables(1L, 1);

        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        when(workflowInstanceMapper.queryDetailById(1)).thenReturn(null);
        Map<String, Object> processNotExist = workflowInstanceService.viewVariables(1L, 1);
        Assertions.assertEquals(Status.WORKFLOW_INSTANCE_NOT_EXIST, processNotExist.get(Constants.STATUS));
    }

    @Test
    public void testViewGantt() throws Exception {
        WorkflowInstance workflowInstance = getProcessInstance();
        TaskInstance taskInstance = getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        when(workflowInstanceMapper.queryDetailById(1)).thenReturn(workflowInstance);
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getWorkflowDefinitionVersion())).thenReturn(new WorkflowDefinitionLog());
        when(workflowInstanceMapper.queryDetailById(1)).thenReturn(workflowInstance);
        DAG<Long, TaskNode, TaskNodeRelation> graph = new DAG<>();
        for (long i = 1; i <= 7; ++i) {
            graph.addNode(i, new TaskNode());
        }

        when(processService.genDagGraph(Mockito.any(WorkflowDefinition.class)))
                .thenReturn(graph);

        Map<String, Object> successRes = workflowInstanceService.viewGantt(0L, 1);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        when(workflowInstanceMapper.queryDetailById(1)).thenReturn(null);
        Map<String, Object> processNotExist = workflowInstanceService.viewVariables(1L, 1);
        Assertions.assertEquals(Status.WORKFLOW_INSTANCE_NOT_EXIST, processNotExist.get(Constants.STATUS));
    }

    /**
     * get Mock Admin User
     *
     * @return admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.GENERAL_USER);
        return loginUser;
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
        project.setName("project_test1");
        project.setUserId(1);
        return project;
    }

    /**
     * get Mock process instance
     *
     * @return process instance
     */
    private WorkflowInstance getProcessInstance() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test_process_instance");
        workflowInstance.setWorkflowDefinitionCode(46L);
        workflowInstance.setWorkflowDefinitionVersion(1);
        workflowInstance.setStartTime(new Date());
        workflowInstance.setEndTime(new Date());
        return workflowInstance;
    }

    /**
     * get mock processDefinition
     *
     * @return ProcessDefinition
     */
    private WorkflowDefinition getProcessDefinition() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(46L);
        workflowDefinition.setVersion(1);
        workflowDefinition.setId(46);
        workflowDefinition.setName("test_pdf");
        workflowDefinition.setProjectCode(2L);
        workflowDefinition.setDescription("");
        return workflowDefinition;
    }

    private Tenant getTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("root");
        return tenant;
    }

    /**
     * get Mock worker group
     *
     * @return worker group
     */
    private WorkerGroup getWorkGroup() {
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName("test_workergroup");
        return workerGroup;
    }

    /**
     * get Mock task instance
     *
     * @return task instance
     */
    private TaskInstance getTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setExecutorId(-1);
        return taskInstance;
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
