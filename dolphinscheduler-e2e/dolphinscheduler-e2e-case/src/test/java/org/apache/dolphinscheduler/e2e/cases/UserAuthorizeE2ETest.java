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

import lombok.SneakyThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.core.Constants;
import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.datasource.DataSourcePage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.resource.FileManagePage;
import org.apache.dolphinscheduler.e2e.pages.resource.FunctionManagePage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.resource.UdfManagePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@DolphinScheduler(composeFiles = "docker/datasource-mysql/docker-compose.yaml")
public class UserAuthorizeE2ETest {
    private static final String tenant = System.getProperty("user.name");
    private static final String admin = "admin";
    private static final String adminPassword = "dolphinscheduler123";
    private static final String adminEmail = "admin@gmail.com";
    private static final String adminPhone = "15800000000";

    private static final String user = "test_user";
    private static final String password = "test_user123";
    private static final String email = "test_user@gmail.com";
    private static final String phone = "15800000000";

    private static final String project = "test_project";

    private static final String dataSourceType = "MYSQL";
    private static final String dataSourceName = "mysql_test";
    private static final String dataSourceDescription = "mysql_test";
    private static final String ip = "mysql";
    private static final String port = "3306";
    private static final String userName = "root";
    private static final String mysqlPassword = "123456";
    private static final String database = "mysql";
    private static final String jdbcParams = "{\"useSSL\": false}";
    private static final String fileName = "test_file";
    private static final String fileScripts = "echo 123";
    private static final String uploadUdfFileName = "hive-jdbc-3.1.2.jar";
    private static final String udfFunctionName = "test_udfFunction";
    private static final String udfClassName = "org.dolphinscheduler.UdfTest";
    private static final String udfDescription = "test_udfDescription";
    private static final Path uploadUdfFilePath = Constants.HOST_TMP_PATH.resolve(uploadUdfFileName);

    private static RemoteWebDriver browser;


    @BeforeAll
    @SneakyThrows
    public static void setup() {
        TenantPage tenantPage = new LoginPage(browser)
            .login(admin, adminPassword)
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
            .create(tenant);

        await().untilAsserted(() -> assertThat(tenantPage.tenantList())
            .as("Tenant list should contain newly-created tenant")
            .extracting(WebElement::getText)
            .anyMatch(it -> it.contains(tenant)));

        downloadFile("https://repo1.maven.org/maven2/org/apache/hive/hive-jdbc/3.1.2/hive-jdbc-3.1.2.jar", uploadUdfFilePath.toFile().getAbsolutePath());

        tenantPage.goToNav(SecurityPage.class)
            .goToTab(UserPage.class)
            .create(user, password, email, phone)
            .update(admin, admin, adminPassword, adminEmail, adminPhone)
            .goToNav(ProjectPage.class).create(project)
            .goToNav(DataSourcePage.class)
            .createDataSource(dataSourceType, dataSourceName, dataSourceDescription, ip, port, userName, mysqlPassword, database, jdbcParams)
            .goToNav(ResourcePage.class)
            .goToTab(FileManagePage.class).createFile(fileName, fileScripts)
            .goToNav(ResourcePage.class)
            .goToTab(UdfManagePage.class)
            .uploadFile(uploadUdfFilePath.toFile().getAbsolutePath());

        new WebDriverWait(browser, 10).until(ExpectedConditions.invisibilityOfElementLocated(By.id("fileUpdateDialog")));

        tenantPage.goToNav(ResourcePage.class)
            .goToTab(FunctionManagePage.class)
            .createUdfFunction(udfFunctionName, udfClassName, uploadUdfFileName, udfDescription);

    }

    static void downloadFile(String downloadUrl, String filePath) throws Exception {
        int byteRead;

        URL url = new URL(downloadUrl);

        URLConnection conn = url.openConnection();
        InputStream inputStream = conn.getInputStream();
        FileOutputStream fs = new FileOutputStream(filePath);

        byte[] buffer = new byte[1024];
        while ((byteRead = inputStream.read(buffer)) != -1) {
            fs.write(buffer, 0, byteRead);
        }

        inputStream.close();
        fs.close();
    }

    @AfterAll
    public static void cleanup() {
        new NavBarPage(browser)
            .goToNav(ProjectPage.class)
            .delete(project)
            .goToNav(DataSourcePage.class)
            .delete(dataSourceName)
            .goToNav(SecurityPage.class)
            .goToTab(UserPage.class)
            .delete(user)
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
            .delete(tenant);
    }

    @Test
    @Order(10)
    void testAuthorizeProject() {
        final UserPage page = new UserPage(browser);

        page.authorizeProject(user, project);
        page.clickAuthorize(user);

        await().untilAsserted(() -> {

            assertThat(page.selectedList())
                .as("Selected project list should contain newly-authorized project")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(project));
        });

        page.closeAuthorize();
    }

    @Test
    @Order(20)
    void testAuthorizeDataSource() {
        final UserPage page = new UserPage(browser);

        page.authorizeDataSource(user, dataSourceName);
        page.clickAuthorize(user);

        await().untilAsserted(() -> {

            assertThat(page.selectedList())
                .as("Selected dataSource list should contain newly-authorized dataSource")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(dataSourceName));
        });

        page.closeAuthorize();
    }

    @Test
    @Order(30)
    void testAuthorizeUdfFunction() {
        final UserPage page = new UserPage(browser);

        page.authorizeUdfFunction(user, udfFunctionName);
        page.clickAuthorize(user);

        await().untilAsserted(() -> {

            assertThat(page.selectedList())
                .as("Selected udfFunction list should contain newly-authorized udfFunction")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(udfClassName));
        });
    }

}

