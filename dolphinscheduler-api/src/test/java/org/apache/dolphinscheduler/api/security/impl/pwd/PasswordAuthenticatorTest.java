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

package org.apache.dolphinscheduler.api.security.impl.pwd;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.mock.mockito.MockBean;

public class PasswordAuthenticatorTest extends AbstractControllerTest {

    private static Logger logger = LoggerFactory.getLogger(PasswordAuthenticatorTest.class);

    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    @MockBean
    private SessionService sessionService;
    @MockBean
    private UsersService usersService;

    private PasswordAuthenticator authenticator;

    private User mockUser;
    private Session mockSession;

    @Override
    @BeforeEach
    public void setUp() {
        authenticator = new PasswordAuthenticator();
        beanFactory.autowireBean(authenticator);

        mockUser = new User();
        mockUser.setUserName("test");
        mockUser.setEmail("test@test.com");
        mockUser.setUserPassword("test");
        mockUser.setId(1);
        mockUser.setState(1);

        mockSession = new Session();
        mockSession.setId(UUID.randomUUID().toString());
        mockSession.setIp("127.0.0.1");
        mockSession.setUserId(1);
        mockSession.setLastLoginTime(new Date());
    }

    @Test
    public void testLogin() {
        when(usersService.queryUser("test", "test")).thenReturn(mockUser);
        User login = authenticator.login("test", "test");
        Assertions.assertNotNull(login);
    }

    @Test
    public void testAuthenticate() {
        when(usersService.queryUser("test", "test")).thenReturn(mockUser);
        when(sessionService.createSessionIfAbsent(mockUser)).thenReturn(mockSession);
        Result result = authenticator.authenticate("test", "test", "127.0.0.1");
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        logger.info(result.toString());

        mockUser.setState(0);
        when(usersService.queryUser("test", "test")).thenReturn(mockUser);
        when(sessionService.createSessionIfAbsent(mockUser)).thenReturn(mockSession);
        Result result1 = authenticator.authenticate("test", "test", "127.0.0.1");
        Assertions.assertEquals(Status.USER_DISABLED.getCode(), (int) result1.getCode());
        logger.info(result1.toString());
    }

    @Test
    public void testGetAuthUser() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(usersService.queryUser(mockUser.getId())).thenReturn(mockUser);
        when(sessionService.getSession(any())).thenReturn(mockSession);

        User user = authenticator.getAuthUser(request);
        Assertions.assertNotNull(user);
    }
}
