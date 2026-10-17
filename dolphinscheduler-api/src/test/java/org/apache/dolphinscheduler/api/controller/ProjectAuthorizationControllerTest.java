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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.permission.ProjectAuthorizationService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Collections;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ProjectAuthorizationControllerTest {

    @Mock
    private ProjectAuthorizationService authorizationService;

    private MockMvc mockMvc;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProjectAuthorizationController(authorizationService)).build();
    }

    @Test
    void queryReturnsExplicitPermissionNames() throws Exception {
        Mockito.when(authorizationService.queryMemberPermissions(user, 100L, 2))
                .thenReturn(EnumSet.of(ProjectPermission.VIEW));
        mockMvc.perform(get("/projects/100/members/2/permissions")
                .requestAttr(Constants.SESSION_USER, user).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("VIEW"));
    }

    @Test
    void replaceUsesPathProjectAndMemberAndDeduplicatesPermissions() throws Exception {
        mockMvc.perform(put("/projects/100/members/2/permissions")
                .requestAttr(Constants.SESSION_USER, user)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"VIEW\",\"EXECUTE\",\"VIEW\"]"))
                .andExpect(status().isOk());
        Mockito.verify(authorizationService).replaceMemberPermissions(user, 100L, 2,
                EnumSet.of(ProjectPermission.VIEW, ProjectPermission.EXECUTE));
    }

    @Test
    void emptyArrayRevokesAllPermissions() throws Exception {
        mockMvc.perform(put("/projects/100/members/2/permissions")
                .requestAttr(Constants.SESSION_USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isOk());
        Mockito.verify(authorizationService).replaceMemberPermissions(user, 100L, 2, Collections.emptySet());
    }

    @Test
    void unknownPermissionIsRejectedBeforeServiceCall() throws Exception {
        mockMvc.perform(put("/projects/100/members/2/permissions")
                .requestAttr(Constants.SESSION_USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("[\"UNKNOWN\"]"))
                .andExpect(status().isBadRequest());
        Mockito.verifyNoInteractions(authorizationService);
    }
    @Test
    void candidatesAreLimitedToIdsAndNames() throws Exception {
        User candidate = new User();
        candidate.setId(2);
        candidate.setUserName("candidate");
        candidate.setUserPassword("sensitive");
        Mockito.when(authorizationService.queryAssignableMembers(user, 100L)).thenReturn(Collections.singletonList(
                new org.apache.dolphinscheduler.api.vo.UserSimpleInfoVO(candidate)));
        mockMvc.perform(get("/projects/100/members/candidates").requestAttr(Constants.SESSION_USER, user)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(2))
                .andExpect(jsonPath("$.data[0].userName").value("candidate"))
                .andExpect(jsonPath("$.data[0].userPassword").doesNotExist());
    }

}
