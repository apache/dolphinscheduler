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

package org.apache.dolphinscheduler.e2e.pages.project.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Getter
public final class WorkflowDefinitionTab extends NavBarPage implements ProjectDetailPage.Tab {

    @FindBy(className = "btn-create-workflow")
    private WebElement buttonCreateWorkflow;

    @FindBys({
            @FindBy(className = "btn-selected"),
            @FindBy(className = "n-checkbox"),
    })
    private WebElement checkBoxSelectAll;

    @FindBys({
            @FindBy(className = "btn-delete-all"),
            @FindBy(className = "n-button__content"),
    })
    private WebElement buttonDeleteAll;

    @FindBys({
            @FindBy(className = "n-popconfirm__action"),
            @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    @FindBy(className = "items")
    private List<WebElement> workflowList;

    @FindBy(className = "task-cate-logic")
    private WebElement subProcessList;

    public WorkflowDefinitionTab(RemoteWebDriver driver) {
        super(driver);
    }

    public WorkflowForm createWorkflow() {
        buttonCreateWorkflow().click();

        return new WorkflowForm(driver);
    }

    public WorkflowForm createSubWorkflowTask() {
        buttonCreateWorkflow().click();
        subProcessList().click();

        return new WorkflowForm(driver);
    }

    public WorkflowDefinitionTab publish(String workflow) {
        workflowList()
                .stream()
                .filter(it -> it.findElement(By.className("workflow-name")).getAttribute("innerText").equals(workflow))
                .flatMap(it -> it.findElements(By.className("btn-publish")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find publish button in workflow definition"))
                .click();

        driver.executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    public WorkflowRunDialog run(String workflow) {
        workflowList()
                .stream()
                .filter(it -> it.findElement(By.className("workflow-name")).getAttribute("innerText").equals(workflow))
                .flatMap(it -> it.findElements(By.className("btn-run")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find run button in workflow definition"))
                .click();

        return new WorkflowRunDialog(this);
    }

    public WorkflowDefinitionTab cancelPublishAll() {
        List<WebElement> cancelButtons = workflowList()
                .stream()
                .flatMap(it -> it.findElements(By.className("btn-publish")).stream())
                .filter(WebElement::isDisplayed)
                .collect(Collectors.toList());

        for (WebElement cancelButton : cancelButtons) {
            cancelButton.click();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());
        }

        return this;
    }

    public WorkflowDefinitionTab delete(String workflow) {
        Awaitility.await().untilAsserted(() -> assertThat(workflowList())
                .as("Workflow list should contain newly-created workflow")
                .anyMatch(
                        it -> it.getText().contains(workflow)));

        workflowList()
                .stream()
                .filter(it -> it.findElement(By.className("workflow-name")).getAttribute("innerText").equals(workflow))
                .flatMap(it -> it.findElements(By.className("btn-delete")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find delete button in workflow definition"))
                .click();

        return this;
    }

    public WorkflowDefinitionTab deleteAll() {
        if (workflowList().isEmpty()) {
            return this;
        }

        checkBoxSelectAll().click();
        buttonDeleteAll().click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }
}
