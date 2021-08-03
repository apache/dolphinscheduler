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
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.utils.RegistryCenterUtils;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * abstract controller test
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
@PrepareForTest({ RegistryCenterUtils.class, RegistryClient.class })
@PowerMockIgnore({"javax.management.*"})
public class AbstractControllerTest {

    public static final String SESSION_ID = "sessionId";

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SessionService sessionService;

    protected User user;

    protected String sessionId;

    @Before
    public void setUp() {
        PowerMockito.suppress(PowerMockito.constructor(RegistryClient.class));
        PowerMockito.mockStatic(RegistryCenterUtils.class);

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        createSession();
    }

    @After
    public void after() throws Exception {
        sessionService.signOut("127.0.0.1", user);
    }

    private void createSession() {

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);

        user = loginUser;

        String session = sessionService.createSession(loginUser, "127.0.0.1");
        sessionId = session;

        Assert.assertTrue(StringUtils.isNotEmpty(session));

    }
}
