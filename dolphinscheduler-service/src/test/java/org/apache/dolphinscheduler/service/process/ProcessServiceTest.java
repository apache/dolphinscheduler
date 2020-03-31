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
package org.apache.dolphinscheduler.service.process;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

/**
 * ProcessService Tester
 */
public class ProcessServiceTest {

    private ProcessService processService;

    @Test
    public void testGetSubmitTaskState() {
        processService = PowerMockito.spy(new ProcessService());
        TaskInstance taskInstance = new TaskInstance();
        Mockito.doReturn(false).when(processService).checkTaskExistsInTaskQueue(taskInstance);

        taskInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
        Assert.assertEquals(ExecutionStatus.RUNNING_EXEUTION, processService.getSubmitTaskState(taskInstance, ExecutionStatus.SUCCESS));

        taskInstance.setState(ExecutionStatus.KILL);
        Assert.assertEquals(ExecutionStatus.KILL, processService.getSubmitTaskState(taskInstance, ExecutionStatus.SUCCESS));

        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        Assert.assertEquals(ExecutionStatus.PAUSE, processService.getSubmitTaskState(taskInstance, ExecutionStatus.READY_PAUSE));

        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        Assert.assertEquals(ExecutionStatus.KILL, processService.getSubmitTaskState(taskInstance, ExecutionStatus.READY_STOP));

        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        Assert.assertEquals(ExecutionStatus.SUBMITTED_SUCCESS, processService.getSubmitTaskState(taskInstance, ExecutionStatus.RUNNING_EXEUTION));

        taskInstance.setState(ExecutionStatus.FAILURE);
        Assert.assertEquals(ExecutionStatus.SUBMITTED_SUCCESS, processService.getSubmitTaskState(taskInstance, ExecutionStatus.RUNNING_EXEUTION));

        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        Assert.assertEquals(ExecutionStatus.FAILURE, processService.getSubmitTaskState(taskInstance, ExecutionStatus.FAILURE));
    }
}
