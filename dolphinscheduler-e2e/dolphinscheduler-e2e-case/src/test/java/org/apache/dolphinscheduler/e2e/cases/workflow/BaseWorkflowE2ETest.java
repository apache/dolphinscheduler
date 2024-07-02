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

package org.apache.dolphinscheduler.e2e.cases.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.models.users.AdminUser;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.remote.RemoteWebDriver;

@Slf4j
public abstract class BaseWorkflowE2ETest {

    protected static final String projectName = UUID.randomUUID().toString();

    protected static final AdminUser adminUser = new AdminUser();

    protected static RemoteWebDriver browser;

    protected void untilWorkflowDefinitionExist(String workflowName) {
        WorkflowDefinitionTab workflowDefinitionPage = new ProjectPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(projectName)
                .goToTab(WorkflowDefinitionTab.class);

        await().untilAsserted(() -> assertThat(workflowDefinitionPage.workflowList())
                .as("Workflow list should contain newly-created workflow: %s", workflowName)
                .anyMatch(
                        it -> it.getText().contains(workflowName)));
    }

    protected void runWorkflow(String workflowName) {
        final ProjectDetailPage projectPage = new ProjectPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(projectName);

        projectPage
                .goToTab(WorkflowDefinitionTab.class)
                .run(workflowName)
                .submit();

    }

    protected WorkflowInstanceTab.Row untilWorkflowInstanceExist(String workflowName) {
        final ProjectDetailPage projectPage = new ProjectPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(projectName);

        return await()
                .until(() -> {
                    browser.navigate().refresh();
                    return projectPage
                            .goToTab(WorkflowInstanceTab.class)
                            .instances()
                            .stream()
                            .filter(it -> it.workflowInstanceName().startsWith(workflowName))
                            .findFirst()
                            .orElse(null);
                }, Objects::nonNull);
    }

    protected WorkflowInstanceTab.Row untilWorkflowInstanceSuccess(String workflowName) {
        final ProjectDetailPage projectPage = new ProjectPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(projectName);
        return await()
                .until(() -> {
                    browser.navigate().refresh();
                    return projectPage
                            .goToTab(WorkflowInstanceTab.class)
                            .instances()
                            .stream()
                            .filter(it -> it.workflowInstanceName().startsWith(workflowName))
                            .filter(WorkflowInstanceTab.Row::isSuccess)
                            .findFirst()
                            .orElse(null);
                }, Objects::nonNull);
    }

    protected WorkflowInstanceTab.Row untilWorkflowInstanceFailed(String workflowName) {
        final ProjectDetailPage projectPage = new ProjectPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(projectName);
        return await()
                .until(() -> {
                    browser.navigate().refresh();
                    List<WorkflowInstanceTab.Row> workflowInstances = projectPage
                            .goToTab(WorkflowInstanceTab.class)
                            .instances()
                            .stream()
                            .filter(it -> it.workflowInstanceName().startsWith(workflowName))
                            .filter(WorkflowInstanceTab.Row::isFailed)
                            .collect(Collectors.toList());
                    if (workflowInstances.isEmpty()) {
                        return null;
                    }
                    if (workflowInstances.size() > 1) {
                        throw new RuntimeException("More than one failed workflow instance found: " +
                                workflowInstances.stream()
                                        .map(WorkflowInstanceTab.Row::workflowInstanceName)
                                        .collect(Collectors.joining(", ")));
                    }
                    return workflowInstances.get(0);
                }, Objects::nonNull);
    }

    protected TaskInstanceTab.Row untilTaskInstanceSuccess(String workflowName, String taskName) {
        final ProjectDetailPage projectPage = new ProjectPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(projectName);
        return await()
                .until(() -> {
                    browser.navigate().refresh();
                    List<TaskInstanceTab.Row> taskInstances = projectPage
                            .goToTab(TaskInstanceTab.class)
                            .instances()
                            .stream()
                            .filter(it -> it.taskInstanceName().startsWith(taskName))
                            .filter(it -> it.workflowInstanceName().startsWith(workflowName))
                            .filter(TaskInstanceTab.Row::isSuccess)
                            .collect(Collectors.toList());

                    if (taskInstances.isEmpty()) {
                        return null;
                    }
                    if (taskInstances.size() > 1) {
                        throw new RuntimeException("More than one failed task instance found: " +
                                taskInstances.stream()
                                        .map(TaskInstanceTab.Row::taskInstanceName).collect(Collectors.joining(", ")));
                    }
                    return taskInstances.get(0);
                }, Objects::nonNull);
    }

    protected TaskInstanceTab.Row untilTaskInstanceFailed(String workflowName, String taskName) {
        final ProjectDetailPage projectPage = new ProjectPage(browser)
                .goToNav(ProjectPage.class)
                .goTo(projectName);
        return await()
                .until(() -> {
                    browser.navigate().refresh();
                    List<TaskInstanceTab.Row> taskInstances = projectPage
                            .goToTab(TaskInstanceTab.class)
                            .instances()
                            .stream()
                            .filter(it -> it.taskInstanceName().startsWith(taskName))
                            .filter(it -> it.workflowInstanceName().startsWith(workflowName))
                            .filter(TaskInstanceTab.Row::isFailed)
                            .collect(Collectors.toList());

                    if (taskInstances.isEmpty()) {
                        return null;
                    }
                    if (taskInstances.size() > 1) {
                        throw new RuntimeException("More than one failed task instance found: " +
                                taskInstances.stream()
                                        .map(TaskInstanceTab.Row::taskInstanceName).collect(Collectors.joining(", ")));
                    }
                    return taskInstances.get(0);
                }, Objects::nonNull);
    }

}
