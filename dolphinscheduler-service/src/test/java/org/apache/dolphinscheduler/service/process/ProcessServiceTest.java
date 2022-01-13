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

package org.apache.dolphinscheduler.service.process;

import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;

import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtilsTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * process service test
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CronUtilsTest.class);

    @InjectMocks
    private ProcessService processService;
    @Mock
    private CommandMapper commandMapper;
    @Mock
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;
    @Mock
    private ErrorCommandMapper errorCommandMapper;
    @Mock
    private ProcessDefinitionMapper processDefineMapper;
    @Mock
    private ProcessInstanceMapper processInstanceMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TaskInstanceMapper taskInstanceMapper;
    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;
    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;
    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;
    @Mock
    private ProcessDefinitionLogMapper processDefineLogMapper;
    @Mock
    private ResourceMapper resourceMapper;

    @Test
    public void testCreateSubCommand() {
        ProcessInstance parentInstance = new ProcessInstance();
        parentInstance.setWarningType(WarningType.SUCCESS);
        parentInstance.setWarningGroupId(0);

        TaskInstance task = new TaskInstance();
        task.setTaskParams("{\"processDefinitionCode\":10}}");
        task.setId(10);
        task.setTaskCode(1L);
        task.setTaskDefinitionVersion(1);

        ProcessInstance childInstance = null;
        ProcessInstanceMap instanceMap = new ProcessInstanceMap();
        instanceMap.setParentProcessInstanceId(1);
        instanceMap.setParentTaskInstanceId(10);
        Command command;

        //father history: start; child null == command type: start
        parentInstance.setHistoryCmd("START_PROCESS");
        parentInstance.setCommandType(CommandType.START_PROCESS);
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(10L);
        Mockito.when(processDefineMapper.queryByCode(10L)).thenReturn(processDefinition);
        command = processService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assert.assertEquals(CommandType.START_PROCESS, command.getCommandType());

        //father history: start,start failure; child null == command type: start
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assert.assertEquals(CommandType.START_PROCESS, command.getCommandType());

        //father history: scheduler,start failure; child null == command type: scheduler
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("SCHEDULER,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assert.assertEquals(CommandType.SCHEDULER, command.getCommandType());

        //father history: complement,start failure; child null == command type: complement

        String startString = "2020-01-01 00:00:00";
        String endString = "2020-01-10 00:00:00";
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("COMPLEMENT_DATA,START_FAILURE_TASK_PROCESS");
        Map<String, String> complementMap = new HashMap<>();
        complementMap.put(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE, startString);
        complementMap.put(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE, endString);
        parentInstance.setCommandParam(JSONUtils.toJsonString(complementMap));
        command = processService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assert.assertEquals(CommandType.COMPLEMENT_DATA, command.getCommandType());

        JsonNode complementDate = JSONUtils.parseObject(command.getCommandParam());
        Date start = DateUtils.stringToDate(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE).asText());
        Date end = DateUtils.stringToDate(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE).asText());
        Assert.assertEquals(startString, DateUtils.dateToString(start));
        Assert.assertEquals(endString, DateUtils.dateToString(end));

        //father history: start,failure,start failure; child not null == command type: start failure
        childInstance = new ProcessInstance();
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(parentInstance, childInstance, instanceMap, task);
        Assert.assertEquals(CommandType.START_FAILURE_TASK_PROCESS, command.getCommandType());
    }

    @Test
    public void testVerifyIsNeedCreateCommand() {

        List<Command> commands = new ArrayList<>();

        Command command = new Command();
        command.setCommandType(CommandType.REPEAT_RUNNING);
        command.setCommandParam("{\"" + CMD_PARAM_RECOVER_PROCESS_ID_STRING + "\":\"111\"}");
        commands.add(command);
        Mockito.when(commandMapper.selectList(null)).thenReturn(commands);
        Assert.assertFalse(processService.verifyIsNeedCreateCommand(command));

        Command command1 = new Command();
        command1.setCommandType(CommandType.REPEAT_RUNNING);
        command1.setCommandParam("{\"" + CMD_PARAM_RECOVER_PROCESS_ID_STRING + "\":\"222\"}");
        Assert.assertTrue(processService.verifyIsNeedCreateCommand(command1));

        Command command2 = new Command();
        command2.setCommandType(CommandType.PAUSE);
        Assert.assertTrue(processService.verifyIsNeedCreateCommand(command2));
    }

    @Test
    public void testCreateRecoveryWaitingThreadCommand() {
        int id = 123;
        Mockito.when(commandMapper.deleteById(id)).thenReturn(1);
        ProcessInstance subProcessInstance = new ProcessInstance();
        subProcessInstance.setIsSubProcess(Flag.YES);
        Command originCommand = new Command();
        originCommand.setId(id);
        processService.createRecoveryWaitingThreadCommand(originCommand, subProcessInstance);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(111);
        processService.createRecoveryWaitingThreadCommand(null, subProcessInstance);

        Command recoverCommand = new Command();
        recoverCommand.setCommandType(CommandType.RECOVER_WAITING_THREAD);
        processService.createRecoveryWaitingThreadCommand(recoverCommand, subProcessInstance);

        Command repeatRunningCommand = new Command();
        recoverCommand.setCommandType(CommandType.REPEAT_RUNNING);
        processService.createRecoveryWaitingThreadCommand(repeatRunningCommand, subProcessInstance);

        ProcessInstance subProcessInstance2 = new ProcessInstance();
        subProcessInstance2.setId(111);
        subProcessInstance2.setIsSubProcess(Flag.NO);
        processService.createRecoveryWaitingThreadCommand(repeatRunningCommand, subProcessInstance2);
    }

    @Test
    public void testHandleCommand() {

        //cannot construct process instance, return null;
        String host = "127.0.0.1";
        Command command = new Command();
        command.setProcessDefinitionCode(222);
        command.setCommandType(CommandType.REPEAT_RUNNING);
        command.setCommandParam("{\"" + CMD_PARAM_RECOVER_PROCESS_ID_STRING + "\":\"111\",\""
                + CMD_PARAM_SUB_PROCESS_DEFINE_CODE + "\":\"222\"}");
        Assert.assertNull(processService.handleCommand(logger, host, command));

        int definitionVersion = 1;
        long definitionCode = 123;
        int processInstanceId = 222;
        //there is not enough thread for this command
        Command command1 = new Command();
        command1.setId(1);
        command1.setProcessDefinitionCode(definitionCode);
        command1.setProcessDefinitionVersion(definitionVersion);
        command1.setCommandParam("{\"ProcessInstanceId\":222}");
        command1.setCommandType(CommandType.START_PROCESS);
        Mockito.when(commandMapper.deleteById(1)).thenReturn(1);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(definitionVersion);
        processDefinition.setCode(definitionCode);
        processDefinition.setGlobalParams("[{\"prop\":\"startParam1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}]");
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(processInstanceId);
        processInstance.setProcessDefinitionCode(definitionCode);
        processInstance.setProcessDefinitionVersion(definitionVersion);
        Mockito.when(processDefineLogMapper.queryByDefinitionCodeAndVersion(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion())).thenReturn(new ProcessDefinitionLog(processDefinition));
        Mockito.when(processInstanceMapper.queryDetailById(222)).thenReturn(processInstance);
        Assert.assertNotNull(processService.handleCommand(logger, host, command1));

        Command command2 = new Command();
        command2.setId(2);
        command2.setCommandParam("{\"ProcessInstanceId\":222,\"StartNodeIdList\":\"n1,n2\"}");
        command2.setProcessDefinitionCode(definitionCode);
        command2.setProcessDefinitionVersion(definitionVersion);
        command2.setCommandType(CommandType.RECOVER_SUSPENDED_PROCESS);
        command2.setProcessInstanceId(processInstanceId);
        Mockito.when(commandMapper.deleteById(2)).thenReturn(1);
        Assert.assertNotNull(processService.handleCommand(logger, host, command2));

        Command command3 = new Command();
        command3.setId(3);
        command3.setProcessDefinitionCode(definitionCode);
        command3.setProcessDefinitionVersion(definitionVersion);
        command3.setProcessInstanceId(processInstanceId);
        command3.setCommandParam("{\"WaitingThreadInstanceId\":222}");
        command3.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        Mockito.when(commandMapper.deleteById(3)).thenReturn(1);
        Assert.assertNotNull(processService.handleCommand(logger, host, command3));

        Command command4 = new Command();
        command4.setId(4);
        command4.setProcessDefinitionCode(definitionCode);
        command4.setProcessDefinitionVersion(definitionVersion);
        command4.setCommandParam("{\"WaitingThreadInstanceId\":222,\"StartNodeIdList\":\"n1,n2\"}");
        command4.setCommandType(CommandType.REPEAT_RUNNING);
        command4.setProcessInstanceId(processInstanceId);
        Mockito.when(commandMapper.deleteById(4)).thenReturn(1);
        Assert.assertNotNull(processService.handleCommand(logger, host, command4));

        Command command5 = new Command();
        command5.setId(5);
        command5.setProcessDefinitionCode(definitionCode);
        command5.setProcessDefinitionVersion(definitionVersion);
        HashMap<String, String> startParams = new HashMap<>();
        startParams.put("startParam1", "testStartParam1");
        HashMap<String, String> commandParams = new HashMap<>();
        commandParams.put(CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(startParams));
        command5.setCommandParam(JSONUtils.toJsonString(commandParams));
        command5.setCommandType(CommandType.START_PROCESS);
        command5.setDryRun(Constants.DRY_RUN_FLAG_NO);
        Mockito.when(commandMapper.deleteById(5)).thenReturn(1);

        ProcessInstance processInstance1 = processService.handleCommand(logger, host, command5);
        Assert.assertTrue(processInstance1.getGlobalParams().contains("\"testStartParam1\""));
    }

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setId(123);
        Mockito.when(userMapper.selectById(123)).thenReturn(user);
        Assert.assertEquals(user, processService.getUserById(123));
    }

    @Test
    public void testFormatTaskAppId() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(333);
        taskInstance.setProcessInstanceId(222);
        Mockito.when(processService.findProcessInstanceById(taskInstance.getProcessInstanceId())).thenReturn(null);
        Assert.assertEquals("", processService.formatTaskAppId(taskInstance));

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(111);
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(222);
        processInstance.setProcessDefinitionVersion(1);
        processInstance.setProcessDefinitionCode(1L);
        Mockito.when(processService.findProcessInstanceById(taskInstance.getProcessInstanceId())).thenReturn(processInstance);
        Assert.assertEquals("", processService.formatTaskAppId(taskInstance));
    }

    @Test
    public void testRecurseFindSubProcessId() {
        int parentProcessDefineId = 1;
        long parentProcessDefineCode = 1L;
        int parentProcessDefineVersion = 1;

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(parentProcessDefineCode);
        processDefinition.setVersion(parentProcessDefineVersion);

        long postTaskCode = 2L;
        int postTaskVersion = 2;

        List<ProcessTaskRelationLog> relationLogList = new ArrayList<>();
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setPostTaskCode(postTaskCode);
        processTaskRelationLog.setPostTaskVersion(postTaskVersion);
        relationLogList.add(processTaskRelationLog);
        Mockito.when(processDefineMapper.queryByCode(parentProcessDefineCode)).thenReturn(processDefinition);
        Mockito.when(processTaskRelationLogMapper.queryByProcessCodeAndVersion(parentProcessDefineCode
                , parentProcessDefineVersion)).thenReturn(relationLogList);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        TaskDefinitionLog taskDefinitionLog1 = new TaskDefinitionLog();
        taskDefinitionLog1.setTaskParams("{\"processDefinitionCode\": 123L}");
        taskDefinitionLogs.add(taskDefinitionLog1);

        List<Long> ids = new ArrayList<>();
        processService.recurseFindSubProcess(parentProcessDefineCode, ids);

        Assert.assertEquals(0, ids.size());
    }

    @Test
    public void testSwitchVersion() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setProjectCode(1L);
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(1);

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setCode(1L);
        processDefinitionLog.setVersion(2);
        Assert.assertEquals(0, processService.switchVersion(processDefinition, processDefinitionLog));
    }

    @Test
    public void testSaveTaskDefine() {
        User operator = new User();
        operator.setId(-1);
        operator.setUserType(UserType.GENERAL_USER);
        long projectCode = 751485690568704L;
        String taskJson = "[{\"code\":751500437479424,\"name\":\"aa\",\"version\":1,\"description\":\"\",\"delayTime\":0,"
                + "\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"sleep 1s\\necho 11\","
                + "\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"waitStartTimeout\":{}},"
                + "\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"yarn\",\"failRetryTimes\":0,\"failRetryInterval\":1,"
                + "\"timeoutFlag\":\"OPEN\",\"timeoutNotifyStrategy\":\"FAILED\",\"timeout\":1,\"environmentCode\":751496815697920},"
                + "{\"code\":751516889636864,\"name\":\"bb\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
                + "\"localParams\":[],\"rawScript\":\"echo 22\",\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},"
                + "\"waitStartTimeout\":{}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":\"0\","
                + "\"failRetryInterval\":\"1\",\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":0,\"delayTime\":\"0\",\"environmentCode\":-1}]";
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskJson, TaskDefinitionLog.class);
        TaskDefinitionLog taskDefinition = new TaskDefinitionLog();
        taskDefinition.setCode(751500437479424L);
        taskDefinition.setName("aa");
        taskDefinition.setProjectCode(751485690568704L);
        taskDefinition.setTaskType(TaskType.SHELL.getDesc());
        taskDefinition.setUserId(-1);
        taskDefinition.setVersion(1);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());
        Mockito.when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskDefinition.getCode(), taskDefinition.getVersion())).thenReturn(taskDefinition);
        Mockito.when(taskDefinitionLogMapper.queryMaxVersionForDefinition(taskDefinition.getCode())).thenReturn(1);
        Mockito.when(taskDefinitionMapper.queryByCode(taskDefinition.getCode())).thenReturn(taskDefinition);
        int result = processService.saveTaskDefine(operator, projectCode, taskDefinitionLogs, Boolean.TRUE);
        Assert.assertEquals(0, result);
    }

    @Test
    public void testGenDagGraph() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(1);
        processDefinition.setCode(11L);

        ProcessTaskRelationLog processTaskRelation = new ProcessTaskRelationLog();
        processTaskRelation.setName("def 1");
        processTaskRelation.setProcessDefinitionVersion(1);
        processTaskRelation.setProjectCode(1L);
        processTaskRelation.setProcessDefinitionCode(1L);
        processTaskRelation.setPostTaskCode(3L);
        processTaskRelation.setPreTaskCode(2L);
        processTaskRelation.setUpdateTime(new Date());
        processTaskRelation.setCreateTime(new Date());
        List<ProcessTaskRelationLog> list = new ArrayList<>();
        list.add(processTaskRelation);

        TaskDefinitionLog taskDefinition = new TaskDefinitionLog();
        taskDefinition.setCode(3L);
        taskDefinition.setName("1-test");
        taskDefinition.setProjectCode(1L);
        taskDefinition.setTaskType(TaskType.SHELL.getDesc());
        taskDefinition.setUserId(1);
        taskDefinition.setVersion(2);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());

        TaskDefinitionLog td2 = new TaskDefinitionLog();
        td2.setCode(2L);
        td2.setName("unit-test");
        td2.setProjectCode(1L);
        td2.setTaskType(TaskType.SHELL.getDesc());
        td2.setUserId(1);
        td2.setVersion(1);
        td2.setCreateTime(new Date());
        td2.setUpdateTime(new Date());

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        taskDefinitionLogs.add(taskDefinition);
        taskDefinitionLogs.add(td2);

        Mockito.when(taskDefinitionLogMapper.queryByTaskDefinitions(any())).thenReturn(taskDefinitionLogs);
        Mockito.when(processTaskRelationLogMapper.queryByProcessCodeAndVersion(Mockito.anyLong(), Mockito.anyInt())).thenReturn(list);

        DAG<String, TaskNode, TaskNodeRelation> stringTaskNodeTaskNodeRelationDAG = processService.genDagGraph(processDefinition);
        Assert.assertEquals(1, stringTaskNodeTaskNodeRelationDAG.getNodesCount());
    }

    @Test
    public void testCreateCommand() {
        Command command = new Command();
        command.setProcessDefinitionCode(123);
        command.setCommandParam("{\"ProcessInstanceId\":222}");
        command.setCommandType(CommandType.START_PROCESS);
        int mockResult = 1;
        Mockito.when(commandMapper.insert(command)).thenReturn(mockResult);
        int exeMethodResult = processService.createCommand(command);
        Assert.assertEquals(mockResult, exeMethodResult);
        Mockito.verify(commandMapper, Mockito.times(1)).insert(command);
    }

    @Test
    public void testChangeOutParam() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setProcessInstanceId(62);
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(62);
        taskInstance.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test1\",\"type\":\"VARCHAR\",\"value\":\"\"}]");
        taskInstance.setTaskParams("{\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select id from tb_test limit 1\","
                + "\"udfs\":\"\",\"sqlType\":\"0\",\"sendEmail\":false,\"displayRows\":10,\"title\":\"\","
                + "\"groupId\":null,\"localParams\":[{\"prop\":\"test1\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"12\"}],"
                + "\"connParams\":\"\",\"preStatements\":[],\"postStatements\":[],\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],"
                + "\\\"failedNode\\\":[\\\"\\\"]}\",\"dependence\":\"{}\"}");
        processService.changeOutParam(taskInstance);
    }

    @Test
    public void testUpdateTaskDefinitionResources() throws Exception {
        TaskDefinition taskDefinition = new TaskDefinition();
        String taskParameters = "{\n"
                + "    \"mainClass\": \"org.apache.dolphinscheduler.SparkTest\",\n"
                + "    \"mainJar\": {\n"
                + "        \"id\": 1\n"
                + "    },\n"
                + "    \"deployMode\": \"cluster\",\n"
                + "    \"resourceList\": [\n"
                + "        {\n"
                + "            \"id\": 3\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": 4\n"
                + "        }\n"
                + "    ],\n"
                + "    \"localParams\": [],\n"
                + "    \"driverCores\": 1,\n"
                + "    \"driverMemory\": \"512M\",\n"
                + "    \"numExecutors\": 2,\n"
                + "    \"executorMemory\": \"2G\",\n"
                + "    \"executorCores\": 2,\n"
                + "    \"appName\": \"\",\n"
                + "    \"mainArgs\": \"\",\n"
                + "    \"others\": \"\",\n"
                + "    \"programType\": \"JAVA\",\n"
                + "    \"sparkVersion\": \"SPARK2\",\n"
                + "    \"dependence\": {},\n"
                + "    \"conditionResult\": {\n"
                + "        \"successNode\": [\n"
                + "            \"\"\n"
                + "        ],\n"
                + "        \"failedNode\": [\n"
                + "            \"\"\n"
                + "        ]\n"
                + "    },\n"
                + "    \"waitStartTimeout\": {}\n"
                + "}";
        taskDefinition.setTaskParams(taskParameters);

        Map<Integer, Resource> resourceMap =
                Stream.of(1, 3, 4)
                        .map(i -> {
                            Resource resource = new Resource();
                            resource.setId(i);
                            resource.setFileName("file" + i);
                            resource.setFullName("/file" + i);
                            return resource;
                        })
                        .collect(
                                Collectors.toMap(
                                        Resource::getId,
                                        resource -> resource)
                        );
        for (Integer integer : Arrays.asList(1, 3, 4)) {
            Mockito.when(resourceMapper.selectById(integer))
                    .thenReturn(resourceMap.get(integer));
        }

        Whitebox.invokeMethod(processService,
                "updateTaskDefinitionResources",
                taskDefinition);

        String taskParams = taskDefinition.getTaskParams();
        SparkParameters sparkParameters = JSONUtils.parseObject(taskParams, SparkParameters.class);
        ResourceInfo mainJar = sparkParameters.getMainJar();
        Assert.assertEquals(1, mainJar.getId());
        Assert.assertEquals("file1", mainJar.getRes());
        Assert.assertEquals("/file1", mainJar.getResourceName());

        Assert.assertEquals(2, sparkParameters.getResourceList().size());
        ResourceInfo res1 = sparkParameters.getResourceList().get(0);
        ResourceInfo res2 = sparkParameters.getResourceList().get(1);
        Assert.assertEquals(3, res1.getId());
        Assert.assertEquals("file3", res1.getRes());
        Assert.assertEquals("/file3", res1.getResourceName());
        Assert.assertEquals(4, res2.getId());
        Assert.assertEquals("file4", res2.getRes());
        Assert.assertEquals("/file4", res2.getResourceName());

    }

    @Test
    public void testUpdateResourceInfo() throws Exception {
        // test if input is null
        ResourceInfo resourceInfoNull = null;
        ResourceInfo updatedResourceInfo1 = Whitebox.invokeMethod(processService,
                "updateResourceInfo",
                resourceInfoNull);
        Assert.assertNull(updatedResourceInfo1);

        // test if resource id less than 1
        ResourceInfo resourceInfoVoid = new ResourceInfo();
        ResourceInfo updatedResourceInfo2 = Whitebox.invokeMethod(processService,
                "updateResourceInfo",
                resourceInfoVoid);
        Assert.assertNull(updatedResourceInfo2);

        // test normal situation
        ResourceInfo resourceInfoNormal = new ResourceInfo();
        resourceInfoNormal.setId(1);
        Resource resource = new Resource();
        resource.setId(1);
        resource.setFileName("test.txt");
        resource.setFullName("/test.txt");
        Mockito.when(resourceMapper.selectById(1)).thenReturn(resource);
        ResourceInfo updatedResourceInfo3 = Whitebox.invokeMethod(processService,
                "updateResourceInfo",
                resourceInfoNormal);

        Assert.assertEquals(1, updatedResourceInfo3.getId());
        Assert.assertEquals("test.txt", updatedResourceInfo3.getRes());
        Assert.assertEquals("/test.txt", updatedResourceInfo3.getResourceName());

    }

}
