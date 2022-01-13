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

import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskKillResponseCommand;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.netty.channel.Channel;

/**
 *  task response processor test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class})
public class TaskKillResponseProcessorTest {

    private TaskKillResponseProcessor taskKillResponseProcessor;

    private TaskKillResponseCommand taskKillResponseCommand;

    private Channel channel;

    private TaskResponseService taskResponseService;

    @Before
    public void before() {
        PowerMockito.mockStatic(SpringApplicationContext.class);

        taskResponseService = PowerMockito.mock(TaskResponseService.class);
        PowerMockito.when(SpringApplicationContext.getBean(TaskResponseService.class)).thenReturn(taskResponseService);
        taskKillResponseProcessor = new TaskKillResponseProcessor();
        channel = PowerMockito.mock(Channel.class);
        taskKillResponseCommand = new TaskKillResponseCommand();
        taskKillResponseCommand.setAppIds(
                new ArrayList<String>() {{ add("task_1"); }});
        taskKillResponseCommand.setHost("localhost");
        taskKillResponseCommand.setProcessId(1);
        taskKillResponseCommand.setStatus(1);
        taskKillResponseCommand.setTaskInstanceId(1);

    }

    @Test
    public void testProcess() {
        Command command = taskKillResponseCommand.convert2Command();
        Assert.assertEquals(CommandType.TASK_KILL_RESPONSE,command.getType());
        taskKillResponseProcessor.process(channel,command);
    }
}
