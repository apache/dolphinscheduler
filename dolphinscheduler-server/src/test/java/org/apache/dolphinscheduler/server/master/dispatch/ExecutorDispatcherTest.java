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
package org.apache.dolphinscheduler.server.master.dispatch;


import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.utils.ExecutionContextTestUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteProcessor;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistry;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * executor dispatch test
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DependencyConfig.class, SpringApplicationContext.class, SpringZKServer.class, WorkerRegistry.class,
        NettyExecutorManager.class, ExecutorDispatcher.class, ZookeeperRegistryCenter.class, WorkerConfig.class,
        ZookeeperNodeManager.class, ZookeeperCachedOperator.class, ZookeeperConfig.class})
public class ExecutorDispatcherTest {

    @Autowired
    private ExecutorDispatcher executorDispatcher;

    @Autowired
    private WorkerRegistry workerRegistry;

    @Autowired
    private WorkerConfig workerConfig;

    @Test(expected = ExecuteException.class)
    public void testDispatchWithException() throws ExecuteException {
        ExecutionContext executionContext = ExecutionContextTestUtils.getExecutionContext(10000);
        executorDispatcher.dispatch(executionContext);
    }

    @Test
    public void testDispatch() throws ExecuteException {
        int port = 30000;
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(port);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(org.apache.dolphinscheduler.remote.command.CommandType.TASK_EXECUTE_REQUEST, Mockito.mock(TaskExecuteProcessor.class));
        nettyRemotingServer.start();
        //
        workerConfig.setListenPort(port);
        workerRegistry.registry();

        ExecutionContext executionContext = ExecutionContextTestUtils.getExecutionContext(port);
        executorDispatcher.dispatch(executionContext);
    }
}
