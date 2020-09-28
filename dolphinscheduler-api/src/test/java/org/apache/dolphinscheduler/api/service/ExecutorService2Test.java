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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.alibaba.fastjson.JSONObject;

/**
 * test for ExecutorService
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ExecutorService2Test {

    @InjectMocks
    private ExecutorService executorService;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private MonitorService monitorService;

    private int processDefinitionId = 1;

    private int tenantId = 1;

    private int userId = 1;

    private ProcessDefinition processDefinition = new ProcessDefinition();

    private User loginUser = new User();

    private String projectName = "projectName";

    private Project project = new Project();

    private String cronTime;

    @Before
    public void init(){
        // user
        loginUser.setId(userId);

        // processDefinition
        processDefinition.setId(processDefinitionId);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        processDefinition.setTenantId(tenantId);
        processDefinition.setUserId(userId);

        // project
        project.setName(projectName);

        // cronRangeTime
        cronTime = "2020-01-01 00:00:00,2020-01-31 23:00:00";

        // mock
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(checkProjectAndAuth());
        Mockito.when(processDefinitionMapper.selectById(processDefinitionId)).thenReturn(processDefinition);
        Mockito.when(processService.getTenantForProcess(tenantId, userId)).thenReturn(new Tenant());
        Mockito.when(processService.createCommand(any(Command.class))).thenReturn(1);
        Mockito.when(monitorService.getServerListFromZK(true)).thenReturn(getMasterServersList());
    }

    /**
     * not complement
     * @throws ParseException
     */
    @Test
    public void testNoComplement() throws ParseException {
        try {
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
            Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                    processDefinitionId, cronTime, CommandType.START_PROCESS,
                    null, null,
                    null, null, 0,
                    "", "", RunMode.RUN_MODE_SERIAL,
                    Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110);
            Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
            verify(processService, times(1)).createCommand(any(Command.class));
        }catch (Exception e){
        }
    }

    /**
     * date error
     * @throws ParseException
     */
    @Test
    public void testDateError() throws ParseException {
        try {
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
            Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                    processDefinitionId, "2020-01-31 23:00:00,2020-01-01 00:00:00", CommandType.COMPLEMENT_DATA,
                    null, null,
                    null, null, 0,
                    "", "", RunMode.RUN_MODE_SERIAL,
                    Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110);
            Assert.assertEquals(Status.START_PROCESS_INSTANCE_ERROR, result.get(Constants.STATUS));
            verify(processService, times(0)).createCommand(any(Command.class));
        }catch (Exception e){
        }
    }

    /**
     * serial
     * @throws ParseException
     */
    @Test
    public void testSerial() throws ParseException {
        try {
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
            Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                    processDefinitionId, cronTime, CommandType.COMPLEMENT_DATA,
                    null, null,
                    null, null, 0,
                    "", "", RunMode.RUN_MODE_SERIAL,
                    Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110);
            Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
            verify(processService, times(1)).createCommand(any(Command.class));
        }catch (Exception e){
        }
    }

    /**
     * without schedule
     * @throws ParseException
     */
    @Test
    public void testParallelWithOutSchedule() throws ParseException {
        try{
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
            Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                    processDefinitionId, cronTime, CommandType.COMPLEMENT_DATA,
                    null, null,
                    null, null, 0,
                    "", "", RunMode.RUN_MODE_PARALLEL,
                    Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110);
            Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
            verify(processService, times(31)).createCommand(any(Command.class));
        }catch (Exception e){
        }
    }

    /**
     * with schedule
     * @throws ParseException
     */
    @Test
    public void testParallelWithSchedule() throws ParseException {
        try{
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(oneSchedulerList());
            Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                    processDefinitionId, cronTime, CommandType.COMPLEMENT_DATA,
                    null, null,
                    null, null, 0,
                    "", "", RunMode.RUN_MODE_PARALLEL,
                    Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110);
            Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
            verify(processService, times(15)).createCommand(any(Command.class));
        }catch (Exception e){
        }
    }


    @Test
    public void testNoMsterServers() throws ParseException{
        Mockito.when(monitorService.getServerListFromZK(true)).thenReturn(new ArrayList<Server>());

        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                processDefinitionId, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                "", "", RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110);
        Assert.assertEquals(result.get(Constants.STATUS),Status.MASTER_NOT_EXISTS);

    }

    private List<Server> getMasterServersList(){
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

    private List<Schedule> zeroSchedulerList(){
        return Collections.EMPTY_LIST;
    }

    private List<Schedule> oneSchedulerList(){
        List<Schedule> schedulerList = new LinkedList<>();
        Schedule schedule = new Schedule();
        schedule.setCrontab("0 0 0 1/2 * ?");
        schedulerList.add(schedule);
        return schedulerList;
    }

    private Map<String, Object> checkProjectAndAuth(){
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

    @Test
    public void testCreateSubCommand() {

        ProcessService processService1 = new ProcessService();
        ProcessInstance parentInstance = new ProcessInstance();
        parentInstance.setProcessDefinitionId(1);
        parentInstance.setWarningType(WarningType.SUCCESS);
        parentInstance.setWarningGroupId(0);

        TaskInstance task = new TaskInstance();
        task.setTaskJson("{params:{processDefinitionId:100}}");
        task.setId(10);

        ProcessInstance childInstance = null;
        ProcessInstanceMap instanceMap = new ProcessInstanceMap();
        instanceMap.setParentProcessInstanceId(1);
        instanceMap.setParentTaskInstanceId(10);
        Command command = null;

        //father history: start; child null == command type: start
        parentInstance.setHistoryCmd("START_PROCESS");
        parentInstance.setCommandType(CommandType.START_PROCESS);
        command = processService1.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_PROCESS, command.getCommandType());

        //father history: start,start failure; child null == command type: start
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService1.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_PROCESS, command.getCommandType());


        //father history: scheduler,start failure; child null == command type: scheduler
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("SCHEDULER,START_FAILURE_TASK_PROCESS");
        command = processService1.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.SCHEDULER, command.getCommandType());

        //father history: complement,start failure; child null == command type: complement
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("COMPLEMENT_DATA,START_FAILURE_TASK_PROCESS");
        parentInstance.setCommandParam("{complementStartDate:'2020-01-01',complementEndDate:'2020-01-10'}");
        command = processService1.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.COMPLEMENT_DATA, command.getCommandType());

        JSONObject complementDate = JSONUtils.parseObject(command.getCommandParam());
        Assert.assertEquals("2020-01-01", String.valueOf(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE)));
        Assert.assertEquals("2020-01-10", String.valueOf(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE)));

        //father history: start,failure,start failure; child not null == command type: start failure
        childInstance = new ProcessInstance();
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService1.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_FAILURE_TASK_PROCESS, command.getCommandType());


    }

}