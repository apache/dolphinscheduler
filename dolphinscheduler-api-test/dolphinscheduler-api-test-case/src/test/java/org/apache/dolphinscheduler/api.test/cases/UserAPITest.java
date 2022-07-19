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

package org.apache.dolphinscheduler.api.test.cases;

import org.apache.dolphinscheduler.api.test.base.AbstractAPITest;
import org.apache.dolphinscheduler.api.test.core.extensions.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.pages.security.user.UserPageAPI;
import org.apache.dolphinscheduler.api.test.pages.security.user.entity.UserCreateRequestEntity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisplayName("User Page API test")
public class UserAPITest extends AbstractAPITest {
    private UserPageAPI userPageAPI = null;
    private UserCreateRequestEntity userCreateRequestEntity;

    @BeforeAll
    public void initUserPageAPIFactory() {
        userPageAPI = pageAPIFactory.createUserPageAPI();
    }

    @Test
    @Order(1)
    public void testUserToken() {
        userPageAPI.createUser(userPageAPI.getUserEntityInstance()).getResponse().prettyPrint();
    }
}
