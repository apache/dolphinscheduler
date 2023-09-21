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

import org.apache.dolphinscheduler.e2e.core.Constants;
import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.resource.UdfManagePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;

import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.SneakyThrows;

@DolphinScheduler(composeFiles = "docker/file-manage/docker-compose.yaml")
public class UdfManageE2ETest {
    private static RemoteWebDriver browser;

    private static final String tenant = System.getProperty("user.name");

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String email = "admin@gmail.com";

    private static final String phone = "15800000000";

    private static final String testDirectoryName = "test_directory";

    private static final String testRenameDirectoryName = "test_rename_directory";

    private static final String testUploadUdfFileName = "hive-jdbc-3.1.2.jar";

    private static final Path testUploadUdfFilePath = Constants.HOST_TMP_PATH.resolve(testUploadUdfFileName);

    private static final String testUploadUdfRenameFileName = "hive-jdbc.jar";

    @BeforeAll
    public static void setup() {
        TenantPage tenantPage = new LoginPage(browser)
            .login(user, password)
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
            .create(tenant);

        Awaitility.await().untilAsserted(() -> assertThat(tenantPage.tenantList())
            .as("Tenant list should contain newly-created tenant")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(tenant)));

        UserPage userPage = tenantPage.goToNav(SecurityPage.class)
            .goToTab(UserPage.class);

        new WebDriverWait(userPage.driver(), Duration.ofSeconds(20)).until(ExpectedConditions.visibilityOfElementLocated(
                new By.ByClassName("name")));

        userPage.update(user, user, email, phone, tenant)
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

        Files.deleteIfExists(testUploadUdfFilePath);
    }

    @Test
    @Order(10)
    void testCreateDirectory() {
        final UdfManagePage page = new UdfManagePage(browser);

        new WebDriverWait(page.driver(), Duration.ofSeconds(20))
            .until(ExpectedConditions.urlContains("/resource-manage"));
        page.createDirectory(testDirectoryName);
        Awaitility.await().untilAsserted(() -> assertThat(page.udfList())
            .as("File list should contain newly-created file")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(testDirectoryName)));
    }

//when s3  the directory cannot be renamed
//    @Test
//    @Order(20)
//    void testRenameDirectory() {
//        final UdfManagePage page = new UdfManagePage(browser);
//
//        page.rename(testDirectoryName, testRenameDirectoryName);
//
//        await().untilAsserted(() -> {
//            browser.navigate().refresh();
//
//            assertThat(page.udfList())
//                .as("File list should contain newly-created file")
//                .extracting(WebElement::getText)
//                .anyMatch(it -> it.contains(testRenameDirectoryName));
//        });
//    }

    @Test
    @Order(30)
    void testDeleteDirectory() {
        final UdfManagePage page = new UdfManagePage(browser);
        page.delete(testDirectoryName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.udfList()
            ).noneMatch(
                it -> it.getText().contains(testDirectoryName)
            );
        });
    }

    @Test
    @Order(40)
    @SneakyThrows
    void testUploadUdf() {
        final UdfManagePage page = new UdfManagePage(browser);

        downloadFile("https://repo1.maven.org/maven2/org/apache/hive/hive-jdbc/3.1.2/hive-jdbc-3.1.2.jar", testUploadUdfFilePath.toFile().getAbsolutePath());
        page.uploadFile(testUploadUdfFilePath.toFile().getAbsolutePath());
        Awaitility.await().untilAsserted(() -> {
            assertThat(page.udfList())
                .as("File list should contain newly-created file")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(testUploadUdfFileName));
        });
    }

    void downloadFile(String downloadUrl, String filePath) throws Exception {
        int byteRead;

        URL url = new URL(downloadUrl);

        URLConnection conn = url.openConnection();
        InputStream inStream = conn.getInputStream();
        FileOutputStream fs = new FileOutputStream(filePath);

        byte[] buffer = new byte[1024];
        while ((byteRead = inStream.read(buffer)) != -1) {
            fs.write(buffer, 0, byteRead);
        }

        inStream.close();
        fs.close();
    }

    @Test
    @Order(60)
    void testRenameUdf() {
        final UdfManagePage page = new UdfManagePage(browser);
        page.rename(testUploadUdfFileName, testUploadUdfRenameFileName);

        Awaitility.await().untilAsserted(() -> {
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

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.udfList()
            ).noneMatch(
                it -> it.getText().contains(testUploadUdfRenameFileName)
            );
        });
    }
}
