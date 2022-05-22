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
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.datasource.DataSourcePage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


@DolphinScheduler(composeFiles = "docker/datasource-postgresql/docker-compose.yaml")
public class PostgresDataSourceE2ETest {
    private static RemoteWebDriver browser;

    private static final String tenant = System.getProperty("user.name");

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String dataSourceType = "POSTGRESQL";

    private static final String dataSourceName = "postgres_test";

    private static final String dataSourceDescription = "postgres_test";

    private static final String ip = "postgres";

    private static final String port = "5432";

    private static final String userName = "postgres";

    private static final String pgPassword = "postgres";

    private static final String database = "postgres";

    private static final String jdbcParams = "";


    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
            .login(user, password)
            .goToNav(DataSourcePage.class);
    }

    @Test
    @Order(10)
    void testCreatePostgresDataSource() {
        final DataSourcePage page = new DataSourcePage(browser);

        page.createDataSource(dataSourceType, dataSourceName, dataSourceDescription, ip, port, userName, pgPassword, database, jdbcParams);

        new WebDriverWait(page.driver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(
                new By.ByClassName("dialog-create-data-source")));

        await().untilAsserted(() -> assertThat(page.dataSourceItemsList())
            .as("DataSource list should contain newly-created database")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(dataSourceName)));
    }

    @Test
    @Order(20)
    void testDeletePostgresDataSource() {
        final DataSourcePage page = new DataSourcePage(browser);

        page.delete(dataSourceName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                    page.dataSourceItemsList()
            ).noneMatch(
                    it -> it.getText().contains(dataSourceName)
            );
        });
    }
}
