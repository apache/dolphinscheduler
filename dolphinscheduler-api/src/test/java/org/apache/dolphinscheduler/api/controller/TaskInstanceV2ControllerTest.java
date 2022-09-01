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

package org.apache.dolphinscheduler.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceQueryRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
        Assert.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), taskResult.getCode());
    }

    @Ignore
    @Test
    public void testForceTaskSuccess() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("taskInstanceId", "104");

        Result mockResult = new Result();
        putMsg(mockResult, Status.SUCCESS);
        when(taskInstanceService.forceTaskSuccess(any(User.class), anyLong(), anyInt())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/v2/task-instance/force-success", "cxc_1113")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

}
