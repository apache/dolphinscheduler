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

package org.apache.dolphinscheduler.api.controller.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceQueryRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class TaskInstanceV2ControllerTest extends AbstractControllerTest {

    @InjectMocks
    private TaskInstanceV2Controller taskInstanceV2Controller;

    @Mock
    private TaskInstanceService taskInstanceService;

    @Test
    public void testQueryTaskListPaging() {

        TaskInstanceQueryRequest taskInstanceQueryReq = new TaskInstanceQueryRequest();
        taskInstanceQueryReq.setProcessInstanceId(1);
        taskInstanceQueryReq.setProcessInstanceName("");
        taskInstanceQueryReq.setProcessDefinitionName("");
        taskInstanceQueryReq.setTaskName("");
        taskInstanceQueryReq.setExecutorName("");
        taskInstanceQueryReq.setStartTime("2022-06-01 00:00:00");
        taskInstanceQueryReq.setEndTime("2022-09-01 00:00:00");
        taskInstanceQueryReq.setSearchVal("");
        taskInstanceQueryReq.setStateType(TaskExecutionStatus.SUCCESS);
        taskInstanceQueryReq.setHost("127.0.0.1");
        taskInstanceQueryReq.setTaskExecuteType(TaskExecuteType.BATCH);
        taskInstanceQueryReq.setPageNo(1);
        taskInstanceQueryReq.setPageSize(20);

        Result result = new Result();
        PageInfo<TaskInstance> pageInfo =
                new PageInfo<>(taskInstanceQueryReq.getPageNo(), taskInstanceQueryReq.getPageSize());
        pageInfo.setTotalList(Collections.singletonList(new TaskInstance()));
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        when(taskInstanceService.queryTaskListPaging(any(), eq(1L), eq(taskInstanceQueryReq.getProcessInstanceId()),
                eq(taskInstanceQueryReq.getProcessInstanceName()), eq(taskInstanceQueryReq.getProcessInstanceName()),
                eq(taskInstanceQueryReq.getTaskName()), eq(taskInstanceQueryReq.getExecutorName()), any(), any(),
                eq(taskInstanceQueryReq.getSearchVal()), Mockito.any(), eq(taskInstanceQueryReq.getHost()),
                eq(taskInstanceQueryReq.getTaskExecuteType()), any(), any())).thenReturn(result);
        Result taskResult = taskInstanceV2Controller.queryTaskListPaging(null, 1L, taskInstanceQueryReq);
        Assertions.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), taskResult.getCode());
    }

    @Test
    public void testForceTaskSuccess() {

        Result mockResult = new Result();
        putMsg(mockResult, Status.SUCCESS);

        when(taskInstanceService.forceTaskSuccess(any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(mockResult);

        Result taskResult = taskInstanceV2Controller.forceTaskSuccess(null, 1L, 1);
        Assertions.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), taskResult.getCode());

    }

    @Test
    public void testTaskSavePoint() {

        Result mockResult = new Result();
        putMsg(mockResult, Status.SUCCESS);

        when(taskInstanceService.taskSavePoint(any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(mockResult);

        Result taskResult = taskInstanceV2Controller.taskSavePoint(null, 1L, 1);
        Assertions.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), taskResult.getCode());

    }

    @Test
    public void testStopTask() {

        Result mockResult = new Result();
        putMsg(mockResult, Status.SUCCESS);

        when(taskInstanceService.stopTask(any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(mockResult);

        Result taskResult = taskInstanceV2Controller.stopTask(null, 1L, 1);
        Assertions.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), taskResult.getCode());

    }

    @Test
    public void testQueryTaskInstanceById() {
        TaskInstance taskInstance = new TaskInstance();

        when(taskInstanceService.queryTaskInstanceById(any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(taskInstance);

        TaskInstance taskResult = taskInstanceV2Controller.queryTaskInstanceByCode(null, 1L, 1L);
        Assertions.assertNotNull(taskResult);

    }

}
