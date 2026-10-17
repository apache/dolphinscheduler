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

package org.apache.dolphinscheduler.e2e.cases.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.cases.workflow.BaseWorkflowE2ETest;
import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.core.WebDriverHolder;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.FlinkStreamTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SeaTunnelTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;
import org.openqa.selenium.StaleElementReferenceException;

@DolphinScheduler(composeFiles = "docker/seatunnel-task/docker-compose.yaml")
@DisableIfTestFails
public class SeaTunnelTaskE2ETest extends BaseWorkflowE2ETest {

    private static final String BATCH_SCRIPT = engineScript("BATCH", "ds-seatunnel-batch-data");

    private static final String STREAM_SCRIPT = engineScript("STREAMING", "ds-seatunnel-stream-data");

    private static final String BATCH_TASK = "st-batch";

    private static final String SOLID = "none";

    private static final String DASHED = "5 5";

    @BeforeAll
    public static void setup() {
        browser = WebDriverHolder.getWebDriver();

        TenantPage tenantPage = new LoginPage(browser)
                .login(adminUser)
                .goToNav(SecurityPage.class)
                .goToTab(TenantPage.class);

        if (tenantPage.tenants().stream().noneMatch(tenant -> tenant.tenantCode().equals(adminUser.getTenant()))) {
            tenantPage
                    .create(adminUser.getTenant())
                    .goToNav(SecurityPage.class)
                    .goToTab(UserPage.class)
                    .update(adminUser);
        }

        tenantPage
                .goToNav(ProjectPage.class)
                .createProjectUntilSuccess(projectName);
    }

    @Test
    @Order(10)
    void testExecutionTypeDefaultsToBatchAndPersists() {
        String workflowName = "SeaTunnelExecutionType";
        String taskName = "st-default";
        WorkflowForm workflowForm = workflowDefinitionTab().createWorkflow().openTaskCategory("di");
        SeaTunnelTaskForm task = workflowForm.addTask(WorkflowForm.TaskType.SEATUNNEL);
        assertThat(task.isExecutionType("BATCH")).isTrue();
        task.script(BATCH_SCRIPT).deployMode("local").name(taskName).submit()
                .formatDAG().confirm()
                .submit()
                .name(workflowName)
                .submit();
        untilWorkflowDefinitionExist(workflowName);

        workflowForm = workflowDefinitionTab().edit(workflowName);
        workflowForm.getTask(taskName);
        new SeaTunnelTaskForm(workflowForm).executionType("STREAM").script(STREAM_SCRIPT).cancel();
        workflowForm.getTask(taskName);
        task = new SeaTunnelTaskForm(workflowForm);
        assertThat(task.isExecutionType("BATCH")).isTrue();
        assertThat(task.script()).isEqualTo(BATCH_SCRIPT);
        task.executionType("STREAM").script(STREAM_SCRIPT).submit()
                .submit()
                .submit();

        workflowForm = workflowDefinitionTab().edit(workflowName);
        workflowForm.getTask(taskName);
        task = new SeaTunnelTaskForm(workflowForm);
        assertThat(task.isExecutionType("STREAM")).isTrue();
        assertThat(task.script()).isEqualTo(STREAM_SCRIPT);
        task.cancel();
    }

    @Test
    @Order(20)
    void testCopiedStreamTaskKeepsExecutionType() {
        String workflowName = "SeaTunnelCopy";
        String taskName = "st-source";
        WorkflowForm workflowForm = workflowDefinitionTab().createWorkflow().openTaskCategory("di");
        workflowForm.<SeaTunnelTaskForm>addTask(WorkflowForm.TaskType.SEATUNNEL)
                .executionType("STREAM")
                .script(STREAM_SCRIPT)
                .deployMode("local")
                .name(taskName)
                .submit()
                .formatDAG().confirm()
                .submit()
                .name(workflowName)
                .submit();
        untilWorkflowDefinitionExist(workflowName);

        workflowForm = workflowDefinitionTab().edit(workflowName);
        String copyName = workflowForm.copyTask(taskName);
        assertThat(copyName).startsWith(taskName + "_");
        workflowForm.formatDAG().confirm()
                .submit()
                .submit();

        workflowForm = workflowDefinitionTab().edit(workflowName);
        for (String name : new String[]{copyName, taskName}) {
            workflowForm.getTask(name);
            SeaTunnelTaskForm task = new SeaTunnelTaskForm(workflowForm);
            assertThat(task.isExecutionType("STREAM")).as("Execution type of %s", name).isTrue();
            assertThat(task.script()).as("Script of %s", name).isEqualTo(STREAM_SCRIPT);
            task.cancel();
        }
    }

    @Test
    @Order(30)
    void testEdgeStyleFollowsExecutionType() {
        String workflowName = "SeaTunnelEdgeStyle";
        String taskName = "st-edge";
        WorkflowForm workflowForm = workflowDefinitionTab().createWorkflow();
        workflowForm.<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script("echo pre-a")
                .name("pre-a")
                .submit()
                .<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script("echo pre-b")
                .name("pre-b")
                .submit()
                .openTaskCategory("di")
                .<SeaTunnelTaskForm>addTask(WorkflowForm.TaskType.SEATUNNEL)
                .script(BATCH_SCRIPT)
                .deployMode("local")
                .preTask("pre-a")
                .preTask("pre-b")
                .name(taskName)
                .submit()
                .formatDAG().confirm();
        untilEdgeStyle(workflowForm, SOLID);

        workflowForm.getTask(taskName);
        new SeaTunnelTaskForm(workflowForm).executionType("STREAM").cancel();
        untilEdgeStyle(workflowForm, SOLID);

        workflowForm.getTask(taskName);
        new SeaTunnelTaskForm(workflowForm).executionType("STREAM").submit();
        untilEdgeStyle(workflowForm, DASHED);
        workflowForm.submit()
                .name(workflowName)
                .submit();
        untilWorkflowDefinitionExist(workflowName);

        workflowForm = workflowDefinitionTab().edit(workflowName);
        untilEdgeStyle(workflowForm, DASHED);
        workflowForm.getTask(taskName);
        new SeaTunnelTaskForm(workflowForm).executionType("BATCH").submit();
        untilEdgeStyle(workflowForm, SOLID);
        workflowForm.submit()
                .submit();

        workflowForm = workflowDefinitionTab().edit(workflowName);
        untilEdgeStyle(workflowForm, SOLID);
        workflowForm.getTask(taskName);
        SeaTunnelTaskForm task = new SeaTunnelTaskForm(workflowForm);
        assertThat(task.isExecutionType("BATCH")).isTrue();
        task.cancel();
    }

    @Test
    @Order(40)
    void testBatchRunIsListedUnderBatchTasks() {
        String workflowName = "SeaTunnelBatchRun";
        String taskName = BATCH_TASK;
        createAndRunWorkflow(workflowName, workflowDefinitionTab().createWorkflow().openTaskCategory("di")
                .<SeaTunnelTaskForm>addTask(WorkflowForm.TaskType.SEATUNNEL)
                .script(BATCH_SCRIPT)
                .deployMode("local")
                .name(taskName)
                .submit());

        untilWorkflowInstanceSuccess(workflowName);
        untilTaskInstanceSuccess(workflowName, taskName);
        TaskInstanceTab taskInstanceTab = projectPage().goToTab(TaskInstanceTab.class);
        // Match the sink rows, not DolphinScheduler's echo of the raw script.
        Pattern firstRow = consoleRow(1, "ds-seatunnel-batch-data");
        Pattern secondRow = consoleRow(2, "ds-seatunnel-batch-data");
        await().ignoreExceptionsInstanceOf(StaleElementReferenceException.class)
                .until(() -> {
                    String log = taskInstanceTab.log(taskName);
                    return firstRow.matcher(log).find() && secondRow.matcher(log).find();
                });
    }

    @Test
    @Order(50)
    void testStreamRunIsListedUnderStreamTasks() {
        String workflowName = "SeaTunnelStreamRun";
        String taskName = "st-stream";
        createAndRunWorkflow(workflowName, workflowDefinitionTab().createWorkflow().openTaskCategory("di")
                .<SeaTunnelTaskForm>addTask(WorkflowForm.TaskType.SEATUNNEL)
                .executionType("STREAM")
                .script(STREAM_SCRIPT)
                .deployMode("local")
                .name(taskName)
                .submit());

        TaskInstanceTab taskInstanceTab = untilStreamTaskInstance(taskName, TaskInstanceTab.Row::isRunning);
        assertThat(taskInstanceTab.isSavepointEnabled(taskName)).isFalse();
        Pattern firstRow = consoleRow(1, "ds-seatunnel-stream-data");
        Pattern secondRow = consoleRow(2, "ds-seatunnel-stream-data");
        await().ignoreExceptionsInstanceOf(StaleElementReferenceException.class)
                .until(() -> {
                    String log = taskInstanceTab.log(taskName);
                    return firstRow.matcher(log).find() && secondRow.matcher(log).find();
                });
        assertThat(taskNames(taskInstanceTab.streamInstances())).contains(taskName).doesNotContain(BATCH_TASK);

        // BATCH_TASK was run by the BATCH scenario above.
        taskInstanceTab.selectType("Batch");
        await().until(() -> taskNames(taskInstanceTab.instances()).contains(BATCH_TASK));
        assertThat(taskNames(taskInstanceTab.instances())).doesNotContain(taskName);

        taskInstanceTab.selectType("Stream").stopStream(taskName);
        untilStreamTaskInstance(taskName, TaskInstanceTab.Row::isStopped);
    }

    @Test
    @Order(60)
    void testFlinkStreamSavepointStaysEnabled() {
        String workflowName = "FlinkStreamRun";
        String taskName = "flink-stream";
        // dml-sync keeps the local-mode job attached to the task, like the SeaTunnel STREAM job.
        String script = "SET 'table.dml-sync' = 'true';\n"
                + "CREATE TABLE src (message STRING) WITH ('connector' = 'datagen', 'rows-per-second' = '1');\n"
                + "CREATE TABLE snk (message STRING) WITH ('connector' = 'print', 'print-identifier' = 'ds-flink');\n"
                + "INSERT INTO snk SELECT message FROM src;";
        createAndRunWorkflow(workflowName, workflowDefinitionTab().createWorkflow()
                .<FlinkStreamTaskForm>addTask(WorkflowForm.TaskType.FLINK_STREAM)
                .programType("SQL")
                .deployMode("local")
                .script(script)
                .name(taskName)
                .submit());

        TaskInstanceTab taskInstanceTab = untilStreamTaskInstance(taskName, TaskInstanceTab.Row::isRunning);
        assertThat(taskInstanceTab.isSavepointEnabled(taskName)).isTrue();
        Pattern printRow = Pattern.compile(Pattern.quote("ds-flink> +I["));
        await().ignoreExceptionsInstanceOf(StaleElementReferenceException.class)
                .until(() -> printRow.matcher(taskInstanceTab.log(taskName)).results().count() >= 2);
        // FlinkStreamTask cancels only jobs whose YARN application ID it finds in the task log. A local-mode
        // job has none, so the job ends when the test container is torn down.
    }

    private void createAndRunWorkflow(String workflowName, WorkflowForm workflowForm) {
        workflowForm
                .submit()
                .name(workflowName)
                .submit();
        untilWorkflowDefinitionExist(workflowName);
        workflowDefinitionTab().publish(workflowName);
        runWorkflow(workflowName);
    }

    private TaskInstanceTab untilStreamTaskInstance(String taskName, Predicate<TaskInstanceTab.Row> state) {
        final ProjectDetailPage projectPage = projectPage();
        return await("Stream task instance should reach the expected state: " + taskName)
                .ignoreExceptionsInstanceOf(StaleElementReferenceException.class)
                .until(() -> {
                    browser.navigate().refresh();
                    TaskInstanceTab taskInstanceTab = projectPage
                            .goToTab(TaskInstanceTab.class)
                            .selectType("Stream");
                    TaskInstanceTab.Row instance = taskInstanceTab.streamInstances()
                            .stream()
                            .filter(it -> it.taskInstanceName().equals(taskName))
                            .findFirst()
                            .orElse(null);
                    if (instance != null && instance.isFailed()) {
                        throw new IllegalStateException("Stream task instance failed: " + taskName);
                    }
                    return instance != null && state.test(instance) ? taskInstanceTab : null;
                }, Objects::nonNull);
    }

    private void untilEdgeStyle(WorkflowForm workflowForm, String style) {
        await().ignoreExceptionsInstanceOf(StaleElementReferenceException.class)
                .untilAsserted(() -> assertThat(workflowForm.edgeDashArrays()).containsExactly(style, style));
    }

    private ProjectDetailPage projectPage() {
        return new ProjectPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(projectName);
    }

    private WorkflowDefinitionTab workflowDefinitionTab() {
        return projectPage().goToTab(WorkflowDefinitionTab.class);
    }

    private static List<String> taskNames(List<TaskInstanceTab.Row> rows) {
        return rows.stream().map(TaskInstanceTab.Row::taskInstanceName).collect(Collectors.toList());
    }

    private static Pattern consoleRow(int index, String marker) {
        return Pattern.compile("(?m)^.*ConsoleSinkWriter.*rowIndex=" + index
                + ":.*SeaTunnelRow#kind=INSERT.*" + Pattern.quote(marker) + ".*$");
    }

    private static String engineScript(String mode, String marker) {
        // In SeaTunnel 2.3.3 FakeSource signals completion only for BATCH.
        // STREAMING emits these four split rows, then stays alive until UI Stop.
        return "env {\n  parallelism = 1\n  job.mode = " + mode + "\n}\n"
                + "source {\n  FakeSource {\n    row.num = 4\n    split.num = 4\n"
                + "    split.read-interval = 1000\n    string.template = [\"" + marker + "\"]\n"
                + "    schema {\n      fields {\n        message = string\n      }\n    }\n  }\n}\n"
                + "sink {\n  Console {}\n}\n";
    }
}
