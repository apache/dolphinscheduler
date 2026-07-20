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

import java.util.LinkedHashMap;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;

@DolphinScheduler(composeFiles = "docker/datasource-connection-check-disabled/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class DataSourceConnectionCheckDisabledAPITest {

    private static final String username = "admin";

    private static final String password = "dolphinscheduler123";

    private static String sessionId;

    private static DataSourcePage dataSourcePage;

    private static int createdDataSourceId;

    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginResponse = loginPage.login(username, password);
        sessionId = JSONUtils.convertValue(loginResponse.getBody().getData(), LoginResponseData.class).getSessionId();
        dataSourcePage = new DataSourcePage(sessionId);
    }

    @AfterAll
    public static void cleanup() {
        if (createdDataSourceId > 0) {
            dataSourcePage.deleteDataSource(createdDataSourceId);
        }
    }

    @Test
    @Order(10)
    void testCreateDataSourceWithInvalidHostShouldSuccess() {
        HttpResponse response = dataSourcePage.createDataSource("mysql_test_invalid", "MYSQL",
                "invalid-host-12345", 3306, "root", "password", "test_db");

        Assertions.assertTrue(response.getBody().getSuccess(),
                "Creating datasource with invalid host should succeed when connection check is disabled");

        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) response.getBody().getData();
        createdDataSourceId = ((Number) data.get("id")).intValue();
        Assertions.assertNotNull(data.get("id"), "Datasource id should not be null");
    }

    @Test
    @Order(20)
    void testUpdateDataSourceWithInvalidHostShouldSuccess() {
        HttpResponse updateResponse = dataSourcePage.updateDataSource(createdDataSourceId,
                "mysql_test_invalid_updated", "MYSQL", "another-invalid-host", 3307, "root", "new_password",
                "test_db_updated");

        Assertions.assertTrue(updateResponse.getBody().getSuccess(),
                "Updating datasource with invalid host should succeed when connection check is disabled");
    }

    @Test
    @Order(30)
    void testQueryDataSource() {
        HttpResponse response = dataSourcePage.queryDataSource(createdDataSourceId);

        Assertions.assertTrue(response.getBody().getSuccess(), "Querying datasource should succeed");
    }

    @Test
    @Order(40)
    void testDeleteDataSource() {
        HttpResponse response = dataSourcePage.deleteDataSource(createdDataSourceId);

        Assertions.assertTrue(response.getBody().getSuccess(), "Deleting datasource should succeed");
        createdDataSourceId = 0;
    }
}
