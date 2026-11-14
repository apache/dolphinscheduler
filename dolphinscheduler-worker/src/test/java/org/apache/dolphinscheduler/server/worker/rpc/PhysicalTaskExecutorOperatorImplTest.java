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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.server.worker.executor.PhysicalTaskEngineDelegator;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PhysicalTaskExecutorOperatorImplTest {

    private static final int TASK_INSTANCE_ID = 16789;

    @InjectMocks
    private PhysicalTaskExecutorOperatorImpl physicalTaskExecutorOperator;

    @Mock
    private PhysicalTaskEngineDelegator physicalTaskEngineDelegator;

    @Test
    public void testKillTaskReportsTheTaskWhichCouldNotBeStopped() {
        // the kill waits for the cancellation of the task, so the failure of a task which could not be
        // cancelled reaches the caller instead of a success which hides it
        Mockito.doThrow(new TaskException("Failed to cancel the flink jobs"))
                .when(physicalTaskEngineDelegator).killLogicTask(TASK_INSTANCE_ID);

        TaskExecutorKillResponse killResponse =
                physicalTaskExecutorOperator.killTask(TaskExecutorKillRequest.of(TASK_INSTANCE_ID));

        Assertions.assertFalse(killResponse.isSuccess());
        Assertions.assertTrue(killResponse.getMessage().contains("Failed to cancel the flink jobs"),
                "the reason of the failure must be reported, message: " + killResponse.getMessage());
    }

    @Test
    public void testKillTaskReportsTheStoppedTask() {
        TaskExecutorKillResponse killResponse =
                physicalTaskExecutorOperator.killTask(TaskExecutorKillRequest.of(TASK_INSTANCE_ID));

        Assertions.assertTrue(killResponse.isSuccess());
        Mockito.verify(physicalTaskEngineDelegator).killLogicTask(TASK_INSTANCE_ID);
    }
}
