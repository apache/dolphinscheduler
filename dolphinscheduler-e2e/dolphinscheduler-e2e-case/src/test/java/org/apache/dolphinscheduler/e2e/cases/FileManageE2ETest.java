/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.dolphinscheduler.e2e.cases;


import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.resource.FileManagePage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DolphinScheduler(composeFiles = "docker/file-manage/docker-compose.yaml")
public class FileManageE2ETest {
    private static RemoteWebDriver browser;

    private static final String tenant = System.getProperty("user.name");

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String email = "admin@gmail.com";

    private static final String phone = "15800000000";

    private static final String testDiretoryName = "test_directory";

    @BeforeAll
    public static void setup() {
        TenantPage tenantPage = new LoginPage(browser)
                .login(user, password)
                .create(tenant);

        await().untilAsserted(() -> assertThat(tenantPage.tenantList())
                .as("Tenant list should contain newly-created tenant")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(tenant)));

        tenantPage.goToNav(SecurityPage.class)
            .goToTab(UserPage.class)
            .update(user, user, password, email, phone)
            .goToNav(ResourcePage.class)
            .goToTab(FileManagePage.class);
    }

    @Test
    @Order(10)
    void testCreateDirectory() {
        final FileManagePage page = new FileManagePage(browser);

        page.createDirectory(testDiretoryName, "test_desc");

        await().untilAsserted(() -> assertThat(page.fileList())
                .as("File list should contain newly-created file")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(testDiretoryName)));
    }

    @Test
    @Order(20)
    void testCreateDuplicateDirectory() {
        final FileManagePage page = new FileManagePage(browser);

        page.createDirectory(testDiretoryName, "test_desc");

        await().untilAsserted(() -> assertThat(browser.findElement(By.tagName("body")).getText())
                .contains("resource already exists")
        );

        page.createDirectoryBox().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testDeleteDirectory() {
        final FileManagePage page = new FileManagePage(browser);

        page.delete(testDiretoryName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                    page.fileList()
            ).noneMatch(
                    it -> it.getText().contains(testDiretoryName)
            );
        });
    }
}
