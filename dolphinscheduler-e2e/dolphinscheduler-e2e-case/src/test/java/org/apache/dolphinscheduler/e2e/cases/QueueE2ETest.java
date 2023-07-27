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
import org.apache.dolphinscheduler.e2e.pages.security.QueuePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;

import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class QueueE2ETest {

    private static final String queueName = "test_queue_name";
    private static final String queueValue = "test_queue_value";
    private static final String editQueueName = "edit_test_queue_name";
    private static final String editQueueValue = "edit_test_queue_value";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
                .login("admin", "dolphinscheduler123")
                .goToNav(SecurityPage.class)
                .goToTab(QueuePage.class)
        ;
    }

    @Test
    @Order(10)
    void testCreateQueue() {
        final QueuePage page = new QueuePage(browser);
        page.create(queueName, queueValue);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.queueList())
                    .as("Queue list should contain newly-created queue")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(queueName));
        });
    }

    @Test
    @Order(20)
    void testCreateDuplicateQueue() {
        final QueuePage page = new QueuePage(browser);
        page.create(queueName, queueValue);

        Awaitility.await().untilAsserted(() ->
                assertThat(browser.findElement(By.tagName("body")).getText())
                        .contains("already exists")
        );

        page.createQueueForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testEditQueue() {
        QueuePage page = new QueuePage(browser);

        page.update(queueName, editQueueName, editQueueValue);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.queueList())
                    .as("Queue list should contain newly-modified Queue")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(editQueueName));
        });
    }

}
