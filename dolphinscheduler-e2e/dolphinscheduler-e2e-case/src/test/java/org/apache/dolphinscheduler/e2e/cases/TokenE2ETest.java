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
import org.apache.dolphinscheduler.e2e.pages.security.TokenPage;

import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
public class TokenE2ETest {

    private static final String userName = "admin";

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
        TokenPage page = new TokenPage(browser);
        page.create(userName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.tokenList())
                .as("Token list should contain newly-created token")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(userName));
        });
    }

    @Test
    @Order(30)
    void testEditToken() {
        TokenPage page = new TokenPage(browser);
        String oldToken = page.getToken(userName);
        page.update(userName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.tokenList())
                .as("Token list should contain newly-modified token")
                .extracting(WebElement::getText)
                .isNotEqualTo(oldToken);
        });
    }

    @Test
    @Order(40)
    void testDeleteToken() {
        TokenPage page = new TokenPage(browser);
        page.delete(userName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.tokenList())
                .noneMatch(it -> it.getText().contains(userName));
        });
    }

}
