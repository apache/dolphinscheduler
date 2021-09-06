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

package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.server.master.cache.impl.TaskInstanceCacheManagerImpl;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.net.InetSocketAddress;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.netty.channel.Channel;

/**
 *  task ack processor test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class, TaskResponseEvent.class})
public class TaskAckProcessorTest {

    private TaskAckProcessor taskAckProcessor;
    private TaskResponseService taskResponseService;
    private TaskInstanceCacheManagerImpl taskInstanceCacheManager;
    private ProcessService processService;
    private TaskExecuteAckCommand taskExecuteAckCommand;
    private TaskResponseEvent taskResponseEvent;
    private Channel channel;

    @Before
    public void before() {
        PowerMockito.mockStatic(SpringApplicationContext.class);

        taskResponseService = PowerMockito.mock(TaskResponseService.class);
        PowerMockito.when(SpringApplicationContext.getBean(TaskResponseService.class)).thenReturn(taskResponseService);

        taskInstanceCacheManager = PowerMockito.mock(TaskInstanceCacheManagerImpl.class);
        PowerMockito.when(SpringApplicationContext.getBean(TaskInstanceCacheManagerImpl.class)).thenReturn(taskInstanceCacheManager);

        processService = PowerMockito.mock(ProcessService.class);
        PowerMockito.when(SpringApplicationContext.getBean(ProcessService.class)).thenReturn(processService);

        taskAckProcessor = new TaskAckProcessor();

        channel = PowerMockito.mock(Channel.class);
        taskResponseEvent = PowerMockito.mock(TaskResponseEvent.class);

        taskExecuteAckCommand = new TaskExecuteAckCommand();
        taskExecuteAckCommand.setStatus(1);
        taskExecuteAckCommand.setExecutePath("/dolphinscheduler/worker");
        taskExecuteAckCommand.setHost("localhost");
        taskExecuteAckCommand.setLogPath("/temp/worker.log");
        taskExecuteAckCommand.setStartTime(new Date());
        taskExecuteAckCommand.setTaskInstanceId(1);
        taskExecuteAckCommand.setProcessInstanceId(1);
    }

    @Test
    public void testProcess() {
//        Command command = taskExecuteAckCommand.convert2Command();
//        Assert.assertEquals(CommandType.TASK_EXECUTE_ACK,command.getType());
//        InetSocketAddress socketAddress = new InetSocketAddress("localhost",12345);
//        PowerMockito.when(channel.remoteAddress()).thenReturn(socketAddress);
//        PowerMockito.mockStatic(TaskResponseEvent.class);
//
//        PowerMockito.when(TaskResponseEvent.newAck(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), channel))
//                .thenReturn(taskResponseEvent);
//        TaskInstance taskInstance = PowerMockito.mock(TaskInstance.class);
//        PowerMockito.when(processService.findTaskInstanceById(Mockito.any())).thenReturn(taskInstance);
//
//        taskAckProcessor.process(channel,command);
    }
}
