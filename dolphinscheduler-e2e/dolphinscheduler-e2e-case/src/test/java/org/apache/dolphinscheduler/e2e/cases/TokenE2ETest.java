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
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
public class TokenE2ETest {

    private static String token = "";

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
        token = page.create().toString();
        new WebDriverWait(page.driver(), 10).until(ExpectedConditions.visibilityOfElementLocated(new ById("dialogGenerateToken")));

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.tokenList())
                    .as("Token list should contain newly-created token")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(token));
        });
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
