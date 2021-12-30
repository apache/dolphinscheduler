/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.dolphinscheduler.e2e.pages.project.workflow;

import lombok.Getter;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public final class WorkflowDefinitionTab extends NavBarPage implements ProjectDetailPage.Tab {
    @FindBy(id = "btnCreateProcess")
    private WebElement buttonCreateProcess;
    @FindBy(className = "select-all")
    private WebElement checkBoxSelectAll;
    @FindBy(className = "btn-delete-all")
    private WebElement buttonDeleteAll;
    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;
    @FindBy(className = "items")
    private List<WebElement> workflowList;

    public WorkflowDefinitionTab(RemoteWebDriver driver) {
        super(driver);
    }

    public WorkflowForm createWorkflow() {
        buttonCreateProcess().click();

        return new WorkflowForm(driver);
    }

    public WorkflowDefinitionTab publish(String workflow) {
        workflowList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").equals(workflow))
            .flatMap(it -> it.findElements(By.className("button-publish")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cannot find publish button in workflow definition"))
            .click();

        return this;
    }

    public WorkflowRunDialog run(String workflow) {
        workflowList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").equals(workflow))
            .flatMap(it -> it.findElements(By.className("button-run")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cannot find run button in workflow definition"))
            .click();

        return new WorkflowRunDialog(this);
    }

    public WorkflowDefinitionTab cancelPublishAll() {
        final Supplier<List<WebElement>> cancelButtons = () ->
            workflowList()
                .stream()
                .flatMap(it -> it.findElements(By.className("btn-cancel-publish")).stream())
                .filter(WebElement::isDisplayed)
                .collect(Collectors.toList());

        for (List<WebElement> buttons = cancelButtons.get();
             !buttons.isEmpty();
             buttons = cancelButtons.get()) {
            buttons.forEach(WebElement::click);
            driver().navigate().refresh();
        }

        return this;
    }

    public WorkflowDefinitionTab deleteAll() {
        if (workflowList().isEmpty()) {
            return this;
        }

        checkBoxSelectAll().click();
        buttonDeleteAll().click();
        buttonConfirm()
            .stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No confirm button is displayed"))
            .click();

        return this;
    }
}
