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

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;

import lombok.Getter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
public final class WorkflowRunDialog {

    private final WorkflowDefinitionTab parent;

    @FindBys({
            @FindBy(xpath = "//div[contains(text(), 'Please set the parameters before starting')]/../.."),
            @FindBy(className = "btn-submit")
    })
    private WebElement buttonSubmit;

    public WorkflowRunDialog(WorkflowDefinitionTab parent) {
        this.parent = parent;

        PageFactory.initElements(parent().driver(), this);
    }

    public WorkflowDefinitionTab submit() {
        By runDialogTitleXpath =
                By.xpath(String.format("//*[contains(text(), '%s')]", "Please set the parameters before starting"));
        WebDriverWaitFactory.createWebDriverWait(parent.driver())
                .until(ExpectedConditions.visibilityOfElementLocated(runDialogTitleXpath));
        WebDriverWaitFactory.createWebDriverWait(parent.driver())
                .until(ExpectedConditions.elementToBeClickable(buttonSubmit()));

        buttonSubmit().click();
        WebDriverWaitFactory.createWebDriverWait(parent.driver())
                .until(ExpectedConditions.invisibilityOfElementLocated(runDialogTitleXpath));
        return parent();
    }
}
