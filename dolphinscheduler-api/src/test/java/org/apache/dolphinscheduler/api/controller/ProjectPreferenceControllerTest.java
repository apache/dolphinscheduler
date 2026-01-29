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
import org.apache.dolphinscheduler.api.service.impl.ProjectPreferenceServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectPreferenceControllerTest {

    @InjectMocks
    private ProjectPreferenceController projectPreferenceController;

    @Mock
    private ProjectPreferenceServiceImpl projectPreferenceService;

    @Test
    public void testUpdateProjectPreference() {
        User loginUser = getGeneralUser();

        Mockito.when(projectPreferenceService.updateProjectPreference(Mockito.any(), Mockito.anyLong(),
                Mockito.anyString())).thenReturn(getSuccessResult());

        Result result = projectPreferenceController.updateProjectPreference(loginUser, 1, "value");
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testQueryProjectPreferenceByProjectCode() {
        User loginUser = getGeneralUser();

        Mockito.when(projectPreferenceService.queryProjectPreferenceByProjectCode(Mockito.any(), Mockito.anyLong()))
                .thenReturn(getSuccessResult());
        Result result = projectPreferenceController.queryProjectPreferenceByProjectCode(loginUser, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testEnableProjectPreference() {
        User loginUser = getGeneralUser();

        Mockito.when(
                projectPreferenceService.enableProjectPreference(Mockito.any(), Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(getSuccessResult());
        Result result = projectPreferenceController.enableProjectPreference(loginUser, 1, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    private User getGeneralUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("userName");
        loginUser.setId(1);
        return loginUser;
    }

    private Result getSuccessResult() {
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.SUCCESS.getMsg());
        return result;
    }

}
