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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.TaskDefinitionVO;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskDefinitionControllerTest {

    @InjectMocks
    private TaskDefinitionController taskDefinitionController;

    @Mock
    private TaskDefinitionService taskDefinitionService;

    private User user;

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test
    public void testQueryTaskDefinitionDetailMasksSensitiveLocalParams() {
        TaskDefinitionVO taskDefinitionVO = new TaskDefinitionVO();
        taskDefinitionVO.setTaskParams(sensitiveTaskParams());
        Mockito.when(taskDefinitionService.queryTaskDefinitionDetail(user, 1L, 2L))
                .thenReturn(taskDefinitionVO);

        Result<TaskDefinitionVO> response = taskDefinitionController.queryTaskDefinitionDetail(user, 1L, 2L);

        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
        Assertions.assertTrue(response.getData().getTaskParams().contains(TaskConstants.SENSITIVE_DATA_MASK));
        Assertions.assertFalse(response.getData().getTaskParams().contains("abc"));
        Assertions.assertTrue(taskDefinitionVO.getTaskParams().contains("abc"));
    }

    @Test
    public void testQueryTaskDefinitionVersionsMasksSensitiveLocalParams() {
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setTaskParams(sensitiveTaskParams());
        PageInfo<TaskDefinitionLog> pageInfo = new PageInfo<>(1, 10);
        pageInfo.setTotalList(Collections.singletonList(taskDefinitionLog));
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.SUCCESS.getMsg());
        result.setData(pageInfo);
        Mockito.when(taskDefinitionService.queryTaskDefinitionVersions(user, 1L, 2L, 1, 10))
                .thenReturn(result);

        Result response = taskDefinitionController.queryTaskDefinitionVersions(user, 1L, 2L, 1, 10);

        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
        @SuppressWarnings("unchecked")
        PageInfo<TaskDefinitionLog> maskedPage = (PageInfo<TaskDefinitionLog>) response.getData();
        Assertions.assertTrue(
                maskedPage.getTotalList().get(0).getTaskParams().contains(TaskConstants.SENSITIVE_DATA_MASK));
        Assertions.assertFalse(maskedPage.getTotalList().get(0).getTaskParams().contains("abc"));
        Assertions.assertTrue(taskDefinitionLog.getTaskParams().contains("abc"));
    }

    private static String sensitiveTaskParams() {
        return "{\"localParams\":[{\"prop\":\"token\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                + "\"value\":\"abc\",\"sensitive\":true}]}";
    }
}
