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
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskDispatchProcessor;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * executor dispatch test
 */
@ExtendWith(SpringExtension.class)
@Disabled
public class ExecutorDispatcherTest {

    @Autowired
    private ExecutorDispatcher executorDispatcher;

    @Autowired
    private WorkerRegistryClient workerRegistryClient;

    @Autowired
    private WorkerConfig workerConfig;

    @Test
    public void testDispatchWithException() throws ExecuteException {
        ExecutionContext executionContext = ExecutionContextTestUtils.getExecutionContext(10000);
        Assertions.assertThrows(ExecuteException.class, () -> {
                executorDispatcher.dispatch(executionContext);
        });
    }

    @Test
    public void testDispatch() throws Exception {
        int port = 30000;
        final NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(port);
        NettyRemotingServer nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.TASK_DISPATCH_REQUEST, Mockito.mock(
                TaskDispatchProcessor.class));
        nettyRemotingServer.start();
        //
        workerConfig.setListenPort(port);
        workerRegistryClient.start();

        ExecutionContext executionContext = ExecutionContextTestUtils.getExecutionContext(port);
        executorDispatcher.dispatch(executionContext);

        workerRegistryClient.close();
    }
}
