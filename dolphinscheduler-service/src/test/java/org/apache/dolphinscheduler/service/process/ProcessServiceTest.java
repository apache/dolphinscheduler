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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.DqComparisonTypeMapper;
import org.apache.dolphinscheduler.dao.mapper.DqExecuteResultMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleExecuteSqlMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleInputEntryMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DqTaskState;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.InputType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.OptionSourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ValueType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtilsTest;
import org.apache.dolphinscheduler.spi.params.base.FormType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;
import static org.mockito.ArgumentMatchers.any;

/**
 * process service test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CronUtilsTest.class);

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ProcessServiceImpl processService;
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
    @Mock
    private TaskGroupMapper taskGroupMapper;
    @Mock
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Mock
    private DqExecuteResultMapper dqExecuteResultMapper;

    @Mock
    private DqRuleMapper dqRuleMapper;

    @Mock
    private DqRuleInputEntryMapper dqRuleInputEntryMapper;

    @Mock
    private DqRuleExecuteSqlMapper dqRuleExecuteSqlMapper;

    @Mock
    private DqComparisonTypeMapper dqComparisonTypeMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

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
        Mockito.when(processDefineMapper.queryByDefineId(100)).thenReturn(processDefinition);
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
        processDefinition.setExecutionType(ProcessExecutionTypeEnum.PARALLEL);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(222);
        processInstance.setProcessDefinitionCode(11L);
        processInstance.setHost("127.0.0.1:5678");
        processInstance.setProcessDefinitionVersion(1);
        processInstance.setId(processInstanceId);
        processInstance.setProcessDefinitionCode(definitionCode);
        processInstance.setProcessDefinitionVersion(definitionVersion);

        Mockito.when(processDefineMapper.queryByCode(command1.getProcessDefinitionCode())).thenReturn(processDefinition);
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

        ProcessDefinition processDefinition1 = new ProcessDefinition();
        processDefinition1.setId(123);
        processDefinition1.setName("test");
        processDefinition1.setVersion(1);
        processDefinition1.setCode(11L);
        processDefinition1.setVersion(1);
        processDefinition1.setExecutionType(ProcessExecutionTypeEnum.SERIAL_WAIT);
        List<ProcessInstance> lists = new ArrayList<>();
        ProcessInstance processInstance11 = new ProcessInstance();
        processInstance11.setId(222);
        processInstance11.setProcessDefinitionCode(11L);
        processInstance11.setProcessDefinitionVersion(1);
        processInstance11.setHost("127.0.0.1:5678");
        lists.add(processInstance11);

        ProcessInstance processInstance2 = new ProcessInstance();
        processInstance2.setId(223);
        processInstance2.setProcessDefinitionCode(11L);
        processInstance2.setProcessDefinitionVersion(1);
        Mockito.when(processInstanceMapper.queryDetailById(223)).thenReturn(processInstance2);
        Mockito.when(processDefineMapper.queryByCode(11L)).thenReturn(processDefinition1);
        Mockito.when(commandMapper.deleteById(1)).thenReturn(1);
        Assert.assertNotNull(processService.handleCommand(logger, host, command1));

        Command command6 = new Command();
        command6.setId(6);
        command6.setProcessDefinitionCode(11L);
        command6.setCommandParam("{\"ProcessInstanceId\":223}");
        command6.setCommandType(CommandType.RECOVER_SERIAL_WAIT);
        command6.setProcessDefinitionVersion(1);
        Mockito.when(processInstanceMapper.queryByProcessDefineCodeAndProcessDefinitionVersionAndStatusAndNextId(11L, 1, Constants.RUNNING_PROCESS_STATE, 223)).thenReturn(lists);
        Mockito.when(processInstanceMapper.updateNextProcessIdById(223, 222)).thenReturn(true);
        Mockito.when(commandMapper.deleteById(6)).thenReturn(1);
        ProcessInstance processInstance6 = processService.handleCommand(logger, host, command6);
        Assert.assertTrue(processInstance6 != null);

        processDefinition1.setExecutionType(ProcessExecutionTypeEnum.SERIAL_DISCARD);
        Mockito.when(processDefineMapper.queryByCode(11L)).thenReturn(processDefinition1);
        ProcessInstance processInstance7 = new ProcessInstance();
        processInstance7.setId(224);
        processInstance7.setProcessDefinitionCode(11L);
        processInstance7.setProcessDefinitionVersion(1);
        Mockito.when(processInstanceMapper.queryDetailById(224)).thenReturn(processInstance7);

        Command command7 = new Command();
        command7.setId(7);
        command7.setProcessDefinitionCode(11L);
        command7.setCommandParam("{\"ProcessInstanceId\":224}");
        command7.setCommandType(CommandType.RECOVER_SERIAL_WAIT);
        command7.setProcessDefinitionVersion(1);
        Mockito.when(commandMapper.deleteById(7)).thenReturn(1);
        Mockito.when(processInstanceMapper.queryByProcessDefineCodeAndProcessDefinitionVersionAndStatusAndNextId(11L, 1, Constants.RUNNING_PROCESS_STATE, 224)).thenReturn(null);
        ProcessInstance processInstance8 = processService.handleCommand(logger, host, command7);
        Assert.assertTrue(processInstance8 == null);

        ProcessDefinition processDefinition2 = new ProcessDefinition();
        processDefinition2.setId(123);
        processDefinition2.setName("test");
        processDefinition2.setVersion(1);
        processDefinition2.setCode(12L);
        processDefinition2.setExecutionType(ProcessExecutionTypeEnum.SERIAL_PRIORITY);
        Mockito.when(processDefineMapper.queryByCode(12L)).thenReturn(processDefinition2);
        ProcessInstance processInstance9 = new ProcessInstance();
        processInstance9.setId(225);
        processInstance9.setProcessDefinitionCode(11L);
        processInstance9.setProcessDefinitionVersion(1);
        Command command9 = new Command();
        command9.setId(9);
        command9.setProcessDefinitionCode(12L);
        command9.setCommandParam("{\"ProcessInstanceId\":225}");
        command9.setCommandType(CommandType.RECOVER_SERIAL_WAIT);
        command9.setProcessDefinitionVersion(1);
        Mockito.when(processInstanceMapper.queryDetailById(225)).thenReturn(processInstance9);
        Mockito.when(processInstanceMapper.queryByProcessDefineCodeAndProcessDefinitionVersionAndStatusAndNextId(12L, 1, Constants.RUNNING_PROCESS_STATE, 0)).thenReturn(lists);
        Mockito.when(processInstanceMapper.updateById(processInstance)).thenReturn(1);
        Mockito.when(commandMapper.deleteById(9)).thenReturn(1);
        ProcessInstance processInstance10 = processService.handleCommand(logger, host, command9);
        Assert.assertTrue(processInstance10 == null);
    }

    @Test(expected = ServiceException.class)
    public void testDeleteNotExistCommand() {
        String host = "127.0.0.1";
        int definitionVersion = 1;
        long definitionCode = 123;
        int processInstanceId = 222;

        Command command1 = new Command();
        command1.setId(1);
        command1.setProcessDefinitionCode(definitionCode);
        command1.setProcessDefinitionVersion(definitionVersion);
        command1.setCommandParam("{\"ProcessInstanceId\":222}");
        command1.setCommandType(CommandType.START_PROCESS);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(definitionVersion);
        processDefinition.setCode(definitionCode);
        processDefinition.setGlobalParams("[{\"prop\":\"startParam1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}]");
        processDefinition.setExecutionType(ProcessExecutionTypeEnum.PARALLEL);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(222);
        processInstance.setProcessDefinitionCode(11L);
        processInstance.setHost("127.0.0.1:5678");
        processInstance.setProcessDefinitionVersion(1);
        processInstance.setId(processInstanceId);
        processInstance.setProcessDefinitionCode(definitionCode);
        processInstance.setProcessDefinitionVersion(definitionVersion);

        Mockito.when(processDefineMapper.queryByCode(command1.getProcessDefinitionCode())).thenReturn(processDefinition);
        Mockito.when(processDefineLogMapper.queryByDefinitionCodeAndVersion(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion())).thenReturn(new ProcessDefinitionLog(processDefinition));
        Mockito.when(processInstanceMapper.queryDetailById(222)).thenReturn(processInstance);

        // will throw exception when command id is 0 and delete fail
        processService.handleCommand(logger, host, command1);
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
        Mockito.when(processDefineMapper.selectById(parentProcessDefineId)).thenReturn(processDefinition);

        long postTaskCode = 2L;
        int postTaskVersion = 2;

        List<ProcessTaskRelationLog> relationLogList = new ArrayList<>();
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setPostTaskCode(postTaskCode);
        processTaskRelationLog.setPostTaskVersion(postTaskVersion);
        relationLogList.add(processTaskRelationLog);
        Mockito.when(processTaskRelationLogMapper.queryByProcessCodeAndVersion(parentProcessDefineCode
                , parentProcessDefineVersion)).thenReturn(relationLogList);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        TaskDefinitionLog taskDefinitionLog1 = new TaskDefinitionLog();
        taskDefinitionLog1.setTaskParams("{\"processDefinitionCode\": 123L}");
        taskDefinitionLogs.add(taskDefinitionLog1);
        Mockito.when(taskDefinitionLogMapper.queryByTaskDefinitions(Mockito.anySet())).thenReturn(taskDefinitionLogs);

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
    public void getDqRule() {
        Mockito.when(dqRuleMapper.selectById(1)).thenReturn(new DqRule());
        Assert.assertNotNull(processService.getDqRule(1));
    }

    @Test
    public void getRuleInputEntry() {
        Mockito.when(dqRuleInputEntryMapper.getRuleInputEntryList(1)).thenReturn(getRuleInputEntryList());
        Assert.assertNotNull(processService.getRuleInputEntry(1));
    }

    @Test
    public void getDqExecuteSql() {
        Mockito.when(dqRuleExecuteSqlMapper.getExecuteSqlList(1)).thenReturn(getRuleExecuteSqlList());
        Assert.assertNotNull(processService.getDqExecuteSql(1));
    }

    private List<DqRuleInputEntry> getRuleInputEntryList() {
        List<DqRuleInputEntry> list = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT.getFormType());
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setShow(true);
        srcConnectorType.setValue("JDBC");
        srcConnectorType.setPlaceholder("Please select the source connector type");
        srcConnectorType.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcConnectorType.setOptions("[{\"label\":\"HIVE\",\"value\":\"HIVE\"},{\"label\":\"JDBC\",\"value\":\"JDBC\"}]");
        srcConnectorType.setInputType(InputType.DEFAULT.getCode());
        srcConnectorType.setValueType(ValueType.NUMBER.getCode());
        srcConnectorType.setEmit(true);

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值名");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT.getFormType());
        statisticsName.setCanEdit(true);
        statisticsName.setShow(true);
        statisticsName.setPlaceholder("Please enter statistics name, the alias in statistics execute sql");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsName.setInputType(InputType.DEFAULT.getCode());
        statisticsName.setValueType(ValueType.STRING.getCode());
        statisticsName.setEmit(false);

        DqRuleInputEntry statisticsExecuteSql = new DqRuleInputEntry();
        statisticsExecuteSql.setTitle("统计值计算SQL");
        statisticsExecuteSql.setField("statistics_execute_sql");
        statisticsExecuteSql.setType(FormType.TEXTAREA.getFormType());
        statisticsExecuteSql.setCanEdit(true);
        statisticsExecuteSql.setShow(true);
        statisticsExecuteSql.setPlaceholder("Please enter the statistics execute sql");
        statisticsExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsExecuteSql.setValueType(ValueType.LIKE_SQL.getCode());
        statisticsExecuteSql.setEmit(false);

        list.add(srcConnectorType);
        list.add(statisticsName);
        list.add(statisticsExecuteSql);

        return list;
    }

    private List<DqRuleExecuteSql> getRuleExecuteSqlList() {
        List<DqRuleExecuteSql> list = new ArrayList<>();

        DqRuleExecuteSql executeSqlDefinition = new DqRuleExecuteSql();
        executeSqlDefinition.setIndex(0);
        executeSqlDefinition.setSql("SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})");
        executeSqlDefinition.setTableAlias("total_count");
        executeSqlDefinition.setType(ExecuteSqlType.COMPARISON.getCode());
        list.add(executeSqlDefinition);

        return list;
    }

    public DqExecuteResult getExecuteResult() {
        DqExecuteResult dqExecuteResult = new DqExecuteResult();
        dqExecuteResult.setId(1);
        dqExecuteResult.setState(DqTaskState.FAILURE.getCode());

        return dqExecuteResult;
    }

    public List<DqExecuteResult> getExecuteResultList() {

        List<DqExecuteResult> list = new ArrayList<>();
        DqExecuteResult dqExecuteResult = new DqExecuteResult();
        dqExecuteResult.setId(1);
        dqExecuteResult.setState(DqTaskState.FAILURE.getCode());
        list.add(dqExecuteResult);

        return list;
    }

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
        taskDefinition.setTaskType("SHELL");
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
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setUserId(1);
        taskDefinition.setVersion(2);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());

        TaskDefinitionLog td2 = new TaskDefinitionLog();
        td2.setCode(2L);
        td2.setName("unit-test");
        td2.setProjectCode(1L);
        td2.setTaskType("SHELL");
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

    @Test
    public void testCreateTaskGroupQueue() {
        Mockito.when(taskGroupQueueMapper.insert(Mockito.any(TaskGroupQueue.class))).thenReturn(1);
        TaskGroupQueue taskGroupQueue = processService.insertIntoTaskGroupQueue(1, "task name", 1, 1, 1, TaskGroupQueueStatus.WAIT_QUEUE);
        Assert.assertNotNull(taskGroupQueue);
    }

    @Test
    public void testDoRelease() {

        TaskGroupQueue taskGroupQueue = getTaskGroupQueue();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setProcessInstanceId(1);
        taskInstance.setTaskGroupId(taskGroupQueue.getGroupId());

        Mockito.when(taskGroupQueueMapper.queryByTaskId(1)).thenReturn(taskGroupQueue);
        Mockito.when(taskGroupQueueMapper.updateById(taskGroupQueue)).thenReturn(1);

        processService.releaseTaskGroup(taskInstance);

    }

    @Test
    public void testFindTaskInstanceByIdList() {
        List<Integer> emptyList = new ArrayList<>();
        Mockito.when(taskInstanceMapper.selectBatchIds(emptyList)).thenReturn(new ArrayList<>());
        Assert.assertEquals(0, processService.findTaskInstanceByIdList(emptyList).size());

        List<Integer> idList = Collections.singletonList(1);
        TaskInstance instance = new TaskInstance();
        instance.setId(1);

        Mockito.when(taskInstanceMapper.selectBatchIds(idList)).thenReturn(Collections.singletonList(instance));
        List<TaskInstance> taskInstanceByIdList = processService.findTaskInstanceByIdList(idList);

        Assert.assertEquals(1, taskInstanceByIdList.size());
        Assert.assertEquals(instance.getId(), taskInstanceByIdList.get(0).getId());
    }

    @Test
    public void testFindCommandPageBySlot() {
        int pageSize = 1;
        int pageNumber = 0;
        int masterCount = 0;
        int thisMasterSlot = 2;
        List<Command> commandList = processService.findCommandPageBySlot(pageSize, pageNumber, masterCount, thisMasterSlot);
        Assert.assertEquals(0, commandList.size());
    }

    private TaskGroupQueue getTaskGroupQueue() {
        TaskGroupQueue taskGroupQueue = new TaskGroupQueue();
        taskGroupQueue.setTaskName("task name");
        taskGroupQueue.setId(1);
        taskGroupQueue.setGroupId(1);
        taskGroupQueue.setTaskId(1);
        taskGroupQueue.setPriority(1);
        taskGroupQueue.setStatus(TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        Date date = new Date(System.currentTimeMillis());
        taskGroupQueue.setUpdateTime(date);
        taskGroupQueue.setCreateTime(date);
        return taskGroupQueue;
    }
}
