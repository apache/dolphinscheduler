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
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_ID;

import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
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
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtilsTest;

import java.util.ArrayList;
import java.util.Date;
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
    private ProcessTaskRelationMapper processTaskRelationMapper;
    @Mock
    private ProcessDefinitionLogMapper processDefineLogMapper;

    @Test
    public void testCreateSubCommand() {
        ProcessInstance parentInstance = new ProcessInstance();
        parentInstance.setWarningType(WarningType.SUCCESS);
        parentInstance.setWarningGroupId(0);

        TaskInstance task = new TaskInstance();
        task.setTaskParams("{\"processDefinitionId\":100}}");
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
        recoverCommand.setCommandType(CommandType.RECOVER_WAITTING_THREAD);
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
        int validThreadNum = 1;
        Command command = new Command();
        command.setProcessDefinitionId(222);
        command.setCommandType(CommandType.REPEAT_RUNNING);
        command.setCommandParam("{\"" + CMD_PARAM_RECOVER_PROCESS_ID_STRING + "\":\"111\",\""
                + CMD_PARAM_SUB_PROCESS_DEFINE_ID + "\":\"222\"}");
        Mockito.when(processDefineMapper.selectById(command.getProcessDefinitionId())).thenReturn(null);
        Assert.assertNull(processService.handleCommand(logger, host, validThreadNum, command));

        //there is not enough thread for this command
        Command command1 = new Command();
        command1.setProcessDefinitionId(123);
        command1.setCommandParam("{\"ProcessInstanceId\":222}");
        command1.setCommandType(CommandType.START_PROCESS);
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(1);
        processDefinition.setCode(11L);
        processDefinition.setProcessDefinitionJson("{\"globalParams\":[{\"prop\":\"startParam1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}],\"tasks\":[{\"conditionResult\":"
                + "{\"failedNode\":[\"\"],\"successNode\":[\"\"]},\"delayTime\":\"0\",\"dependence\":{}"
                + ",\"description\":\"\",\"id\":\"tasks-3011\",\"maxRetryTimes\":\"0\",\"name\":\"tsssss\""
                + ",\"params\":{\"localParams\":[],\"rawScript\":\"echo \\\"123123\\\"\",\"resourceList\":[]}"
                + ",\"preTasks\":[],\"retryInterval\":\"1\",\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\""
                + ",\"timeout\":{\"enable\":false,\"interval\":null,\"strategy\":\"\"},\"type\":\"SHELL\""
                + ",\"waitStartTimeout\":{},\"workerGroup\":\"default\"}],\"tenantId\":4,\"timeout\":0}");
        processDefinition.setGlobalParams("[{\"prop\":\"startParam1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}]");
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(222);
        Mockito.when(processDefineMapper.selectById(command1.getProcessDefinitionId())).thenReturn(processDefinition);
        Mockito.when(processInstanceMapper.queryDetailById(222)).thenReturn(processInstance);
        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command1));

        Command command2 = new Command();
        command2.setCommandParam("{\"ProcessInstanceId\":222,\"StartNodeIdList\":\"n1,n2\"}");
        command2.setProcessDefinitionId(123);
        command2.setCommandType(CommandType.RECOVER_SUSPENDED_PROCESS);

        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command2));

        Command command3 = new Command();
        command3.setProcessDefinitionId(123);
        command3.setCommandParam("{\"WaitingThreadInstanceId\":222}");
        command3.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command3));

        Command command4 = new Command();
        command4.setProcessDefinitionId(123);
        command4.setCommandParam("{\"WaitingThreadInstanceId\":222,\"StartNodeIdList\":\"n1,n2\"}");
        command4.setCommandType(CommandType.REPEAT_RUNNING);
        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command4));

        Command command5 = new Command();
        command5.setProcessDefinitionId(123);
        HashMap<String, String> startParams = new HashMap<>();
        startParams.put("startParam1", "testStartParam1");
        HashMap<String, String> commandParams = new HashMap<>();
        commandParams.put(CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(startParams));
        command5.setCommandParam(JSONUtils.toJsonString(commandParams));
        command5.setCommandType(CommandType.START_PROCESS);
        ProcessInstance processInstance1 = processService.handleCommand(logger, host, validThreadNum, command5);
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
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(10L);
        int parentId = 111;
        List<Integer> ids = new ArrayList<>();
        ProcessDefinition processDefinition2 = new ProcessDefinition();
        processDefinition2.setCode(11L);
        Mockito.when(processDefineMapper.selectById(parentId)).thenReturn(processDefinition);
        List<ProcessTaskRelationLog> relationLogList = new ArrayList<>();
        Mockito.when(processTaskRelationLogMapper.queryByProcessCodeAndVersion(Mockito.anyLong()
                , Mockito.anyInt()))
                .thenReturn(relationLogList);

        processService.recurseFindSubProcessId(parentId, ids);

    }

    @Test
    public void testSaveProcessDefinition() {
        User user = new User();
        user.setId(1);

        Project project = new Project();
        project.setCode(1L);

        ProcessData processData = new ProcessData();
        processData.setTasks(new ArrayList<>());

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(1);
        processDefinition.setCode(11L);
        Assert.assertEquals(-1, processService.saveProcessDefinition(user, project, "name",
                "desc", "locations", "connects", processData,
                processDefinition, true));
    }

    @Test
    public void testSwitchVersion() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(1);

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setCode(1L);
        processDefinitionLog.setVersion(2);
        Assert.assertEquals(0, processService.switchVersion(processDefinition, processDefinitionLog));
    }

    @Test
    public void testGenDagGraph() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(1);
        processDefinition.setCode(11L);

        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setName("def 1");
        processTaskRelationLog.setProcessDefinitionVersion(1);
        processTaskRelationLog.setProjectCode(1L);
        processTaskRelationLog.setProcessDefinitionCode(1L);
        processTaskRelationLog.setPostTaskCode(3L);
        processTaskRelationLog.setPreTaskCode(2L);
        processTaskRelationLog.setUpdateTime(new Date());
        processTaskRelationLog.setCreateTime(new Date());
        List<ProcessTaskRelationLog> list = new ArrayList<>();
        list.add(processTaskRelationLog);

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
        Assert.assertNotEquals(0, stringTaskNodeTaskNodeRelationDAG.getNodesCount());

    }

    @Test
    public void testGenProcessData() {
        String processDefinitionJson = "{\"tasks\":[{\"id\":null,\"code\":3,\"version\":0,\"name\":\"1-test\",\"desc\":null,"
                + "\"type\":\"SHELL\",\"runFlag\":\"FORBIDDEN\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":0,"
                + "\"params\":{},\"preTasks\":[\"unit-test\"],\"preTaskNodeList\":[{\"code\":2,\"name\":\"unit-test\","
                + "\"version\":0}],\"extras\":null,\"depList\":[\"unit-test\"],\"dependence\":null,\"conditionResult\":null,"
                + "\"taskInstancePriority\":null,\"workerGroup\":null,\"timeout\":{\"enable\":false,\"strategy\":null,"
                + "\"interval\":0},\"delayTime\":0}],\"globalParams\":[],\"timeout\":0,\"tenantId\":0}";

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(1);
        processDefinition.setCode(11L);

        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setName("def 1");
        processTaskRelationLog.setProcessDefinitionVersion(1);
        processTaskRelationLog.setProjectCode(1L);
        processTaskRelationLog.setProcessDefinitionCode(1L);
        processTaskRelationLog.setPostTaskCode(3L);
        processTaskRelationLog.setPreTaskCode(2L);
        processTaskRelationLog.setUpdateTime(new Date());
        processTaskRelationLog.setCreateTime(new Date());
        List<ProcessTaskRelationLog> list = new ArrayList<>();
        list.add(processTaskRelationLog);

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
        String json = JSONUtils.toJsonString(processService.genProcessData(processDefinition));

        Assert.assertEquals(processDefinitionJson, json);
    }

    @Test
    public void locationToMap() {
        String locations = "{\"tasks-64888\":{\"name\":\"test_a\",\"targetarr\":\"\",\"nodenumber\":\"1\",\"x\":134,\"y\":183},"
                + "\"tasks-24501\":{\"name\":\"test_b\",\"targetarr\":\"tasks-64888\",\"nodenumber\":\"0\",\"x\":392,\"y\":184},"
                + "\"tasks-81137\":{\"name\":\"test_c\",\"targetarr\":\"\",\"nodenumber\":\"1\",\"x\":122,\"y\":327},"
                + "\"tasks-41367\":{\"name\":\"test_d\",\"targetarr\":\"tasks-81137\",\"nodenumber\":\"0\",\"x\":409,\"y\":324}}";
        Map<String, String> frontTaskIdAndNameMap = new HashMap<>();
        frontTaskIdAndNameMap.put("test_a", "tasks-64888");
        frontTaskIdAndNameMap.put("test_b", "tasks-24501");
        frontTaskIdAndNameMap.put("test_c", "tasks-81137");
        frontTaskIdAndNameMap.put("test_d", "tasks-41367");
        Map<String, String> locationToMap = processService.locationToMap(locations);
        Assert.assertEquals(frontTaskIdAndNameMap, locationToMap);
    }

    @Test
    public void testCreateCommand() {
        Command command = new Command();
        command.setProcessDefinitionId(123);
        command.setCommandParam("{\"ProcessInstanceId\":222}");
        command.setCommandType(CommandType.START_PROCESS);
        int mockResult = 1;
        Mockito.when(commandMapper.insert(command)).thenReturn(mockResult);
        int exeMethodResult = processService.createCommand(command);
        Assert.assertEquals(mockResult, exeMethodResult);
        Mockito.verify(commandMapper, Mockito.times(1)).insert(command);
    }

}
