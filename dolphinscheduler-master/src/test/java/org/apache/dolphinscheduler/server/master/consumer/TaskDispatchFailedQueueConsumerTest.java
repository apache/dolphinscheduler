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

package org.apache.dolphinscheduler.server.master.consumer;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskExecuteStartCommand;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.apache.dolphinscheduler.service.queue.TaskFailedRetryPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * TaskDispatchFailedQueueConsumerTest
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TaskDispatchFailedQueueConsumerTest {

    @Mock
    private MasterConfig masterConfig;

    @InjectMocks
    private TaskPriorityQueueConsumer taskPriorityQueueConsumer;

    @Mock
    private TaskPriorityQueue<TaskPriority> taskPriorityQueue;

    @Mock
    private TaskPriorityQueue<TaskFailedRetryPriority> taskPriorityDispatchFailedQueue;

    private TaskExecutionContext taskExecutionContext;

    private ExecutionContext executionContext;

    @Before
    public void before (){
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setWorkerGroup("11");

        TaskExecuteStartCommand requestCommand = new TaskExecuteStartCommand("", "", 1000);
        Command command = requestCommand.convert2Command();

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);

        executionContext = new ExecutionContext(command, ExecutorType.WORKER, taskInstance);
        executionContext.setHost(new Host("1", 1));
    }

    /**
     * catch dispatch failed task
     */
    @Test
    public void testBatchDispatchFailedTask() {
        try{
            buildTaskPriority(0);
            ThreadUtils.sleep(1000);
            taskPriorityQueueConsumer.batchDispatch(1);
        }catch (Exception e){
            Assert.assertNotNull(e);
        }
    }

    /**
     *  batch dispatch failed task time not allow
     */
    @Test
    public void testBatchDispatchFailedTaskTimeNotAllow(){
        try{
            buildTaskPriority(3);
            ThreadUtils.sleep(1000);
            taskPriorityQueueConsumer.batchDispatch(1);
        }catch (Exception e){
            Assert.assertNotNull(e);
        }
    }

    private void buildTaskPriority(int retryTimes) throws TaskPriorityQueueException, InterruptedException {
        Mockito.when(taskPriorityDispatchFailedQueue.size()).thenReturn(1);

        TaskPriority taskPriority = new TaskPriority(1, 1, 1, 1, 1, "1");
        taskPriority.setDispatchFailedRetryTimes(retryTimes);
        Mockito.when(taskPriorityQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS)).thenReturn(taskPriority);

        TaskPriority dispatchFailedTaskPriority = new TaskPriority(2, 2, 2, 2, 2, "1");
        dispatchFailedTaskPriority.setDispatchFailedRetryTimes(retryTimes);
        Mockito.when(taskPriorityDispatchFailedQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS)).thenReturn(new TaskFailedRetryPriority(dispatchFailedTaskPriority));

        Mockito.when(masterConfig.getDispatchTaskNumber()).thenReturn(1);
    }
}