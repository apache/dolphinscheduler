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
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowSaveDialog;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SeaTunnelTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.TaskNodeForm;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

@DolphinScheduler(composeFiles = "docker/seatunnel-task/docker-compose.yaml")
class WorkflowSeaTunnelE2ETest {

    private static final String PROJECT = "seatunnel-execution-type";
    private static final String STREAM_MARKER = "ds-seatunnel-stream-data";
    private static final String BATCH_MARKER = "ds-seatunnel-batch-data";

    private static final String READY_MODAL = ".n-modal:not(.fade-in-scale-up-transition-enter-active)"
            + ":not(.fade-in-scale-up-transition-leave-active)";

    private static RemoteWebDriver browser;

    @Test
    @Order(1)
    void testExecutionTypeCopyEdgesAndEngineLifecycle() {
        String workflow = "seatunnel-modes";
        String batchScript = engineScript("BATCH", BATCH_MARKER);
        String streamScript = engineScript("STREAMING", STREAM_MARKER);
        AdminUser admin = new AdminUser();
        TenantPage tenants = new LoginPage(browser).login(admin)
                .goToNav(SecurityPage.class).goToTab(TenantPage.class);
        if (tenants.tenants().stream().noneMatch(tenant -> tenant.tenantCode().equals(admin.getTenant()))) {
            tenants.create(admin.getTenant()).goToNav(SecurityPage.class).goToTab(UserPage.class).update(admin);
        }
        ProjectPage projects = tenants.goToNav(ProjectPage.class);
        projects.buttonCreateProject().click();
        projects.createProjectForm().inputProjectName().sendKeys(PROJECT);
        projects.createProjectForm().buttonSubmit().sendKeys(Keys.ENTER);
        WorkflowDefinitionTab definitions = openProject().goToTab(WorkflowDefinitionTab.class);
        WorkflowForm form = definitions.createWorkflow();
        submitNode(form.<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL, 100, 100)
                .script("echo batch-control\n").name("before"));
        browser.findElement(By.cssSelector(".task-cate-di .n-collapse-item__header")).click();

        SeaTunnelTaskForm task = form.addTask(WorkflowForm.TaskType.SEATUNNEL, 350, 100);
        assertThat(task.isExecutionType("BATCH")).isTrue();
        submitNode(predecessor(task.script(batchScript).deployMode("local").name("seatunnel"), "before"));
        browser.findElement(By.cssSelector(".task-cate-universal .n-collapse-item__header")).click();
        submitNode(predecessor(form.<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL, 600, 100)
                .script("echo after\n").name("after"), "seatunnel"));
        form.waitForEdgeStyle(2, false);
        saveWorkflow(form, workflow);

        form = reopenWorkflow(workflow);
        String sourceCode = form.taskCode("seatunnel");
        form.waitForEdgeStyle(2, false);
        SeaTunnelTaskForm savedBatch = openTask(form, sourceCode);
        assertThat(savedBatch.isExecutionType("BATCH")).isTrue();
        savedBatch.executionType("STREAM").script(streamScript).cancel();
        form.waitForEdgeStyle(2, false);
        savedBatch = openTask(form, sourceCode);
        assertThat(savedBatch.isExecutionType("BATCH")).isTrue();
        assertThat(savedBatch.script()).isEqualTo(batchScript);
        submitNode(savedBatch.executionType("STREAM").script(streamScript));
        form.waitForEdgeStyle(2, true);
        saveWorkflow(form, null);

        form = reopenWorkflow(workflow);
        form.waitForEdgeStyle(2, true);
        SeaTunnelTaskForm savedStream = openTask(form, sourceCode);
        assertThat(savedStream.isExecutionType("STREAM")).isTrue();
        assertThat(savedStream.script()).isEqualTo(streamScript);
        submitNode(savedStream.executionType("BATCH").script(batchScript));
        form.waitForEdgeStyle(2, false);
        saveWorkflow(form, null);

        form = reopenWorkflow(workflow);
        form.waitForEdgeStyle(2, false);
        SeaTunnelTaskForm edited = openTask(form, sourceCode);
        assertThat(edited.isExecutionType("BATCH")).isTrue();
        submitNode(edited.executionType("STREAM").script(streamScript));
        form.waitForEdgeStyle(2, true);
        String copyCode = form.copyTask(sourceCode);
        assertThat(copyCode).isNotEqualTo(sourceCode);
        SeaTunnelTaskForm copy = openTask(form, copyCode);
        assertThat(copy.isExecutionType("STREAM")).isTrue();
        String copyName = copy.inputNodeName().getAttribute("value");
        assertThat(copyName).startsWith("seatunnel_");
        submitNode(copy);
        saveWorkflow(form, null);

        form = reopenWorkflow(workflow);
        form.waitForEdgeStyle(2, true);
        SeaTunnelTaskForm reloadedCopy = openTask(form, copyCode);
        assertThat(reloadedCopy.isExecutionType("STREAM")).isTrue();
        assertThat(reloadedCopy.script()).isEqualTo(streamScript);
        assertThat(reloadedCopy.deployMode()).isEqualTo("local");
        submitNode(reloadedCopy);
        SeaTunnelTaskForm reloadedSource = openTask(form, sourceCode);
        assertThat(reloadedSource.isExecutionType("STREAM")).isTrue();
        assertThat(reloadedSource.script()).isEqualTo(streamScript);
        submitNode(reloadedSource);

        // Run the finite BATCH job before the STREAM job so only one local engine runs at a time.
        createEngineWorkflow("seatunnel-batch-run", "seatunnel-batch", "BATCH", batchScript);
        TaskInstanceTab instances = taskInstances().selectType("Batch");
        waitForInstance(instances, "seatunnel-batch", false, TaskInstanceTab.Row::isSuccess);
        assertConsoleOutput(instances, "seatunnel-batch", false, BATCH_MARKER);

        createEngineWorkflow("seatunnel-stream-run", "seatunnel-stream", "STREAM", streamScript);
        instances = taskInstances().selectType("Stream");
        waitForInstance(instances, "seatunnel-stream", true, TaskInstanceTab.Row::isRunning);
        assertThat(instances.isSavepointEnabled("seatunnel-stream")).isFalse();
        assertConsoleOutput(instances, "seatunnel-stream", true, STREAM_MARKER);
        List<String> streamNames = instances.streamInstances().stream().map(TaskInstanceTab.Row::taskInstanceName)
                .collect(Collectors.toList());
        assertThat(streamNames).contains("seatunnel-stream").doesNotContain("seatunnel-batch", copyName);

        instances.selectType("Batch");
        waitForInstance(instances, "seatunnel-batch", false, TaskInstanceTab.Row::isSuccess);
        assertThat(instances.instances().stream().map(TaskInstanceTab.Row::taskInstanceName)
                .collect(Collectors.toList())).doesNotContain("seatunnel-stream");

        instances.selectType("Stream");
        instances.stopStream("seatunnel-stream");
        waitForInstance(instances, "seatunnel-stream", true, TaskInstanceTab.Row::isStopped);
    }

    private TaskNodeForm predecessor(TaskNodeForm task, String name) {
        WebDriverWaitFactory.createWebDriverWait(browser).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(READY_MODAL + " .pre-tasks-model .n-base-selection"))).click();
        WebDriverWaitFactory.createWebDriverWait(browser)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .until(unused -> {
                    WebElement option = browser.findElements(
                            By.cssSelector(".n-base-select-menu .n-base-select-option__content")).stream()
                            .filter(WebElement::isDisplayed).filter(WebElement::isEnabled)
                            .filter(element -> element.getText().equals(name)).findFirst().orElse(null);
                    if (option == null) {
                        return false;
                    }
                    option.click();
                    return true;
                });
        task.inputNodeName().click();
        return task;
    }

    private void submitNode(TaskNodeForm task) {
        WebElement name = task.inputNodeName();
        WebDriverWaitFactory.createWebDriverWait(browser).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(READY_MODAL + " .input-node-name input")));
        task.submit();
        WebDriverWaitFactory.createWebDriverWait(browser).until(ExpectedConditions.invisibilityOf(name));
    }

    private void saveWorkflow(WorkflowForm form, String name) {
        WorkflowSaveDialog dialog = form.submit();
        WebDriverWaitFactory.createWebDriverWait(browser).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(READY_MODAL + " .input-name input")));
        if (name != null) {
            dialog.name(name);
        }
        dialog.submit();
    }

    private String engineScript(String mode, String marker) {
        // In SeaTunnel 2.3.3 FakeSource signals completion only for BATCH.
        // STREAMING emits these four split rows, then stays alive until UI Stop.
        return "env {\n  parallelism = 1\n  job.mode = " + mode + "\n}\n"
                + "source {\n  FakeSource {\n    row.num = 4\n    split.num = 4\n"
                + "    split.read-interval = 1000\n    string.template = [\"" + marker + "\"]\n"
                + "    schema {\n      fields {\n        message = string\n      }\n    }\n  }\n}\n"
                + "sink {\n  Console {}\n}\n";
    }

    private void createEngineWorkflow(String workflow, String name, String executionType, String script) {
        WorkflowForm form = definitions().createWorkflow();
        browser.findElement(By.cssSelector(".task-cate-di .n-collapse-item__header")).click();
        submitNode(form.<SeaTunnelTaskForm>addTask(WorkflowForm.TaskType.SEATUNNEL, 350, 100)
                .executionType(executionType).script(script).deployMode("local").name(name));
        saveWorkflow(form, workflow);
        definitions().publish(workflow).run(workflow).submit();
    }

    private void waitForInstance(TaskInstanceTab instances, String name, boolean stream,
                                 Predicate<TaskInstanceTab.Row> state) {
        boolean[] refreshPending = {false};
        WebDriverWaitFactory.createWebDriverWait(browser, Duration.ofSeconds(120))
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Expected task state was not reached: " + name)
                .until(unused -> {
                    if (!refreshPending[0]) {
                        instances.refresh();
                        refreshPending[0] = true;
                    }
                    List<TaskInstanceTab.Row> rows = stream ? instances.streamInstances() : instances.instances();
                    TaskInstanceTab.Row instance = rows.stream()
                            .filter(row -> row.taskInstanceName().equals(name)).findFirst().orElse(null);
                    if (instance != null) {
                        assertThat(instance.dryRun()).isEqualTo("NO");
                        assertThat(instance.isFailed()).as("Task failed: %s", name).isFalse();
                        if (name.startsWith("seatunnel-")) {
                            assertThat(instance.taskType()).isEqualTo("SEATUNNEL");
                        }
                    }
                    boolean reachedState = instance != null && state.test(instance);
                    refreshPending[0] = browser.findElements(By.cssSelector(".n-tab-pane .n-data-table"))
                            .stream().filter(WebElement::isDisplayed)
                            .anyMatch(table -> table.getDomAttribute("class").contains("n-data-table--loading"));
                    return reachedState && !refreshPending[0];
                });
    }

    private void openLog(String name, boolean stream) {
        // These task names belong to this case; locate their operation cell directly.
        String rows = stream
                ? "//div[contains(concat(' ', normalize-space(@class), ' '), ' n-tab-pane ')]"
                        + "//*[contains(concat(' ', normalize-space(@class), ' '), ' n-data-table-tbody ')]"
                        + "//tr[td[@data-col-key='workflowDefinitionName']]"
                : "//*[contains(concat(' ', normalize-space(@class), ' '), ' batch-task-instance-items ')]";
        By operation = By.xpath(rows + "[td[@data-col-key='name' and normalize-space(.)='" + name + "']]"
                + "/td[@data-col-key='operation']");
        ExpectedCondition<WebElement> completion = ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".logModalRef:not(.fade-in-scale-up-transition-enter-active) .n-log"));
        boolean[] clicked = {false};
        WebDriverWaitFactory.createWebDriverWait(browser)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .until(unused -> {
                    if (clicked[0] && completion.apply(browser) != null) {
                        return true;
                    }
                    WebElement button = browser.findElements(operation).stream()
                            .filter(WebElement::isDisplayed)
                            .map(cell -> cell.findElements(By.cssSelector(".n-button")).get(stream ? 2 : 1))
                            .filter(WebElement::isEnabled).findFirst().orElse(null);
                    if (button == null) {
                        return false;
                    }
                    button.click();
                    clicked[0] = true;
                    return completion.apply(browser) != null;
                });
    }

    private void assertConsoleOutput(TaskInstanceTab instances, String name, boolean stream, String marker) {
        // Match the sink logger and row indexes, not DolphinScheduler's rawScript echo.
        Pattern first = consoleRow(1, marker);
        Pattern second = consoleRow(2, marker);
        openLog(name, stream);
        try {
            WebDriverWaitFactory.createWebDriverWait(browser, Duration.ofSeconds(120))
                    .ignoring(StaleElementReferenceException.class)
                    .withMessage("Expected two real ConsoleSinkWriter data rows: " + name)
                    .until(unused -> {
                        String output = instances.logText();
                        if (first.matcher(output).find() && second.matcher(output).find()) {
                            return true;
                        }
                        instances.refreshLog();
                        return false;
                    });
        } finally {
            instances.closeLog();
        }
    }

    private Pattern consoleRow(int index, String marker) {
        return Pattern.compile("(?m)^.*ConsoleSinkWriter.*rowIndex=" + index
                + ":.*SeaTunnelRow#kind=INSERT.*" + Pattern.quote(marker) + ".*$");
    }

    private WorkflowDefinitionTab definitions() {
        ProjectDetailPage page = project();
        return withPageLoadTimeout(Duration.ofSeconds(10), () -> page.goToTab(WorkflowDefinitionTab.class));
    }

    private TaskInstanceTab taskInstances() {
        return project().goToTab(TaskInstanceTab.class);
    }

    private ProjectDetailPage project() {
        new ProjectPage(browser).goToNav(ProjectPage.class);
        return openProject();
    }

    private ProjectDetailPage openProject() {
        By project = By.xpath("//*[contains(@class, 'project-name')]//button[normalize-space(.)='" + PROJECT + "']");
        WebDriverWaitFactory.createWebDriverWait(browser)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .until(driver -> {
                    WebElement button = ExpectedConditions.elementToBeClickable(project).apply(driver);
                    if (button == null) {
                        return false;
                    }
                    button.click();
                    return true;
                });
        return new ProjectDetailPage(browser);
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
        return withPageLoadTimeout(Duration.ofSeconds(10), () -> {
            WebDriverWaitFactory.createWebDriverWait(browser).until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".dag-container")));
            return new WorkflowForm(browser);
        });
    }

    private <T> T withPageLoadTimeout(Duration timeout, Supplier<T> action) {
        Duration pageLoadTimeout = browser.manage().timeouts().getPageLoadTimeout();
        Throwable failure = null;
        try {
            browser.manage().timeouts().pageLoadTimeout(timeout);
            return action.get();
        } catch (RuntimeException | Error error) {
            failure = error;
            throw error;
        } finally {
            try {
                browser.manage().timeouts().pageLoadTimeout(pageLoadTimeout);
            } catch (RuntimeException | Error restoreError) {
                if (failure == null) {
                    throw restoreError;
                }
                failure.addSuppressed(restoreError);
            }
        }
    }
}
