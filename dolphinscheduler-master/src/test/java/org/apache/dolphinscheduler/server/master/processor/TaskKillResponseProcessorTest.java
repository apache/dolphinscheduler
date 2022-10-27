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
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskKillResponseCommand;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.netty.channel.Channel;

/**
 *  task response processor test
 */
public class TaskKillResponseProcessorTest {

    private TaskKillResponseProcessor taskKillResponseProcessor;

    private TaskKillResponseCommand taskKillResponseCommand;

    private Channel channel;

    @BeforeEach
    public void before() {
        taskKillResponseProcessor = new TaskKillResponseProcessor();
        channel = Mockito.mock(Channel.class);
        taskKillResponseCommand = new TaskKillResponseCommand();
        taskKillResponseCommand.setAppIds(
                new ArrayList<String>() {

                    {
                        add("task_1");
                    }
                });
        taskKillResponseCommand.setHost("localhost");
        taskKillResponseCommand.setProcessId(1);
        taskKillResponseCommand.setStatus(TaskExecutionStatus.RUNNING_EXECUTION);
        taskKillResponseCommand.setTaskInstanceId(1);

    }

    @Test
    public void testProcess() {
        Command command = taskKillResponseCommand.convert2Command();
        Assertions.assertEquals(CommandType.TASK_KILL_RESPONSE, command.getType());
        taskKillResponseProcessor.process(channel, command);
    }
}
