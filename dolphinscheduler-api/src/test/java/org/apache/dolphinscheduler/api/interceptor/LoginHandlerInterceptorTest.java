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

package org.apache.dolphinscheduler.api.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.common.enums.ProfileType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@ActiveProfiles(value = {ProfileType.H2})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
@Transactional
@Rollback
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class LoginHandlerInterceptorTest {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandlerInterceptorTest.class);

    @Autowired
    LoginHandlerInterceptor interceptor;
    @MockBean(name = "authenticator")
    private Authenticator authenticator;
    @MockBean(name = "userMapper")
    private UserMapper    userMapper;

    @Test
    public void testPreHandle() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        // test no token and no cookie
        Assert.assertFalse(interceptor.preHandle(request, response, null));

        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUserType(UserType.GENERAL_USER);
        mockUser.setState(1);

        // test no token
        when(authenticator.getAuthUser(request)).thenReturn(mockUser);
        Assert.assertTrue(interceptor.preHandle(request, response, null));

        // test token
        String token = "123456";
        when(request.getHeader("token")).thenReturn(token);
        when(userMapper.queryUserByToken(eq(token), any(Date.class))).thenReturn(mockUser);
        Assert.assertTrue(interceptor.preHandle(request, response, null));

        // test disable user
        mockUser.setState(0);
        when(authenticator.getAuthUser(request)).thenReturn(mockUser);
        Assert.assertFalse(interceptor.preHandle(request, response, null));
    }
}
