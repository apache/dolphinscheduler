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

package org.apache.dolphinscheduler.e2e.cases.ssl;

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
import org.apache.dolphinscheduler.e2e.pages.security.EnvironmentPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;

@DolphinScheduler(composeFiles = "docker/ssl-task/docker-compose.yml")
@DisableIfTestFails
public class SslPythonTaskE2ETest extends BaseWorkflowE2ETest {

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
        String workflowName = "SslPythonSuccessCase";
        String taskName = "SslPythonSuccessTask";
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

}
