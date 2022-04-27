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
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

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

    private String testHost;
    private ProcessInstance processInstance;
    private TaskInstance taskInstance;

    @Before
    public void before() throws Exception {
        given(masterConfig.getListenPort()).willReturn(8080);

        testHost = failoverService.getLocalAddress();
        String ip = testHost.split(":")[0];
        int port = Integer.valueOf(testHost.split(":")[1]);
        Assert.assertEquals(8080, port);

        given(registryClient.getLock(Mockito.anyString())).willReturn(true);
        given(registryClient.releaseLock(Mockito.anyString())).willReturn(true);
        given(registryClient.getHostByEventDataPath(Mockito.anyString())).willReturn(testHost);
        given(registryClient.getStoppable()).willReturn(cause -> {
        });
        given(registryClient.checkNodeExists(Mockito.anyString(), Mockito.any())).willReturn(true);
        doNothing().when(registryClient).handleDeadServer(Mockito.anySet(), Mockito.any(NodeType.class), Mockito.anyString());

        processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setHost(testHost);
        processInstance.setRestartTime(new Date());
        processInstance.setHistoryCmd("xxx");
        processInstance.setCommandType(CommandType.STOP);

        taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setStartTime(new Date());
        taskInstance.setHost(testHost);

        given(processService.queryNeedFailoverTaskInstances(Mockito.anyString())).willReturn(Arrays.asList(taskInstance));
        given(processService.queryNeedFailoverProcessInstanceHost()).willReturn(Lists.newArrayList(testHost));
        given(processService.queryNeedFailoverProcessInstances(Mockito.anyString())).willReturn(Arrays.asList(processInstance));
        doNothing().when(processService).processNeedFailoverProcessInstances(Mockito.any(ProcessInstance.class));
        given(processService.findValidTaskListByProcessId(Mockito.anyInt())).willReturn(Lists.newArrayList(taskInstance));
        given(processService.findProcessInstanceDetailById(Mockito.anyInt())).willReturn(processInstance);

        Thread.sleep(1000);
        Server server = new Server();
        server.setHost(ip);
        server.setPort(port);
        server.setCreateTime(new Date());
        given(registryClient.getServerList(NodeType.WORKER)).willReturn(Arrays.asList(server));
        given(registryClient.getServerList(NodeType.MASTER)).willReturn(Arrays.asList(server));
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
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        failoverService.failoverServerWhenDown(testHost, NodeType.MASTER);
        Assert.assertNotEquals(taskInstance.getState(), ExecutionStatus.NEED_FAULT_TOLERANCE);

        processInstance.setHost(testHost);
        taskInstance.setState(ExecutionStatus.SUCCESS);
        failoverService.failoverServerWhenDown(testHost, NodeType.MASTER);
        Assert.assertNotEquals(taskInstance.getState(), ExecutionStatus.NEED_FAULT_TOLERANCE);
        Assert.assertEquals(Constants.NULL, processInstance.getHost());

        processInstance.setHost(testHost);
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        failoverService.failoverServerWhenDown(testHost, NodeType.MASTER);
        Assert.assertEquals(taskInstance.getState(), ExecutionStatus.NEED_FAULT_TOLERANCE);
        Assert.assertEquals(Constants.NULL, processInstance.getHost());
    }

    @Test
    public void failoverWorkTest() {
        failoverService.failoverServerWhenDown(testHost, NodeType.WORKER);
        Assert.assertEquals(taskInstance.getState(), ExecutionStatus.NEED_FAULT_TOLERANCE);
    }
}
