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

package org.apache.dolphinscheduler.test.base;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.dolphinscheduler.test.apis.EndPoints;
import org.apache.dolphinscheduler.test.apis.common.FormParam;
import org.apache.dolphinscheduler.test.core.extensions.RandomParametersExtension;
import org.apache.dolphinscheduler.test.core.extensions.TimingExtension;
import org.apache.dolphinscheduler.test.utils.RestResponse;
import org.apache.dolphinscheduler.test.utils.Result;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith({RandomParametersExtension.class, TimingExtension.class})
public abstract class AbstractControllerTest implements TestLifecycleLogger {

    protected RequestSpecification reqSpec = null;
    protected RequestSpecification request = RestAssured.given();
    protected String sessionId;

    @BeforeAll
    @DisplayName("init user session")
    public void init(TestInfo testInfo) {
        createSession();
    }

    @AfterAll
    @DisplayName("release user session")
    public void tearDown() throws Exception {
    }

    private void createSession() {
        reqSpec = EndPoints.requestSpec();
        RestResponse<Result> result = EndPoints.getSession(request, reqSpec);
        result.isResponseSuccessful();
        sessionId = result.getResponse().getCookie(FormParam.SESSION_ID.getParam());
        logger.info("user sessionId: " + sessionId);
    }
}
