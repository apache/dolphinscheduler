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

package org.apache.dolphinscheduler.server.master.service;

import static org.apache.dolphinscheduler.common.Constants.COMMON_TASK_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SWITCH;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

/**
 * MasterRegistryClientTest
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RegistryClient.class})
@PowerMockIgnore({"javax.management.*"})
public class FailoverServiceTest {
    @InjectMocks
    private FailoverService failoverService;

    @Mock
    private MasterConfig masterConfig;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private ProcessService processService;

    @Mock
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    private static int masterPort = 5678;
    private static int workerPort = 1234;

    private String testMasterHost;
    private String testWorkerHost;
    private ProcessInstance processInstance;
    private TaskInstance masterTaskInstance;
    private TaskInstance workerTaskInstance;

    @Before
    public void before() throws Exception {
        // init spring context
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        given(masterConfig.getListenPort()).willReturn(masterPort);

        testMasterHost = failoverService.getLocalAddress();
        String ip = testMasterHost.split(":")[0];
        int port = Integer.valueOf(testMasterHost.split(":")[1]);
        Assert.assertEquals(masterPort, port);

        testWorkerHost = ip + ":" + workerPort;

        given(registryClient.getLock(Mockito.anyString())).willReturn(true);
        given(registryClient.releaseLock(Mockito.anyString())).willReturn(true);
        given(registryClient.getHostByEventDataPath(Mockito.anyString())).willReturn(testMasterHost);
        given(registryClient.getStoppable()).willReturn(cause -> {
        });
        given(registryClient.checkNodeExists(Mockito.anyString(), Mockito.any())).willReturn(true);
        doNothing().when(registryClient).handleDeadServer(Mockito.anySet(), Mockito.any(NodeType.class), Mockito.anyString());

        processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setHost(testMasterHost);
        processInstance.setRestartTime(new Date());
        processInstance.setHistoryCmd("xxx");
        processInstance.setCommandType(CommandType.STOP);

        masterTaskInstance = new TaskInstance();
        masterTaskInstance.setId(1);
        masterTaskInstance.setStartTime(new Date());
        masterTaskInstance.setHost(testMasterHost);
        masterTaskInstance.setTaskType(TASK_TYPE_SWITCH);

        workerTaskInstance = new TaskInstance();
        workerTaskInstance.setId(2);
        workerTaskInstance.setStartTime(new Date());
        workerTaskInstance.setHost(testWorkerHost);
        workerTaskInstance.setTaskType(COMMON_TASK_TYPE);

        given(processService.queryNeedFailoverTaskInstances(Mockito.anyString())).willReturn(Arrays.asList(masterTaskInstance, workerTaskInstance));
        given(processService.queryNeedFailoverProcessInstanceHost()).willReturn(Lists.newArrayList(testMasterHost));
        given(processService.queryNeedFailoverProcessInstances(Mockito.anyString())).willReturn(Arrays.asList(processInstance));
        doNothing().when(processService).processNeedFailoverProcessInstances(Mockito.any(ProcessInstance.class));
        given(processService.findValidTaskListByProcessId(Mockito.anyInt())).willReturn(Lists.newArrayList(masterTaskInstance, workerTaskInstance));
        given(processService.findProcessInstanceDetailById(Mockito.anyInt())).willReturn(processInstance);

        Thread.sleep(1000);
        Server masterServer = new Server();
        masterServer.setHost(ip);
        masterServer.setPort(masterPort);
        masterServer.setCreateTime(new Date());

        Server workerServer = new Server();
        workerServer.setHost(ip);
        workerServer.setPort(workerPort);
        workerServer.setCreateTime(new Date());

        given(registryClient.getServerList(NodeType.WORKER)).willReturn(new ArrayList<>(Arrays.asList(workerServer)));
        given(registryClient.getServerList(NodeType.MASTER)).willReturn(new ArrayList<>(Arrays.asList(masterServer)));
        ReflectionTestUtils.setField(failoverService, "registryClient", registryClient);

        doNothing().when(workflowExecuteThreadPool).submitStateEvent(Mockito.any(StateEvent.class));
    }

    @Test
    public void checkMasterFailoverTest() {
        failoverService.checkMasterFailover();
    }

    @Test
    public void failoverMasterTest() {
        processInstance.setHost(Constants.NULL);
        masterTaskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        failoverService.failoverServerWhenDown(testMasterHost, NodeType.MASTER);
        Assert.assertNotEquals(masterTaskInstance.getState(), ExecutionStatus.NEED_FAULT_TOLERANCE);

        processInstance.setHost(testMasterHost);
        masterTaskInstance.setState(ExecutionStatus.SUCCESS);
        failoverService.failoverServerWhenDown(testMasterHost, NodeType.MASTER);
        Assert.assertNotEquals(masterTaskInstance.getState(), ExecutionStatus.NEED_FAULT_TOLERANCE);
        Assert.assertEquals(Constants.NULL, processInstance.getHost());

        processInstance.setHost(testMasterHost);
        masterTaskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        failoverService.failoverServerWhenDown(testMasterHost, NodeType.MASTER);
        Assert.assertEquals(masterTaskInstance.getState(), ExecutionStatus.NEED_FAULT_TOLERANCE);
        Assert.assertEquals(Constants.NULL, processInstance.getHost());
    }

    @Test
    public void failoverWorkTest() {
        failoverService.failoverServerWhenDown(testWorkerHost, NodeType.WORKER);
        Assert.assertEquals(workerTaskInstance.getState(), ExecutionStatus.NEED_FAULT_TOLERANCE);
    }
}
