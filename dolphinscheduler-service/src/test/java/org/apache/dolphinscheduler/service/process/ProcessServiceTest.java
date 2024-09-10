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

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_PARAMS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.DqRuleExecuteSqlMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleInputEntryMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DqTaskState;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.InputType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.OptionSourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.spi.params.base.FormType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * process service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessServiceTest {

    @InjectMocks
    private ProcessServiceImpl processService;

    @Mock
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;

    @Mock
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Mock
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Mock
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Mock
    private TaskDefinitionDao taskDefinitionDao;

    @Mock
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Mock
    private DqRuleMapper dqRuleMapper;

    @Mock
    private DqRuleInputEntryMapper dqRuleInputEntryMapper;

    @Mock
    private DqRuleExecuteSqlMapper dqRuleExecuteSqlMapper;

    @Mock
    CuringParamsService curingGlobalParamsService;

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setId(123);
        when(userMapper.selectById(123)).thenReturn(user);
        Assertions.assertEquals(user, processService.getUserById(123));
    }

    @Test
    public void testFormatTaskAppId() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(333);
        taskInstance.setWorkflowInstanceId(222);
        when(processService.findWorkflowInstanceById(taskInstance.getWorkflowInstanceId())).thenReturn(null);
        Assertions.assertEquals("", processService.formatTaskAppId(taskInstance));

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setId(111);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(222);
        workflowInstance.setWorkflowDefinitionVersion(1);
        workflowInstance.setWorkflowDefinitionCode(1L);
        when(processService.findWorkflowInstanceById(taskInstance.getWorkflowInstanceId()))
                .thenReturn(workflowInstance);
        Assertions.assertEquals("", processService.formatTaskAppId(taskInstance));
    }

    @Test
    public void testFindAllSubWorkflowDefinitionCode() {
        int parentProcessDefineId = 1;
        long parentProcessDefineCode = 1L;
        int parentProcessDefineVersion = 1;

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(parentProcessDefineCode);
        workflowDefinition.setVersion(parentProcessDefineVersion);
        when(workflowDefinitionMapper.selectById(parentProcessDefineId)).thenReturn(workflowDefinition);

        long postTaskCode = 2L;
        int postTaskVersion = 2;

        List<WorkflowTaskRelationLog> relationLogList = new ArrayList<>();
        WorkflowTaskRelationLog processTaskRelationLog = new WorkflowTaskRelationLog();
        processTaskRelationLog.setPostTaskCode(postTaskCode);
        processTaskRelationLog.setPostTaskVersion(postTaskVersion);
        relationLogList.add(processTaskRelationLog);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(parentProcessDefineCode,
                parentProcessDefineVersion)).thenReturn(relationLogList);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        TaskDefinitionLog taskDefinitionLog1 = new TaskDefinitionLog();
        taskDefinitionLog1.setTaskParams("{\"workflowDefinitionCode\": 123L}");
        taskDefinitionLogs.add(taskDefinitionLog1);
        when(taskDefinitionLogMapper.queryByTaskDefinitions(Mockito.anySet())).thenReturn(taskDefinitionLogs);

        Assertions.assertTrue(processService.findAllSubWorkflowDefinitionCode(parentProcessDefineCode).isEmpty());
    }

    @Test
    public void testSwitchVersion() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setProjectCode(1L);
        workflowDefinition.setId(123);
        workflowDefinition.setName("test");
        workflowDefinition.setVersion(1);

        WorkflowDefinitionLog processDefinitionLog = new WorkflowDefinitionLog();
        processDefinitionLog.setCode(1L);
        processDefinitionLog.setVersion(2);
        Assertions.assertEquals(0, processService.switchVersion(workflowDefinition, processDefinitionLog));
    }

    @Test
    public void getDqRule() {
        when(dqRuleMapper.selectById(1)).thenReturn(new DqRule());
        Assertions.assertNotNull(processService.getDqRule(1));
    }

    @Test
    public void getRuleInputEntry() {
        when(dqRuleInputEntryMapper.getRuleInputEntryList(1)).thenReturn(getRuleInputEntryList());
        Assertions.assertNotNull(processService.getRuleInputEntry(1));
    }

    @Test
    public void getDqExecuteSql() {
        when(dqRuleExecuteSqlMapper.getExecuteSqlList(1)).thenReturn(getRuleExecuteSqlList());
        Assertions.assertNotNull(processService.getDqExecuteSql(1));
    }

    private List<DqRuleInputEntry> getRuleInputEntryList() {
        List<DqRuleInputEntry> list = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT.getFormType());
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setIsShow(true);
        srcConnectorType.setData("JDBC");
        srcConnectorType.setPlaceholder("Please select the source connector type");
        srcConnectorType.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcConnectorType
                .setOptions("[{\"label\":\"HIVE\",\"value\":\"HIVE\"},{\"label\":\"JDBC\",\"value\":\"JDBC\"}]");
        srcConnectorType.setInputType(InputType.DEFAULT.getCode());
        srcConnectorType.setDataType(DataType.NUMBER.getCode());
        srcConnectorType.setIsEmit(true);

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值名");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT.getFormType());
        statisticsName.setCanEdit(true);
        statisticsName.setIsShow(true);
        statisticsName.setPlaceholder("Please enter statistics name, the alias in statistics execute sql");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsName.setInputType(InputType.DEFAULT.getCode());
        statisticsName.setDataType(DataType.STRING.getCode());
        statisticsName.setIsEmit(false);

        DqRuleInputEntry statisticsExecuteSql = new DqRuleInputEntry();
        statisticsExecuteSql.setTitle("统计值计算SQL");
        statisticsExecuteSql.setField("statistics_execute_sql");
        statisticsExecuteSql.setType(FormType.TEXTAREA.getFormType());
        statisticsExecuteSql.setCanEdit(true);
        statisticsExecuteSql.setIsShow(true);
        statisticsExecuteSql.setPlaceholder("Please enter the statistics execute sql");
        statisticsExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsExecuteSql.setDataType(DataType.LIKE_SQL.getCode());
        statisticsExecuteSql.setIsEmit(false);

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

    @Test
    public void testSetGlobalParamIfCommanded() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        String globalParams =
                "[{\"prop\":\"global_param\",\"value\":\"4\",\"direct\":\"IN\",\"type\":\"VARCHAR\"},{\"prop\":\"O_ERRCODE\",\"value\":\"\",\"direct\":\"OUT\",\"type\":\"VARCHAR\"}]";
        workflowDefinition.setGlobalParams(globalParams);
        Map<String, String> globalParamMap = workflowDefinition.getGlobalParamMap();
        Assertions.assertTrue(globalParamMap.size() == 2);
        Assertions.assertTrue(workflowDefinition.getGlobalParamList().size() == 2);

        HashMap<String, String> startParams = new HashMap<>();
        String expectValue = "6";
        startParams.put("global_param", expectValue);
        HashMap<String, String> commandParams = new HashMap<>();
        commandParams.put(CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(startParams));
        Map<String, Property> mockStartParams = new HashMap<>();

        mockStartParams.put("global_param", new Property("global_param", Direct.IN,
                org.apache.dolphinscheduler.plugin.task.api.enums.DataType.VARCHAR, startParams.get("global_param")));
        when(curingGlobalParamsService.parseWorkflowStartParam(commandParams)).thenReturn(mockStartParams);

        processService.setGlobalParamIfCommanded(workflowDefinition, commandParams);
        Assertions.assertTrue(globalParamMap.get("global_param").equals(expectValue));
        Assertions.assertTrue(globalParamMap.containsKey("O_ERRCODE"));
    }

    @Test
    public void testSaveTaskDefine() {
        User operator = new User();
        operator.setId(-1);
        operator.setUserType(UserType.GENERAL_USER);
        long projectCode = 751485690568704L;
        String taskJson =
                "[{\"code\":751500437479424,\"name\":\"aa\",\"version\":1,\"description\":\"\",\"delayTime\":0,"
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
        when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskDefinition.getCode(),
                taskDefinition.getVersion())).thenReturn(taskDefinition);
        when(taskDefinitionLogMapper.queryMaxVersionForDefinition(taskDefinition.getCode())).thenReturn(1);
        when(taskDefinitionMapper.queryByCodeList(Collections.singletonList(taskDefinition.getCode())))
                .thenReturn(Collections.singletonList(taskDefinition));
        when(taskDefinitionMapper.queryByCode(Mockito.anyLong())).thenReturn(taskDefinition);
        int result = processService.saveTaskDefine(operator, projectCode, taskDefinitionLogs, Boolean.TRUE);
        Assertions.assertEquals(0, result);
    }

    @Test
    public void testGenDagGraph() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setId(123);
        workflowDefinition.setName("test");
        workflowDefinition.setVersion(1);
        workflowDefinition.setCode(11L);

        WorkflowTaskRelationLog processTaskRelation = new WorkflowTaskRelationLog();
        processTaskRelation.setName("def 1");
        processTaskRelation.setWorkflowDefinitionVersion(1);
        processTaskRelation.setProjectCode(1L);
        processTaskRelation.setWorkflowDefinitionCode(1L);
        processTaskRelation.setPostTaskCode(3L);
        processTaskRelation.setPreTaskCode(2L);
        processTaskRelation.setUpdateTime(new Date());
        processTaskRelation.setCreateTime(new Date());
        List<WorkflowTaskRelationLog> list = new ArrayList<>();
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
        taskDefinition.setIsCache(Flag.NO);

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

        when(taskDefinitionLogDao.queryTaskDefineLogList(any())).thenReturn(taskDefinitionLogs);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(list);

        DAG<Long, TaskNode, TaskNodeRelation> stringTaskNodeTaskNodeRelationDAG =
                processService.genDagGraph(workflowDefinition);
        Assertions.assertEquals(1, stringTaskNodeTaskNodeRelationDAG.getNodesCount());
    }

    @Test
    public void testChangeOutParam() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setWorkflowInstanceId(62);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(62);
        taskInstance.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test1\",\"type\":\"VARCHAR\",\"value\":\"\"}]");
        taskInstance.setTaskParams("{\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select id from tb_test limit 1\","
                + "\"sqlType\":\"0\",\"sendEmail\":false,\"displayRows\":10,\"title\":\"\","
                + "\"groupId\":null,\"localParams\":[{\"prop\":\"test1\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"12\"}],"
                + "\"connParams\":\"\",\"preStatements\":[],\"postStatements\":[],\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],"
                + "\\\"failedNode\\\":[\\\"\\\"]}\",\"dependence\":\"{}\"}");
        processService.changeOutParam(taskInstance);
    }

    @Test
    public void testUpdateResourceInfo() throws Exception {
        // test if input is null
        ResourceInfo resourceInfoNull = null;
        ResourceInfo updatedResourceInfo1 = processService.updateResourceInfo(0, resourceInfoNull);
        Assertions.assertNull(updatedResourceInfo1);

        // test if resource id less than 1
        ResourceInfo resourceInfoVoid = new ResourceInfo();
        ResourceInfo updatedResourceInfo2 = processService.updateResourceInfo(0, resourceInfoVoid);
        Assertions.assertNull(updatedResourceInfo2.getResourceName());

        // test normal situation
        ResourceInfo resourceInfoNormal = new ResourceInfo();
        resourceInfoNormal.setResourceName("/test.txt");

        ResourceInfo updatedResourceInfo3 = processService.updateResourceInfo(0, resourceInfoNormal);

        Assertions.assertEquals("/test.txt", updatedResourceInfo3.getResourceName());

    }

    @Test
    public void testCreateTaskGroupQueue() {
        when(taskGroupQueueMapper.insert(Mockito.any(TaskGroupQueue.class))).thenReturn(1);
        TaskGroupQueue taskGroupQueue =
                processService.insertIntoTaskGroupQueue(1, "task name", 1, 1, 1, TaskGroupQueueStatus.WAIT_QUEUE);
        Assertions.assertNotNull(taskGroupQueue);
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
