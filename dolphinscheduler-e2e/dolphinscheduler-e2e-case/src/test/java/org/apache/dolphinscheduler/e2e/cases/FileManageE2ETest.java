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


import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.core.Constants;
import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.resource.FileManagePage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import lombok.SneakyThrows;

@DolphinScheduler(composeFiles = "docker/file-manage/docker-compose.yaml")
public class FileManageE2ETest {
    private static RemoteWebDriver browser;

    private static final String tenant = System.getProperty("user.name");

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String email = "admin@gmail.com";

    private static final String phone = "15800000000";

    private static final String testDiretoryName = "test_directory";

    private static final String testSubDirectoryName = "test_sub_directory";

    private static final String testRenameDirectoryName = "test_rename_directory";

    private static final String testFileName = "test_file";

    private static final String testRenameFileName = "test_rename_file.sh";

    private static final String testUnder1GBFileName = "test_file_0.01G";

    private static final Path testOver1GBFilePath = Constants.HOST_TMP_PATH.resolve("test_file_1.5G");

    private static final Path testUnder1GBFilePath = Constants.HOST_TMP_PATH.resolve(testUnder1GBFileName);

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

    @AfterAll
    @SneakyThrows
    public static void cleanup() {
        Files.deleteIfExists(testUnder1GBFilePath);
        Files.deleteIfExists(testOver1GBFilePath);
        Files.walk(Constants.HOST_CHROME_DOWNLOAD_PATH)
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);
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
    @Order(11)
    void testCancelCreateDirectory() {
        final FileManagePage page = new FileManagePage(browser);

        page.cancelCreateDirectory(testDiretoryName, "test_desc");

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
    @Order(21)
    void testCreateSubDirectory() {
        final FileManagePage page = new FileManagePage(browser);

        page.createSubDirectory(testDiretoryName, testSubDirectoryName, "test_desc");

        await().untilAsserted(() -> assertThat(page.fileList())
            .as("File list should contain newly-created file")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(testSubDirectoryName)));
    }

    @Test
    @Order(22)
    void testRenameDirectory() {
        final FileManagePage page = new FileManagePage(browser);

        page.rename(testSubDirectoryName, testRenameDirectoryName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.fileList())
                .as("File list should contain newly-created file")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(testRenameDirectoryName));
        });
    }

    @Test
    @Order(30)
    void testDeleteDirectory() {
        final FileManagePage page = new FileManagePage(browser);

        page.goToNav(ResourcePage.class)
            .goToTab(FileManagePage.class)
            .delete(testDiretoryName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                    page.fileList()
            ).noneMatch(
                    it -> it.getText().contains(testDiretoryName)
            );
        });
    }

    @Test
    @Order(40)
    void testCreateFile() {
        final FileManagePage page = new FileManagePage(browser);
        String scripts = "echo 123";

        page.createFile(testFileName, scripts);

        await().untilAsserted(() -> assertThat(page.fileList())
            .as("File list should contain newly-created file")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(testFileName)));
    }

    @Test
    @Order(41)
    void testRenameFile() {
        final FileManagePage page = new FileManagePage(browser);

        page.rename(testFileName, testRenameFileName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(page.fileList())
                .as("File list should contain newly-created file")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(testRenameFileName));
        });
    }

    @Test
    @Order(42)
    void testEditFile() {
        final FileManagePage page = new FileManagePage(browser);
        String scripts = "echo 456";

        page.editFile(testRenameFileName, scripts);

        await().untilAsserted(() -> assertThat(page.fileList())
            .as("File list should contain newly-created file")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(testRenameFileName)));
    }

    @Test
    @Order(45)
    void testDeleteFile() {
        final FileManagePage page = new FileManagePage(browser);

        page.delete(testRenameFileName);

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.fileList()
            ).noneMatch(
                it -> it.getText().contains(testRenameFileName)
            );
        });
    }

    @Test
    @Order(60)
    void testUploadOver1GBFile() throws IOException {
        final FileManagePage page = new FileManagePage(browser);

        RandomAccessFile file = new RandomAccessFile(testOver1GBFilePath.toFile(), "rw");
        file.setLength((long) (1.5 * 1024 * 1024 * 1024));

        page.uploadFile(testOver1GBFilePath.toFile().getAbsolutePath());

        await().untilAsserted(() ->
            assertThat(browser.findElement(By.tagName("body")).getText())
                .contains("Upload File size cannot exceed 1g")
        );
    }

    @Test
    @Order(65)
    void testUploadUnder1GBFile() throws IOException {
        final FileManagePage page = new FileManagePage(browser);

        browser.navigate().refresh();

        RandomAccessFile file = new RandomAccessFile(testUnder1GBFilePath.toFile(), "rw");
        file.setLength((long) (0.01 * 1024 * 1024 * 1024));

        page.uploadFile(testUnder1GBFilePath.toFile().getAbsolutePath());

        await().untilAsserted(() -> {
            assertThat(page.fileList())
                .as("File list should contain newly-created file")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(testUnder1GBFileName));
        });
    }

    @Test
    @Order(70)
    void testDownloadFile() {
        final FileManagePage page = new FileManagePage(browser);

        page.downloadFile(testUnder1GBFileName);

        File file = Constants.HOST_CHROME_DOWNLOAD_PATH.resolve(testUnder1GBFileName).toFile();

        await().untilAsserted(() -> {
            assert file.exists();
        });
    }
}
