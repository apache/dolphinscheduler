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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
public class ResourceE2ETest {
    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
                .login("admin", "dolphinscheduler123")
                .goToNav(ResourcePage.class)
                .goToTab(FileManagePage.class)
        ;
    }

    @Test
    @Order(10)
    void testCreateDirectory() {
        final FileManagePage page = new FileManagePage(browser);
        page.createDirectory("test_directory", "test_desc");

        await().untilAsserted(() -> assertThat(page.tenantList())
                .as("Tenant list should contain newly-created tenant")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(tenant)));
    }
}
