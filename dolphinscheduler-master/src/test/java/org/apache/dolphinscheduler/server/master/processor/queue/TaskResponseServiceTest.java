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

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.task.TaskExecuteResultMessage;
import org.apache.dolphinscheduler.remote.command.task.TaskExecuteRunningMessage;
import org.apache.dolphinscheduler.server.master.cache.impl.ProcessInstanceExecCacheManagerImpl;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.utils.DataQualityResultOperator;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.channel.Channel;

@ExtendWith(MockitoExtension.class)
public class TaskResponseServiceTest {

    @Mock(name = "processService")
    private ProcessService processService;

    @InjectMocks
    TaskEventService taskEventService;

    @Mock
    private Channel channel;

    private TaskEvent ackEvent;

    private TaskEvent resultEvent;

    private TaskInstance taskInstance;

    @Mock
    private ProcessInstanceExecCacheManagerImpl processInstanceExecCacheManager;

    @Mock
    private DataQualityResultOperator dataQualityResultOperator;

    @Mock
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Mock
    private TaskExecuteThreadPool taskExecuteThreadPool;

    @BeforeEach
    public void before() {
        taskEventService.start();

        TaskExecuteRunningMessage taskExecuteRunningMessage = new TaskExecuteRunningMessage("127.0.0.1:5678",
                "127.0.0.1:1234",
                System.currentTimeMillis());
        taskExecuteRunningMessage.setProcessId(1);
        taskExecuteRunningMessage.setTaskInstanceId(22);
        taskExecuteRunningMessage.setStatus(TaskExecutionStatus.RUNNING_EXECUTION);
        taskExecuteRunningMessage.setExecutePath("path");
        taskExecuteRunningMessage.setLogPath("logPath");
        taskExecuteRunningMessage.setHost("127.*.*.*");
        taskExecuteRunningMessage.setStartTime(System.currentTimeMillis());

        ackEvent = TaskEvent.newRunningEvent(taskExecuteRunningMessage,
                channel,
                taskExecuteRunningMessage.getMessageSenderAddress());

        TaskExecuteResultMessage taskExecuteResultMessage = new TaskExecuteResultMessage(NetUtils.getAddr(1234),
                NetUtils.getAddr(5678),
                System.currentTimeMillis());
        taskExecuteResultMessage.setProcessInstanceId(1);
        taskExecuteResultMessage.setTaskInstanceId(22);
        taskExecuteResultMessage.setStatus(TaskExecutionStatus.SUCCESS.getCode());
        taskExecuteResultMessage.setEndTime(System.currentTimeMillis());
        taskExecuteResultMessage.setVarPool("varPol");
        taskExecuteResultMessage.setAppIds("ids");
        taskExecuteResultMessage.setProcessId(1);
        resultEvent = TaskEvent.newResultEvent(taskExecuteResultMessage,
                channel,
                taskExecuteResultMessage.getMessageSenderAddress());

        taskInstance = new TaskInstance();
        taskInstance.setId(22);
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
    }

    @Test
    public void testAddResponse() {
        taskEventService.addEvent(ackEvent);
        taskEventService.addEvent(resultEvent);
    }

    @AfterEach
    public void after() {
        if (taskEventService != null) {
            taskEventService.stop();
        }
    }
}
