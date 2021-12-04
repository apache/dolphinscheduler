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
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class TenantE2ETest {
    private static final String tenant = System.getProperty("user.name");

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
            .login("admin", "dolphinscheduler123")
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
        ;
    }

    @Test
    @Order(10)
    void testCreateTenant() {
        new TenantPage(browser).create(tenant);
    }

    @Test
    @Order(20)
    void testCreateDuplicateTenant() {
        final var page = new TenantPage(browser);

        page.create(tenant);

        await().untilAsserted(() ->
            assertThat(browser.findElement(By.tagName("body")).getText())
                .contains("already exists")
        );

        page.createTenantForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testDeleteTenant() {
        final var page = new TenantPage(browser);
        page.delete(tenant);

        await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(
                page.tenantList()
            ).noneMatch(
                it -> it.getText().contains(tenant)
            );
        });
    }
}
