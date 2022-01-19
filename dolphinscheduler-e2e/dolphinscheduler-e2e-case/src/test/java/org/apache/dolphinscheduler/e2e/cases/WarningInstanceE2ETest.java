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


import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.WarningInstancePage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class WarningInstanceE2ETest {
    private static final String alarmInstanceName = "test_warningInstance";
    private static final String alarmPluginName = "DingTalk";
    private static final String webHookContent = "adsadfsdsd12assa111klfgd";
    private static final String keyword = "Aghjgj789ggjhhcbm";

    private static final String editAlarmInstanceName = "test_edit_warningInstance";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
            .login("admin", "dolphinscheduler123")
            .goToNav(SecurityPage.class)
            .goToTab(WarningInstancePage.class);
    }

    @Test
    @Order(1)
    void testCreateWarningInstance() {
        final WarningInstancePage page = new WarningInstancePage(browser);

        page.create(alarmInstanceName, alarmPluginName, webHookContent, keyword);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.warningInstanceList())
                .as("AlarmInstance list should contain newly-created alarmInstanceName")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(alarmInstanceName));
        });
    }

    @Test
    @Order(20)
    void testCreateDuplicateWarningInstance() {
        final WarningInstancePage page = new WarningInstancePage(browser);

        page.create(alarmInstanceName, alarmPluginName, webHookContent, keyword);

        await().untilAsserted(() ->
            assertThat(browser.findElement(By.tagName("body")).getText())
                .contains("already exists")
        );

        page.createWarningInstanceForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testEditWarningInstance() {
        final WarningInstancePage page = new WarningInstancePage(browser);
        page.update(alarmInstanceName, editAlarmInstanceName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.warningInstanceList())
                .as("WarningInstance list should contain newly-modified WarningInstance")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(editAlarmInstanceName));
        });
    }

    @Test
    @Order(40)
    void testDeleteWarningInstance() {
        final WarningInstancePage page = new WarningInstancePage(browser);

        page.delete(editAlarmInstanceName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.warningInstanceList()
            ).noneMatch(
                    it -> it.getText().contains(alarmInstanceName) || it.getText().contains(editAlarmInstanceName)
            );
        });
    }
}
