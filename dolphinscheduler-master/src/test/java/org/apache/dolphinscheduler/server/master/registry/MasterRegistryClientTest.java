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

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.task.MasterHeartBeatTask;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * MasterRegistryClientTest
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MasterRegistryClientTest {

    @InjectMocks
    private MasterRegistryClient masterRegistryClient;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private ProcessService processService;

    @Mock
    private MasterHeartBeatTask masterHeartBeatTask;

    @Mock
    private MasterConfig masterConfig;

    @BeforeEach
    public void before() throws Exception {
        given(registryClient.getHostByEventDataPath(Mockito.anyString())).willReturn("127.0.0.1:8080");
        ReflectionTestUtils.setField(masterRegistryClient, "registryClient", registryClient);
        ReflectionTestUtils.setField(masterRegistryClient, "masterHeartBeatTask", masterHeartBeatTask);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setHost("127.0.0.1:8080");
        processInstance.setRestartTime(new Date());
        processInstance.setHistoryCmd("xxx");
        processInstance.setCommandType(CommandType.STOP);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setStartTime(new Date());
        taskInstance.setHost("127.0.0.1:8080");
        given(registryClient.checkNodeExists(Mockito.anyString(), Mockito.any())).willReturn(true);
        Server server = new Server();
        server.setHost("127.0.0.1");
        server.setPort(8080);
        server.setCreateTime(new Date());
    }

    @Test
    public void registryTest() {
        masterRegistryClient.registry();
    }

    @Test
    public void removeNodePathTest() {
        masterRegistryClient.removeMasterNodePath("/path", NodeType.MASTER, false);
        masterRegistryClient.removeMasterNodePath("/path", NodeType.MASTER, true);
        // Cannot mock static methods
        masterRegistryClient.removeWorkerNodePath("/path", NodeType.WORKER, true);
    }
}
