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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.RERUN;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_START;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ExecutorServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * executor service 2 test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExecutorServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceTest.class);

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @InjectMocks
    private ExecutorServiceImpl executorService;

    @Mock
    private ProcessService processService;

    @Mock
    private CommandService commandService;

    @Mock
    private WorkerGroupService workerGroupService;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private MonitorService monitorService;

    @Mock
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Mock
    private ProcessInstanceMapper processInstanceMapper;

    private int processDefinitionId = 1;

    private long processDefinitionCode = 1L;

    private int processInstanceId = 1;

    private int tenantId = 1;

    private int userId = 1;

    private int taskQueueId = 1;

    private ProcessDefinition processDefinition = new ProcessDefinition();

    private ProcessInstance processInstance = new ProcessInstance();

    private TaskGroupQueue taskGroupQueue = new TaskGroupQueue();

    private User loginUser = new User();

    private long projectCode = 1L;

    private String projectName = "projectName";

    private Project project = new Project();

    private String cronTime;

    @BeforeEach
    public void init() {
        // user
        loginUser.setId(userId);

        // processDefinition
        processDefinition.setId(processDefinitionId);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        processDefinition.setTenantId(tenantId);
        processDefinition.setUserId(userId);
        processDefinition.setVersion(1);
        processDefinition.setCode(1L);
        processDefinition.setProjectCode(projectCode);

        // processInstance
        processInstance.setId(processInstanceId);
        processInstance.setState(WorkflowExecutionStatus.FAILURE);
        processInstance.setExecutorId(userId);
        processInstance.setTenantId(tenantId);
        processInstance.setProcessDefinitionVersion(1);
        processInstance.setProcessDefinitionCode(1L);

        // project
        project.setCode(projectCode);
        project.setName(projectName);

        // taskGroupQueue
        taskGroupQueue.setId(taskQueueId);
        taskGroupQueue.setStatus(TaskGroupQueueStatus.WAIT_QUEUE);
        taskGroupQueue.setProcessId(processInstanceId);

        // cronRangeTime
        cronTime = "2020-01-01 00:00:00,2020-01-31 23:00:00";

        // mock
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_START))
                .thenReturn(checkProjectAndAuth());
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(processDefinition);
        Mockito.when(processService.getTenantForProcess(tenantId, userId)).thenReturn(new Tenant());
        doReturn(1).when(commandService).createCommand(argThat(c -> c.getId() == null));
        doReturn(0).when(commandService).createCommand(argThat(c -> c.getId() != null));
        Mockito.when(monitorService.getServerListFromRegistry(true)).thenReturn(getMasterServersList());
        Mockito.when(processService.findProcessInstanceDetailById(processInstanceId))
                .thenReturn(Optional.ofNullable(processInstance));
        Mockito.when(processService.findProcessDefinition(1L, 1)).thenReturn(processDefinition);
        Mockito.when(taskGroupQueueMapper.selectById(1)).thenReturn(taskGroupQueue);
        Mockito.when(processInstanceMapper.selectById(1)).thenReturn(processInstance);
    }

    @Test
    public void testForceStartTaskInstance() {

        Map<String, Object> result = executorService.forceStartTaskInstance(loginUser, taskQueueId);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    /**
     * not complement
     */
    @Test
    public void testNoComplement() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode))
                .thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode,
                "{\"complementStartDate\":\"2020-01-01 00:00:00\",\"complementEndDate\":\"2020-01-31 23:00:00\"}",
                CommandType.START_PROCESS,
                null, null,
                null, null, null,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 10, null, 0, Constants.DRY_RUN_FLAG_NO,
                Constants.TEST_FLAG_NO,
                ComplementDependentMode.OFF_MODE);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(commandService, times(1)).createCommand(any(Command.class));

    }

    /**
     * not complement
     */
    @Test
    public void testComplementWithStartNodeList() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode))
                .thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode,
                "{\"complementStartDate\":\"2020-01-01 00:00:00\",\"complementEndDate\":\"2020-01-31 23:00:00\"}",
                CommandType.START_PROCESS,
                null, "n1,n2",
                null, null, null,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 110, null, 0, Constants.DRY_RUN_FLAG_NO,
                Constants.TEST_FLAG_NO,
                ComplementDependentMode.OFF_MODE);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(commandService, times(1)).createCommand(any(Command.class));

    }

    @Test
    public void testComplementWithDependentMode() {
        Schedule schedule = new Schedule();
        schedule.setStartTime(new Date());
        schedule.setEndTime(new Date());
        schedule.setCrontab("0 0 7 * * ? *");
        schedule.setFailureStrategy(FailureStrategy.CONTINUE);
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedule.setWarningType(WarningType.NONE);
        schedule.setCreateTime(new Date());
        schedule.setUpdateTime(new Date());
        List<Schedule> schedules = Lists.newArrayList(schedule);
        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(
                processDefinitionCode))
                .thenReturn(schedules);

        DependentProcessDefinition dependentProcessDefinition = new DependentProcessDefinition();
        dependentProcessDefinition.setProcessDefinitionCode(2);
        dependentProcessDefinition.setProcessDefinitionVersion(1);
        dependentProcessDefinition.setTaskDefinitionCode(1);
        dependentProcessDefinition.setWorkerGroup(Constants.DEFAULT_WORKER_GROUP);
        dependentProcessDefinition.setTaskParams(
                "{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\",\"dependItemList\":[{\"depTaskCode\":2,\"status\":\"SUCCESS\"}]}]},\"conditionResult\":{\"successNode\":[1],\"failedNode\":[1]}}");
        Mockito.when(processService.queryDependentProcessDefinitionByProcessDefinitionCode(processDefinitionCode))
                .thenReturn(Lists.newArrayList(dependentProcessDefinition));

        Map<Long, String> processDefinitionWorkerGroupMap = new HashMap<>();
        processDefinitionWorkerGroupMap.put(1L, Constants.DEFAULT_WORKER_GROUP);
        Mockito.when(workerGroupService.queryWorkerGroupByProcessDefinitionCodes(Lists.newArrayList(1L)))
                .thenReturn(processDefinitionWorkerGroupMap);

        Command command = new Command();
        command.setId(1);
        command.setCommandType(CommandType.COMPLEMENT_DATA);
        command.setCommandParam(
                "{\"StartNodeList\":\"1\",\"complementStartDate\":\"2020-01-01 00:00:00\",\"complementEndDate\":\"2020-01-31 23:00:00\"}");
        command.setWorkerGroup(Constants.DEFAULT_WORKER_GROUP);
        command.setProcessDefinitionCode(processDefinitionCode);
        command.setExecutorId(1);

        int count = executorService.createComplementDependentCommand(schedules, command);
        Assertions.assertEquals(1, count);
    }

    /**
     * date error
     */
    @Test
    public void testDateError() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode))
                .thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode,
                "{\"complementStartDate\":\"2022-01-07 12:12:12\",\"complementEndDate\":\"2022-01-06 12:12:12\"}",
                CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, null,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 110, null, 0, Constants.DRY_RUN_FLAG_NO,
                Constants.TEST_FLAG_NO,
                ComplementDependentMode.OFF_MODE);
        Assertions.assertEquals(Status.START_PROCESS_INSTANCE_ERROR, result.get(Constants.STATUS));
        verify(commandService, times(0)).createCommand(any(Command.class));
    }

    /**
     * serial
     */
    @Test
    public void testSerial() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode))
                .thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode,
                "{\"complementStartDate\":\"2020-01-01 00:00:00\",\"complementEndDate\":\"2020-01-31 23:00:00\"}",
                CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, null,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 110, null, 0, Constants.DRY_RUN_FLAG_NO,
                Constants.TEST_FLAG_NO,
                ComplementDependentMode.OFF_MODE);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(commandService, times(1)).createCommand(any(Command.class));
    }

    /**
     * without schedule
     */
    @Test
    public void testParallelWithOutSchedule() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode))
                .thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode,
                "{\"complementStartDate\":\"2020-01-01 00:00:00\",\"complementEndDate\":\"2020-01-31 23:00:00\"}",
                CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, null,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 110, null, 0, Constants.DRY_RUN_FLAG_NO,
                Constants.TEST_FLAG_NO,
                ComplementDependentMode.OFF_MODE);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(commandService, times(31)).createCommand(any(Command.class));

    }

    /**
     * with schedule
     */
    @Test
    public void testParallelWithSchedule() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode))
                .thenReturn(oneSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode,
                "{\"complementStartDate\":\"2020-01-01 00:00:00\",\"complementEndDate\":\"2020-01-31 23:00:00\"}",
                CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, null,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 110, null, 15, Constants.DRY_RUN_FLAG_NO,
                Constants.TEST_FLAG_NO,
                ComplementDependentMode.OFF_MODE);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(commandService, times(15)).createCommand(any(Command.class));

    }

    @Test
    public void testNoMasterServers() {
        Mockito.when(monitorService.getServerListFromRegistry(true)).thenReturn(new ArrayList<>());

        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode,
                "{\"complementStartDate\":\"2020-01-01 00:00:00\",\"complementEndDate\":\"2020-01-31 23:00:00\"}",
                CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, null,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 110, null, 0, Constants.DRY_RUN_FLAG_NO,
                Constants.TEST_FLAG_NO,
                ComplementDependentMode.OFF_MODE);
        Assertions.assertEquals(result.get(Constants.STATUS), Status.MASTER_NOT_EXISTS);

    }

    @Test
    public void testExecuteRepeatRunning() {
        Mockito.when(commandService.verifyIsNeedCreateCommand(any(Command.class))).thenReturn(true);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode, RERUN))
                .thenReturn(checkProjectAndAuth());
        Map<String, Object> result =
                executorService.execute(loginUser, projectCode, processInstanceId, ExecuteType.REPEAT_RUNNING);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testOfTestRun() {
        Mockito.when(commandService.verifyIsNeedCreateCommand(any(Command.class))).thenReturn(true);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode, RERUN))
                .thenReturn(checkProjectAndAuth());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode,
                "{\"complementStartDate\":\"2020-01-01 00:00:00\",\"complementEndDate\":\"2020-01-31 23:00:00\"}",
                CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 110, null, 15, Constants.DRY_RUN_FLAG_NO,
                Constants.TEST_FLAG_YES,
                ComplementDependentMode.OFF_MODE);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testStartCheckByProcessDefinedCode() {
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        Mockito.doNothing().when(processService).recurseFindSubProcess(1, ids);

        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefinitionMapper.queryDefinitionListByIdList(new Integer[ids.size()]))
                .thenReturn(processDefinitionList);

        Map<String, Object> result = executorService.startCheckByProcessDefinedCode(1L);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    private List<Server> getMasterServersList() {
        List<Server> masterServerList = new ArrayList<>();
        Server masterServer1 = new Server();
        masterServer1.setId(1);
        masterServer1.setHost("192.168.220.188");
        masterServer1.setPort(1121);
        masterServerList.add(masterServer1);

        Server masterServer2 = new Server();
        masterServer2.setId(2);
        masterServer2.setHost("192.168.220.189");
        masterServer2.setPort(1122);
        masterServerList.add(masterServer2);

        return masterServerList;
    }

    private List zeroSchedulerList() {
        return Collections.EMPTY_LIST;
    }

    private List<Schedule> oneSchedulerList() {
        List<Schedule> schedulerList = new LinkedList<>();
        Schedule schedule = new Schedule();
        schedule.setCrontab("0 0 0 1/2 * ?");
        schedulerList.add(schedule);
        return schedulerList;
    }

    private Map<String, Object> checkProjectAndAuth() {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

    @Test
    public void testCreateComplementToParallel() {
        List<String> result = new ArrayList<>();
        int expectedParallelismNumber = 3;
        LinkedList<Integer> listDate = new LinkedList<>();
        listDate.add(0);
        listDate.add(1);
        listDate.add(2);
        listDate.add(3);
        listDate.add(4);

        int listDateSize = listDate.size();
        int createCount = Math.min(listDate.size(), expectedParallelismNumber);
        logger.info("In parallel mode, current expectedParallelismNumber:{}", createCount);

        int itemsPerCommand = (listDateSize / createCount);
        int remainingItems = (listDateSize % createCount);
        int startDateIndex = 0;
        int endDateIndex = 0;

        for (int i = 1; i <= createCount; i++) {
            int extra = (i <= remainingItems) ? 1 : 0;
            int singleCommandItems = (itemsPerCommand + extra);

            if (i == 1) {
                endDateIndex += singleCommandItems - 1;
            } else {
                startDateIndex = endDateIndex + 1;
                endDateIndex += singleCommandItems;
            }

            logger.info("startDate:{}, endDate:{}", listDate.get(startDateIndex), listDate.get(endDateIndex));
            result.add(listDate.get(startDateIndex) + "," + listDate.get(endDateIndex));
        }

        Assertions.assertEquals("0,1", result.get(0));
        Assertions.assertEquals("2,3", result.get(1));
        Assertions.assertEquals("4,4", result.get(2));
    }

}
