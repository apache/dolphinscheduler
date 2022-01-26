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

import lombok.SneakyThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.core.Constants;
import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.resource.UdfManagePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;

@DolphinScheduler(composeFiles = "docker/file-manage/docker-compose.yaml")
public class UdfManageE2ETest {
    private static RemoteWebDriver browser;

    private static final String tenant = System.getProperty("user.name");

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String email = "admin@gmail.com";

    private static final String phone = "15800000000";

    private static final String testDiretoryName = "test_directory";

    private static final String testRenameDirectoryName = "test_rename_directory";

    private final String testUploadUdfFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("docker/udf-manage/hive-jdbc-3.1.2.jar")).getPath();

    private static final String testUploadUdfFileName = "hive-jdbc-3.1.2.jar";

    private static final String testUploadUdfRenameFileName = "hive-jdbc.jar";

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
            .goToTab(UdfManagePage.class);
    }

    @AfterAll
    @SneakyThrows
    public static void cleanup() {
        Files.walk(Constants.HOST_CHROME_DOWNLOAD_PATH)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    @Test
    @Order(10)
    void testCreateDirectory() {
        final UdfManagePage page = new UdfManagePage(browser);

        page.createDirectory(testDiretoryName, "test_desc");

        await().untilAsserted(() -> assertThat(page.udfList())
            .as("File list should contain newly-created file")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(testDiretoryName)));
    }

    @Test
    @Order(20)
    void testRenameDirectory() {
        final UdfManagePage page = new UdfManagePage(browser);

        page.rename(testDiretoryName, testRenameDirectoryName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.udfList())
                .as("File list should contain newly-created file")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(testRenameDirectoryName));
        });
    }

    @Test
    @Order(30)
    void testDeleteDirectory() {
        final UdfManagePage page = new UdfManagePage(browser);

        page.delete(testRenameDirectoryName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.udfList()
            ).noneMatch(
                it -> it.getText().contains(testRenameDirectoryName)
            );
        });
    }

    @Test
    @Order(40)
    void testUploadUdf() {
        final UdfManagePage page = new UdfManagePage(browser);

        page.uploadFile(testUploadUdfFilePath);

        await().untilAsserted(() -> {
            assertThat(page.udfList())
                .as("File list should contain newly-created file")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(testUploadUdfFileName));
        });
    }

    @Test
    @Order(50)
    void testDownloadUdf() {
        final UdfManagePage page = new UdfManagePage(browser);

        page.downloadFile(testUploadUdfFileName);

        File file = new File(Paths.get(Constants.HOST_CHROME_DOWNLOAD_PATH.toFile().getAbsolutePath(), testUploadUdfFileName).toFile().getAbsolutePath());
        Files.exists()
        await().untilAsserted(() -> {
            assert file.exists();
        });
    }

    @Test
    @Order(60)
    void testRenameUdf() {
        final UdfManagePage page = new UdfManagePage(browser);

        page.rename(testUploadUdfFileName, testUploadUdfRenameFileName);

        await().untilAsserted(() -> {
            assertThat(page.udfList())
                .as("File list should contain newly-created file")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(testUploadUdfRenameFileName));
        });
    }

    @Test
    @Order(70)
    void testDeleteUdf() {
        final UdfManagePage page = new UdfManagePage(browser);

        page.delete(testUploadUdfRenameFileName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.udfList()
            ).noneMatch(
                it -> it.getText().contains(testUploadUdfRenameFileName)
            );
        });
    }
}
