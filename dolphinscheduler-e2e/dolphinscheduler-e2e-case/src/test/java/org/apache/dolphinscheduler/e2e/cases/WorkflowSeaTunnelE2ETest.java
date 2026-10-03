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
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SeaTunnelTaskForm;

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
    void testExecutionTypeSurvivesWorkflowSaveAndReload() {
        String project = "seatunnel-execution-type";
        String workflow = "seatunnel-modes";
        WorkflowDefinitionTab definitions = new LoginPage(browser)
                .login("admin", "dolphinscheduler123")
                .goToNav(ProjectPage.class)
                .create(project)
                .goTo(project)
                .goToTab(WorkflowDefinitionTab.class);
        WorkflowForm form = definitions.createWorkflow();
        browser.findElement(By.cssSelector(".task-cate-di .n-collapse-item__header")).click();

        SeaTunnelTaskForm task = form.addTask(WorkflowForm.TaskType.SEATUNNEL);
        assertThat(task.isExecutionType("BATCH")).isTrue();
        task.name("seatunnel").submit();
        form.submit().name(workflow).submit();

        SeaTunnelTaskForm savedBatch = reopenTask(workflow);
        assertThat(savedBatch.isExecutionType("BATCH")).isTrue();
        savedBatch.executionType("STREAM").submit().submit().submit();

        SeaTunnelTaskForm savedStream = reopenTask(workflow);
        assertThat(savedStream.isExecutionType("STREAM")).isTrue();
        savedStream.executionType("BATCH").submit().submit().submit();

        SeaTunnelTaskForm edited = reopenTask(workflow);
        assertThat(edited.isExecutionType("BATCH")).isTrue();
        edited.submit();

        new ProjectPage(browser).goToNav(ProjectPage.class).goTo(project)
                .goToTab(WorkflowDefinitionTab.class).delete(workflow);
        new ProjectPage(browser).goToNav(ProjectPage.class).delete(project);
    }

    private SeaTunnelTaskForm reopenTask(String workflow) {
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
        WorkflowForm saved = new WorkflowForm(browser);
        saved.getTask("seatunnel");
        return new SeaTunnelTaskForm(saved);
    }
}
