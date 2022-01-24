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

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm.TaskType;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab.Row;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SubWorkflowTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SwitchTaskForm;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class WorkflowSwitchE2ETest {
    private static final String project = "test-workflow-1";
    private static final String tenant = System.getProperty("user.name");

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
            .login("admin", "dolphinscheduler123")
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
            .create(tenant)
            .goToNav(ProjectPage.class)
            .create(project)
        ;
    }

    @AfterAll
    public static void cleanup() {
        new NavBarPage(browser)
            .goToNav(ProjectPage.class)
            .goTo(project)
            .goToTab(WorkflowDefinitionTab.class)
            .cancelPublishAll()
            .deleteAll()
        ;
        new NavBarPage(browser)
            .goToNav(ProjectPage.class)
            .delete(project)
            .goToNav(SecurityPage.class)
            .goToTab(TenantPage.class)
            .delete(tenant)
        ;
    }

    @Test
    @Order(1)
    void testCreateWorkflow() {
        final String workflow = "test-workflow-1";

        final WorkflowDefinitionTab workflowDefinitionPage =
            new ProjectPage(browser)
                .goTo(project)
                .goToTab(WorkflowDefinitionTab.class);

        workflowDefinitionPage
            .createWorkflow()

            .<ShellTaskForm> addTask(TaskType.SHELL)
            .script("echo ${today}\necho ${global_param}\n")
            .name("test-1")
            .addParam("today", "${system.datetime}")
            .submit()

            .<SwitchTaskForm> addTask(TaskType.SWITCH)
            .script("")
            .name("switch")
            .submit()

            .<ShellTaskForm>addTask(TaskType.SHELL)
            .script("echo success")
            .name("success")
            .submit()

            .<ShellTaskForm>addTask(TaskType.SHELL)
            .script("echo fail")
            .name("fail")
            .submit()

            .submit()
            .name(workflow)
            .tenant(tenant)
            .addGlobalParam("global_param", "hello world")
            .submit()
        ;

        await().untilAsserted(() -> assertThat(
            workflowDefinitionPage.workflowList()
        ).anyMatch(it -> it.getText().contains(workflow)));

        workflowDefinitionPage.publish(workflow);
    }

    @Test
    @Order(30)
    void testRunWorkflow() {
        final String workflow = "test-workflow-1";

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

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            final Row row = projectPage
                .goToTab(WorkflowInstanceTab.class)
                .instances()
                .iterator()
                .next();

            assertThat(row.isSuccess()).isTrue();
            assertThat(row.executionTime()).isEqualTo(1);
        });

        // Test rerun
        projectPage
            .goToTab(WorkflowInstanceTab.class)
            .instances()
            .stream()
            .filter(it -> it.rerunButton().isDisplayed())
            .iterator()
            .next()
            .rerun();

        await().untilAsserted(() -> {
            browser.navigate().refresh();

            final Row row = projectPage
                .goToTab(WorkflowInstanceTab.class)
                .instances()
                .iterator()
                .next();

            assertThat(row.isSuccess()).isTrue();
            assertThat(row.executionTime()).isEqualTo(2);
        });
    }
}
