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

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class UdfFuncServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UdfFuncServiceTest.class);

    @Autowired
    private UdfFuncService udfFuncService;

    @Test
    public void queryUdfFuncListPaging(){

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> map = udfFuncService.queryUdfFuncListPaging(loginUser, "", 1, 10);
        Assert.assertEquals(Status.SUCCESS, map.get(Constants.STATUS));

        PageInfo pageInfo = (PageInfo) map.get("data");
        logger.info(pageInfo.getLists().toString());

    }
}