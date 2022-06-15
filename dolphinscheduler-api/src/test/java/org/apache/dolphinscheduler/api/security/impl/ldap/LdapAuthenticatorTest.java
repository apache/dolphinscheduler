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

package org.apache.dolphinscheduler.api.security.impl.ldap;

import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {
                "security.authentication.type=LDAP",
                "security.authentication.ldap.user.admin=read-only-admin",
                "security.authentication.ldap.urls=ldap://ldap.forumsys.com:389/",
                "security.authentication.ldap.base.dn=dc=example,dc=com",
                "security.authentication.ldap.username=cn=read-only-admin,dc=example,dc=com",
                "security.authentication.ldap.password=password",
                "security.authentication.ldap.user.identity.attribute=uid",
                "security.authentication.ldap.user.email.attribute=mail",
        })
public class LdapAuthenticatorTest extends AbstractControllerTest {
    private static Logger logger = LoggerFactory.getLogger(LdapAuthenticatorTest.class);
    @Autowired
    protected AutowireCapableBeanFactory beanFactory;
    @MockBean(name = "ldapService")
    private LdapService ldapService;
    @MockBean(name = "sessionServiceImpl")
    private SessionService sessionService;
    @MockBean(name = "usersServiceImpl")
    private UsersService usersService;

    private LdapAuthenticator ldapAuthenticator;

    //test param
    private User mockUser;
    private Session mockSession;

    private String ldapUid = "test";
    private String ldapUserPwd = "password";
    private String ldapEmail = "test@example.com";
    private String ip = "127.0.0.1";
    private UserType userType = UserType.GENERAL_USER;

    @Override
    @Before
    public void setUp() {
        ldapAuthenticator = new LdapAuthenticator();
        beanFactory.autowireBean(ldapAuthenticator);

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUserName(ldapUid);
        mockUser.setEmail(ldapEmail);
        mockUser.setUserType(userType);
        mockUser.setState(Flag.YES.getCode());

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

        Result result = ldapAuthenticator.authenticate(ldapUid, ldapUserPwd, ip);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        logger.info(result.toString());

        when(sessionService.createSession(mockUser, ip)).thenReturn(null);
        result = ldapAuthenticator.authenticate(ldapUid, ldapUserPwd, ip);
        Assert.assertEquals(Status.LOGIN_SESSION_FAILED.getCode(), (int) result.getCode());

        when(sessionService.createSession(mockUser, ip)).thenReturn(mockSession.getId());
        when(usersService.getUserByUserName(ldapUid)).thenReturn(null);
        result = ldapAuthenticator.authenticate(ldapUid, ldapUserPwd, ip);
        Assert.assertEquals(Status.USER_NAME_PASSWD_ERROR.getCode(), (int) result.getCode());
    }

    @Test
    public void testGetAuthUser() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(usersService.queryUser(mockUser.getId())).thenReturn(mockUser);
        when(sessionService.getSession(request)).thenReturn(mockSession);

        User user = ldapAuthenticator.getAuthUser(request);
        Assert.assertNotNull(user);

        when(sessionService.getSession(request)).thenReturn(null);
        user = ldapAuthenticator.getAuthUser(request);
        Assert.assertNull(user);
    }
}
