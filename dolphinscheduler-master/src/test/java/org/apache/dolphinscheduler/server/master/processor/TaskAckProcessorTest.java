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

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.task.TaskExecuteRunningMessage;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.channel.Channel;

/**
 * task ack processor test
 */
@ExtendWith(MockitoExtension.class)
public class TaskAckProcessorTest {

    private TaskExecuteRunningProcessor taskExecuteRunningProcessor;
    private TaskEventService taskEventService;
    private ProcessService processService;
    private TaskExecuteRunningMessage taskExecuteRunningMessage;
    private TaskEvent taskResponseEvent;
    private Channel channel;

    private MockedStatic<SpringApplicationContext> mockedStaticSpringApplicationContext;

    @BeforeEach
    public void before() {
        mockedStaticSpringApplicationContext = Mockito.mockStatic(SpringApplicationContext.class);

        taskEventService = Mockito.mock(TaskEventService.class);
        mockedStaticSpringApplicationContext.when(() -> SpringApplicationContext.getBean(TaskEventService.class))
                .thenReturn(taskEventService);

        processService = Mockito.mock(ProcessService.class);
        mockedStaticSpringApplicationContext.when(() -> SpringApplicationContext.getBean(ProcessService.class))
                .thenReturn(processService);

        taskExecuteRunningProcessor = new TaskExecuteRunningProcessor();

        channel = Mockito.mock(Channel.class);
        taskResponseEvent = Mockito.mock(TaskEvent.class);

        taskExecuteRunningMessage = new TaskExecuteRunningMessage("127.0.0.1:5678",
                " 127.0.0.1:1234",
                System.currentTimeMillis());
        taskExecuteRunningMessage.setStatus(TaskExecutionStatus.RUNNING_EXECUTION);
        taskExecuteRunningMessage.setExecutePath("/dolphinscheduler/worker");
        taskExecuteRunningMessage.setHost("localhost");
        taskExecuteRunningMessage.setLogPath("/temp/worker.log");
        taskExecuteRunningMessage.setStartTime(System.currentTimeMillis());
        taskExecuteRunningMessage.setTaskInstanceId(1);
        taskExecuteRunningMessage.setProcessInstanceId(1);
    }

    @AfterEach
    public void after() {
        mockedStaticSpringApplicationContext.close();
    }

    @Test
    public void testProcess() {
    }
}
