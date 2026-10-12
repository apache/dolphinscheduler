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
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SeaTunnelTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

@DolphinScheduler(composeFiles = "docker/seatunnel-task/docker-compose.yaml")
class WorkflowSeaTunnelE2ETest {

    private static final String PROJECT = "seatunnel-execution-type";
    private static final String STREAM_MARKER = "ds-seatunnel-stream-data";
    private static final String BATCH_MARKER = "ds-seatunnel-batch-data";
    private static final String ENGINE_PROCESS = "(^|[[:space:]])[o]rg[.]apache[.]seatunnel[.]core[.]starter"
            + "[.]seatunnel[.]SeaTunnelClient([[:space:]]|$)";
    private static final String PID_FILE = "/tmp/ds-seatunnel-e2e-stream.pid";

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
        tenants.goToNav(ProjectPage.class).create(PROJECT);
        WorkflowDefinitionTab definitions = openProject().goToTab(WorkflowDefinitionTab.class);
        WorkflowForm form = definitions.createWorkflow();
        form.<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL, 100, 100)
                .script("echo batch-control\n").name("before").submit();
        browser.findElement(By.cssSelector(".task-cate-di .n-collapse-item__header")).click();

        SeaTunnelTaskForm task = form.addTask(WorkflowForm.TaskType.SEATUNNEL, 350, 100);
        assertThat(task.isExecutionType("BATCH")).isTrue();
        task.script(batchScript).deployMode("local").name("seatunnel").preTask("before").submit();
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
        savedBatch.executionType("STREAM").script(streamScript).cancel();
        form.waitForEdgeStyle(2, false);
        savedBatch = openTask(form, sourceCode);
        assertThat(savedBatch.isExecutionType("BATCH")).isTrue();
        assertThat(savedBatch.script()).isEqualTo(batchScript);
        savedBatch.executionType("STREAM").script(streamScript).submit();
        form.waitForEdgeStyle(2, true);
        form.submit().submit();

        form = reopenWorkflow(workflow);
        form.waitForEdgeStyle(2, true);
        SeaTunnelTaskForm savedStream = openTask(form, sourceCode);
        assertThat(savedStream.isExecutionType("STREAM")).isTrue();
        assertThat(savedStream.script()).isEqualTo(streamScript);
        savedStream.executionType("BATCH").script(batchScript).submit();
        form.waitForEdgeStyle(2, false);
        form.submit().submit();

        form = reopenWorkflow(workflow);
        form.waitForEdgeStyle(2, false);
        SeaTunnelTaskForm edited = openTask(form, sourceCode);
        assertThat(edited.isExecutionType("BATCH")).isTrue();
        edited.executionType("STREAM").script(streamScript).submit();
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
        assertThat(reloadedCopy.script()).isEqualTo(streamScript);
        assertThat(reloadedCopy.deployMode()).isEqualTo("local");
        reloadedCopy.submit();
        SeaTunnelTaskForm reloadedSource = openTask(form, sourceCode);
        assertThat(reloadedSource.isExecutionType("STREAM")).isTrue();
        assertThat(reloadedSource.script()).isEqualTo(streamScript);
        reloadedSource.submit();

        // Copy persistence is checked above. Execute only one local engine at a time.
        createEngineWorkflow("seatunnel-stream-run", "seatunnel-stream", "STREAM", streamScript,
                "before-stream", "set -eu\n"
                        + "test \"$SEATUNNEL_HOME\" = /opt/seatunnel\n"
                        + "test -x \"$SEATUNNEL_HOME/bin/seatunnel.sh\"\n"
                        + "command -v java\ncommand -v pstree\necho seatunnel-fixture-ready\n");
        TaskInstanceTab instances = taskInstances().selectType("Stream");
        waitForInstance(instances, "seatunnel-stream", true, TaskInstanceTab.Row::isRunning);
        assertConsoleOutput(instances, "seatunnel-stream", true, STREAM_MARKER);

        // A successful scheduler task records the actual engine JVM PID and argv.
        // The bracketed main-class pattern cannot match pgrep's own command line.
        createShellWorkflow("seatunnel-process-witness", "stream-process-running",
                "set -eu\ncommand -v pgrep >/dev/null\n"
                        + "test ! -e " + PID_FILE + "\n"
                        + "pids=$(pgrep -f '" + ENGINE_PROCESS + "')\n"
                        + "set -- $pids\ntest \"$#\" -eq 1\npid=$1\n"
                        + "kill -0 \"$pid\"\ntest -r /proc/$pid/cmdline\n"
                        + "args=$(tr '\\0' ' ' < /proc/$pid/cmdline)\n"
                        + "case \"$args\" in *' --deploy-mode local'*) ;; *) exit 1 ;; esac\n"
                        + "case \"$args\" in *' --async'*) exit 1 ;; esac\n"
                        + "printf '%s\\n' \"$args\"\n"
                        + "printf '%s\\n' \"$pid\" > " + PID_FILE + "\n"
                        + "printf 'SEATUNNEL_ENGINE_PID=%s\\n' \"$pid\"\n");
        instances = taskInstances().selectType("Batch");
        waitForInstance(instances, "stream-process-running", false, TaskInstanceTab.Row::isSuccess);
        waitForInstance(instances, "before-stream", false, TaskInstanceTab.Row::isSuccess);
        assertThat(instances.instances().stream().map(TaskInstanceTab.Row::taskInstanceName)
                .collect(Collectors.toList())).doesNotContain("seatunnel-stream");

        instances.selectType("Stream");
        waitForInstance(instances, "seatunnel-stream", true, TaskInstanceTab.Row::isRunning);
        instances.stopStream("seatunnel-stream");
        waitForInstance(instances, "seatunnel-stream", true, TaskInstanceTab.Row::isStopped);

        // Verify the witnessed PID exited before starting the finite BATCH engine.
        // A missing tool or a pgrep error must fail, not masquerade as process absence.
        createEngineWorkflow("seatunnel-batch-run", "seatunnel-batch", "BATCH", batchScript,
                "stream-process-cleanup", "set -eu\ncommand -v pgrep >/dev/null\n"
                        + "test -s " + PID_FILE + "\npid=$(cat " + PID_FILE + ")\n"
                        + "for attempt in $(seq 1 60); do\n"
                        + "  if ! kill -0 \"$pid\" 2>/dev/null && test ! -d /proc/$pid; then\n"
                        + "    if pgrep -f '" + ENGINE_PROCESS + "'; then exit 1; else test \"$?\" -eq 1; fi\n"
                        + "    rm " + PID_FILE + "\n"
                        + "    printf 'SEATUNNEL_ENGINE_EXITED=%s\\n' \"$pid\"\n    exit 0\n  fi\n"
                        + "  sleep 1\ndone\necho 'SeaTunnel engine survived UI Stop' >&2\nexit 1\n");
        instances = taskInstances().selectType("Batch");
        waitForInstance(instances, "stream-process-cleanup", false, TaskInstanceTab.Row::isSuccess);
        waitForInstance(instances, "seatunnel-batch", false, TaskInstanceTab.Row::isSuccess);
        assertConsoleOutput(instances, "seatunnel-batch", false, BATCH_MARKER);
        instances.selectType("Stream");
        waitForInstance(instances, "seatunnel-stream", true, TaskInstanceTab.Row::isStopped);
        List<String> streamNames = instances.streamInstances().stream().map(TaskInstanceTab.Row::taskInstanceName)
                .collect(Collectors.toList());
        assertThat(streamNames).contains("seatunnel-stream").doesNotContain("seatunnel-batch", copyName);
        WorkflowInstanceTab workflows = project().goToTab(WorkflowInstanceTab.class);
        WebDriverWaitFactory.createWebDriverWait(browser, Duration.ofSeconds(120))
                .ignoring(StaleElementReferenceException.class)
                .withMessage("The finite SeaTunnel BATCH workflow must succeed")
                .until(unused -> workflows.instances().stream()
                        .anyMatch(row -> row.workflowInstanceName().startsWith("seatunnel-batch-run")
                                && row.isSuccess()));
        // Testcontainers also tears down this isolated fixture if an assertion fails.
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

    private void createEngineWorkflow(String workflow, String name, String executionType, String script,
                                      String predecessor, String predecessorScript) {
        WorkflowForm form = definitions().createWorkflow();
        form.<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL, 100, 100)
                .script(predecessorScript).name(predecessor).submit();
        browser.findElement(By.cssSelector(".task-cate-di .n-collapse-item__header")).click();
        form.<SeaTunnelTaskForm>addTask(WorkflowForm.TaskType.SEATUNNEL, 350, 100)
                .executionType(executionType).script(script).deployMode("local")
                .name(name).preTask(predecessor).submit();
        form.submit().name(workflow).submit();
        definitions().publish(workflow).run(workflow).submit();
    }

    private void createShellWorkflow(String workflow, String name, String script) {
        WorkflowForm form = definitions().createWorkflow();
        form.<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL, 100, 100)
                .script(script).name(name).submit();
        form.submit().name(workflow).submit();
        definitions().publish(workflow).run(workflow).submit();
    }

    private void waitForInstance(TaskInstanceTab instances, String name, boolean stream,
                                 Predicate<TaskInstanceTab.Row> state) {
        WebDriverWaitFactory.createWebDriverWait(browser, Duration.ofSeconds(120))
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Expected task state was not reached: " + name)
                .until(unused -> {
                    instances.refresh();
                    List<TaskInstanceTab.Row> rows = stream ? instances.streamInstances() : instances.instances();
                    TaskInstanceTab.Row instance = rows.stream()
                            .filter(row -> row.taskInstanceName().equals(name)).findFirst().orElse(null);
                    if (instance == null) {
                        return false;
                    }
                    assertThat(instance.dryRun()).isEqualTo("NO");
                    assertThat(instance.isFailed()).as("Task failed: %s", name).isFalse();
                    if (name.startsWith("seatunnel-")) {
                        assertThat(instance.taskType()).isEqualTo("SEATUNNEL");
                    }
                    return state.test(instance);
                });
    }

    private void assertConsoleOutput(TaskInstanceTab instances, String name, boolean stream, String marker) {
        // Match the sink logger and row indexes, not DolphinScheduler's rawScript echo.
        Pattern first = consoleRow(1, marker);
        Pattern second = consoleRow(2, marker);
        instances.openLog(name, stream);
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
        return project().goToTab(WorkflowDefinitionTab.class);
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
        browser.navigate().refresh();
        return new WorkflowForm(browser);
    }
}
