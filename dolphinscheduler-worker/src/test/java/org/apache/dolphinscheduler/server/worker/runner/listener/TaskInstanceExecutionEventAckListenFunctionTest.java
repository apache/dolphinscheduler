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

package org.apache.dolphinscheduler.server.worker.runner.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

import org.apache.dolphinscheduler.extract.master.transportor.ITaskInstanceExecutionEvent;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionFinishEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionInfoEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionRunningEventAck;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskInstanceExecutionEventAckListenFunctionTest {

    private static final Logger log = LoggerFactory.getLogger(TaskInstanceExecutionEventAckListenFunctionTest.class);
    private MessageRetryRunner messageRetryRunner = Mockito.mock(MessageRetryRunner.class);

    @Test
    public void testTaskInstanceExecutionEventAckListenFunctionManager() {
        TaskInstanceExecutionFinishEventAckListenFunction taskInstanceExecutionFinishEventAckListenFunction =
                new TaskInstanceExecutionFinishEventAckListenFunction(messageRetryRunner);
        TaskInstanceExecutionInfoEventAckListenFunction taskInstanceExecutionInfoEventAckListenFunction =
                new TaskInstanceExecutionInfoEventAckListenFunction(messageRetryRunner);
        TaskInstanceExecutionRunningEventAckListenFunction taskInstanceExecutionRunningEventAckListenFunction =
                new TaskInstanceExecutionRunningEventAckListenFunction(messageRetryRunner);
        TaskInstanceExecutionEventAckListenFunctionManager taskInstanceExecutionEventAckListenFunctionManager =
                new TaskInstanceExecutionEventAckListenFunctionManager(
                        taskInstanceExecutionRunningEventAckListenFunction,
                        taskInstanceExecutionFinishEventAckListenFunction,
                        taskInstanceExecutionInfoEventAckListenFunction);
        Assertions.assertEquals(taskInstanceExecutionRunningEventAckListenFunction,
                taskInstanceExecutionEventAckListenFunctionManager
                        .getTaskInstanceExecutionRunningEventAckListenFunction());
        Assertions.assertEquals(taskInstanceExecutionInfoEventAckListenFunction,
                taskInstanceExecutionEventAckListenFunctionManager
                        .getTaskInstanceExecutionInfoEventAckListenFunction());
        Assertions.assertEquals(taskInstanceExecutionFinishEventAckListenFunction,
                taskInstanceExecutionEventAckListenFunctionManager
                        .getTaskInstanceExecutionFinishEventAckListenFunction());
    }

    @Test
    public void testTaskInstanceExecutionEventAckListenFunctionDryRun() {
        int taskInstanceId1 = 111;
        int taskInstanceId2 = 222;
        int taskInstanceId3 = 333;
        TaskInstanceExecutionFinishEventAckListenFunction taskInstanceExecutionFinishEventAckListenFunction =
                new TaskInstanceExecutionFinishEventAckListenFunction(messageRetryRunner);
        taskInstanceExecutionFinishEventAckListenFunction.handleTaskInstanceExecutionEventAck(
                TaskInstanceExecutionFinishEventAck.success(taskInstanceId1));

        ArgumentCaptor acInt = ArgumentCaptor.forClass(int.class);
        ArgumentCaptor acEventType =
                ArgumentCaptor.forClass(ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.class);

        Mockito.verify(messageRetryRunner, times(1)).removeRetryMessage(
                (int) acInt.capture(),
                (ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType) acEventType.capture());

        assertEquals(taskInstanceId1, acInt.getValue());

        TaskInstanceExecutionInfoEventAckListenFunction taskInstanceExecutionInfoEventAckListenFunction =
                new TaskInstanceExecutionInfoEventAckListenFunction(messageRetryRunner);
        taskInstanceExecutionInfoEventAckListenFunction.handleTaskInstanceExecutionEventAck(
                TaskInstanceExecutionInfoEventAck.success(taskInstanceId2));

        Mockito.verify(messageRetryRunner, times(2)).removeRetryMessage(
                (int) acInt.capture(),
                (ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType) acEventType.capture());
        assertEquals(taskInstanceId2, acInt.getValue());

        TaskInstanceExecutionRunningEventAckListenFunction taskInstanceExecutionRunningEventAckListenFunction =
                new TaskInstanceExecutionRunningEventAckListenFunction(messageRetryRunner);
        taskInstanceExecutionRunningEventAckListenFunction.handleTaskInstanceExecutionEventAck(
                TaskInstanceExecutionRunningEventAck.success(taskInstanceId3));
        Mockito.verify(messageRetryRunner, times(3)).removeRetryMessage(
                (int) acInt.capture(),
                (ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType) acEventType.capture());
        assertEquals(taskInstanceId3, acInt.getValue());
    }
}
