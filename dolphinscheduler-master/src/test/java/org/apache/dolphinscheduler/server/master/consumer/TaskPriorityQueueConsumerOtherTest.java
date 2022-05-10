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
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;


/**
 * TaskPriorityQueueConsumerOtherTest
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TaskPriorityQueueConsumerOtherTest {

    @Mock
    private ProcessService processService;

    @Mock
    private MasterConfig masterConfig;

    @InjectMocks
    private TaskPriorityQueueConsumer taskPriorityQueueConsumer;

    @Mock
    private TaskPriorityQueue<TaskPriority> taskPriorityQueue;

    @Mock
    private TaskPriorityQueue<TaskPriority> taskPriorityDispatchFailedQueue;

    private TaskExecutionContext taskExecutionContext;

    private ExecutionContext executionContext;

    @Before
    public void before (){
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setWorkerGroup("11");

        TaskExecuteRequestCommand requestCommand = new TaskExecuteRequestCommand();
        requestCommand.setTaskExecutionContext(JSONUtils.toJsonString(taskExecutionContext));
        Command command = requestCommand.convert2Command();

        executionContext = new ExecutionContext(command, ExecutorType.WORKER, taskExecutionContext.getWorkerGroup());
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
        TaskPriority taskPriority = new TaskPriority(1, 1, 1, 1, "1");
        taskPriority.setLastDispatchTime(System.currentTimeMillis());
        taskPriority.setDispatchFailedRetryTimes(retryTimes);
        Mockito.when(taskPriorityQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS)).thenReturn(taskPriority);

        TaskPriority dispatchFailedTaskPriority = new TaskPriority(2, 2, 2, 2, "1");
        dispatchFailedTaskPriority.setLastDispatchTime(System.currentTimeMillis());
        dispatchFailedTaskPriority.setDispatchFailedRetryTimes(retryTimes);
        Mockito.when(taskPriorityDispatchFailedQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS)).thenReturn(dispatchFailedTaskPriority);

        Mockito.when(masterConfig.getDispatchTaskNumber()).thenReturn(1);
    }

    /**
     * change failed task instance status to pending
     */
    @Test
    public void testDispatchFailedTaskInstanceState2Pending() {
        buildTaskInstance(ExecutionStatus.SUBMITTED_SUCCESS);
        taskPriorityQueueConsumer.dispatchFailedTaskInstanceState2Pending(taskExecutionContext, executionContext);
        Assert.assertNotNull(executionContext);
    }

    /**
     * task instance null
     */
    @Test
    public void testTaskInstanceNull(){
        taskPriorityQueueConsumer.dispatchFailedTaskInstanceState2Pending(taskExecutionContext, executionContext);
        Assert.assertNotNull(executionContext);
    }

    /**
     * task instance state illegal
     */
    @Test
    public void testTaskInstanceStateIllegal(){
        buildTaskInstance(ExecutionStatus.DISPATCH);
        taskPriorityQueueConsumer.dispatchFailedTaskInstanceState2Pending(taskExecutionContext, executionContext);
        Assert.assertNotNull(executionContext);
    }

    private void buildTaskInstance(ExecutionStatus executionStatus){
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setProcessInstanceId(1);
        taskInstance.setState(executionStatus);
        Mockito.when(processService.findTaskInstanceById(Mockito.anyInt())).thenReturn(taskInstance);
    }
}
