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

package org.apache.dolphinscheduler.e2e.cases.security;


import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.TenantPage;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/tenant/docker-compose.yaml")
class TenantE2ETest {
    private RemoteWebDriver browser;

    @Test
    @Order(1)
    void testLogin() {
        final LoginPage page = new LoginPage(browser);
        page.inputUsername().sendKeys("admin");
        page.inputPassword().sendKeys("dolphinscheduler123");
        page.buttonLogin().click();
    }

    @Test
    @Order(10)
    void testCreateTenant() {
        final TenantPage page = new TenantPage(browser);
        final String tenant = System.getProperty("user.name");

        page.buttonCreateTenant().click();
        page.createTenantForm().inputTenantCode().sendKeys(tenant);
        page.createTenantForm().inputDescription().sendKeys("Test");
        page.createTenantForm().buttonSubmit().click();

        await().untilAsserted(() -> assertThat(page.tenantList())
                .as("Tenant list should contain newly-created tenant")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(tenant)));
    }

    @Test
    @Order(20)
    void testCreateDuplicateTenant() {
        final String tenant = System.getProperty("user.name");
        final TenantPage page = new TenantPage(browser);
        page.buttonCreateTenant().click();
        page.createTenantForm().inputTenantCode().sendKeys(tenant);
        page.createTenantForm().inputDescription().sendKeys("Test");
        page.createTenantForm().buttonSubmit().click();

        await().untilAsserted(() -> assertThat(browser.findElementByTagName("body")
                                                      .getText().contains("already exists"))
                .as("Should fail when creating a duplicate tenant")
                .isTrue());

        page.createTenantForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testDeleteTenant() {
        final String tenant = System.getProperty("user.name");
        final TenantPage page = new TenantPage(browser);

        page.tenantList()
            .stream()
            .filter(it -> it.getText().contains(tenant))
            .findFirst()
            .ifPresent(it -> it.findElement(By.className("delete")).click());

        page.buttonConfirm().click();
    }
}
