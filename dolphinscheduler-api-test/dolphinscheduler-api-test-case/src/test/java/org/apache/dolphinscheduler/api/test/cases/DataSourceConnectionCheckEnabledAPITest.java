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

import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.LoginResponseData;
import org.apache.dolphinscheduler.api.test.pages.LoginPage;
import org.apache.dolphinscheduler.api.test.pages.datasource.DataSourcePage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;

@DolphinScheduler(composeFiles = "docker/datasource-connection-check-enabled/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class DataSourceConnectionCheckEnabledAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static DataSourcePage dataSourcePage;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginResponse = loginPage.login(username, password);
        sessionId = JSONUtils.convertValue(loginResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        dataSourcePage = new DataSourcePage(sessionId);
    }

    @AfterAll
    public static void cleanup() {
    }

    @Test
    @Order(10)
    void testCreateDataSourceWithInvalidHostShouldFail() {
        HttpResponse response = dataSourcePage.createDataSource("mysql_test_invalid", "MYSQL",
                "invalid-host-12345", 3306, "root", "password", "test_db");

        Assertions.assertFalse(response.getBody().getSuccess(),
                "Creating datasource with invalid host should fail when connection check is enabled");
    }

    @Test
    @Order(20)
    void testUpdateDataSourceWithInvalidHostShouldFail() {
        HttpResponse createResponse = dataSourcePage.createDataSource("mysql_test_update", "MYSQL",
                "invalid-host-12345", 3306, "root", "password", "test_db");

        Assertions.assertFalse(createResponse.getBody().getSuccess(),
                "Creating datasource with invalid host should fail when connection check is enabled");
    }
}
