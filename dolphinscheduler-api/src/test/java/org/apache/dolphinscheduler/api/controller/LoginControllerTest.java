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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.api.configuration.OAuth2Configuration;
import org.apache.dolphinscheduler.api.configuration.OAuth2Configuration.OAuth2ClientProperties;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.security.impl.AbstractSsoAuthenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.SessionDao;

import org.apache.http.HttpStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * login controller test
 */
public class LoginControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(LoginControllerTest.class);

    @Autowired
    private SessionDao sessionDao;

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
    public void testLogin_withNullUserName() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userName", "");
        paramsMap.add("userPassword", "dolphinscheduler123");

        mockMvc.perform(post("/login").params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Status.USER_NAME_NULL.getCode()));
    }

    @Test
    public void testLogin_withInvalidCredentials() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userName", "admin");
        paramsMap.add("userPassword", "invalid_password");

        mockMvc.perform(post("/login").params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Status.USER_NAME_PASSWD_ERROR.getCode()));
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
    void testSignOutWithExpireSession() throws Exception {
        final Session session = sessionDao.queryById(sessionId);
        session.setLastLoginTime(new Date(System.currentTimeMillis() - Constants.SESSION_TIME_OUT * 1000 - 1));
        sessionDao.updateById(session);

        mockMvc.perform(post("/signOut")
                .header("sessionId", sessionId))
                .andExpect(status().is(HttpStatus.SC_UNAUTHORIZED))
                .andReturn();
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
    public void testSsoLogin_WithPasswordAuthenticator_ReturnsSuccess() throws Exception {
        mockMvc.perform(get("/login/sso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Status.SUCCESS.getCode()));
    }

    @Test
    public void testLogin_withEmptyIp() {
        Authenticator authenticator = new Authenticator() {

            @Override
            public Result<Map<String, String>> authenticate(@org.springframework.lang.NonNull String username,
                                                            String password,
                                                            @org.springframework.lang.NonNull String ip) {
                Result<Map<String, String>> res = new Result<>();
                res.setCode(Status.SUCCESS.getCode());
                res.setMsg(Status.SUCCESS.getMsg());
                res.setData(new HashMap<>());
                return res;
            }

            @Override
            public User getAuthUser(HttpServletRequest request) {
                return null;
            }
        };
        SessionService sessionService = mock(SessionService.class);
        UsersService usersService = mock(UsersService.class);
        ApiConfig apiConfig = mock(ApiConfig.class);
        LoginController controller =
                new LoginController(sessionService, authenticator, usersService, Optional.empty(), Optional.empty(),
                        apiConfig);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader(Constants.HTTP_X_FORWARDED_FOR)).thenReturn(null);
        when(request.getHeader(Constants.HTTP_X_REAL_IP)).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null); // force null

        Result result = controller.login("admin", "pwd", request, response);
        Assertions.assertEquals(Status.IP_IS_EMPTY.getCode(), result.getCode());
    }

    // OAuth2 tests
    @Test
    public void testLoginByAuth2_Success() throws Exception {
        OAuth2Configuration oAuth2Configuration = new OAuth2Configuration();
        OAuth2ClientProperties clientProps = new OAuth2ClientProperties();
        clientProps.setClientId("cid");
        clientProps.setClientSecret("csecret");
        clientProps.setRedirectUri("http://localhost/redirect");
        clientProps.setTokenUri("http://oauth/token");
        clientProps.setUserInfoUri("http://oauth/userinfo");
        clientProps.setCallbackUrl("http://ui/callback");
        oAuth2Configuration.getProvider().put("github", clientProps);

        Authenticator auth = new Authenticator() {

            @Override
            public Result<Map<String, String>> authenticate(@org.springframework.lang.NonNull String username,
                                                            String password,
                                                            @org.springframework.lang.NonNull String ip) {
                return null;
            }

            @Override
            public User getAuthUser(HttpServletRequest request) {
                return null;
            }
        };
        SessionService sessionService = mock(SessionService.class);
        UsersService usersService = mock(UsersService.class);
        ApiConfig apiConfig = mock(ApiConfig.class);
        LoginController controller =
                new LoginController(sessionService, auth, usersService, Optional.of(oAuth2Configuration),
                        Optional.empty(), apiConfig);

        try (MockedStatic<OkHttpUtils> okHttpMock = Mockito.mockStatic(OkHttpUtils.class)) {
            OkHttpResponse tokenResp = new OkHttpResponse(200, "{\"access_token\":\"tok123\"}");
            okHttpMock.when(() -> OkHttpUtils.post(
                    eq("http://oauth/token"),
                    any(), any(), any(), anyInt(), anyInt(), anyInt()))
                    .thenReturn(tokenResp);
            OkHttpResponse userInfoResp = new OkHttpResponse(200, "{\"login\":\"oauth_user\"}");
            okHttpMock.when(() -> OkHttpUtils.get(
                    eq("http://oauth/userinfo"),
                    any(), any(), anyInt(), anyInt(), anyInt()))
                    .thenReturn(userInfoResp);

            User user = new User();
            user.setUserName("oauth_user");
            when(usersService.getUserByUserName("oauth_user")).thenReturn(null);
            when(usersService.createUser(UserType.GENERAL_USER, "oauth_user", null)).thenReturn(user);
            Session session = new Session();
            session.setId("sid-1");
            when(sessionService.createSessionIfAbsent(user)).thenReturn(session);

            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = new MockHttpServletResponse();
            controller.loginByAuth2("code123", "github", request, response);
            Assertions.assertEquals(302, ((MockHttpServletResponse) response).getStatus());
            String redirect = ((MockHttpServletResponse) response).getRedirectedUrl();
            Assertions.assertTrue(redirect.startsWith("http://ui/callback?sessionId=sid-1"));
        }
    }

    @Test
    public void testLoginByAuth2_Failure() throws Exception {
        OAuth2Configuration oAuth2Configuration = new OAuth2Configuration();
        OAuth2ClientProperties clientProps = new OAuth2ClientProperties();
        clientProps.setClientId("cid");
        clientProps.setClientSecret("csecret");
        clientProps.setRedirectUri("http://localhost/redirect");
        clientProps.setTokenUri("http://oauth/token");
        clientProps.setUserInfoUri("http://oauth/userinfo");
        clientProps.setCallbackUrl("http://ui/callback");
        oAuth2Configuration.getProvider().put("github", clientProps);

        Authenticator auth = new Authenticator() {

            @Override
            public Result<Map<String, String>> authenticate(@NonNull String username,
                                                            String password,
                                                            @NonNull String ip) {
                return null;
            }

            @Override
            public User getAuthUser(HttpServletRequest request) {
                return null;
            }
        };
        SessionService sessionService = mock(SessionService.class);
        UsersService usersService = mock(UsersService.class);
        ApiConfig apiConfig = mock(ApiConfig.class);
        LoginController controller =
                new LoginController(sessionService, auth, usersService, Optional.of(oAuth2Configuration),
                        Optional.empty(), apiConfig);

        try (MockedStatic<OkHttpUtils> okHttpMock = Mockito.mockStatic(OkHttpUtils.class)) {
            okHttpMock.when(() -> OkHttpUtils.post(
                    eq("http://oauth/token"),
                    any(), any(), any(), anyInt(), anyInt(), anyInt()))
                    .thenThrow(new RuntimeException("network error"));
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = new MockHttpServletResponse();
            controller.loginByAuth2("code123", "github", request, response);
            Assertions.assertEquals(302, ((MockHttpServletResponse) response).getStatus());
            String redirect = ((MockHttpServletResponse) response).getRedirectedUrl();
            Assertions.assertTrue(
                    redirect.contains("error=oauth2 auth error") || redirect.contains("error=oauth2%20auth%20error"),
                    "Redirect URL should contain error parameter");
        }
    }

    @Test
    void testGetOauth2ProviderWithNoConfig() {
        Authenticator authenticator = mock(Authenticator.class);
        SessionService sessionService = mock(SessionService.class);
        UsersService usersService = mock(UsersService.class);
        ApiConfig apiConfig = mock(ApiConfig.class);
        LoginController controller =
                new LoginController(sessionService, authenticator, usersService, Optional.empty(), Optional.empty(),
                        apiConfig);

        Result<List<OAuth2ClientProperties>> result = controller.oauth2Provider();
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
        Assertions.assertTrue(result.getData().isEmpty());
    }

    @Test
    void testGetClientIpAddress_variousScenarios() {
        HttpServletRequest request1 = mock(HttpServletRequest.class);
        when(request1.getHeader(Constants.HTTP_X_FORWARDED_FOR)).thenReturn("192.168.1.1, 10.0.0.1");
        Assertions.assertEquals("192.168.1.1", BaseController.getClientIpAddress(request1));

        HttpServletRequest request2 = mock(HttpServletRequest.class);
        when(request2.getHeader(Constants.HTTP_X_FORWARDED_FOR)).thenReturn("unknown");
        when(request2.getHeader(Constants.HTTP_X_REAL_IP)).thenReturn("192.168.1.2");
        Assertions.assertEquals("192.168.1.2", BaseController.getClientIpAddress(request2));

        HttpServletRequest request3 = mock(HttpServletRequest.class);
        when(request3.getHeader(Constants.HTTP_X_FORWARDED_FOR)).thenReturn("");
        when(request3.getHeader(Constants.HTTP_X_REAL_IP)).thenReturn("192.168.1.3");
        Assertions.assertEquals("192.168.1.3", BaseController.getClientIpAddress(request3));

        HttpServletRequest request4 = mock(HttpServletRequest.class);
        when(request4.getHeader(Constants.HTTP_X_FORWARDED_FOR)).thenReturn(null);
        when(request4.getHeader(Constants.HTTP_X_REAL_IP)).thenReturn(null);
        when(request4.getRemoteAddr()).thenReturn("192.168.1.4");
        Assertions.assertEquals("192.168.1.4", BaseController.getClientIpAddress(request4));

        HttpServletRequest request5 = mock(HttpServletRequest.class);
        when(request5.getHeader(Constants.HTTP_X_FORWARDED_FOR)).thenReturn("192.168.1.5");
        Assertions.assertEquals("192.168.1.5", BaseController.getClientIpAddress(request5));
    }

    private static class DummySsoAuthenticator extends AbstractSsoAuthenticator {

        @Override
        public User login(@org.springframework.lang.NonNull String state, String code) {
            return null;
        }

        @Override
        public String getSignInUrl(String state) {
            return "http://sso.example.com/auth?state=" + state;
        }
    }

    @Test
    public void testSsoLogin_WithAbstractSsoAuthenticator_ReturnsSignInUrl() {
        DummySsoAuthenticator dummy = new DummySsoAuthenticator();
        SessionService sessionService = mock(SessionService.class);
        UsersService usersService = mock(UsersService.class);
        ApiConfig apiConfig = mock(ApiConfig.class);
        LoginController controller =
                new LoginController(sessionService, dummy, usersService, Optional.empty(), Optional.empty(),
                        apiConfig);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(Constants.SSO_LOGIN_USER_STATE)).thenReturn(null);

        Result result = controller.ssoLogin(request);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());
        Assertions.assertNotNull(result.getData());
        verify(session).setAttribute(eq(Constants.SSO_LOGIN_USER_STATE), Mockito.any());
    }
}
