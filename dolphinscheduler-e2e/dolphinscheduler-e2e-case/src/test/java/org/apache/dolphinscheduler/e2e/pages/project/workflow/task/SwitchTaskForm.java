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

package org.apache.dolphinscheduler.e2e.pages.project.workflow.task;

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;

import lombok.Getter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
public final class SwitchTaskForm extends TaskNodeForm {

    static final String NODE_MODAL = "//div[contains(concat(' ', normalize-space(@class), ' '), ' n-modal ')"
            + " and not(contains(@class, 'fade-in-scale-up-transition-enter-active'))"
            + " and not(contains(@class, 'fade-in-scale-up-transition-leave-active'))]";
    static final String CONDITION_FORM = NODE_MODAL
            + "//div[contains(concat(' ', normalize-space(@class), ' '), ' n-form-item ')]"
            + "[./label[normalize-space(.)='Condition']]";

    @FindBy(xpath = CONDITION_FORM + "//button[contains(@class, 'btn-create-custom-parameter')]")
    private WebElement buttonAddBranch;

    @FindBy(xpath = NODE_MODAL
            + "//div[contains(concat(' ', normalize-space(@class), ' '), ' n-form-item ')]"
            + "[./label[normalize-space(.)='Default Branch Flow']]"
            + "//div[contains(concat(' ', normalize-space(@class), ' '), ' n-base-selection ')]")
    private WebElement inputElseBranch;

    public SwitchTaskForm(WorkflowForm parent) {
        super(parent);
    }

    public SwitchTaskForm elseBranch(String elseBranchName) {
        selectBranch(inputElseBranch(), elseBranchName);
        inputNodeName().click();
        return this;
    }

    public SwitchTaskForm addIfBranch(String switchScript, String ifBranchName) {
        WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.elementToBeClickable(buttonAddBranch())).click();
        SwitchTaskIfBranch branch = new SwitchTaskIfBranch(this);
        branch.condition(switchScript);
        selectBranch(branch.inputIfBranch(), ifBranchName);
        inputNodeName().click();
        return this;
    }

    private void selectBranch(WebElement selection, String name) {
        final WebDriver driver = parent().driver();
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(selection)).click();
        WebDriverWaitFactory.createWebDriverWait(driver).until(unused -> driver
                .findElements(By.className("n-base-select-option")).stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .filter(option -> option.getText().trim().equals(name))
                .findFirst().orElse(null)).click();
    }
}
