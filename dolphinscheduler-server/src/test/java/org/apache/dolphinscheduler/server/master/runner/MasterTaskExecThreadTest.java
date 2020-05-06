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

import junit.framework.Assert;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.consumer.TaskPriorityQueueConsumer;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueueImpl;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DependencyConfig.class, SpringApplicationContext.class, SpringZKServer.class,
        NettyExecutorManager.class, ExecutorDispatcher.class, ZookeeperRegistryCenter.class, TaskPriorityQueueConsumer.class,
        ZookeeperNodeManager.class, ZookeeperCachedOperator.class, ZookeeperConfig.class, MasterConfig.class})
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


}
