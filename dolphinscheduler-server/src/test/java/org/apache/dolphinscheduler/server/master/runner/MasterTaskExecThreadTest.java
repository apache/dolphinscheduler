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
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.cache.impl.TaskInstanceCacheManagerImpl;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.consumer.TaskPriorityQueueConsumer;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

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
    public void submitWaitComplete() throws Exception {
        // prepare
        ProcessInstance processInstance = processInstance();
        TaskInstance taskInstance = taskInstance();

        TaskInstanceCacheManagerImpl taskInstanceCacheManager = mock(TaskInstanceCacheManagerImpl.class);
        ProcessService processService = mock(ProcessService.class);
        MasterConfig masterConfig=mock(MasterConfig.class);
        TaskPriorityQueue taskUpdateQueue=mock(TaskPriorityQueue.class);
        AlertDao alertDao=mock(AlertDao.class);
        NettyExecutorManager nettyExecutorManager=mock(NettyExecutorManager.class);
        ZookeeperRegistryCenter zookeeperRegistryCenter=mock(ZookeeperRegistryCenter.class);

        // mock constructor
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
        when(applicationContext.getBean(MasterConfig.class)).thenReturn(masterConfig);
        when(applicationContext.getBean(TaskPriorityQueue.class)).thenReturn(taskUpdateQueue);
        when(applicationContext.getBean(AlertDao.class)).thenReturn(alertDao);
        when(applicationContext.getBean(TaskInstanceCacheManagerImpl.class)).thenReturn(taskInstanceCacheManager);
        when(applicationContext.getBean(NettyExecutorManager.class)).thenReturn(nettyExecutorManager);
        when(applicationContext.getBean(ZookeeperRegistryCenter.class)).thenReturn(zookeeperRegistryCenter);

        // when
        when(processService.findProcessInstanceById(taskInstance.getProcessInstanceId())).thenReturn(processInstance);
        when(masterConfig.getMasterTaskCommitRetryTimes()).thenReturn(Integer.valueOf(1));
        when(masterConfig.getMasterTaskCommitInterval()).thenReturn(Integer.valueOf(1));
        when(processService.submitTask(taskInstance)).thenReturn(taskInstance);
        doNothing().when(taskInstanceCacheManager).cacheTaskInstance(taskInstance);
        when(processService.updateTaskInstance(taskInstance)).thenReturn(true);

        // this must be there after above method invoked
        MasterTaskExecThread masterTaskExecThread=new MasterTaskExecThread(taskInstance);

        // update task state
        TaskInstance newTaskInstance = taskInstance();
        newTaskInstance.setState(ExecutionStatus.SUCCESS);
        when(taskInstanceCacheManager.getByTaskInstanceId(taskInstance.getId())).thenReturn(newTaskInstance);

        // call
        Boolean result = masterTaskExecThread.call();

        // assert
        Assert.assertEquals(result,true);
    }


    private ProcessInstance processInstance(){
        ProcessInstance processInstance=new ProcessInstance();
        processInstance.setId(1);
        processInstance.setState(ExecutionStatus.SUCCESS);
        return processInstance;
    }

    private TaskInstance taskInstance(){
        TaskInstance taskInstance=new TaskInstance();
        taskInstance.setProcessInstanceId(1);
        taskInstance.setId(1);
        taskInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
        taskInstance.setProcessInstancePriority(Priority.HIGH);
        taskInstance.setTaskType(TaskType.HTTP.getDescp().toUpperCase());
        taskInstance.setTaskJson("{}");
        return taskInstance;
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
