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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class TaskInstanceControllerTest extends AbstractControllerTest {

    @InjectMocks
    private TaskInstanceController taskInstanceController;

    @Mock
    private TaskInstanceService taskInstanceService;

    @Test
    public void testQueryTaskListPaging() {

        Result result = new Result();
        Integer pageNo = 1;
        Integer pageSize = 20;
        PageInfo pageInfo = new PageInfo<TaskInstance>(pageNo, pageSize);
        result.setData(pageInfo);
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.SUCCESS.getMsg());

        when(taskInstanceService.queryTaskListPaging(any(), eq(1L), eq(1), eq(""), eq(""), eq(""), eq(""), any(), any(),
                eq(""), Mockito.any(), eq("192.168.xx.xx"), eq(TaskExecuteType.BATCH), any(), any()))
                        .thenReturn(result);
        Result taskResult = taskInstanceController.queryTaskListPaging(null, 1L, 1, "", "", "",
                "", "", TaskExecutionStatus.SUCCESS, "192.168.xx.xx", "2020-01-01 00:00:00", "2020-01-02 00:00:00",
                TaskExecuteType.BATCH, pageNo, pageSize);
        Assertions.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), taskResult.getCode());
    }

    @Disabled
    @Test
    public void testForceTaskSuccess() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("taskInstanceId", "104");

        Result mockResult = new Result();
        putMsg(mockResult, Status.SUCCESS);
        when(taskInstanceService.forceTaskSuccess(any(User.class), anyLong(), anyInt())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/task-instance/force-success", "cxc_1113")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

}
