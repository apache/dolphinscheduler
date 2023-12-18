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
package org.apache.dolphinscheduler.e2e.pages.project.workflow.task;

import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@Getter
public final class SubWorkflowTaskForm extends TaskNodeForm {
    @FindBys({
            @FindBy(className = "select-child-node"),
            @FindBy(className = "n-base-selection"),
    })
    private WebElement btnSelectChildNodeDropdown;

    @FindBy(className = "n-base-select-option__content")
    private List<WebElement> selectChildNode;

    private WebDriver driver;


    public SubWorkflowTaskForm(WorkflowForm parent) {
        super(parent);

        this.driver = parent.driver();
    }

    public SubWorkflowTaskForm childNode(String node) {
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(btnSelectChildNodeDropdown));
        
        btnSelectChildNodeDropdown().click();

        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfElementLocated(By.className(
                "n-base-select-option__content")));

        selectChildNode()
                .stream()
                .filter(it -> it.getText().contains(node))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("No %s in child node dropdown list", node)))
                .click();

        return this;
    }
}
