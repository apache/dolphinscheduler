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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.task.TaskDispatchRequest;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.channel.Channel;

/**
 * test task execute processor
 */
@ExtendWith(MockitoExtension.class)
public class WorkerTaskDispatchProcessorTest {

    @InjectMocks
    private WorkerTaskDispatchProcessor workerTaskDispatchProcessor;

    @Mock
    private WorkerConfig workerConfig;

    @Mock
    private WorkerMessageSender workerMessageSender;

    @Mock
    private WorkerRpcClient workerRpcClient;

    @Mock
    private TaskPluginManager taskPluginManager;

    @Mock
    private WorkerManagerThread workerManagerThread;

    @Mock
    private StorageOperate storageOperate;

    @Mock
    private WorkerRegistryClient workerRegistryClient;

    @Test
    public void process() {
        Channel channel = Mockito.mock(Channel.class);
        TaskExecutionContext taskExecutionContext = getTaskExecutionContext();
        Message dispatchMessage = createDispatchCommand(taskExecutionContext);
        workerTaskDispatchProcessor.process(channel, dispatchMessage);

        Mockito.verify(workerManagerThread, Mockito.atMostOnce()).offer(Mockito.any());
    }

    public Message createDispatchCommand(TaskExecutionContext taskExecutionContext) {
        return new TaskDispatchRequest(taskExecutionContext).convert2Command();
    }

    public TaskExecutionContext getTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setProcessId(12345);
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setProcessDefineVersion(1);
        taskExecutionContext.setTaskType("SQL");
        taskExecutionContext.setFirstSubmitTime(System.currentTimeMillis());
        taskExecutionContext.setDelayTime(0);
        taskExecutionContext.setLogPath("/tmp/test.log");
        taskExecutionContext.setHost("localhost");
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/exec/process/1/2/3/4");
        return taskExecutionContext;
    }
}
