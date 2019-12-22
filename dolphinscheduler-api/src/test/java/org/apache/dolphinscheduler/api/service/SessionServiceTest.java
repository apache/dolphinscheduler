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

import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.SessionMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class SessionServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SessionServiceTest.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionMapper sessionMapper;

    @Before
    public void setUp() {
        removeSession();
    }


    @After
    public void after(){

        removeSession();
    }

    /**
     * create session
     */
    @Test
    public void testGetSession(){

        //add session
        String sessionId = getSessionId();
        // get sessionId from  header
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(Constants.SESSION_ID,sessionId);
        mockHttpServletRequest.addHeader("HTTP_X_FORWARDED_FOR","127.0.0.1");
        //query
        Session session = sessionService.getSession(mockHttpServletRequest);
        Assert.assertNotNull(session);
        logger.info("session ip {}",session.getIp());

        // get sessionId from cookie
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader("HTTP_X_FORWARDED_FOR","127.0.0.1");
        MockCookie mockCookie = new MockCookie(Constants.SESSION_ID,sessionId);
        mockHttpServletRequest.setCookies(mockCookie);
        //query
        session = sessionService.getSession(mockHttpServletRequest);
        Assert.assertNotNull(session);
        logger.info("session ip {}",session.getIp());
        Assert.assertEquals(session.getIp(),"127.0.0.1");


    }

    /**
     * create session
     */
    @Test
    public void testCreateSession(){

        String ip = "127.0.0.1";
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        user.setId(88888888);
        String sessionId = sessionService.createSession(user, ip);
        logger.info("createSessionId is "+sessionId);
        Assert.assertTrue(StringUtils.isNotEmpty(sessionId));
    }
    /**
     * sign out
     * remove ip restrictions
     */
    @Test
    public void testSignOut(){

        int userId = 88888888;
        String ip = "127.0.0.1";
        //create session
        getSessionId();
        User user = new User();
        user.setId(userId);
        //signOut
        sessionService.signOut(ip ,user);
        //check exist
        Session session = sessionMapper.queryByUserIdAndIp(userId, ip);
        Assert.assertNull(session);
    }



    private String getSessionId(){

        String ip = "127.0.0.1";
        User user = new User();
        user.setId(88888888);
        return sessionService.createSession(user, ip);
    }

    /**
     * remove  session
     */
    private void removeSession(){

        String ip = "127.0.0.1";
        User user = new User();
        user.setId(88888888);
        sessionService.signOut( ip,user);
    }
}