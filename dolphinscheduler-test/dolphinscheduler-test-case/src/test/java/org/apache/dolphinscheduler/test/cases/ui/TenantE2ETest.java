/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.dolphinscheduler.test.cases.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.dolphinscheduler.test.cases.common.AbstractTenantApiTest;
import org.apache.dolphinscheduler.test.core.Browser;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.test.pages.security.tenantManage.TenantManagePage;
import org.apache.dolphinscheduler.test.pages.login.LoginPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;


import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@DisplayName("Tenant E2E test")
public class TenantE2ETest extends AbstractTenantApiTest {
    private static final String tenant = System.getProperty("user.name");
    private static final Logger log = getLogger(TenantE2ETest.class);
    public static WebDriver driver;
    public static Browser browser;
    public static TenantManagePage page;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        browser = new Browser(driver, "http://localhost:3000/login");
        browser.go();
        try {
            page = browser.
                    toPage(LoginPage.class).
                    loginAs().
                    toSecurityTab().
                    toTenantManage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @AfterAll
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testCreateTenant() throws Exception {
        tenantRequestEntity = new TenantRequestEntity();
        tenantRequestEntity.setTenantCode(fairy.person().getUsername());
        tenantRequestEntity.setQueueId(1);
        tenantRequestEntity.setDescription(fairy.person().getFullName());

        page.create(tenantRequestEntity);

        await().untilAsserted(() -> assertThat(page.getTenantList())
                .as("Tenant list should contain newly-created tenant")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(tenantRequestEntity.getTenantCode())));
    }

    @Test
    @Order(2)
    void testUpdateTenant() {
        page.update(tenantRequestEntity.getTenantCode(), tenantRequestEntity.getDescription());

        await().untilAsserted(() -> {
            page.withRefresh();
            assertThat(page.getTenantList())
                    .as("Tenant list should contain newly-modified tenant")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(tenantRequestEntity.getTenantCode()));
        });
    }

    @Test
    @Order(3)
    void testDeleteTenant() throws Exception {
        page.delete(tenantRequestEntity.getTenantCode());

        await().untilAsserted(() -> {
            page.withRefresh();
            assertThat(
                    page.getTenantList()
            ).noneMatch(
                    it -> it.getText().contains(tenantRequestEntity.getTenantCode())
            );
        });
    }

    @Test
    @Order(4)
    void testCreateDuplicateTenant() throws Exception {
        testCreateTenant();
        page.create(tenantRequestEntity.getTenantCode());
        await().untilAsserted(() ->
                assertThat(page.withDialog(By.tagName("body")).getText())
                        .contains("already exists")
        );

        page.getTenantForm().getButtonCancel().click();

    }

}
