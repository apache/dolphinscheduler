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
import org.apache.dolphinscheduler.api.service.impl.WorkFlowLineageServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * work flow lineage controller test
 */
@ExtendWith(MockitoExtension.class)
public class WorkFlowLineageControllerTest {

    @InjectMocks
    private WorkFlowLineageController workFlowLineageController;

    @Mock
    private WorkFlowLineageServiceImpl workFlowLineageService;

    protected User user;

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    @Test
    public void testQueryWorkFlowLineageByName() {
        long projectCode = 1L;
        String searchVal = "test";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);
        Mockito.when(workFlowLineageService.queryWorkFlowLineageByName(projectCode, searchVal)).thenReturn(result);
        Result response = workFlowLineageController.queryWorkFlowLineageByName(user, projectCode, searchVal);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryWorkFlowLineageByCode() {
        long projectCode = 1L;
        long code = 1L;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);
        Mockito.when(workFlowLineageService.queryWorkFlowLineageByCode(projectCode, code)).thenReturn(result);
        Result response = workFlowLineageController.queryWorkFlowLineageByCode(user, projectCode, code);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }
}
