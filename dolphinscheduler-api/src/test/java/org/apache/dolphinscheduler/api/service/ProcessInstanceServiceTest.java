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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProcessInstanceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
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
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.INSTANCE_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.INSTANCE_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_INSTANCE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * process instance service test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
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


    private String shellJson = "[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":123456789,"
        + "\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"},{\"name\":\"\",\"preTaskCode\":123456789,"
        + "\"preTaskVersion\":1,\"postTaskCode\":123451234,\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"}]";

    private String taskJson = "[{\"name\":\"shell1\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
        + "\"localParams\":[],\"rawScript\":\"echo 1\",\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}},"
        + "\"flag\":\"NORMAL\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":\"0\",\"failRetryInterval\":\"1\","
        + "\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":null,\"delayTime\":\"0\"},{\"name\":\"shell2\",\"description\":\"\","
        + "\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 2\",\"conditionResult\":{\"successNode\""
        + ":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{}},\"flag\":\"NORMAL\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\","
        + "\"failRetryTimes\":\"0\",\"failRetryInterval\":\"1\",\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":null,\"delayTime\":\"0\"}]";

    private String taskRelationJson = "[{\"name\":\"\",\"preTaskCode\":4254865123776,\"preTaskVersion\":1,\"postTaskCode\":4254862762304,\"postTaskVersion\":1,\"conditionType\":0,"
        + "\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":4254865123776,\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":{}}]";

    private String taskDefinitionJson = "[{\"code\":4254862762304,\"name\":\"test1\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
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

        //project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_INSTANCE)).thenReturn(result);
        Result proejctAuthFailRes = processInstanceService.queryProcessInstanceList(loginUser, projectCode, 46, "2020-01-01 00:00:00",
            "2020-01-02 00:00:00", "", "test_user", ExecutionStatus.SUBMITTED_SUCCESS,
            "192.168.xx.xx", "",1, 10);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int) proejctAuthFailRes.getCode());

        Date start = DateUtils.getScheduleDate("2020-01-01 00:00:00");
        Date end = DateUtils.getScheduleDate("2020-01-02 00:00:00");
        ProcessInstance processInstance = getProcessInstance();
        List<ProcessInstance> processInstanceList = new ArrayList<>();
        Page<ProcessInstance> pageReturn = new Page<>(1, 10);
        processInstanceList.add(processInstance);
        pageReturn.setRecords(processInstanceList);

        // data parameter check
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        when(processDefineMapper.selectById(Mockito.anyInt())).thenReturn(getProcessDefinition());
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class)
            , Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            eq("192.168.xx.xx"), Mockito.any(), Mockito.any())).thenReturn(pageReturn);

        Result dataParameterRes = processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "20200101 00:00:00",
            "20200102 00:00:00", "", loginUser.getUserName(), ExecutionStatus.SUBMITTED_SUCCESS,
            "192.168.xx.xx", "",1, 10);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) dataParameterRes.getCode());

        //project auth success
        putMsg(result, Status.SUCCESS, projectCode);

        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()), eq(1L), eq(""), eq(-1), Mockito.any(),
            eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);

        Result successRes = processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
            "2020-01-02 00:00:00", "", loginUser.getUserName(), ExecutionStatus.SUBMITTED_SUCCESS,
            "192.168.xx.xx", "",1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)successRes.getCode());

        // data parameter empty
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()), eq(1L), eq(""), eq(-1), Mockito.any(),
            eq("192.168.xx.xx"), eq(null), eq(null))).thenReturn(pageReturn);
        successRes = processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "",
            "", "", loginUser.getUserName(), ExecutionStatus.SUBMITTED_SUCCESS,
            "192.168.xx.xx", "",1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)successRes.getCode());

        //executor null
        when(usersService.queryUser(loginUser.getId())).thenReturn(null);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(-1);
        Result executorExistRes = processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
            "2020-01-02 00:00:00", "", "admin", ExecutionStatus.SUBMITTED_SUCCESS,
            "192.168.xx.xx", "",1, 10);

        Assert.assertEquals(Status.SUCCESS.getCode(), (int)executorExistRes.getCode());

        //executor name empty
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()), eq(1L), eq(""), eq(0), Mockito.any(),
            eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        Result executorEmptyRes = processInstanceService.queryProcessInstanceList(loginUser, projectCode, 1, "2020-01-01 00:00:00",
            "2020-01-02 00:00:00", "", "", ExecutionStatus.SUBMITTED_SUCCESS,
            "192.168.xx.xx", "",1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)executorEmptyRes.getCode());

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
        Date start = DateUtils.getScheduleDate(startTime);
        Date end = DateUtils.getScheduleDate(endTime);

        //project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.queryTopNLongestRunningProcessInstance(loginUser, projectCode, size, startTime, endTime);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND, proejctAuthFailRes.get(Constants.STATUS));

        //project auth success
        putMsg(result, Status.SUCCESS, projectCode);
        ProcessInstance processInstance = getProcessInstance();
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);
        Map<String, Object> successRes = processInstanceService.queryTopNLongestRunningProcessInstance(loginUser, projectCode, size, startTime, endTime);

        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessInstanceById() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        //project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND, proejctAuthFailRes.get(Constants.STATUS));

        //project auth success
        ProcessInstance processInstance = getProcessInstance();
        putMsg(result, Status.SUCCESS, projectCode);
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProjectCode(projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(processInstance.getId())).thenReturn(processInstance);
        when(processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
            processInstance.getProcessDefinitionVersion())).thenReturn(processDefinition);
        Map<String, Object> successRes = processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        //worker group null
        Map<String, Object> workerNullRes = processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assert.assertEquals(Status.SUCCESS, workerNullRes.get(Constants.STATUS));

        //worker group exist
        WorkerGroup workerGroup = getWorkGroup();
        Map<String, Object> workerExistRes = processInstanceService.queryProcessInstanceById(loginUser, projectCode, 1);
        Assert.assertEquals(Status.SUCCESS, workerExistRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryTaskListByProcessId() throws IOException {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        //project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.queryTaskListByProcessId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND, proejctAuthFailRes.get(Constants.STATUS));

        //project auth success
        putMsg(result, Status.SUCCESS, projectCode);
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setState(ExecutionStatus.SUCCESS);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType("SHELL");
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);
        Result res = new Result();
        res.setCode(Status.SUCCESS.ordinal());
        res.setData("xxx");
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(processInstance.getId())).thenReturn(processInstance);
        when(processService.findValidTaskListByProcessId(processInstance.getId())).thenReturn(taskInstanceList);
        when(loggerService.queryLog(taskInstance.getId(), 0, 4098)).thenReturn(res);
        Map<String, Object> successRes = processInstanceService.queryTaskListByProcessId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testParseLogForDependentResult() throws IOException {
        String logString = "[INFO] 2019-03-19 17:11:08.475 org.apache.dolphinscheduler.server.worker.log.TaskLogger:[172]"
            + " - [taskAppId=TASK_223_10739_452334] dependent item complete :|| 223-ALL-day-last1Day,SUCCESS\n"
            + "[INFO] 2019-03-19 17:11:08.476 org.apache.dolphinscheduler.server.worker.runner.TaskScheduleThread:[172]"
            + " - task : 223_10739_452334 exit status code : 0\n"
            + "[root@node2 current]# ";
        Map<String, DependResult> resultMap =
            processInstanceService.parseLogForDependentResult(logString);
        Assert.assertEquals(1, resultMap.size());
    }

    @Test
    public void testQuerySubProcessInstanceByTaskId() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        //project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND, proejctAuthFailRes.get(Constants.STATUS));

        //task null
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findTaskInstanceById(1)).thenReturn(null);
        Map<String, Object> taskNullRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_EXISTS, taskNullRes.get(Constants.STATUS));

        //task not sub process
        TaskInstance taskInstance = getTaskInstance();
        taskInstance.setTaskType("HTTP");
        taskInstance.setProcessInstanceId(1);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        when(taskDefinitionMapper.queryByCode(taskInstance.getTaskCode())).thenReturn(taskDefinition);
        Map<String, Object> notSubprocessRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, notSubprocessRes.get(Constants.STATUS));

        //sub process not exist
        TaskInstance subTask = getTaskInstance();
        subTask.setTaskType("SUB_PROCESS");
        subTask.setProcessInstanceId(1);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findTaskInstanceById(subTask.getId())).thenReturn(subTask);
        when(processService.findSubProcessInstance(subTask.getProcessInstanceId(), subTask.getId())).thenReturn(null);
        Map<String, Object> subprocessNotExistRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.SUB_PROCESS_INSTANCE_NOT_EXIST, subprocessNotExistRes.get(Constants.STATUS));

        //sub process exist
        ProcessInstance processInstance = getProcessInstance();
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findSubProcessInstance(taskInstance.getProcessInstanceId(), taskInstance.getId())).thenReturn(processInstance);
        Map<String, Object> subprocessExistRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.SUCCESS, subprocessExistRes.get(Constants.STATUS));
    }

    @Test
    public void testUpdateProcessInstance() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        //project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,INSTANCE_UPDATE )).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
            shellJson, taskJson, "2020-02-21 00:00:00", true, "", "", 0, "");
        Assert.assertEquals(Status.PROJECT_NOT_FOUND, proejctAuthFailRes.get(Constants.STATUS));

        //process instance null
        putMsg(result, Status.SUCCESS, projectCode);
        ProcessInstance processInstance = getProcessInstance();
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,INSTANCE_UPDATE )).thenReturn(result);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(null);
        Map<String, Object> processInstanceNullRes = processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
            shellJson, taskJson,"2020-02-21 00:00:00", true, "", "", 0, "");
        Assert.assertEquals(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceNullRes.get(Constants.STATUS));

        //process instance not finish
        when(processService.findProcessInstanceDetailById(1)).thenReturn(processInstance);
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        putMsg(result, Status.SUCCESS, projectCode);
        Map<String, Object> processInstanceNotFinishRes = processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
            shellJson, taskJson,"2020-02-21 00:00:00", true, "", "", 0, "");
        Assert.assertEquals(Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR, processInstanceNotFinishRes.get(Constants.STATUS));

        //process instance finish
        processInstance.setState(ExecutionStatus.SUCCESS);
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
        when(processService.getTenantForProcess(Mockito.anyInt(), Mockito.anyInt())).thenReturn(tenant);
        when(processService.updateProcessInstance(processInstance)).thenReturn(1);
        when(processService.saveProcessDefine(loginUser, processDefinition, Boolean.TRUE, Boolean.FALSE)).thenReturn(1);

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        when(processDefinitionService.checkProcessNodeList(taskRelationJson, taskDefinitionLogs)).thenReturn(result);
        putMsg(result, Status.SUCCESS, projectCode);
        when(taskPluginManager.checkTaskParameters(Mockito.any())).thenReturn(true);
        Map<String, Object> processInstanceFinishRes = processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
            taskRelationJson, taskDefinitionJson,"2020-02-21 00:00:00", true, "", "", 0, "root");
        Assert.assertEquals(Status.SUCCESS, processInstanceFinishRes.get(Constants.STATUS));

        //success
        when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        putMsg(result, Status.SUCCESS, projectCode);

        when(processService.saveProcessDefine(loginUser, processDefinition, Boolean.FALSE, Boolean.FALSE)).thenReturn(1);
        Map<String, Object> successRes = processInstanceService.updateProcessInstance(loginUser, projectCode, 1,
            taskRelationJson, taskDefinitionJson,"2020-02-21 00:00:00", Boolean.FALSE, "", "", 0, "root");
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryParentInstanceBySubId() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        //project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND, proejctAuthFailRes.get(Constants.STATUS));

        //process instance null
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_INSTANCE)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(null);
        Map<String, Object> processInstanceNullRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceNullRes.get(Constants.STATUS));

        //not sub process
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setIsSubProcess(Flag.NO);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(processInstance);
        Map<String, Object> notSubProcessRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE, notSubProcessRes.get(Constants.STATUS));

        //sub process
        processInstance.setIsSubProcess(Flag.YES);
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findParentProcessInstance(1)).thenReturn(null);
        Map<String, Object> subProcessNullRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.SUB_PROCESS_INSTANCE_NOT_EXIST, subProcessNullRes.get(Constants.STATUS));

        //success
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.findParentProcessInstance(1)).thenReturn(processInstance);
        Map<String, Object> successRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectCode, 1);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testDeleteProcessInstanceById() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        //process instance null
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode,INSTANCE_DELETE)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(null);
    }

    @Test
    public void testViewVariables() {
        //process instance not null
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setCommandType(CommandType.SCHEDULER);
        processInstance.setScheduleTime(new Date());
        processInstance.setGlobalParams("");
        when(processInstanceMapper.queryDetailById(1)).thenReturn(processInstance);
        Map<String, Object> successRes = processInstanceService.viewVariables(1L,1);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testViewGantt() throws Exception {
        ProcessInstance processInstance = getProcessInstance();
        TaskInstance taskInstance = getTaskInstance();
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        when(processInstanceMapper.queryDetailById(1)).thenReturn(processInstance);
        when(processDefinitionLogMapper.queryByDefinitionCodeAndVersion(
            processInstance.getProcessDefinitionCode(),
            processInstance.getProcessDefinitionVersion()
        )).thenReturn(new ProcessDefinitionLog());
        when(processInstanceMapper.queryDetailById(1)).thenReturn(processInstance);
        when(taskInstanceMapper.queryByInstanceIdAndName(Mockito.anyInt(), Mockito.any())).thenReturn(taskInstance);
        DAG<String, TaskNode, TaskNodeRelation> graph = new DAG<>();
        for (int i = 1; i <= 7; ++i) {
            graph.addNode(i + "", new TaskNode());
        }

        when(processService.genDagGraph(Mockito.any(ProcessDefinition.class)))
            .thenReturn(graph);

        Map<String, Object> successRes = processInstanceService.viewGantt(0L, 1);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
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
        processDefinition.setTenantId(1);
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
