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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;

import org.apache.http.HttpStatus;

import java.util.Map;

import javax.servlet.http.Cookie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * login controller test
 */
public class LoginControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(LoginControllerTest.class);

    @Test
    public void testLogin() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userName", "admin");
        paramsMap.add("userPassword", "dolphinscheduler123");

        MvcResult mvcResult = mockMvc.perform(post("/login")
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
        Map<String, String> data = (Map<String, String>) result.getData();
        Assertions.assertEquals(Constants.SECURITY_CONFIG_TYPE_PASSWORD, data.get(Constants.SECURITY_CONFIG_TYPE));
        Assertions.assertNotEquals(Constants.SECURITY_CONFIG_TYPE_LDAP, data.get(Constants.SECURITY_CONFIG_TYPE));
    }

    @Test
    public void testSignOut() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();

        MvcResult mvcResult = mockMvc.perform(post("/signOut")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testClearCookie() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/cookies")
                .header("sessionId", sessionId)
                .cookie(new Cookie("sessionId", sessionId)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Cookie[] cookies = response.getCookies();
        for (Cookie cookie : cookies) {
            Assertions.assertEquals(0, cookie.getMaxAge());
            Assertions.assertNull(cookie.getValue());
        }
    }

    @Test
    void testGetOauth2Provider() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/oauth2-provider"))
                .andExpect(status().isOk())
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    void testOauth2Redirect() throws Exception {
        String tokenResult = "{\"access_token\":\"test-token\"}";
        String userInfoResult = "{\"login\":\"username\"}";
        MockedStatic<OkHttpUtils> okHttpUtilsMockedStatic = Mockito.mockStatic(OkHttpUtils.class);
        okHttpUtilsMockedStatic
                .when(() -> OkHttpUtils.post(Mockito.notNull(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(tokenResult);
        okHttpUtilsMockedStatic.when(() -> OkHttpUtils.get(Mockito.notNull(), Mockito.any(), Mockito.any()))
                .thenReturn(userInfoResult);
        MvcResult mvcResult = mockMvc.perform(get("/redirect/login/oauth2?code=test&provider=github"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assertions.assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        String redirectedUrl = response.getRedirectedUrl();
        Assertions.assertTrue(redirectedUrl != null && redirectedUrl.contains("sessionId"));
        okHttpUtilsMockedStatic.close();
    }

    @Test
    void testOauth2RedirectError() throws Exception {
        MockedStatic<OkHttpUtils> okHttpUtilsMockedStatic = Mockito.mockStatic(OkHttpUtils.class);
        okHttpUtilsMockedStatic.when(() -> OkHttpUtils.post(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenThrow(new RuntimeException("oauth error"));
        MvcResult mvcResult = mockMvc.perform(get("/redirect/login/oauth2?code=test&provider=github"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assertions.assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        String redirectedUrl = response.getRedirectedUrl();
        Assertions.assertTrue(redirectedUrl != null && redirectedUrl.contains("error"));
        okHttpUtilsMockedStatic.close();
    }
}
