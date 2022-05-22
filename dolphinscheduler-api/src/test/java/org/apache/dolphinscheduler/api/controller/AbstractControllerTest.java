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

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.controller.AbstractControllerTest.RegistryServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.DaoConfiguration;
import org.apache.dolphinscheduler.dao.entity.User;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.test.TestingServer;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * abstract controller test
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApiApplicationServer.class, DaoConfiguration.class, RegistryServer.class})
@AutoConfigureMockMvc
@DirtiesContext
public abstract class AbstractControllerTest {

    public static final String SESSION_ID = "sessionId";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UsersService usersService;

    protected User user;

    protected String sessionId;

    @Before
    public void setUp() {
        user = usersService.queryUser(1);
        createSession(user);
    }

    @After
    public void after() throws Exception {
        sessionService.signOut("127.0.0.1", user);
    }

    private void createSession(User loginUser) {

        user = loginUser;

        String session = sessionService.createSession(loginUser, "127.0.0.1");
        sessionId = session;

        Assert.assertFalse(StringUtils.isEmpty(session));
    }

    public Map<String, Object> success() {
        Map<String, Object> serviceResult = new HashMap<>();
        putMsg(serviceResult, Status.SUCCESS);
        return serviceResult;
    }

    public void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    @Configuration
    public static class RegistryServer {
        @PostConstruct
        public void startEmbedRegistryServer() throws Exception {
            final TestingServer server = new TestingServer(true);
            System.setProperty("registry.zookeeper.connect-string", server.getConnectString());
        }
    }
}
