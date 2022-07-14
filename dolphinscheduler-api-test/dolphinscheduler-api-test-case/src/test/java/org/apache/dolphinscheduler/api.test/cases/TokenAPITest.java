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
import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.extensions.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.pages.token.TokenPageAPI;
import org.apache.dolphinscheduler.api.test.pages.token.entity.TokenGenerateEntity;
import org.apache.dolphinscheduler.api.test.pages.token.entity.TokenRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.DateUtils;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisplayName("Token Page API test")
public class TokenAPITest extends AbstractAPITest {
    private String token;
    private TokenPageAPI tokenPageAPI = null;
    TokenGenerateEntity tokenGenerateEntity = null;

    @BeforeAll
    public void initTokenPageAPIFactory() {
        tokenPageAPI = pageAPIFactory.createTokenPageAPI();
        tokenGenerateEntity = new TokenGenerateEntity();
        tokenGenerateEntity.setExpireTime(DateUtils.dateToString(new Date()));
        tokenGenerateEntity.setUserId(1);
        token = tokenPageAPI.generateToken(tokenGenerateEntity).getResponse().jsonPath().getString(Constants.DATA_KEY);
    }

    @Test
    @Order(1)
    public void testCreateToken() {
        TokenRequestEntity tokenRequestEntity = new TokenRequestEntity();
        tokenRequestEntity.setToken(token);
        tokenRequestEntity.setUserId(1);
        tokenRequestEntity.setExpireTime(DateUtils.dateToString(new Date()));
        tokenPageAPI.createToken(tokenRequestEntity).isResponseSuccessful();
    }

}
