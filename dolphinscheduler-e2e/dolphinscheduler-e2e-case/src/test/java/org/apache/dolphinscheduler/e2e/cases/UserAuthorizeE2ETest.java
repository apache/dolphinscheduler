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
import org.apache.dolphinscheduler.e2e.pages.LogoutPage;
import org.apache.dolphinscheduler.e2e.pages.datasource.DataSourcePage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/datasource-mysql/docker-compose.yaml")
public class UserAuthorizeE2ETest {
    private static final String tenant = System.getProperty("user.name");
    private static final String user01 = "test_user01";
    private static final String password01 = "test_user01123";
    private static final String email01 = "test_user01@gmail.com";
    private static final String phone01 = "15800000000";

    private static final String user02 = "test_user02";
    private static final String password02 = "test_user02123";
    private static final String email02 = "test_user02@gamil.com";
    private static final String phone02 = "13900000000";

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
            .create(user01, password01, email01, phone01)
            .create(user02, password02, email02, phone02);

        new LogoutPage(browser)
            .logout("admin");

        new LoginPage(browser)
            .login(user01, password01)
            .goToNav(ProjectPage.class).create(project)
            .goToNav(DataSourcePage.class)
            .createDataSource(dataSourceType, dataSourceName, dataSourceDescription, ip, port, userName, mysqlPassword, database, jdbcParams);

        new LogoutPage(browser)
            .logout(user01);

        new LoginPage(browser)
            .login("admin", "dolphinscheduler123")
            .goToNav(SecurityPage.class)
            .goToTab(UserPage.class);

    }

    @Test
    @Order(10)
    void testAuthorizeProject() {
        final UserPage userPage = new UserPage(browser);

        userPage.authorizeProject(user01, project);

        new LogoutPage(browser)
            .logout(user01);

        ProjectPage projectPage = new LoginPage(browser)
            .login(user02, password02)
            .goToNav(ProjectPage.class);

        await().untilAsserted(() -> assertThat(projectPage.projectList())
            .as("Project list should contain newly-authorized project")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(project)));

    }

    @Test
    @Order(20)
    void testAuthorizeDataSource() {
        final UserPage userPage = new UserPage(browser);

        userPage.authorizeDataSource(user01, dataSourceName);

        new LogoutPage(browser)
            .logout(user01);

        DataSourcePage dataSourcePage = new LoginPage(browser)
            .login(user02, password02)
            .goToNav(DataSourcePage.class);

        await().untilAsserted(() -> assertThat(dataSourcePage.dataSourceItemsList())
            .as("DataSource list should contain newly-authorized database")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(dataSourceName)));
    }

}

