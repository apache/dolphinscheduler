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

package org.apache.dolphinscheduler.server.master.registry;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.server.master.cache.impl.ProcessInstanceExecCacheManagerImpl;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

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

/**
 * MasterRegistryClientTest
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RegistryClient.class})
@PowerMockIgnore({"javax.management.*"})
public class MasterRegistryClientTest {

    @InjectMocks
    private MasterRegistryClient masterRegistryClient;

    @Mock
    private MasterConfig masterConfig;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private ScheduledExecutorService heartBeatExecutor;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessInstanceExecCacheManagerImpl processInstanceExecCacheManager;

    @Before
    public void before() throws Exception {
        given(registryClient.getLock(Mockito.anyString())).willReturn(true);
        given(registryClient.releaseLock(Mockito.anyString())).willReturn(true);
        given(registryClient.getHostByEventDataPath(Mockito.anyString())).willReturn("127.0.0.1:8080");
        given(registryClient.getStoppable()).willReturn(cause -> {

        });
        doNothing().when(registryClient).handleDeadServer(Mockito.anySet(), Mockito.any(NodeType.class), Mockito.anyString());
        ReflectionTestUtils.setField(masterRegistryClient, "registryClient", registryClient);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setHost("127.0.0.1:8080");
        processInstance.setRestartTime(new Date());
        processInstance.setHistoryCmd("xxx");
        processInstance.setCommandType(CommandType.STOP);
        given(processService.queryNeedFailoverProcessInstances(Mockito.anyString())).willReturn(Arrays.asList(processInstance));
        doNothing().when(processService).processNeedFailoverProcessInstances(Mockito.any(ProcessInstance.class));
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setStartTime(new Date());
        taskInstance.setHost("127.0.0.1:8080");
        given(processService.queryNeedFailoverTaskInstances(Mockito.anyString())).willReturn(Arrays.asList(taskInstance));
        given(processService.findProcessInstanceDetailById(Mockito.anyInt())).willReturn(processInstance);
        given(registryClient.checkNodeExists(Mockito.anyString(), Mockito.any())).willReturn(true);
        Server server = new Server();
        server.setHost("127.0.0.1");
        server.setPort(8080);
        server.setCreateTime(new Date());
        given(registryClient.getServerList(NodeType.WORKER)).willReturn(Arrays.asList(server));
        given(registryClient.getServerList(NodeType.MASTER)).willReturn(Arrays.asList(server));
    }

    @Test
    public void registryTest() {
        masterRegistryClient.registry();
    }

    @Test
    public void handleConnectionStateTest() {
        masterRegistryClient.handleConnectionState(ConnectionState.CONNECTED);
        masterRegistryClient.handleConnectionState(ConnectionState.RECONNECTED);
        masterRegistryClient.handleConnectionState(ConnectionState.SUSPENDED);
    }

    @Test
    public void removeNodePathTest() {
        masterRegistryClient.removeMasterNodePath("/path", NodeType.MASTER, false);
        masterRegistryClient.removeMasterNodePath("/path", NodeType.MASTER, true);
        //Cannot mock static methods
        masterRegistryClient.removeWorkerNodePath("/path", NodeType.WORKER, true);
    }
}
