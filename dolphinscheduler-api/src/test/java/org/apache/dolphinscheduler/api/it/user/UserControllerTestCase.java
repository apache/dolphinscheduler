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

package org.apache.dolphinscheduler.api.it.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.it.core.ApiIntegrationTestBase;
import org.apache.dolphinscheduler.api.it.core.LoadYaml;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class UserControllerTestCase extends ApiIntegrationTestBase {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldCreateUser_whenAdminCalls() throws Exception {
        String sessionId = sessionHelper.loginAs("it_admin");

        mockMvc.perform(post("/users/create")
                .header(Constants.SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("userName", "new_it_user")
                .param("userPassword", "Password!1")
                .param("tenantId", "100")
                .param("email", "new@example.com")
                .param("state", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0));

        User created = userMapper.queryByUserNameAccurately("new_it_user");
        assertThat(created).isNotNull();
        assertThat(created.getTenantId()).isEqualTo(100);
        assertThat(created.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldRejectCreateUser_whenGeneralUserCalls() throws Exception {
        String sessionId = sessionHelper.loginAs("it_general_user");

        mockMvc.perform(post("/users/create")
                .header(Constants.SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("userName", "should_not_be_created")
                .param("userPassword", "Password!1")
                .param("tenantId", "100")
                .param("email", "x@example.com")
                .param("state", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.code").value(
                        org.apache.dolphinscheduler.api.enums.Status.USER_NO_OPERATION_PERM.getCode()));

        org.assertj.core.api.Assertions.assertThat(
                userMapper.queryByUserNameAccurately("should_not_be_created")).isNull();
    }

    @Test
    @LoadYaml("classpath:it/user/UserControllerTestCase/createUserDuplicated.yaml")
    void shouldRejectCreateUser_whenUserNameDuplicated() throws Exception {
        String sessionId = sessionHelper.loginAs("it_admin");

        mockMvc.perform(post("/users/create")
                .header(Constants.SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("userName", "existing_user")
                .param("userPassword", "Password!1")
                .param("tenantId", "100")
                .param("email", "dup@example.com")
                .param("state", "1"))
                .andExpect(jsonPath("$.code").value(
                        org.apache.dolphinscheduler.api.enums.Status.USER_NAME_EXIST.getCode()));
    }

    @Test
    void shouldRejectCreateUser_whenTenantNotExist() throws Exception {
        String sessionId = sessionHelper.loginAs("it_admin");

        mockMvc.perform(post("/users/create")
                .header(Constants.SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("userName", "user_with_bad_tenant")
                .param("userPassword", "Password!1")
                .param("tenantId", "999999")
                .param("email", "x@example.com")
                .param("state", "1"))
                .andExpect(jsonPath("$.code").value(
                        org.apache.dolphinscheduler.api.enums.Status.TENANT_NOT_EXIST.getCode()));

        org.assertj.core.api.Assertions.assertThat(
                userMapper.queryByUserNameAccurately("user_with_bad_tenant")).isNull();
    }

    @Test
    @LoadYaml("classpath:it/user/UserControllerTestCase/updateUser.yaml")
    void shouldUpdateUser_whenAdminCalls() throws Exception {
        String sessionId = sessionHelper.loginAs("it_admin");

        mockMvc.perform(post("/users/update")
                .header(Constants.SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1200")
                .param("userName", "target_for_update")
                .param("userPassword", "")
                .param("queue", "default")
                .param("email", "updated@example.com")
                .param("tenantId", "100")
                .param("state", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        User updated = userMapper.selectById(1200);
        org.assertj.core.api.Assertions.assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @LoadYaml("classpath:it/user/UserControllerTestCase/deleteUser.yaml")
    void shouldDeleteUser_whenAdminCalls() throws Exception {
        String sessionId = sessionHelper.loginAs("it_admin");

        mockMvc.perform(post("/users/delete")
                .header(Constants.SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        org.assertj.core.api.Assertions.assertThat(userMapper.selectById(1300)).isNull();
    }

    @Test
    @LoadYaml("classpath:it/user/UserControllerTestCase/deleteUserWithRelatedProject.yaml")
    void shouldRejectDeleteUser_whenUserHasRelatedProject() throws Exception {
        String sessionId = sessionHelper.loginAs("it_admin");

        mockMvc.perform(post("/users/delete")
                .header(Constants.SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(
                        org.apache.dolphinscheduler.api.enums.Status.TRANSFORM_PROJECT_OWNERSHIP.getCode()));

        org.assertj.core.api.Assertions.assertThat(userMapper.selectById(1400)).isNotNull();
    }

    @Test
    @LoadYaml("classpath:it/user/UserControllerTestCase/queryUserList.yaml")
    void shouldQueryUserList_whenAdminCalls() throws Exception {
        String sessionId = sessionHelper.loginAs("it_admin");

        mockMvc.perform(get("/users/list-paging")
                .header(Constants.SESSION_ID, sessionId)
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("searchVal", "list_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalList.length()").value(3));
    }

    @Test
    void shouldGetCurrentUser_byAuthorizedSession() throws Exception {
        String sessionId = sessionHelper.loginAs("it_admin");

        mockMvc.perform(get("/users/get-user-info")
                .header(Constants.SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userName").value("it_admin"));
    }
}
