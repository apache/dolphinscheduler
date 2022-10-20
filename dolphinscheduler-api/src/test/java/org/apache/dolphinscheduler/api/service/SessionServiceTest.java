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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.service.impl.SessionServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.SessionMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * session service test
 */
@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SessionServiceTest.class);

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Mock
    private SessionMapper sessionMapper;

    private String sessionId = "aaaaaaaaaaaaaaaaaa";

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void after() {
    }

    /**
     * create session
     */
    @Test
    public void testGetSession() {

        Mockito.when(sessionMapper.selectById(sessionId)).thenReturn(getSession());
        // get sessionId from header
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(Constants.SESSION_ID, sessionId);
        mockHttpServletRequest.addHeader("HTTP_X_FORWARDED_FOR", "127.0.0.1");
        // query
        Session session = sessionService.getSession(mockHttpServletRequest);
        Assertions.assertNotNull(session);
        logger.info("session ip {}", session.getIp());

        // get sessionId from cookie
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader("HTTP_X_FORWARDED_FOR", "127.0.0.1");
        MockCookie mockCookie = new MockCookie(Constants.SESSION_ID, sessionId);
        mockHttpServletRequest.setCookies(mockCookie);
        // query
        session = sessionService.getSession(mockHttpServletRequest);
        Assertions.assertNotNull(session);
        logger.info("session ip {}", session.getIp());
        Assertions.assertEquals(session.getIp(), "127.0.0.1");
    }

    /**
     * create session
     */
    @Test
    public void testCreateSession() {
        String ip = "127.0.0.1";
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        user.setId(1);
        Mockito.when(sessionMapper.queryByUserId(1)).thenReturn(getSessions());
        String sessionId = sessionService.createSession(user, ip);
        logger.info("createSessionId is " + sessionId);
        Assertions.assertTrue(!StringUtils.isEmpty(sessionId));
    }

    /**
     * sign out
     * remove ip restrictions
     */
    @Test
    public void testSignOut() {
        int userId = 88888888;
        String ip = "127.0.0.1";
        User user = new User();
        user.setId(userId);

        Mockito.when(sessionMapper.queryByUserIdAndIp(userId, ip)).thenReturn(getSession());

        sessionService.signOut(ip, user);

    }

    private Session getSession() {
        Session session = new Session();
        session.setId(sessionId);
        session.setIp("127.0.0.1");
        session.setLastLoginTime(DateUtils.add(new Date(), Calendar.DAY_OF_MONTH, 40));
        session.setUserId(1);
        return session;
    }

    private List<Session> getSessions() {
        List<Session> sessionList = new ArrayList<>();
        sessionList.add(getSession());
        return sessionList;
    }
}
