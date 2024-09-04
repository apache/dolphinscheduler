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
import org.apache.dolphinscheduler.e2e.models.environment.PythonEnvironment;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.PythonTaskForm;
import org.apache.dolphinscheduler.e2e.pages.resource.FileManagePage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.security.EnvironmentPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;

@DolphinScheduler(composeFiles = "docker/python-task/docker-compose.yaml")
@DisableIfTestFails
public class PythonTaskE2ETest extends BaseWorkflowE2ETest {

    private static final PythonEnvironment pythonEnvironment = new PythonEnvironment();

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
                .goToNav(SecurityPage.class)
                .goToTab(EnvironmentPage.class)
                .createEnvironmentUntilSuccess(pythonEnvironment.getEnvironmentName(),
                        pythonEnvironment.getEnvironmentConfig(),
                        pythonEnvironment.getEnvironmentDesc(),
                        pythonEnvironment.getEnvironmentWorkerGroup());

        tenantPage
                .goToNav(ProjectPage.class)
                .createProjectUntilSuccess(projectName);
    }

    @Test
    @Order(10)
    void testRunPythonTasks_SuccessCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        // todo: use yaml to define the workflow
        String workflowName = "PythonSuccessCase";
        String taskName = "PythonSuccessTask";
        String pythonScripts = "print(\"success\")";
        workflowDefinitionPage
                .createWorkflow()
                .<PythonTaskForm>addTask(WorkflowForm.TaskType.PYTHON)
                .script(pythonScripts)
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
    @Order(20)
    void testRunPythonTasks_WorkflowParamsCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        // todo: use yaml to define the workflow
        String workflowName = "PythonWorkflowParamsCase";
        String taskName = "PythonWorkflowParamsTask";
        String pythonScripts = "import sys\n"
                + "\n"
                + "if '${name}' == 'tom':\n"
                + "    print('success')\n"
                + "else:\n"
                + "    sys.exit(2)";
        workflowDefinitionPage
                .createWorkflow()
                .<PythonTaskForm>addTask(WorkflowForm.TaskType.PYTHON)
                .script(pythonScripts)
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
    @Order(30)
    void testRunPythonTasks_LocalParamsCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "PythonLocalParamsCase";
        String taskName = "PythonLocalParamsSuccess";
        String pythonScripts = "import sys\n"
                + "\n"
                + "if '${name}' == 'tom':\n"
                + "    print('success')\n"
                + "else:\n"
                + "    sys.exit(2)";
        workflowDefinitionPage
                .createWorkflow()
                .<PythonTaskForm>addTask(WorkflowForm.TaskType.PYTHON)
                .script(pythonScripts)
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
    @Order(40)
    void testRunPythonTasks_GlobalParamsOverrideLocalParamsCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "PythonLocalParamsOverrideWorkflowParamsCase";
        String taskName = "PythonLocalParamsOverrideWorkflowParamsSuccess";
        String pythonScripts = "import sys\n"
                + "\n"
                + "if '${name}' == 'jerry':\n"
                + "    print('success')\n"
                + "else:\n"
                + "    sys.exit(2)";
        workflowDefinitionPage
                .createWorkflow()
                .<PythonTaskForm>addTask(WorkflowForm.TaskType.PYTHON)
                .script(pythonScripts)
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
    @Order(50)
    void testRunPythonTasks_UsingResourceFile() {
        long current_timestamp = new Date().getTime();
        String testFileName = String.format("echo_%s", current_timestamp);
        new ResourcePage(browser)
                .goToNav(ResourcePage.class)
                .goToTab(FileManagePage.class)
                .createFileUntilSuccess(testFileName, "echo 123");

        final WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "PythonUsingResourceFileWorkflowCase";
        String taskName = "PythonUsingResourceFileSuccessTask";
        String pythonScripts = "import sys\n"
                + "\n"
                + "file_content = \"\"\n"
                + "\n"
                + "with open('${file_name}', 'r', encoding='UTF8') as f:\n"
                + "    file_content = f.read()\n"
                + "\n"
                + "if len(file_content) != 0:\n"
                + "    print(f'file_content: {file_content}')\n"
                + "else:\n"
                + "    sys.exit(2)\n"
                + "    ";
        workflowDefinitionPage
                .createWorkflow()
                .<PythonTaskForm>addTask(WorkflowForm.TaskType.PYTHON)
                .script(pythonScripts)
                .name(taskName)
                .selectResource(testFileName)
                .addParam("file_name", String.format("%s.sh", testFileName))
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
    @Order(60)
    void testRunPythonTasks_FailedCase() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "PythonFailedWorkflowCase";
        String taskName = "PythonFailedTask";
        String pythonScripts = "import sys\n"
                + "sys.exit(1)";
        workflowDefinitionPage
                .createWorkflow()
                .<PythonTaskForm>addTask(WorkflowForm.TaskType.PYTHON)
                .script(pythonScripts)
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
