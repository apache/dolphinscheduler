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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.stream.Stream;

@Getter
public final class WorkflowSaveDialog {
    private final WebDriver driver;
    private final WorkflowForm parent;

    @FindBys({
            @FindBy(className = "input-name"),
            @FindBy(tagName = "input")
    })
    private WebElement inputName;

    @FindBy(className = "btn-submit")
    private WebElement buttonSubmit;

    @FindBys({
            @FindBy(className = "input-global-params"),
            @FindBy(tagName = "button"),
    })
    private WebElement buttonGlobalCustomParameters;

    @FindBy(className = "input-global-params")
    private WebElement globalParamsItems;

    public WorkflowSaveDialog(WorkflowForm parent) {
        this.parent = parent;
        this.driver = parent.driver();

        PageFactory.initElements(driver, this);
    }

    public WorkflowSaveDialog name(String name) {
        inputName().sendKeys(name);

        return this;
    }

    public WorkflowSaveDialog addGlobalParam(String key, String value) {
        final int len = globalParamsItems().findElements(By.tagName("input")).size();

        final WebDriver driver = parent().driver();

        if (len == 0) {
            buttonGlobalCustomParameters().click();

            globalParamsItems().findElements(By.tagName("input")).get(0).sendKeys(key);
            globalParamsItems().findElements(By.tagName("input")).get(1).sendKeys(value);
        } else {
            globalParamsItems().findElements(By.tagName("button")).get(len-1).click();

            globalParamsItems().findElements(By.tagName("input")).get(len).sendKeys(key);
            globalParamsItems().findElements(By.tagName("input")).get(len+1).sendKeys(value);
        }

        return this;
    }

    public WorkflowForm submit() {
        buttonSubmit().click();

        return parent;
    }
}
