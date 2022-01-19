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
import org.apache.dolphinscheduler.e2e.pages.security.WarningGroupPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class WarningGroupE2ETest {
    private static final String alarmGroupName = "test_WarningGroup";
    private static final String alarmGroupDescription = "test_WarningGroup_Description";

    private static final String editAlarmGroupName = "test_WarningGroup_edit";
    private static final String editAlarmGroupDescription = "test_WarningGroup_Description_edit";


    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
            .login("admin", "dolphinscheduler123")
            .goToNav(SecurityPage.class)
            .goToTab(WarningGroupPage.class);
    }

    @Test
    @Order(1)
    void testCreateWarningGroup() {
        final WarningGroupPage page = new WarningGroupPage(browser);

        page.create(alarmGroupName, alarmGroupDescription);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.alarmGroupList())
                .as("WarningGroup list should contain newly-created WarningGroupName")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(alarmGroupName));
        });
    }

    @Test
    @Order(20)
    void testCreateDuplicateWarningGroup() {
        final WarningGroupPage page = new WarningGroupPage(browser);

        page.create(alarmGroupName, alarmGroupDescription);

        await().untilAsserted(() ->
            assertThat(browser.findElement(By.tagName("body")).getText())
                .contains("already exists")
        );

        page.createWarningGroupForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testEditWarningGroup() {
        final WarningGroupPage page = new WarningGroupPage(browser);
        page.update(alarmGroupName, editAlarmGroupName, editAlarmGroupDescription);

        await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.alarmGroupList())
                .as("WarningGroup list should contain newly-modified editAlarmGroupName")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(editAlarmGroupName));
        });
    }

    @Test
    @Order(40)
    void testDeleteWarningGroup() {
        final WarningGroupPage page = new WarningGroupPage(browser);

        page.delete(editAlarmGroupName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.alarmGroupList()
            ).noneMatch(
                    it -> it.getText().contains(alarmGroupName) || it.getText().contains(editAlarmGroupName)            );
        });
    }
}
