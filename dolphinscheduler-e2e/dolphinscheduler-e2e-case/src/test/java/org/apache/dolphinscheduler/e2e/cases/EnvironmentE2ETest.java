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
import org.apache.dolphinscheduler.e2e.pages.security.EnvironmentPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;

import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class EnvironmentE2ETest {

    private static final String environmentName = "test_environment_name";
    private static final String environmentConfig = "test_environment_config";
    private static final String environmentDesc = "test_environment_desc";
    private static final String environmentWorkerGroup = "default";

    private static final String editEnvironmentName = "edit_environment_name";
    private static final String editEnvironmentConfig = "edit_environment_config";
    private static final String editEnvironmentDesc = "edit_environment_desc";
    private static final String editEnvironmentWorkerGroup = "default";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
                .login("admin", "dolphinscheduler123")
                .goToNav(SecurityPage.class)
                .goToTab(EnvironmentPage.class)
        ;
    }

    @Test
    @Order(10)
    void testCreateEnvironment() {
        final EnvironmentPage page = new EnvironmentPage(browser);
        page.create(environmentName, environmentConfig, environmentDesc, environmentWorkerGroup);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.environmentList())
                    .as("Environment list should contain newly-created environment")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(environmentName));
        });
    }

    @Test
    @Order(20)
    void testCreateDuplicateEnvironment() {
        final EnvironmentPage page = new EnvironmentPage(browser);
        page.create(environmentName, environmentConfig, environmentDesc, environmentWorkerGroup);

        Awaitility.await().untilAsserted(() ->
                assertThat(browser.findElement(By.tagName("body")).getText())
                        .contains("already exists")
        );

        page.createEnvironmentForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testEditEnvironment() {
        final EnvironmentPage page = new EnvironmentPage(browser);
        page.update(environmentName, editEnvironmentName, editEnvironmentConfig, editEnvironmentDesc, editEnvironmentWorkerGroup);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.environmentList())
                    .as("Environment list should contain newly-modified environment")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(editEnvironmentName));
        });
    }

    @Test
    @Order(40)
    void testDeleteEnvironment() {
        final EnvironmentPage page = new EnvironmentPage(browser);

        page.delete(editEnvironmentName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                    page.environmentList()
            )
            .as("Environment list should not contain deleted environment")
            .noneMatch(
                    it -> it.getText().contains(environmentName) || it.getText().contains(editEnvironmentName)
            );
        });
    }
}
