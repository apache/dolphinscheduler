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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProcessInstanceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceMapDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * process instance service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessInstanceServiceTest {

    @InjectMocks
    ProcessInstanceServiceImpl processInstanceService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectServiceImpl projectService;

    @Mock
    ProcessService processService;

    @Mock
    TaskInstanceDao taskInstanceDao;

    @Mock
    ProcessInstanceDao processInstanceDao;

    @Mock
    ProcessInstanceMapper processInstanceMapper;

    @Mock
    ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Mock
    ProcessDefinitionMapper processDefineMapper;

    @Mock
    ProcessDefinitionService processDefinitionService;

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
    TaskPluginManager taskPluginManager;

    @Mock
    ScheduleMapper scheduleMapper;

    @Mock
    CuringParamsService curingGlobalParamsService;

    @Mock
    AlertDao alertDao;

    @Mock
    private TaskInstanceService taskInstanceService;

    @Mock
    private ProcessInstanceMapDao processInstanceMapDao;

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
    public void testQueryProcessInstanceList() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(null);
        Mockito.doThrow(new ServiceException()).when(projectService).checkProjectAndAuthThrowException(Mockito.any(),
                Mockito.any(), Mockito.any());
        Assertions.assertThrows(ServiceException.class, () -> {
            processInstanceService.queryProcessInstanceList(loginUser, projectCode, 46, "2020-01-01 00:00:00",
                    "2020-01-02 00:00:00", "", "test_user", WorkflowExecutionStatus.SUBMITTED_SUCCESS,
                    "192.168.xx.xx", "", 1, 10);
        });

        Date start = DateUtils.stringToDate("2020-01-01 00:00:00");
        Date end = DateUtils.stringToDate("2020-01-02 00:00:00");
        ProcessInstance processInstance = getProcessInstance();
        List<ProcessInstance> processInstanceList = new ArrayList<>();
        Page<ProcessInstance> pageReturn = new Page<>(1, 10);
        processInstanceList.add(processInstance);
        pageReturn.setRecords(processInstanceList);

        // data parameter check
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(Mockito.any(), Mockito.any(),
                Mockito.any());
        when(processDefineMapper.selectById(Mockito.anyInt())).thenReturn(getProcessDefinition());
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                eq("192.168.xx.xx"), Mockito.any(), Mockito.any())).thenReturn(pageReturn);
        Assertions.assertThrows(ServiceException.class, () -> processInstanceService.queryProcessInstanceList(
                loginUser,
                projectCode,
                1,
                "20200101 00:00:00",
                "20200102 00:00:00",
                "",
                loginUser.getUserName(),
                WorkflowExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx",
                "",
                1,
                10));

        // project auth success
        putMsg(result, Status.SUCCESS, projectCode);

        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()),
                eq(1L), eq(""), eq(""), Mockito.any(),
                eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);

        Result successRes =
                processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
                        "2020-01-02 00:00:00", "", loginUser.getUserName(), WorkflowExecutionStatus.SUBMITTED_SUCCESS,
                        "192.168.xx.xx", "", 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());

        // data parameter empty
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()),
                eq(1L), eq(""), eq(""), Mockito.any(),
                eq("192.168.xx.xx"), eq(null), eq(null))).thenReturn(pageReturn);
        successRes = processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "",
                "", "", loginUser.getUserName(), WorkflowExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", "", 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());

        // executor null
        when(usersService.queryUser(loginUser.getId())).thenReturn(null);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(-1);
        Result executorExistRes =
                processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
                        "2020-01-02 00:00:00", "", "admin", WorkflowExecutionStatus.SUBMITTED_SUCCESS,
                        "192.168.xx.xx", "", 1, 10);

        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) executorExistRes.getCode());

        // executor name empty
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()),
                eq(1L), eq(""), eq("admin"), Mockito.any(),
                eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        Result executorEmptyRes =
                processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
                        "2020-01-02 00:00:00", "", "", WorkflowExecutionStatus.SUBMITTED_SUCCESS,
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
                processInstanceService.queryByTriggerCode(loginUser, projectCode, 999L);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, proejctAuthFailMap.get(Constants.STATUS));
        // project auth sucess
        putMsg(result, Status.SUCCESS, projectCode);
        when(processInstanceMapper.queryByTriggerCode(projectCode)).thenReturn(new ArrayList());
        proejctAuthFailMap =
                processInstanceService.queryByTriggerCode(loginUser, projectCode, 999L);
        Assertions.assertEquals(Status.SUCCESS, proejctAuthFailMap.get(Constants.STATUS));
    }

    @Test
    public void testQueryTopNLongestRunningProcessInstance() {
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
        Map<String, Object> projectAuthFailRes = processInstanceService
                .queryTopNLongestRunningProcessInstance(loginUser, projectCode, size, startTime, endTime);

        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // project auth success
        putMsg(result, Status.SUCCESS, projectCode);
        ProcessInstance processInstance = getProcessInstance();
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        projectAuthFailRes = processInstanceService
                .queryTopNLongestRunningProcessInstance(loginUser, projectCode, -1, startTime, endTime);
        Assertions.assertEquals(Status.NEGTIVE_SIZE_NUMBER_ERROR, projectAuthFailRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);
        Map<String, Object> successRes = processInstanceService.queryTopNLongestRunningProcessInstance(loginUser,
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
        Map<String, Object> startTimeBiggerFailRes = processInstanceService
                .queryTopNLongestRunningProcessInstance(loginUser, projectCode, size, endTime, startTime);
        Assertions.assertEquals(Status.START_TIME_BIGGER_THAN_END_TIME_ERROR,
                startTimeBiggerFailRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> dataNullFailRes = processInstanceService
                .queryTopNLongestRunningProcessInstance(loginUser, projectCode, size, null, endTime);
        Assertions.assertEquals(Status.DATA_IS_NULL, dataNullFailRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        dataNullFailRes = processInstanceService
                .queryTopNLongestRunningProcessInstance(loginUser, projectCode, size, startTime, null);
        Assertions.assertEquals(Status.DATA_IS_NULL, dataNullFailRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessInstanceById() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes =
                processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // project auth success
        ProcessInstance processInstance = getProcessInstance();
        putMsg(result, Status.SUCCESS, projectCode);
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProjectCode(projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(processInstance.getId()))
                .thenReturn(Optional.of(processInstance));
        when(processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion())).thenReturn(processDefinition);
        Map<String, Object> successRes = processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        // worker group null
        Map<String, Object> workerNullRes = processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, workerNullRes.get(Constants.STATUS));

        // worker group exist
        WorkerGroup workerGroup = getWorkGroup();
        Map<String, Object> workerExistRes = processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, workerExistRes.get(Constants.STATUS));

        when(processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion())).thenReturn(null);;
        workerExistRes = processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, workerExistRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryTaskListByProcessId() throws IOException {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes =
                processInstanceService.queryTaskListByProcessId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // project auth success
        putMsg(result, Status.SUCCESS, projectCode);
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setState(WorkflowExecutionStatus.SUCCESS);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(0);
        taskInstance.setTaskType("SHELL");
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);
        Result res = new Result();
        res.setCode(Status.SUCCESS.ordinal());
        res.setData("xxx");
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(processInstance.getId()))
                .thenReturn(Optional.of(processInstance));
        when(taskInstanceDao.queryValidTaskListByWorkflowInstanceId(processInstance.getId(),
                processInstance.getTestFlag()))
                        .thenReturn(taskInstanceList);
        when(loggerService.queryLog(loginUser, taskInstance.getId(), 0, 4098)).thenReturn(res);
        Map<String, Object> successRes = processInstanceService.queryTaskListByProcessId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testParseLogForDependentResult() throws IOException {
        String logString =
                "[INFO] 2019-03-19 17:11:08.475 org.apache.dolphinscheduler.server.worker.log.TaskLogger:[172]"
                        + " - [taskAppId=TASK_223_10739_452334] dependent item complete, :|| dependentKey: 223-ALL-day-last1Day, result: SUCCESS, dependentDate: Wed Mar 19 17:10:36 CST 2019\n"
                        + "[INFO] 2019-03-19 17:11:08.476 org.apache.dolphinscheduler.server.worker.runner.TaskScheduleThread:[172]"
                        + " - task : 223_10739_452334 exit status code : 0\n"
                        + "[root@node2 current]# ";
        Map<String, DependResult> resultMap =
                processInstanceService.parseLogForDependentResult(logString);
        Assertions.assertEquals(1, resultMap.size());

        resultMap.clear();
        resultMap = processInstanceService.parseLogForDependentResult("");
        Assertions.assertEquals(0, resultMap.size());
    }

    @Test
    public void testQuerySubProcessInstanceByTaskId() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes =
                processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // task null
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(taskInstanceDao.queryById(1)).thenReturn(null);
        Map<String, Object> taskNullRes =
                processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_EXISTS, taskNullRes.get(Constants.STATUS));

        // task not sub process
        TaskInstance taskInstance = getTaskInstance();
        taskInstance.setTaskType("HTTP");
        taskInstance.setProcessInstanceId(1);
        putMsg(result, Status.SUCCESS, projectCode);
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance);
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        Map<String, Object> notSubprocessRes =
                processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, notSubprocessRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        taskDefinition.setProjectCode(0L);
        notSubprocessRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_EXISTS, notSubprocessRes.get(Constants.STATUS));

        taskDefinition.setProjectCode(projectCode);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        // sub process not exist
        TaskInstance subTask = getTaskInstance();
        subTask.setTaskType("SUB_PROCESS");
        subTask.setProcessInstanceId(1);
        putMsg(result, Status.SUCCESS, projectCode);
        when(taskInstanceDao.queryById(subTask.getId())).thenReturn(subTask);
        when(processService.findSubProcessInstance(subTask.getProcessInstanceId(), subTask.getId())).thenReturn(null);
        Map<String, Object> subprocessNotExistRes =
                processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUB_PROCESS_INSTANCE_NOT_EXIST, subprocessNotExistRes.get(Constants.STATUS));

        // sub process exist
        ProcessInstance processInstance = getProcessInstance();
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findSubProcessInstance(taskInstance.getProcessInstanceId(), taskInstance.getId()))
                .thenReturn(processInstance);
        Map<String, Object> subprocessExistRes =
                processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, subprocessExistRes.get(Constants.STATUS));
    }

    @Test
    public void testUpdateProcessInstance() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, INSTANCE_UPDATE)).thenReturn(result);
        Map<String, Object> projectAuthFailRes = processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
                shellJson, taskJson, "2020-02-21 00:00:00", true, "", "", 0);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // process instance null
        putMsg(result, Status.SUCCESS, projectCode);
        ProcessInstance processInstance = getProcessInstance();
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, INSTANCE_UPDATE)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(Optional.empty());
        Assertions.assertThrows(ServiceException.class, () -> {
            processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
                    shellJson, taskJson, "2020-02-21 00:00:00", true, "", "", 0);
        });
        // process instance not finish
        when(processService.findProcessInstanceDetailById(1)).thenReturn(Optional.ofNullable(processInstance));
        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        putMsg(result, Status.SUCCESS, projectCode);
        Map<String, Object> processInstanceNotFinishRes =
                processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
                        shellJson, taskJson, "2020-02-21 00:00:00", true, "", "", 0);
        Assertions.assertEquals(Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR,
                processInstanceNotFinishRes.get(Constants.STATUS));

        // process instance finish
        processInstance.setState(WorkflowExecutionStatus.SUCCESS);
        processInstance.setTimeout(3000);
        processInstance.setCommandType(CommandType.STOP);
        processInstance.setProcessDefinitionCode(46L);
        processInstance.setProcessDefinitionVersion(1);
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setId(1);
        processDefinition.setUserId(1);
        processDefinition.setProjectCode(projectCode);
        Tenant tenant = getTenant();
        when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        when(tenantMapper.queryByTenantCode("root")).thenReturn(tenant);
        when(processService.getTenantForProcess(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(tenant.getTenantCode());
        when(processInstanceDao.updateById(processInstance)).thenReturn(true);
        when(processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.FALSE)).thenReturn(1);

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        when(processDefinitionService.checkProcessNodeList(taskRelationJson, taskDefinitionLogs)).thenReturn(result);
        putMsg(result, Status.SUCCESS, projectCode);
        when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);
        Map<String, Object> processInstanceFinishRes =
                processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
                        taskRelationJson, taskDefinitionJson, "2020-02-21 00:00:00", true, "", "", 0);
        Assertions.assertEquals(Status.SUCCESS, processInstanceFinishRes.get(Constants.STATUS));

        // success
        when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        putMsg(result, Status.SUCCESS, projectCode);

        when(processService.saveProcessDefine(loginUser, processDefinition, Boolean.FALSE, Boolean.FALSE))
                .thenReturn(1);
        Map<String, Object> successRes = processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
                taskRelationJson, taskDefinitionJson, "2020-02-21 00:00:00", Boolean.FALSE, "", "", 0);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
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
                processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, projectAuthFailRes.get(Constants.STATUS));

        // process instance null
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(Optional.empty());
        Assertions.assertThrows(ServiceException.class, () -> {
            processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        });

        // not sub process
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setIsSubProcess(Flag.NO);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(Optional.ofNullable(processInstance));
        Map<String, Object> notSubProcessRes =
                processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE,
                notSubProcessRes.get(Constants.STATUS));

        // sub process
        processInstance.setIsSubProcess(Flag.YES);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findParentProcessInstance(1)).thenReturn(null);
        Map<String, Object> subProcessNullRes =
                processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUB_PROCESS_INSTANCE_NOT_EXIST, subProcessNullRes.get(Constants.STATUS));

        // success
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findParentProcessInstance(1)).thenReturn(processInstance);
        Map<String, Object> successRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testDeleteProcessInstanceById() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, INSTANCE_DELETE)).thenReturn(result);

        Assertions.assertThrows(ServiceException.class,
                () -> processInstanceService.deleteProcessInstanceById(loginUser, 1));

        // not sub process
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setIsSubProcess(Flag.NO);
        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(Optional.ofNullable(processInstance));
        when(processDefinitionLogMapper.queryByDefinitionCodeAndVersion(Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(new ProcessDefinitionLog());
        Assertions.assertThrows(ServiceException.class,
                () -> processInstanceService.deleteProcessInstanceById(loginUser, 1));

        processInstance.setState(WorkflowExecutionStatus.SUCCESS);
        processInstance.setState(WorkflowExecutionStatus.SUCCESS);
        processInstance.setTimeout(3000);
        processInstance.setCommandType(CommandType.STOP);
        processInstance.setProcessDefinitionCode(46L);
        processInstance.setProcessDefinitionVersion(1);
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setId(1);
        processDefinition.setUserId(1);
        processDefinition.setProjectCode(0L);
        when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        when(processService.findProcessInstanceDetailById(Mockito.anyInt())).thenReturn(Optional.empty());
        Assertions.assertThrows(ServiceException.class,
                () -> processInstanceService.deleteProcessInstanceById(loginUser, 1));

        processDefinition.setProjectCode(projectCode);
        when(processService.findProcessInstanceDetailById(Mockito.anyInt())).thenReturn(Optional.of(processInstance));
        when(processService.deleteWorkProcessInstanceById(1)).thenReturn(1);
        processInstanceService.deleteProcessInstanceById(loginUser, 1);

        when(processService.deleteWorkProcessInstanceById(1)).thenReturn(0);
        Assertions.assertDoesNotThrow(() -> processInstanceService.deleteProcessInstanceById(loginUser, 1));
    }

    @Test
    public void testViewVariables() {
        // process instance not null
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setCommandType(CommandType.SCHEDULER);
        processInstance.setScheduleTime(new Date());
        processInstance.setGlobalParams("");
        when(processInstanceMapper.queryDetailById(1)).thenReturn(processInstance);
        Map<String, Object> successRes = processInstanceService.viewVariables(1L, 1);

        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        when(processInstanceMapper.queryDetailById(1)).thenReturn(null);
        Map<String, Object> processNotExist = processInstanceService.viewVariables(1L, 1);
        Assertions.assertEquals(Status.PROCESS_INSTANCE_NOT_EXIST, processNotExist.get(Constants.STATUS));
    }

    @Test
    public void testViewGantt() throws Exception {
        ProcessInstance processInstance = getProcessInstance();
        TaskInstance taskInstance = getTaskInstance();
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        when(processInstanceMapper.queryDetailById(1)).thenReturn(processInstance);
        when(processDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion())).thenReturn(new ProcessDefinitionLog());
        when(processInstanceMapper.queryDetailById(1)).thenReturn(processInstance);
        when(taskInstanceMapper.queryByInstanceIdAndName(Mockito.anyInt(), Mockito.any())).thenReturn(taskInstance);
        DAG<Long, TaskNode, TaskNodeRelation> graph = new DAG<>();
        for (long i = 1; i <= 7; ++i) {
            graph.addNode(i, new TaskNode());
        }

        when(processService.genDagGraph(Mockito.any(ProcessDefinition.class)))
                .thenReturn(graph);

        Map<String, Object> successRes = processInstanceService.viewGantt(0L, 1);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        when(processInstanceMapper.queryDetailById(1)).thenReturn(null);
        Map<String, Object> processNotExist = processInstanceService.viewVariables(1L, 1);
        Assertions.assertEquals(Status.PROCESS_INSTANCE_NOT_EXIST, processNotExist.get(Constants.STATUS));
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
    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("test_process_instance");
        processInstance.setProcessDefinitionCode(46L);
        processInstance.setProcessDefinitionVersion(1);
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        return processInstance;
    }

    /**
     * get mock processDefinition
     *
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(46L);
        processDefinition.setVersion(1);
        processDefinition.setId(46);
        processDefinition.setName("test_pdf");
        processDefinition.setProjectCode(2L);
        processDefinition.setDescription("");
        return processDefinition;
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
