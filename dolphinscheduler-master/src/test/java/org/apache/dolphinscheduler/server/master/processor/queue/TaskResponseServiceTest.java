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

package org.apache.dolphinscheduler.server.master.processor.queue;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.cache.impl.ProcessInstanceExecCacheManagerImpl;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.netty.channel.Channel;

@RunWith(MockitoJUnitRunner.class)
public class TaskResponseServiceTest {

    @Mock(name = "processService")
    private ProcessService processService;

    @InjectMocks
    TaskResponseService taskRspService;

    @Mock
    private Channel channel;

    private TaskResponseEvent ackEvent;

    private TaskResponseEvent resultEvent;

    private TaskInstance taskInstance;

    @Mock
    private ProcessInstanceExecCacheManagerImpl processInstanceExecCacheManager;

    @Before
    public void before() {
        taskRspService.start();

        ackEvent = TaskResponseEvent.newAck(ExecutionStatus.RUNNING_EXECUTION,
                new Date(),
                "127.*.*.*",
                "path",
                "logPath",
                22,
                channel,
                1);

        resultEvent = TaskResponseEvent.newResult(ExecutionStatus.SUCCESS,
                new Date(),
                1,
                "ids",
                22,
                "varPol",
                channel,
                1);

        taskInstance = new TaskInstance();
        taskInstance.setId(22);
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
    }

    @Test
    public void testAddResponse() {
        Mockito.when(processService.findTaskInstanceById(Mockito.any())).thenReturn(taskInstance);
        Mockito.when(channel.writeAndFlush(Mockito.any())).thenReturn(null);
        taskRspService.addResponse(ackEvent);
        taskRspService.addResponse(resultEvent);
    }

    @After
    public void after() {
        if (taskRspService != null) {
            taskRspService.stop();
        }
    }
}
