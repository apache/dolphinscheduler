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

import static org.apache.dolphinscheduler.common.constants.Constants.COMMON_TASK_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SWITCH;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FailoverServiceTest {

    private FailoverService failoverService;

    @Mock
    private MasterConfig masterConfig;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private ProcessService processService;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Mock
    private ProcessInstanceExecCacheManager cacheManager;

    @Mock
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    private static int masterPort = 5678;
    private static int workerPort = 1234;

    private String testMasterHost;
    private String testWorkerHost;
    private ProcessInstance processInstance;
    private TaskInstance masterTaskInstance;
    private TaskInstance workerTaskInstance;

    @BeforeEach
    public void before() throws Exception {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        given(masterConfig.getListenPort()).willReturn(masterPort);
        testMasterHost = NetUtils.getAddr(masterConfig.getListenPort());
        given(masterConfig.getMasterAddress()).willReturn(testMasterHost);
        MasterFailoverService masterFailoverService =
                new MasterFailoverService(registryClient, masterConfig, processService,
                        processInstanceExecCacheManager);
        WorkerFailoverService workerFailoverService = new WorkerFailoverService(registryClient,
                masterConfig,
                processService,
                workflowExecuteThreadPool,
                cacheManager,
                taskInstanceDao);

        failoverService = new FailoverService(masterFailoverService, workerFailoverService);

        String ip = testMasterHost.split(":")[0];
        int port = Integer.parseInt(testMasterHost.split(":")[1]);
        Assertions.assertEquals(masterPort, port);

        testWorkerHost = ip + ":" + workerPort;

        given(registryClient.getLock(Mockito.anyString())).willReturn(true);
        given(registryClient.releaseLock(Mockito.anyString())).willReturn(true);

        processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setHost(testMasterHost);
        processInstance.setStartTime(new Date());
        processInstance.setRestartTime(new Date());
        processInstance.setHistoryCmd("xxx");
        processInstance.setCommandType(CommandType.STOP);
        processInstance.setProcessDefinitionCode(123L);
        processInstance.setProcessDefinition(new ProcessDefinition());

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

        given(processService.queryNeedFailoverProcessInstances(Mockito.anyString()))
                .willReturn(Arrays.asList(processInstance));
        doNothing().when(processService).processNeedFailoverProcessInstances(Mockito.any(ProcessInstance.class));
        given(taskInstanceDao.queryValidTaskListByWorkflowInstanceId(Mockito.anyInt(), Mockito.anyInt()))
                .willReturn(Lists.newArrayList(masterTaskInstance, workerTaskInstance));

        Thread.sleep(1000);
        Server masterServer = new Server();
        masterServer.setHost(ip);
        masterServer.setPort(masterPort);
        masterServer.setCreateTime(new Date());

        Server workerServer = new Server();
        workerServer.setHost(ip);
        workerServer.setPort(workerPort);
        workerServer.setCreateTime(new Date());

        given(registryClient.getServerList(RegistryNodeType.WORKER))
                .willReturn(new ArrayList<>(Arrays.asList(workerServer)));
        given(registryClient.getServerList(RegistryNodeType.MASTER))
                .willReturn(new ArrayList<>(Arrays.asList(masterServer)));

        doNothing().when(workflowExecuteThreadPool).submitStateEvent(Mockito.any(StateEvent.class));
    }

    @Test
    public void failoverMasterTest() {
        processInstance.setHost(Constants.NULL);
        masterTaskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        failoverService.failoverServerWhenDown(testMasterHost, RegistryNodeType.MASTER);
        Assertions.assertNotEquals(masterTaskInstance.getState(), TaskExecutionStatus.NEED_FAULT_TOLERANCE);

        processInstance.setHost(testMasterHost);
        masterTaskInstance.setState(TaskExecutionStatus.SUCCESS);
        failoverService.failoverServerWhenDown(testMasterHost, RegistryNodeType.MASTER);
        Assertions.assertNotEquals(masterTaskInstance.getState(), TaskExecutionStatus.NEED_FAULT_TOLERANCE);

        processInstance.setHost(testMasterHost);
        masterTaskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        failoverService.failoverServerWhenDown(testMasterHost, RegistryNodeType.MASTER);
        Assertions.assertEquals(masterTaskInstance.getState(), TaskExecutionStatus.RUNNING_EXECUTION);
    }

    @Test
    public void failoverWorkTest() {
        workerTaskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        WorkflowExecuteRunnable workflowExecuteRunnable = Mockito.mock(WorkflowExecuteRunnable.class);
        Mockito.when(workflowExecuteRunnable.getAllTaskInstances()).thenReturn(Lists.newArrayList(workerTaskInstance));

        IWorkflowExecuteContext workflowExecuteRunnableContext = Mockito.mock(IWorkflowExecuteContext.class);
        Mockito.when(workflowExecuteRunnable.getWorkflowExecuteContext()).thenReturn(workflowExecuteRunnableContext);
        Mockito.when(workflowExecuteRunnableContext.getWorkflowInstance()).thenReturn(processInstance);

        Mockito.when(cacheManager.getAll()).thenReturn(Lists.newArrayList(workflowExecuteRunnable));
        Mockito.when(cacheManager.getByProcessInstanceId(Mockito.anyInt())).thenReturn(workflowExecuteRunnable);

        failoverService.failoverServerWhenDown(testWorkerHost, RegistryNodeType.WORKER);
        Assertions.assertEquals(TaskExecutionStatus.NEED_FAULT_TOLERANCE, workerTaskInstance.getState());
    }
}
