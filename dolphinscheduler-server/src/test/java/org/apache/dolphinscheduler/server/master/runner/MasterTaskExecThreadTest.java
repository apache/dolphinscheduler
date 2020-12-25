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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.consumer.TaskPriorityQueueConsumer;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DependencyConfig.class, SpringApplicationContext.class, SpringZKServer.class,
        NettyExecutorManager.class, ExecutorDispatcher.class, ZookeeperRegistryCenter.class, TaskPriorityQueueConsumer.class,
        ZookeeperNodeManager.class, ZookeeperCachedOperator.class, ZookeeperConfig.class})
public class MasterTaskExecThreadTest {


    @Test
    public void testExistsValidWorkerGroup1(){
        ZookeeperRegistryCenter zookeeperRegistryCenter = Mockito.mock(ZookeeperRegistryCenter.class);
        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(null);
        MasterTaskExecThread masterTaskExecThread = new MasterTaskExecThread(null);
        masterTaskExecThread.existsValidWorkerGroup("default");
    }
    @Test
    public void testExistsValidWorkerGroup2(){
        ZookeeperRegistryCenter zookeeperRegistryCenter = Mockito.mock(ZookeeperRegistryCenter.class);
        Set<String> workerGorups = new HashSet<>();
        workerGorups.add("test1");
        workerGorups.add("test2");

        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(workerGorups);
        MasterTaskExecThread masterTaskExecThread = new MasterTaskExecThread(null);
        masterTaskExecThread.existsValidWorkerGroup("default");
    }

    @Test
    public void testExistsValidWorkerGroup3(){
        ZookeeperRegistryCenter zookeeperRegistryCenter = Mockito.mock(ZookeeperRegistryCenter.class);
        Set<String> workerGorups = new HashSet<>();
        workerGorups.add("test1");

        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(workerGorups);
        Mockito.when(zookeeperRegistryCenter.getWorkerGroupNodesDirectly("test1")).thenReturn(workerGorups);
        MasterTaskExecThread masterTaskExecThread = new MasterTaskExecThread(null);
        masterTaskExecThread.existsValidWorkerGroup("test1");
    }

    @Test
    public void testPauseTask(){


        ProcessService processService = Mockito.mock(ProcessService.class);
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskInstance taskInstance = getTaskInstance();
        Mockito.when(processService.findTaskInstanceById(252612))
                .thenReturn(taskInstance);

        Mockito.when(processService.updateTaskInstance(taskInstance))
        .thenReturn(true);

        MasterTaskExecThread masterTaskExecThread = new MasterTaskExecThread(taskInstance);
        masterTaskExecThread.pauseTask();
        org.junit.Assert.assertEquals(ExecutionStatus.PAUSE, taskInstance.getState());
    }

    private TaskInstance getTaskInstance(){
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType("SHELL");
        taskInstance.setId(252612);
        taskInstance.setName("C");
        taskInstance.setProcessInstanceId(10111);
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        return taskInstance;
    }

}
