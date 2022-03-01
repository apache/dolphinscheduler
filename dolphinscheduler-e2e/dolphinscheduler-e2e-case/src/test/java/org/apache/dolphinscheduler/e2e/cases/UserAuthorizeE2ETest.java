/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.e2e.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.datasource.DataSourcePage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/datasource-mysql/docker-compose.yaml")
public class UserAuthorizeE2ETest {
    private static final String tenant = System.getProperty("user.name");
    private static final String user = "test_user";
    private static final String password = "test_user123";
    private static final String email = "test_user@gmail.com";
    private static final String phone = "15800000000";

    private static final String project = "test_project";

    private static final String dataSourceType = "MYSQL";
    private static final String dataSourceName = "mysql_test";
    private static final String dataSourceDescription = "mysql_test";
    private static final String ip = "mysql";
    private static final String port = "3306";
    private static final String userName = "root";
    private static final String mysqlPassword = "123456";
    private static final String database = "mysql";
    private static final String jdbcParams = "{\"useSSL\": false}";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        TenantPage tenantPage = new LoginPage(browser)
            .login("admin", "dolphinscheduler123")
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
            .create(tenant);

        await().untilAsserted(() -> assertThat(tenantPage.tenantList())
            .as("Tenant list should contain newly-created tenant")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(tenant)));

        tenantPage.goToNav(SecurityPage.class)
            .goToTab(UserPage.class)
            .create(user, password, email, phone)
            .goToNav(ProjectPage.class).create(project)
            .goToNav(DataSourcePage.class)
            .createDataSource(dataSourceType, dataSourceName, dataSourceDescription, ip, port, userName, mysqlPassword, database, jdbcParams)
            .goToNav(SecurityPage.class)
            .goToTab(UserPage.class);
    }

    @AfterAll
    public static void cleanup() {
        new NavBarPage(browser)
            .goToNav(ProjectPage.class)
            .delete(project)
            .goToNav(DataSourcePage.class)
            .delete(dataSourceName)
            .goToNav(SecurityPage.class)
            .goToTab(UserPage.class)
            .delete(user)
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
            .delete(tenant);
    }

    @Test
    @Order(10)
    void testAuthorizeProject() {
        final UserPage page = new UserPage(browser);

        page.authorizeProject(user, project);
        page.clickAuthorize(user);

        await().untilAsserted(() -> {

            assertThat(page.selectedList())
                .as("Selected project list should contain newly-authorized project")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(project));
        });

        page.closeAuthorize();
    }

    @Test
    @Order(20)
    void testAuthorizeDataSource() {
        final UserPage page = new UserPage(browser);

        page.authorizeDataSource(user, dataSourceName);
        page.clickAuthorize(user);

        await().untilAsserted(() -> {

            assertThat(page.selectedList())
                .as("Selected dataSource list should contain newly-authorized dataSource")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(dataSourceName));
        });

        page.closeAuthorize();
    }

}

