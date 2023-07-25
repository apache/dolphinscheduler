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

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.time.Duration;

import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class UserE2ETest {
    private static final String tenant = System.getProperty("user.name");
    private static final String user = "test_user";
    private static final String password = "testUser123";
    private static final String email = "testUser@gmail.com";
    private static final String phone = "15812389765";

    private static final String editUser = "edit_test_user";
    private static final String editPassword = "editTestUser123";
    private static final String editEmail = "editTestUser@gmail.com";
    private static final String editPhone = "15812389780";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        TenantPage tenantPage = new LoginPage(browser)
                .login("admin", "dolphinscheduler123")
                .goToNav(SecurityPage.class)
                .goToTab(TenantPage.class)
                .create(tenant);

        Awaitility.await().untilAsserted(() -> assertThat(tenantPage.tenantList())
                .as("Tenant list should contain newly-created tenant")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(tenant)));

        tenantPage.goToNav(SecurityPage.class)
                .goToTab(UserPage.class);
    }

    @AfterAll
    public static void cleanup() {
        new NavBarPage(browser)
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
            .delete(tenant);
    }

    @Test
    @Order(1)
    void testCreateUser() {
        final UserPage page = new UserPage(browser);

        page.create(user, password, email, phone, tenant);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.userList())
                .as("User list should contain newly-created user")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(user));
        });
    }

    @Test
    @Order(20)
    void testCreateDuplicateUser() {
        final UserPage page = new UserPage(browser);

        page.create(user, password, email, phone, tenant);

        Awaitility.await().untilAsserted(() ->
            assertThat(browser.findElement(By.tagName("body")).getText())
                .contains("already exists")
        );

        page.createUserForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testEditUser() {
        UserPage page = new UserPage(browser);

        new WebDriverWait(browser, Duration.ofSeconds(20)).until(ExpectedConditions.visibilityOfElementLocated(
                new By.ByClassName("name")));

        browser.navigate().refresh();

        page.update(user, editUser, editEmail, editPhone, tenant);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.userList())
                .as("User list should contain newly-modified User")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(editUser));
        });
    }
    
    @Test
    @Order(40)
    void testDeleteUser() {
        final UserPage page = new UserPage(browser);

        page.delete(editUser);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.userList()
            ).noneMatch(
                it -> it.getText().contains(user) || it.getText().contains(editUser)
            );
        });
    }
}
