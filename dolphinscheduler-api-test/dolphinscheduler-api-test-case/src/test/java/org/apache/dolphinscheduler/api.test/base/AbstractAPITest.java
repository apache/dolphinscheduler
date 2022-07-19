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

package org.apache.dolphinscheduler.api.test.base;

import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.extensions.DolphinSchedulerExtension;
import org.apache.dolphinscheduler.api.test.pages.PageAPI;
import org.apache.dolphinscheduler.api.test.pages.PageAPIFactory;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

@ExtendWith({DolphinSchedulerExtension.class})
public abstract class AbstractAPITest implements TestLifecycleLogger {
    protected RequestSpecification reqSpec = null;
    protected RequestSpecification request = RestAssured.given();
    protected PageAPIFactory pageAPIFactory = null;
    protected String sessionId;

    @BeforeAll
    @DisplayName("init user session")
    public void init(TestInfo testInfo) {
        createSession();
        initPageApiFactory();
    }

    @AfterAll
    @DisplayName("release user session")
    public void tearDown() throws Exception {
        PageAPI.releaseSession(request, reqSpec, sessionId).isResponseSuccessful();
    }

    private void createSession() {
        reqSpec = PageAPI.requestSpec();
        RestResponse<Result> result = PageAPI.getSession(request, reqSpec);
        result.isResponseSuccessful();
        sessionId = result.getResponse().getCookie(Constants.SESSION_ID_KEY);
        logger.info("user sessionId: " + sessionId);
    }

    protected void initPageApiFactory() {
        pageAPIFactory = new PageAPIFactory(reqSpec, sessionId);
    }
}
