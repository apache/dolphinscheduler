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

import org.apache.dolphinscheduler.e2e.cases.workflow.BaseWorkflowE2ETest;
import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.core.WebDriverHolder;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.resource.FileManagePage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junitpioneer.jupiter.DisableIfTestFails;

@TestMethodOrder(MethodOrderer.MethodName.class)
@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisableIfTestFails
public class ShellTaskE2ETest extends BaseWorkflowE2ETest {

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
    void testRunShellTasks_SuccessCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        // todo: use yaml to define the workflow
        String workflowName = "SuccessCase";
        String taskName = "ShellSuccess";
        workflowDefinitionPage
                .createWorkflow()
                .<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script("echo hello world\n")
                .name(taskName)
                .submit()

                .submit()
                .name(workflowName)
                .submit();

        untilWorkflowDefinitionExist(workflowName);

        workflowDefinitionPage.publish(workflowName);

        runWorkflow(workflowName);
        untilWorkflowInstanceExist(workflowName);
        WorkflowInstanceTab.Row workflowInstance = untilWorkflowInstanceSuccess(workflowName);
        assertThat(workflowInstance.executionTime()).isEqualTo(1);

        TaskInstanceTab.Row taskInstance = untilTaskInstanceSuccess(workflowName, taskName);
        assertThat(taskInstance.retryTimes()).isEqualTo(0);
    }

    @Test
    void testRunShellTasks_WorkflowParamsCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        // todo: use yaml to define the workflow
        String workflowName = "WorkflowParamsCase";
        String taskName = "ShellSuccess";
        workflowDefinitionPage
                .createWorkflow()
                .<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script("[ \"${name}\" = \"tom\" ] && echo \"success\" || { echo \"failed\"; exit 1; }")
                .name(taskName)
                .submit()

                .submit()
                .name(workflowName)
                .addGlobalParam("name", "tom")
                .submit();

        untilWorkflowDefinitionExist(workflowName);

        workflowDefinitionPage.publish(workflowName);

        runWorkflow(workflowName);
        untilWorkflowInstanceExist(workflowName);
        WorkflowInstanceTab.Row workflowInstance = untilWorkflowInstanceSuccess(workflowName);
        assertThat(workflowInstance.executionTime()).isEqualTo(1);

        TaskInstanceTab.Row taskInstance = untilTaskInstanceSuccess(workflowName, taskName);
        assertThat(taskInstance.retryTimes()).isEqualTo(0);
    }

    @Test
    void testRunShellTasks_LocalParamsCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "LocalParamsCase";
        String taskName = "ShellSuccess";
        workflowDefinitionPage
                .createWorkflow()
                .<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script("[ \"${name}\" = \"tom\" ] && echo \"success\" || { echo \"failed\"; exit 1; }")
                .name(taskName)
                .addParam("name", "tom")
                .submit()

                .submit()
                .name(workflowName)
                .submit();

        untilWorkflowDefinitionExist(workflowName);

        workflowDefinitionPage.publish(workflowName);

        runWorkflow(workflowName);
        untilWorkflowInstanceExist(workflowName);
        WorkflowInstanceTab.Row workflowInstance = untilWorkflowInstanceSuccess(workflowName);
        assertThat(workflowInstance.executionTime()).isEqualTo(1);

        TaskInstanceTab.Row taskInstance = untilTaskInstanceSuccess(workflowName, taskName);
        assertThat(taskInstance.retryTimes()).isEqualTo(0);
    }

    @Test
    void testRunShellTasks_GlobalParamsOverrideLocalParamsCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "LocalParamsOverrideWorkflowParamsCase";
        String taskName = "ShellSuccess";
        workflowDefinitionPage
                .createWorkflow()
                .<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script("[ \"${name}\" = \"jerry\" ] && echo \"success\" || { echo \"failed\"; exit 1; }")
                .name(taskName)
                .addParam("name", "tom")
                .submit()

                .submit()
                .name(workflowName)
                .addGlobalParam("name", "jerry")
                .submit();

        untilWorkflowDefinitionExist(workflowName);

        workflowDefinitionPage.publish(workflowName);

        runWorkflow(workflowName);
        untilWorkflowInstanceExist(workflowName);
        WorkflowInstanceTab.Row workflowInstance = untilWorkflowInstanceSuccess(workflowName);
        assertThat(workflowInstance.executionTime()).isEqualTo(1);

        TaskInstanceTab.Row taskInstance = untilTaskInstanceSuccess(workflowName, taskName);
        assertThat(taskInstance.retryTimes()).isEqualTo(0);
    }

    @Test
    void testRunShellTasks_UsingResourceFile() {
        String testFileName = "echo";
        new ResourcePage(browser)
                .goToNav(ResourcePage.class)
                .goToTab(FileManagePage.class)
                .createFileUntilSuccess(testFileName, "echo 123");

        final WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "UsingResourceFile";
        String taskName = "ShellSuccess";
        workflowDefinitionPage
                .createWorkflow()
                .<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script("cat " + testFileName + ".sh")
                .name(taskName)
                .selectResource(testFileName)
                .submit()

                .submit()
                .name(workflowName)
                .submit();

        untilWorkflowDefinitionExist(workflowName);

        workflowDefinitionPage.publish(workflowName);

        runWorkflow(workflowName);
        untilWorkflowInstanceExist(workflowName);
        WorkflowInstanceTab.Row workflowInstance = untilWorkflowInstanceSuccess(workflowName);
        assertThat(workflowInstance.executionTime()).isEqualTo(1);

        TaskInstanceTab.Row taskInstance = untilTaskInstanceSuccess(workflowName, taskName);
        assertThat(taskInstance.retryTimes()).isEqualTo(0);
    }

    @Test
    void testRunShellTasks_FailedCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "FailedCase";
        String taskName = "ShellFailed";
        workflowDefinitionPage
                .createWorkflow()
                .<ShellTaskForm>addTask(WorkflowForm.TaskType.SHELL)
                .script("echo 'I am failed'\n exit1\n")
                .name(taskName)
                .submit()

                .submit()
                .name(workflowName)
                .submit();

        untilWorkflowDefinitionExist(workflowName);

        workflowDefinitionPage.publish(workflowName);

        runWorkflow(workflowName);
        untilWorkflowInstanceExist(workflowName);
        WorkflowInstanceTab.Row workflowInstance = untilWorkflowInstanceFailed(workflowName);
        assertThat(workflowInstance.executionTime()).isEqualTo(1);

        TaskInstanceTab.Row taskInstance = untilTaskInstanceFailed(workflowName, taskName);
        assertThat(taskInstance.retryTimes()).isEqualTo(0);
    }

}
