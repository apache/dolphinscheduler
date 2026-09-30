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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@TestPropertySource(properties = {
        "security.authentication.type=PASSWORD",
        "security.authentication.oauth2.enable=false"
})
public class UsersControllerPasswordModeTest extends AbstractControllerTest {

    @Autowired
    private UsersService usersService;

    @Test
    public void testCreateUserSuccess() throws Exception {
        String userName = uniqueUserName("create_success");

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userName", userName);
        paramsMap.add("userPassword", "123456qwe?");
        paramsMap.add("tenantId", "-1");
        paramsMap.add("queue", "");
        paramsMap.add("email", userName + "@example.com");
        paramsMap.add("phone", "15800000000");
        paramsMap.add("state", "1");

        MvcResult mvcResult = mockMvc.perform(post("/users/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testUpdateUserSuccess() throws Exception {
        String userName = uniqueUserName("update_success");
        createUser(userName);
        User user = usersService.getUserByUserName(userName);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", String.valueOf(user.getId()));
        paramsMap.add("userName", userName + "_new");
        paramsMap.add("userPassword", "");
        paramsMap.add("tenantId", "-1");
        paramsMap.add("queue", "");
        paramsMap.add("email", "updated-" + userName + "@example.com");
        paramsMap.add("phone", "15800000001");
        paramsMap.add("state", "1");
        paramsMap.add("timeZone", "Asia/Shanghai");

        MvcResult mvcResult = mockMvc.perform(post("/users/update")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testUpdateUserPasswordSuccess() throws Exception {
        String userName = uniqueUserName("password_success");
        createUser(userName);
        User user = usersService.getUserByUserName(userName);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", String.valueOf(user.getId()));
        paramsMap.add("userName", userName);
        paramsMap.add("userPassword", "updated123?");
        paramsMap.add("tenantId", "-1");
        paramsMap.add("queue", "");
        paramsMap.add("email", userName + "@example.com");
        paramsMap.add("phone", "15800000002");
        paramsMap.add("state", "1");
        paramsMap.add("timeZone", "Asia/Shanghai");

        MvcResult mvcResult = mockMvc.perform(post("/users/update")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testRegisterUserSuccess() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        String userName = uniqueUserName("register_success");
        paramsMap.add("userName", userName);
        paramsMap.add("userPassword", "123456qwe?");
        paramsMap.add("repeatPassword", "123456qwe?");
        paramsMap.add("email", userName + "@example.com");

        MvcResult mvcResult = mockMvc.perform(post("/users/register")
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    private User createUser(String userName) {
        return usersService.createUser(userName, "123456qwe?", userName + "@example.com", -1, "15800000000", "", 1);
    }

    private String uniqueUserName(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}
