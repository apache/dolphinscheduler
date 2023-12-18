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

import org.apache.dolphinscheduler.e2e.core.Constants;
import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.resource.FunctionManagePage;
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

@DolphinScheduler(composeFiles = "docker/file-manage/docker-compose.yaml")
public class FunctionManageE2ETest {
    private static RemoteWebDriver browser;

    private static final String tenant = System.getProperty("user.name");

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String email = "admin@gmail.com";

    private static final String phone = "15800000000";

    private static final String testUdfFunctionName = "test_function";

    private static final String testRenameUdfFunctionName = "test_rename_function";

    private static final String testUploadUdfFileName = "hive-jdbc-3.1.2.jar";

    private static final String testClassName = "org.dolphinscheduler.UdfTest";

    private static final String testDescription = "test_description";

    private static final Path testUploadUdfFilePath = Constants.HOST_TMP_PATH.resolve(testUploadUdfFileName);

    @BeforeAll
    @SneakyThrows
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

        downloadFile("https://repo1.maven.org/maven2/org/apache/hive/hive-jdbc/3.1.2/hive-jdbc-3.1.2.jar", testUploadUdfFilePath.toFile().getAbsolutePath());

        UserPage userPage = tenantPage.goToNav(SecurityPage.class)
                .goToTab(UserPage.class);

        new WebDriverWait(userPage.driver(), Duration.ofSeconds(20)).until(ExpectedConditions.visibilityOfElementLocated(
                new By.ByClassName("name")));

        UdfManagePage udfManagePage = userPage.update(user, user, email, phone, tenant)
                .goToNav(ResourcePage.class)
                .goToTab(UdfManagePage.class)
                .uploadFile(testUploadUdfFilePath.toFile().getAbsolutePath());

        udfManagePage.goToNav(ResourcePage.class)
                .goToTab(FunctionManagePage.class);
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

    static void downloadFile(String downloadUrl, String filePath) throws Exception {
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
    @Order(10)
    void testCreateUdfFunction() {
        FunctionManagePage page = new FunctionManagePage(browser);

        page.createUdfFunction(testUdfFunctionName, testClassName, testUploadUdfFileName, testDescription);

        Awaitility.await().untilAsserted(() -> assertThat(page.functionList())
            .as("Function list should contain newly-created file")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(testUdfFunctionName)));
    }

    @Test
    @Order(20)
    void testRenameUdfFunction() {
        FunctionManagePage page = new FunctionManagePage(browser);

        browser.navigate().refresh();

        page.renameUdfFunction(testUdfFunctionName, testRenameUdfFunctionName);

        Awaitility.await().pollDelay(Duration.ofSeconds(2)).untilAsserted(() -> assertThat(page.functionList())
            .as("Function list should contain newly-created file")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(testRenameUdfFunctionName)));
    }

    @Test
    @Order(30)
    void testDeleteUdfFunction() {
        FunctionManagePage page = new FunctionManagePage(browser);

        page.deleteUdfFunction(testRenameUdfFunctionName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                page.functionList()
            ).noneMatch(
                it -> it.getText().contains(testRenameUdfFunctionName)
            );
        });
    }
}
