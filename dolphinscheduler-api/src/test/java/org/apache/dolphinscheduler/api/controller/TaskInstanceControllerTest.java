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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * task instance controller test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TaskInstanceControllerTest {

    @InjectMocks
    private TaskInstanceController taskInstanceController;

    @Mock
    private TaskInstanceService taskInstanceService;

    @Test
    public void testQueryTaskListPaging() {

        Map<String,Object> result = new HashMap<>();
        Integer pageNo = 1;
        Integer pageSize = 20;
        PageInfo pageInfo = new PageInfo<TaskInstance>(pageNo, pageSize);
        result.put(Constants.DATA_LIST, pageInfo);
        result.put(Constants.STATUS, Status.SUCCESS);

        when(taskInstanceService.queryTaskListPaging(any(), eq(""),  eq(1), eq(""), eq(""), eq(""),any(), any(),
                eq(""), Mockito.any(), eq("192.168.xx.xx"), any(), any())).thenReturn(result);
        Result taskResult = taskInstanceController.queryTaskListPaging(null, "", 1, "", "",
                "", "", ExecutionStatus.SUCCESS,"192.168.xx.xx", "2020-01-01 00:00:00", "2020-01-02 00:00:00",pageNo, pageSize);
        Assert.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), taskResult.getCode());

    }

}
