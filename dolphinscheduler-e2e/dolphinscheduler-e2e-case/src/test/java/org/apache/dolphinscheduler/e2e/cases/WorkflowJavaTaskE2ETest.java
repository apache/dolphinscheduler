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

package org.apache.dolphinscheduler.e2e.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.JavaTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.resource.FileManagePage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.security.EnvironmentPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisableIfTestFails
@Slf4j
public class WorkflowJavaTaskE2ETest {

    private static final String project = "test-workflow";

    private static final String workflow = "test-workflow-1";

    private static final String workflow2 = "test-workflow-2";

    private static final String user = "admin";

    private static final String password = "dolphinscheduler123";

    private static final String email = "admin@gmail.com";

    private static final String phone = "15800000000";

    private static final String tenant = System.getProperty("user.name");

    private static final String environmentName = "JAVA_HOME";

    private static final String environmentConfig = "export JAVA_HOME=${JAVA_HOME:-/opt/java/openjdk}";

    private static final String environmentDesc = "JAVA_HOME_DESC";

    private static final String environmentWorkerGroup = "default";

    private static final String filePath = "/tmp/dolphinscheduler/runner/resources/";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        UserPage userPage = new LoginPage(browser)
                .login(user, password)
                .goToNav(SecurityPage.class)
                .goToTab(TenantPage.class)
                .create(tenant)
                .goToNav(SecurityPage.class)
                .goToTab(EnvironmentPage.class)
                .create(environmentName, environmentConfig, environmentDesc, environmentWorkerGroup)
                .goToNav(SecurityPage.class)
                .goToTab(UserPage.class);

        WebDriverWaitFactory.createWebDriverWait(userPage.driver())
                .until(ExpectedConditions.visibilityOfElementLocated(new By.ByClassName("name")));

        userPage.update(user, user, email, phone, tenant)
                .goToNav(ProjectPage.class)
                .create(project);

        ProjectPage projectPage = new ProjectPage(browser);
        await().untilAsserted(() -> assertThat(projectPage.projectList())
                .as("The project list should include newly created projects.")
                .anyMatch(it -> it.getText().contains(project)));

        getJar();
    }

    @AfterAll
    public static void cleanup() {
        new NavBarPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(project)
                .goToTab(WorkflowDefinitionTab.class)
                .delete(workflow);

        new NavBarPage(browser)
                .goToNav(ProjectPage.class)
                .delete(project);

        browser.navigate().refresh();

        new NavBarPage(browser)
                .goToNav(SecurityPage.class)
                .goToTab(TenantPage.class)
                .delete(tenant);
    }
    private static void createJar(String classFile, String entryName, String mainPackage, String jarName) {

        String classFilePath = "/tmp/dolphinscheduler/runner/resources/" + classFile;

        String jarFilePath = "/tmp/dolphinscheduler/runner/resources/" + jarName;

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainPackage);

        try (
                FileOutputStream fos = new FileOutputStream(jarFilePath);
                JarOutputStream jos = new JarOutputStream(fos, manifest)) {
            Path path = new File(classFilePath).toPath();
            JarEntry entry = new JarEntry(entryName);
            jos.putNextEntry(entry);
            byte[] bytes = Files.readAllBytes(path);
            jos.write(bytes, 0, bytes.length);
            jos.closeEntry();
        } catch (IOException e) {
            log.error("create jar failed:", e);
        }

    }

    private static void getJar() {
        compileJavaFlow();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            log.error("Interrupted :", e);
        }

        /*
         * createJar("Fat.class", "Fat.class", "Fat", "fat.jar"); createJar("Normal1.class", "Normal1.class", "Normal1",
         * "normal1.jar"); createJar("Normal2.class", "Normal2.class", "Normal2", "normal2.jar");
         */

    }

    private static String getJavaPath(String file) {
        URL resource = WorkflowJavaTaskE2ETest.class.getClassLoader().getResource(file);
        if (resource == null) {
            throw new IllegalArgumentException("File not found!");
        } else {
            Path path = null;
            try {
                path = Paths.get(resource.toURI());
            } catch (URISyntaxException e) {
                log.error("URL syntax error:", e);
            }
            String filePath = path.toString();
            return filePath;
        }
    }

    private static void compileJavaFlow() {
        FileManagePage file = new NavBarPage(browser)
                .goToNav(ResourcePage.class)
                .goToTab(FileManagePage.class)
                .uploadFile(getJavaPath("Fat.java"));

        WebDriverWait wait = WebDriverWaitFactory.createWebDriverWait(browser);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Fat.java']")));

        file.uploadFile(getJavaPath("Normal1.java"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Normal1.java']")));

        file.uploadFile(getJavaPath("Normal2.java"));

        ProjectPage projectPage = new NavBarPage(browser)
                .goToNav(ProjectPage.class);

        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(project)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "compile";
        String taskName = "compile";
        String context =
                "$JAVA_HOME/bin/javac Fat.java \n" +
                        "$JAVA_HOME/bin/jar cvf fat.jar Fat.class\n" +
                        "$JAVA_HOME/bin/javac Normal1.java Normal2.java \n" +
                        "$JAVA_HOME/bin/jar cvf normal1.jar Normal1.class\n" +
                        "$JAVA_HOME/bin/javac Normal2.java \n" +
                        "$JAVA_HOME/bin/jar cvf normal2.jar Normal2.class\n"
                        + "mv *.jar /tmp/dolphinscheduler/runner/resources";
        workflowDefinitionPage
                .createWorkflow()
                .<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script(context)
                .selectResource("Fat.java")
                .selectResource("Normal1.java")
                .selectResource("Normal2.java")
                .name(taskName)
                .submit()
                .submit()
                .name(workflowName)
                .submit();

        await().untilAsserted(() -> assertThat(workflowDefinitionPage.workflowList())
                .as("Workflow list should contain newly-created workflow")
                .anyMatch(it -> it.getText().contains(workflowName)));

        workflowDefinitionPage.publish(workflowName);

        final ProjectDetailPage projectDetailPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(project);

        projectDetailPage
                .goToTab(WorkflowInstanceTab.class)
                .deleteAll();
        projectDetailPage
                .goToTab(WorkflowDefinitionTab.class)
                .run(workflowName)
                .submit();

    }

    @Test
    @Order(1)
    void testCreateFatWorkflow() {
        FileManagePage file = new NavBarPage(browser)
                .goToNav(ResourcePage.class)
                .goToTab(FileManagePage.class)
                .uploadFile(filePath + "fat.jar");

        WebDriverWait wait = WebDriverWaitFactory.createWebDriverWait(browser);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='fat.jar']")));

        file.delete("Fat.java").delete("Normal1.java").delete("Normal2.java");

        ProjectPage projectPage = new NavBarPage(browser)
                .goToNav(ProjectPage.class);

        wait.until(ExpectedConditions.visibilityOfAllElements(projectPage.projectList()));

        WorkflowDefinitionTab workflowDefinitionPage = projectPage
                .goTo(project)
                .goToTab(WorkflowDefinitionTab.class);

        WorkflowForm workflow1 = workflowDefinitionPage.createWorkflow();
        workflow1.<JavaTaskForm>addTask(WorkflowForm.TaskType.JAVA)
                .selectRunType("FAT_JAR")
                .selectMainPackage("fat.jar")
                .selectJavaResource("fat.jar")
                .selectJavaResource("fat.jar")
                .name("test-1")
                .selectEnv(environmentName)
                .submit()
                .submit()
                .name(workflow)
                .submit();

        await().untilAsserted(() -> assertThat(workflowDefinitionPage.workflowList())
                .as("Workflow list should contain newly-created workflow")
                .anyMatch(it -> it.getText().contains(workflow)));

        workflowDefinitionPage.publish(workflow);
    }

    @Test
    @Order(30)
    void testRunFatWorkflow() {
        final ProjectDetailPage projectPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(project);

        projectPage
                .goToTab(WorkflowInstanceTab.class)
                .deleteAll();
        projectPage
                .goToTab(WorkflowDefinitionTab.class)
                .run(workflow)
                .submit();

        await()
                .atMost(Duration.ofMinutes(5))
                .untilAsserted(() -> {
                    browser.navigate().refresh();

                    final WorkflowInstanceTab.Row row = projectPage
                            .goToTab(WorkflowInstanceTab.class)
                            .instances()
                            .iterator()
                            .next();

                    assertThat(row.isSuccess()).isTrue();
                    assertThat(row.executionTime()).isEqualTo(1);
                });

    }

    @Test
    @Order(60)
    void testCreateNormalWorkflow() {
        FileManagePage file = new NavBarPage(browser)
                .goToNav(ResourcePage.class)
                .goToTab(FileManagePage.class)
                .uploadFile(filePath + "normal2.jar");

        WebDriverWait wait = WebDriverWaitFactory.createWebDriverWait(browser);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='normal2.jar']")));

        file.uploadFile(filePath + "normal1.jar");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='normal1.jar']")));

        ProjectPage projectPage = new NavBarPage(browser)
                .goToNav(ProjectPage.class);

        wait.until(ExpectedConditions.visibilityOfAllElements(projectPage.projectList()));

        WorkflowDefinitionTab workflowDefinitionPage = projectPage
                .goTo(project)
                .goToTab(WorkflowDefinitionTab.class);

        workflowDefinitionPage.createWorkflow()
                .<JavaTaskForm>addTask(WorkflowForm.TaskType.JAVA)
                .selectRunType("NORMAL_JAR")
                .selectMainPackage("normal1.jar")
                .selectJavaResource("normal1.jar")
                .selectJavaResource("normal1.jar")
                .selectJavaResource("normal2.jar")
                .name("test-2")
                .selectEnv(environmentName)
                .submit()
                .submit()
                .name(workflow2)
                .submit();

        await().untilAsserted(() -> assertThat(workflowDefinitionPage.workflowList())
                .as("Workflow list should contain newly-created workflow")
                .anyMatch(it -> it.getText().contains(workflow2)));

        workflowDefinitionPage.publish(workflow2);
    }

    @Test
    @Order(90)
    void testRunNormalWorkflow() {
        final ProjectDetailPage projectPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(project);

        projectPage
                .goToTab(WorkflowInstanceTab.class)
                .deleteAll();
        projectPage
                .goToTab(WorkflowDefinitionTab.class)
                .run(workflow2)
                .submit();

        await()
                .atMost(Duration.ofMinutes(5))
                .untilAsserted(() -> {
                    browser.navigate().refresh();

                    final WorkflowInstanceTab.Row row = projectPage
                            .goToTab(WorkflowInstanceTab.class)
                            .instances()
                            .iterator()
                            .next();

                    assertThat(row.isSuccess()).isTrue();
                    assertThat(row.executionTime()).isEqualTo(1);
                });

    }

}
