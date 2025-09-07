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
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.GrpcTaskForm;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.apache.dolphinscheduler.e2e.pages.security.UserPage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

@DolphinScheduler(composeFiles = "docker/grpc-task/docker-compose.yaml")
@DisableIfTestFails
public class GrpcTaskE2ETest extends BaseWorkflowE2ETest {

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
    void testRunGrpcTasksSuccess() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "GrpcSuccessCase";
        String taskName = "GrpcSuccessTask";
        String endpoint = "greeterServer:50051";
        String protobufDefinition = "syntax = \"proto3\";\n"
                + "package helloworld;\n"
                + "service Greeter {\n"
                + "  rpc SayHello (HelloRequest) returns (HelloReply) {}\n"
                + "}\n"
                + "message HelloRequest {\n"
                + "  string name = 1;\n"
                + "}\n"
                + "message HelloReply {\n"
                + "  string message = 1;\n"
                + "}\n";
        String methodName = "Greeter/SayHello";
        String message = "{" +
                "\"name\":\"DolphinScheduler\"" +
                "}";

        WorkflowForm workflowForm = workflowDefinitionPage
                .createWorkflow()
                .<GrpcTaskForm>addTask(WorkflowForm.TaskType.GRPC)
                .inputUrl(endpoint)
                .inputServiceDefinition(protobufDefinition)
                .inputMethodName(methodName)
                .inputMessage(message)
                .name(taskName)
                .submit();

        workflowForm.submit()
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
    void testRunGrpcTasksFailed() {
        WorkflowDefinitionTab workflowDefinitionPage =
                new ProjectPage(browser)
                        .goToNav(ProjectPage.class)
                        .goTo(projectName)
                        .goToTab(WorkflowDefinitionTab.class);

        String workflowName = "GrpcFailedCase";
        String taskName = "GrpcFailedTask";
        String endpoint = "greeterServer:50051";
        String protobufDefinition = "syntax = \"proto3\";\n"
                + "package helloworldf;\n"
                + "service Greeter {\n"
                + "  rpc SayHellof (HelloRequest) returns (HelloReply) {}\n"
                + "}\n"
                + "message HelloRequest {\n"
                + "  string notname = 1;\n"
                + "}\n"
                + "message HelloReply {\n"
                + "  string message = 1;\n"
                + "}\n";
        String methodName = "Greeter/SayHellof";
        String message = "{" +
                "\"notname\":\"DolphinScheduler\"" +
                "}";

        WorkflowForm workflowForm = workflowDefinitionPage
                .createWorkflow()
                .<GrpcTaskForm>addTask(WorkflowForm.TaskType.GRPC)
                .inputUrl(endpoint)
                .inputServiceDefinition(protobufDefinition)
                .inputMethodName(methodName)
                .inputMessage(message)
                .name(taskName)
                .submit();

        await().untilAsserted(() -> assertThat(browser)
                .as("can not save workflow")
                .matches(it -> {
                    try {
                        it.findElement(By.className("n-modal-mask"));
                    } catch (NoSuchElementException e) {
                        return true;
                    }
                    return false;
                }));

        workflowForm.submit()
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
