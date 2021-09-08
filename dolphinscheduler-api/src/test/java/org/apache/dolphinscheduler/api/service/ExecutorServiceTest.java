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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ExecutorServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * executor service 2 test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ExecutorServiceTest {

    @InjectMocks
    private ExecutorServiceImpl executorService;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private MonitorService monitorService;

    private int processDefinitionId = 1;

    private long processDefinitionCode = 1L;

    private int processInstanceId = 1;

    private int tenantId = 1;

    private int userId = 1;

    private ProcessDefinition processDefinition = new ProcessDefinition();

    private ProcessInstance processInstance = new ProcessInstance();

    private User loginUser = new User();

    private long projectCode = 1L;

    private String projectName = "projectName";

    private Project project = new Project();

    private String cronTime;

    @Before
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

        // processInstance
        processInstance.setId(processInstanceId);
        processInstance.setState(ExecutionStatus.FAILURE);
        processInstance.setExecutorId(userId);
        processInstance.setTenantId(tenantId);
        processInstance.setProcessDefinitionVersion(1);
        processInstance.setProcessDefinitionCode(1L);

        // project
        project.setCode(projectCode);
        project.setName(projectName);

        // cronRangeTime
        cronTime = "2020-01-01 00:00:00,2020-01-31 23:00:00";

        // mock
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode)).thenReturn(checkProjectAndAuth());
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(processDefinition);
        Mockito.when(processService.getTenantForProcess(tenantId, userId)).thenReturn(new Tenant());
        Mockito.when(processService.createCommand(any(Command.class))).thenReturn(1);
        Mockito.when(monitorService.getServerListFromRegistry(true)).thenReturn(getMasterServersList());
        Mockito.when(processService.findProcessInstanceDetailById(processInstanceId)).thenReturn(processInstance);
        Mockito.when(processService.findProcessDefinition(1L, 1)).thenReturn(processDefinition);
    }

    /**
     * not complement
     */
    @Test
    public void testNoComplement() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode, cronTime, CommandType.START_PROCESS,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L, 10, null, 0);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(1)).createCommand(any(Command.class));

    }

    /**
     * not complement
     */
    @Test
    public void testComplementWithStartNodeList() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode, cronTime, CommandType.START_PROCESS,
                null, "n1,n2",
                null, null, 0,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L,110, null, 0);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(1)).createCommand(any(Command.class));

    }

    /**
     * date error
     */
    @Test
    public void testDateError() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode, "2020-01-31 23:00:00,2020-01-01 00:00:00", CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP,100L, 110, null, 0);
        Assert.assertEquals(Status.START_PROCESS_INSTANCE_ERROR, result.get(Constants.STATUS));
        verify(processService, times(0)).createCommand(any(Command.class));
    }

    /**
     * serial
     */
    @Test
    public void testSerial() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP,100L, 110, null, 0);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(1)).createCommand(any(Command.class));
    }

    /**
     * without schedule
     */
    @Test
    public void testParallelWithOutSchedule() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP,100L, 110, null, 0);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(31)).createCommand(any(Command.class));

    }

    /**
     * with schedule
     */
    @Test
    public void testParallelWithSchedule() {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode)).thenReturn(oneSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L,110, null, 15);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(15)).createCommand(any(Command.class));

    }

    @Test
    public void testNoMasterServers() {
        Mockito.when(monitorService.getServerListFromRegistry(true)).thenReturn(new ArrayList<>());

        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode,
                processDefinitionCode, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 100L,110, null, 0);
        Assert.assertEquals(result.get(Constants.STATUS), Status.MASTER_NOT_EXISTS);

    }

    @Test
    public void testExecuteRepeatRunning() {
        Mockito.when(processService.verifyIsNeedCreateCommand(any(Command.class))).thenReturn(true);

        Map<String, Object> result = executorService.execute(loginUser, projectCode, processInstanceId, ExecuteType.REPEAT_RUNNING);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testStartCheckByProcessDefinedCode() {
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        Mockito.doNothing().when(processService).recurseFindSubProcessId(1, ids);

        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefinitionMapper.queryDefinitionListByIdList(new Integer[ids.size()]))
                .thenReturn(processDefinitionList);

        Map<String, Object> result = executorService.startCheckByProcessDefinedCode(1L);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
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
}
