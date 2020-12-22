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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskKillRequestCommand;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.log.LogClientService;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.netty.channel.Channel;

/**
 * TaskKillProcessorTest
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class, TaskKillProcessor.class, OSUtils.class, ProcessUtils.class, LoggerUtils.class})
public class TaskKillProcessorTest {

    private TaskKillProcessor taskKillProcessor;

    private TaskExecutionContextCacheManagerImpl taskExecutionContextCacheManager;

    private Channel channel;

    private Command command;

    private TaskExecutionContext taskExecutionContext;

    @Before
    public void before() throws Exception {

        TaskCallbackService taskCallbackService = PowerMockito.mock(TaskCallbackService.class);
        WorkerConfig workerConfig = PowerMockito.mock(WorkerConfig.class);
        taskExecutionContextCacheManager = PowerMockito.mock(TaskExecutionContextCacheManagerImpl.class);

        channel = PowerMockito.mock(Channel.class);
        command = new Command();
        command.setType(CommandType.TASK_KILL_REQUEST);
        TaskKillRequestCommand taskKillRequestCommand = new TaskKillRequestCommand();
        taskKillRequestCommand.setTaskInstanceId(1);
        command.setBody(JSONUtils.toJsonString(taskKillRequestCommand).getBytes());
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(1);
        LogClientService logClient = PowerMockito.mock(LogClientService.class);

        NettyRemoteChannel nettyRemoteChannel = PowerMockito.mock(NettyRemoteChannel.class);
        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.mockStatic(OSUtils.class);
        PowerMockito.mockStatic(ProcessUtils.class);
        PowerMockito.mockStatic(LoggerUtils.class);
        PowerMockito.when(SpringApplicationContext.getBean(TaskCallbackService.class)).thenReturn(taskCallbackService);
        PowerMockito.when(SpringApplicationContext.getBean(WorkerConfig.class)).thenReturn(workerConfig);
        PowerMockito.when(SpringApplicationContext.getBean(TaskExecutionContextCacheManagerImpl.class)).thenReturn(taskExecutionContextCacheManager);
        PowerMockito.doNothing().when(taskCallbackService).addRemoteChannel(anyInt(), any());
        PowerMockito.whenNew(NettyRemoteChannel.class).withAnyArguments().thenReturn(null);
        PowerMockito.when(OSUtils.exeCmd(any())).thenReturn(null);
        PowerMockito.when(ProcessUtils.getPidsStr(102)).thenReturn("123");
        PowerMockito.whenNew(LogClientService.class).withAnyArguments().thenReturn(logClient);
        PowerMockito.when(logClient.viewLog(any(), anyInt(), any())).thenReturn("test");
        PowerMockito.when(LoggerUtils.getAppIds(any(), any())).thenReturn(Collections.singletonList("id"));

        Command viewLogResponseCommand = new Command();
        viewLogResponseCommand.setBody("success".getBytes());

        taskKillProcessor = new TaskKillProcessor();
    }

    @Test
    public void testProcess() {

        PowerMockito.when(taskExecutionContextCacheManager.getByTaskInstanceId(1)).thenReturn(taskExecutionContext);
        taskKillProcessor.process(channel, command);

        taskExecutionContext.setProcessId(101);
        taskExecutionContext.setHost("127.0.0.1:22");
        taskExecutionContext.setLogPath("/log");
        taskExecutionContext.setExecutePath("/path");
        taskExecutionContext.setTenantCode("ten");
        taskKillProcessor.process(channel, command);
    }

}
