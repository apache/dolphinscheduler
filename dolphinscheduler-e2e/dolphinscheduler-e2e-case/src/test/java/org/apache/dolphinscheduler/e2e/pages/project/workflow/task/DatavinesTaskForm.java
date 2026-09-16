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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public final class DatavinesTaskForm extends TaskNodeForm {

    private final WebDriver driver;

    @FindBys({
            @FindBy(className = "input-datavines-address"),
            @FindBy(tagName = "input")
    })
    private WebElement addressInput;

    @FindBys({
            @FindBy(className = "input-datavines-job-id"),
            @FindBy(tagName = "input")
    })
    private WebElement jobIdInput;

    @FindBys({
            @FindBy(className = "input-datavines-token"),
            @FindBy(tagName = "input")
    })
    private WebElement tokenInput;

    public DatavinesTaskForm(WorkflowForm parent) {
        super(parent);
        this.driver = parent.driver();
        PageFactory.initElements(driver, this);
    }

    public DatavinesTaskForm address(String address) {
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(addressInput));
        addressInput.sendKeys(address);
        return this;
    }

    public DatavinesTaskForm jobId(String jobId) {
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(jobIdInput));
        jobIdInput.sendKeys(jobId);
        return this;
    }

    public DatavinesTaskForm token(String token) {
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(tokenInput));
        tokenInput.sendKeys(token);
        return this;
    }
}
