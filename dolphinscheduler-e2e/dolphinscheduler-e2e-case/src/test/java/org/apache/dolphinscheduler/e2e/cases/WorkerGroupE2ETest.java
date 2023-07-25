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
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.WorkerGroupPage;

import java.time.Duration;

import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class WorkerGroupE2ETest {
    private static final String workerGroupName = "test_worker_group";
    private static final String editWorkerGroupName = "edit_worker_group";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
            .login("admin", "dolphinscheduler123")
            .goToNav(SecurityPage.class)
            .goToTab(WorkerGroupPage.class);
    }

    @Test
    @Order(1)
    void testCreateWorkerGroup() {
        final WorkerGroupPage page = new WorkerGroupPage(browser);

        new WebDriverWait(page.driver(), Duration.ofSeconds(20))
            .until(ExpectedConditions.urlContains("/security/worker-group-manage"));

        page.create(workerGroupName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.workerGroupList())
                .as("workerGroup list should contain newly-created workerGroup")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(workerGroupName));
        });
    }

    @Test
    @Order(20)
    void testCreateDuplicateWorkerGroup() {
        final WorkerGroupPage page = new WorkerGroupPage(browser);

        page.create(workerGroupName);

        Awaitility.await().untilAsserted(() ->
            assertThat(browser.findElement(By.tagName("body")).getText())
                .contains("already exists")
        );

        page.createWorkerForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testEditWorkerGroup() {
        final WorkerGroupPage page = new WorkerGroupPage(browser);
        page.update(workerGroupName, editWorkerGroupName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.workerGroupList())
                .as("workerGroup list should contain newly-modified workerGroup")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(editWorkerGroupName));
        });
    }


    @Test
    @Order(40)
    void testDeleteWorkerGroup() {
        final WorkerGroupPage page = new WorkerGroupPage(browser);

        page.delete(editWorkerGroupName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.workerGroupList()
            ).noneMatch(
                it -> it.getText().contains(workerGroupName) || it.getText().contains(editWorkerGroupName)
            );
        });
    }
}
