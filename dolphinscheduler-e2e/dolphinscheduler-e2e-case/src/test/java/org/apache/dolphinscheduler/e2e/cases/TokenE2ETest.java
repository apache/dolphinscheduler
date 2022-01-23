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
import org.apache.dolphinscheduler.e2e.pages.security.TokenPage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
public class TokenE2ETest {

    private static TokenPage token = null;

    private static final String editToken = "editToken";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
                .login("admin", "dolphinscheduler123")
                .goToNav(SecurityPage.class)
                .goToTab(TokenPage.class)
        ;
    }

    @Test
    @Order(10)
    void testCreateToken() {
        final TokenPage page = new TokenPage(browser);
        token = page.create();

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.tokenList())
                    .as("Token list should contain newly-created token")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(token));
        });
    }

    @Test
    @Order(20)
    void testCreateDuplicateToken() {
        final TokenPage page = new TokenPage(browser);
        page.create();

        await().untilAsserted(() ->
                assertThat(browser.findElement(By.tagName("body")).getText())
                        .contains("already exists"));

        page.createTokenForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testEditToken() {
        final TokenPage page = new TokenPage(browser);
        page.update(editToken);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.tokenList())
                    .as("Token list should contain newly-modified token")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(editToken));
        });
    }

    @Test
    @Order(40)
    void testDeleteToken() {
        final TokenPage page = new TokenPage(browser);
        page.delete(editToken);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.tokenList())
                    .noneMatch(it -> it.getText().contains(token)
                            || it.getText().contains(editToken));
        });
    }

}
