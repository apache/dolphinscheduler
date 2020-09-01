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
package org.apache.dolphinscheduler.api.security;

import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ActiveProfiles("api")
@SpringBootTest(classes = ApiApplicationServer.class)
public class LdapAuthenticatorTest {
    private static Logger logger = LoggerFactory.getLogger(LdapAuthenticatorTest.class);

    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    @MockBean
    private LdapService ldapService;
    @MockBean
    private SessionService sessionService;
    @MockBean
    private UsersService usersService;

    private LdapAuthenticator authenticator;

    //test param
    private User mockUser;
    private Session mockSession;

    private String ldapUid = "test";
    private String ldapUserPwd = "password";
    private String ldapEmail = "test@example.com";
    private String ip = "127.0.0.1";
    private UserType userType = UserType.GENERAL_USER;

    @Before
    public void setUp() {
        authenticator = new LdapAuthenticator();
        beanFactory.autowireBean(authenticator);

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUserName(ldapUid);
        mockUser.setEmail(ldapEmail);
        mockUser.setUserType(userType);

        mockSession = new Session();
        mockSession.setId(UUID.randomUUID().toString());
        mockSession.setIp(ip);
        mockSession.setUserId(1);
        mockSession.setLastLoginTime(new Date());

    }

    @Test
    public void testAuthenticate() {
        when(usersService.createUser(userType, ldapUid, ldapEmail)).thenReturn(mockUser);
        when(usersService.getUserByUserName(ldapUid)).thenReturn(mockUser);
        when(sessionService.createSession(mockUser, ip)).thenReturn(mockSession.getId());

        when(ldapService.ldapLogin(ldapUid, ldapUserPwd)).thenReturn(ldapEmail);

        Result result = authenticator.authenticate(ldapUid, ldapUserPwd, ip);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        logger.info(result.toString());
    }

    @Test
    public void testGetAuthUser() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(usersService.queryUser(mockUser.getId())).thenReturn(mockUser);
        when(sessionService.getSession(request)).thenReturn(mockSession);

        User user = authenticator.getAuthUser(request);
        Assert.assertNotNull(user);
    }
}
