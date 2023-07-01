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

package org.apache.dolphinscheduler.api.security.impl.sso;

import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.casbin.casdoor.entity.CasdoorUser;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@TestPropertySource(properties = {
        "security.authentication.type=CASDOOR_SSO",
        "casdoor.endpoint=http://localhost:8000",
        "casdoor.client-id=client-id",
        "casdoor.client-secret=client-secret",
        "casdoor.certificate=public-key",
        "casdoor.organization-name=built-in",
        "casdoor.application-name=app-built-in",
        "casdoor.redirect-url=http://localhost:8888/view/login/index.html"
})
public class CasdoorAuthenticatorTest extends AbstractControllerTest {

    @Autowired
    protected AutowireCapableBeanFactory beanFactory;
    @MockBean(name = "sessionServiceImpl")
    private SessionService sessionService;
    @MockBean(name = "usersServiceImpl")
    private UsersService usersService;
    @MockBean(name = "casdoorAuthService")
    private CasdoorAuthService casdoorAuthService;

    private CasdoorAuthenticator casdoorAuthenticator;

    // test param
    private User mockUser;
    private Session mockSession;
    private CasdoorUser mockCasdoorUser;

    private String casdoorUsername = "test";
    private String casdoorEmail = "test@example.com";
    private String code = "code";
    private String state = "random_state";
    private String token = "token";
    private String ip = "127.0.0.1";
    private UserType userType = UserType.GENERAL_USER;

    @Override
    @BeforeEach
    public void setUp() {
        casdoorAuthenticator = new CasdoorAuthenticator();
        beanFactory.autowireBean(casdoorAuthenticator);

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUserName(casdoorUsername);
        mockUser.setEmail(casdoorEmail);
        mockUser.setUserType(userType);
        mockUser.setState(Flag.YES.getCode());

        mockSession = new Session();
        mockSession.setId(UUID.randomUUID().toString());
        mockSession.setIp(ip);
        mockSession.setUserId(1);
        mockSession.setLastLoginTime(new Date());

        mockCasdoorUser = new CasdoorUser();
        mockCasdoorUser.setName(casdoorUsername);
        mockCasdoorUser.setEmail(casdoorEmail);

    }

    @Test
    public void testAuthenticate() {
        when(usersService.getUserByUserName(casdoorUsername)).thenReturn(mockUser);
        when(sessionService.createSession(mockUser, ip)).thenReturn(mockSession.getId());

        when(casdoorAuthService.getOAuthToken(code, state)).thenReturn(token);
        when(casdoorAuthService.parseJwtToken(token)).thenReturn(mockCasdoorUser);

        MockHttpServletRequest request = new MockHttpServletRequest();
        Objects.requireNonNull(request.getSession()).setAttribute(Constants.SSO_LOGIN_USER_STATE, state);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Result result = casdoorAuthenticator.authenticate(state, code, ip);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        Assertions.assertNull(request.getSession().getAttribute(Constants.SSO_LOGIN_USER_STATE));

        Objects.requireNonNull(request.getSession()).setAttribute(Constants.SSO_LOGIN_USER_STATE, state);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        result = casdoorAuthenticator.authenticate("fake_state", code, ip);
        Assertions.assertEquals(Status.STATE_CODE_ERROR.getCode(), (int) result.getCode());

        Objects.requireNonNull(request.getSession()).setAttribute(Constants.SSO_LOGIN_USER_STATE, state);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(sessionService.createSession(mockUser, ip)).thenReturn(null);
        result = casdoorAuthenticator.authenticate(state, code, ip);
        Assertions.assertEquals(Status.LOGIN_SESSION_FAILED.getCode(), (int) result.getCode());

        Objects.requireNonNull(request.getSession()).setAttribute(Constants.SSO_LOGIN_USER_STATE, state);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(sessionService.createSession(mockUser, ip)).thenReturn(mockSession.getId());
        when(usersService.getUserByUserName(casdoorUsername)).thenReturn(null);
        when(usersService.createUser(userType, casdoorUsername, casdoorEmail)).thenReturn(null);
        result = casdoorAuthenticator.authenticate(state, code, ip);
        Assertions.assertEquals(Status.STATE_CODE_ERROR.getCode(), (int) result.getCode());
    }
}
