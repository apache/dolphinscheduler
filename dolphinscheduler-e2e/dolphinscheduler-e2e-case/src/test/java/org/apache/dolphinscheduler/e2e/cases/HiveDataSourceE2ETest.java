/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.dolphinscheduler.e2e.cases;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.datasource.DataSourcePage;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@DolphinScheduler(composeFiles = "docker/datasource-hive/docker-compose.yaml")
public class HiveDataSourceE2ETest {
    private static RemoteWebDriver browser;

    private static final String tenant = System.getProperty("user.name");

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String dataSourceType = "HIVE/IMPALA";

    private static final String dataSourceName = "hive_test";

    private static final String dataSourceDescription = "hive_test";

    private static final String ip = "hive-server";

    private static final String port = "10000";

    private static final String userName = "hadoop";

    private static final String hivePassword = "";

    private static final String database = "default";

    private static final String jdbcParams = "";

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
            .login(user, password)
            .goToNav(DataSourcePage.class);


    }

    @Test
    @Order(10)
    void testCreateHiveDataSource() {
        final DataSourcePage page = new DataSourcePage(browser);

        page.createDataSource(dataSourceType, dataSourceName, dataSourceDescription, ip, port, userName, hivePassword, database, jdbcParams);

        new WebDriverWait(page.driver(), Duration.ofSeconds(20)).until(ExpectedConditions.invisibilityOfElementLocated(
                new By.ByClassName("dialog-create-data-source")));

        Awaitility.await().untilAsserted(() -> assertThat(page.dataSourceItemsList())
            .as("DataSource list should contain newly-created database")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(dataSourceName)));
    }

    @Test
    @Order(20)
    void testDeleteHiveDataSource() {
        final DataSourcePage page = new DataSourcePage(browser);

        page.delete(dataSourceName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.dataSourceItemsList()
            ).noneMatch(
                it -> it.getText().contains(dataSourceName)
            );
        });
    }
}
