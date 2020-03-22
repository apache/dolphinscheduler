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
package org.apache.dolphinscheduler.server.master.dispatch.executor;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteProcessor;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistry;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * netty executor manager test
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DependencyConfig.class, SpringZKServer.class, WorkerRegistry.class,
        ZookeeperNodeManager.class, ZookeeperRegistryCenter.class, WorkerConfig.class,
        ZookeeperCachedOperator.class, ZookeeperConfig.class, SpringApplicationContext.class, NettyExecutorManager.class})
public class NettyExecutorManagerTest {

    @Autowired
    private NettyExecutorManager nettyExecutorManager;


    @Test
    public void testExecute() throws ExecuteException{
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(30000);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(org.apache.dolphinscheduler.remote.command.CommandType.TASK_EXECUTE_REQUEST, new TaskExecuteProcessor());
        nettyRemotingServer.start();
        TaskInstance taskInstance = Mockito.mock(TaskInstance.class);
        ProcessDefinition processDefinition = Mockito.mock(ProcessDefinition.class);
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setCommandType(CommandType.COMPLEMENT_DATA);
        taskInstance.setProcessInstance(processInstance);
        TaskExecutionContext context = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildProcessInstanceRelatedInfo(processInstance)
                .buildProcessDefinitionRelatedInfo(processDefinition)
                .create();
        ExecutionContext executionContext = new ExecutionContext(context.toCommand(), ExecutorType.WORKER);
        executionContext.setHost(Host.of(OSUtils.getHost() + ":" + serverConfig.getListenPort()));
        Boolean execute = nettyExecutorManager.execute(executionContext);
        Assert.assertTrue(execute);
        nettyRemotingServer.close();
    }

    @Test(expected = ExecuteException.class)
    public void testExecuteWithException() throws ExecuteException{
        TaskInstance taskInstance = Mockito.mock(TaskInstance.class);
        ProcessDefinition processDefinition = Mockito.mock(ProcessDefinition.class);
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setCommandType(CommandType.COMPLEMENT_DATA);
        taskInstance.setProcessInstance(processInstance);
        TaskExecutionContext context = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildProcessInstanceRelatedInfo(processInstance)
                .buildProcessDefinitionRelatedInfo(processDefinition)
                .create();
        ExecutionContext executionContext = new ExecutionContext(context.toCommand(), ExecutorType.WORKER);
        executionContext.setHost(Host.of(OSUtils.getHost() + ":4444"));
        nettyExecutorManager.execute(executionContext);

    }
}
