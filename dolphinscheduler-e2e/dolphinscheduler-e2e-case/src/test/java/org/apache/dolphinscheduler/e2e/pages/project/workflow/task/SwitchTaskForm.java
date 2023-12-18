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

import lombok.Getter;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@Getter
public final class SwitchTaskForm extends TaskNodeForm {

    @FindBy(id = "btnAddIfBranch")
    private WebElement buttonAddBranch;

    @FindBys({
            @FindBy(className = "switch-task"),
            @FindBy(className = "switch-else"),
            @FindBy(className = "el-input__inner")
    })
    private WebElement inputElseBranch;

    public SwitchTaskForm(WorkflowForm parent) {
        super(parent);
    }

    public SwitchTaskForm elseBranch(String elseBranchName) {
        ((JavascriptExecutor)parent().driver()).executeScript("arguments[0].click();", inputElseBranch());

        final By optionsLocator = By.className("option-else-branches");

        new WebDriverWait(parent().driver(), Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(optionsLocator));

        List<WebElement> webElements =  parent().driver().findElements(optionsLocator);
        webElements.stream()
                .filter(it -> it.getText().contains(elseBranchName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such else branch: " + elseBranchName))
                .click();

        inputNodeName().click();

        return this;
    }

    public SwitchTaskForm addIfBranch(String switchScript, String ifBranchName) {
        ((JavascriptExecutor)parent().driver()).executeScript("arguments[0].click();", buttonAddBranch);

        SwitchTaskIfBranch switchTaskIfBranch = new SwitchTaskIfBranch(this);
        switchTaskIfBranch.codeEditor().content(switchScript);

        ((JavascriptExecutor)parent().driver()).executeScript("arguments[0].click();", switchTaskIfBranch.inputIfBranch());

        final By optionsLocator = By.className("option-if-branches");

        new WebDriverWait(parent().driver(), Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(optionsLocator));

        List<WebElement> webElements =  parent().driver().findElements(optionsLocator);
        webElements.stream()
                .filter(it -> it.getText().contains(ifBranchName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such if branch: " + ifBranchName))
                .click();

        inputNodeName().click();
        return this;
    }
}
