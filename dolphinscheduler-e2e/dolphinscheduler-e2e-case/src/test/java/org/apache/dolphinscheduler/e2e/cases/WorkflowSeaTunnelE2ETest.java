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

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.models.users.AdminUser;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SeaTunnelTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class WorkflowSeaTunnelE2ETest {

    private static RemoteWebDriver browser;

    @Test
    @Order(1)
    void testExecutionTypeCopyEdgesAndInstanceClassification() {
        String project = "seatunnel-execution-type";
        String workflow = "seatunnel-modes";
        AdminUser admin = new AdminUser();
        TenantPage tenants = new LoginPage(browser).login(admin)
                .goToNav(SecurityPage.class).goToTab(TenantPage.class);
        if (tenants.tenants().stream().noneMatch(tenant -> tenant.tenantCode().equals(admin.getTenant()))) {
            tenants.create(admin.getTenant()).goToNav(SecurityPage.class).goToTab(UserPage.class).update(admin);
        }
        WorkflowDefinitionTab definitions = tenants
                .goToNav(ProjectPage.class)
                .create(project)
                .goTo(project)
                .goToTab(WorkflowDefinitionTab.class);
        WorkflowForm form = definitions.createWorkflow();
        form.<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL, 100, 100)
                .script("echo batch-control\n").name("before").submit();
        browser.findElement(By.cssSelector(".task-cate-di .n-collapse-item__header")).click();

        SeaTunnelTaskForm task = form.addTask(WorkflowForm.TaskType.SEATUNNEL, 350, 100);
        assertThat(task.isExecutionType("BATCH")).isTrue();
        task.name("seatunnel").preTask("before").submit();
        browser.findElement(By.cssSelector(".task-cate-universal .n-collapse-item__header")).click();
        form.<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL, 600, 100)
                .script("echo after\n").name("after").preTask("seatunnel").submit();
        form.waitForEdgeStyle(2, false);
        form.submit().name(workflow).submit();

        form = reopenWorkflow(workflow);
        String sourceCode = form.taskCode("seatunnel");
        form.waitForEdgeStyle(2, false);
        SeaTunnelTaskForm savedBatch = openTask(form, sourceCode);
        assertThat(savedBatch.isExecutionType("BATCH")).isTrue();
        savedBatch.executionType("STREAM").submit();
        form.waitForEdgeStyle(2, true);
        form.submit().submit();

        form = reopenWorkflow(workflow);
        form.waitForEdgeStyle(2, true);
        SeaTunnelTaskForm savedStream = openTask(form, sourceCode);
        assertThat(savedStream.isExecutionType("STREAM")).isTrue();
        savedStream.executionType("BATCH").submit();
        form.waitForEdgeStyle(2, false);
        form.submit().submit();

        form = reopenWorkflow(workflow);
        form.waitForEdgeStyle(2, false);
        SeaTunnelTaskForm edited = openTask(form, sourceCode);
        assertThat(edited.isExecutionType("BATCH")).isTrue();
        edited.executionType("STREAM").submit();
        form.waitForEdgeStyle(2, true);
        String copyCode = form.copyTask(sourceCode);
        assertThat(copyCode).isNotEqualTo(sourceCode);
        SeaTunnelTaskForm copy = openTask(form, copyCode);
        assertThat(copy.isExecutionType("STREAM")).isTrue();
        String copyName = copy.inputNodeName().getAttribute("value");
        assertThat(copyName).startsWith("seatunnel_");
        copy.submit();
        form.submit().submit();

        form = reopenWorkflow(workflow);
        form.waitForEdgeStyle(2, true);
        SeaTunnelTaskForm reloadedCopy = openTask(form, copyCode);
        assertThat(reloadedCopy.isExecutionType("STREAM")).isTrue();
        reloadedCopy.submit();
        SeaTunnelTaskForm reloadedSource = openTask(form, sourceCode);
        assertThat(reloadedSource.isExecutionType("STREAM")).isTrue();
        reloadedSource.submit();

        definitions = new ProjectPage(browser).goToNav(ProjectPage.class).goTo(project)
                .goToTab(WorkflowDefinitionTab.class);
        definitions.publish(workflow).run(workflow).submit();

        // The basic image has no SeaTunnel engine. Run the real scheduler (not dry-run)
        // and verify persisted instance classification, which precedes worker execution.
        // The sample SeaTunnel script and eventual engine result are not tested here.
        TaskInstanceTab instances = new ProjectPage(browser).goToNav(ProjectPage.class).goTo(project)
                .goToTab(TaskInstanceTab.class).selectType("Stream");
        waitForStreamInstance(instances, "seatunnel");
        waitForStreamInstance(instances, copyName);

        instances.selectType("Batch");
        // Positive loaded-table witness: the BATCH predecessor must execute before
        // the source SeaTunnel task can be submitted. Avoid an empty-table false pass.
        WebDriverWaitFactory.createWebDriverWait(browser, Duration.ofSeconds(120)).until(
                unused -> instances.instances().stream().anyMatch(row -> row.taskInstanceName().equals("before")));
        assertThat(instances.instances()).noneMatch(row -> row.taskInstanceName().equals("seatunnel")
                || row.taskInstanceName().equals(copyName));
        // Testcontainers disposes this isolated project's workflow and instances.
    }

    private void waitForStreamInstance(TaskInstanceTab instances, String name) {
        TaskInstanceTab.Row instance = WebDriverWaitFactory.createWebDriverWait(browser, Duration.ofSeconds(120))
                .until(unused -> instances.streamInstances().stream()
                        .filter(row -> row.taskInstanceName().equals(name)).findFirst().orElse(null));
        assertThat(instance.taskType()).isEqualTo("SEATUNNEL");
        assertThat(instance.dryRun()).isEqualTo("NO");
    }

    private SeaTunnelTaskForm openTask(WorkflowForm form, String code) {
        form.openTask(code);
        return new SeaTunnelTaskForm(form);
    }

    private WorkflowForm reopenWorkflow(String workflow) {
        WebDriverWaitFactory.createWebDriverWait(browser).until(ExpectedConditions.visibilityOfElementLocated(
                By.className("btn-create-workflow")));
        Set<String> windows = browser.getWindowHandles();
        WebDriverWaitFactory.createWebDriverWait(browser).until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(@class, 'workflow-name')]//button[normalize-space(.)='" + workflow + "']")))
                .click();
        WebDriverWaitFactory.createWebDriverWait(browser)
                .until(driver -> driver.getWindowHandles().size() > windows.size());
        String detailWindow = browser.getWindowHandles().stream().filter(window -> !windows.contains(window))
                .findFirst().orElseThrow(IllegalStateException::new);
        browser.close();
        browser.switchTo().window(detailWindow);
        browser.navigate().refresh();
        return new WorkflowForm(browser);
    }
}
