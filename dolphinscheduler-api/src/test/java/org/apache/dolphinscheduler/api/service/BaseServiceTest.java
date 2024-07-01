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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.storage.hdfs.HdfsStorageOperator;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base service test
 */
@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseServiceTest.class);

    private BaseServiceImpl baseService;

    @Mock
    private HdfsStorageOperator hdfsStorageOperator;

    @BeforeEach
    public void setUp() {
        baseService = new BaseServiceImpl();
    }

    @Test
    public void testIsAdmin() {

        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        // ADMIN_USER
        Assertions.assertTrue(baseService.isAdmin(user));
        // GENERAL_USER
        user.setUserType(UserType.GENERAL_USER);
        Assertions.assertFalse(baseService.isAdmin(user));

    }

    @Test
    public void testPutMsg() {

        Map<String, Object> result = new HashMap<>();
        baseService.putMsg(result, Status.SUCCESS);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        // has params
        baseService.putMsg(result, Status.PROJECT_NOT_FOUND, "test");

    }

    @Test
    public void testPutMsgTwo() {

        Result result = new Result();
        baseService.putMsg(result, Status.SUCCESS);
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        // has params
        baseService.putMsg(result, Status.PROJECT_NOT_FOUND, "test");
    }

    @Test
    public void testHasPerm() {

        User user = new User();
        user.setId(1);
        // create user
        Assertions.assertTrue(baseService.canOperator(user, 1));

        // admin
        user.setId(2);
        user.setUserType(UserType.ADMIN_USER);
        Assertions.assertTrue(baseService.canOperator(user, 1));

    }

}
