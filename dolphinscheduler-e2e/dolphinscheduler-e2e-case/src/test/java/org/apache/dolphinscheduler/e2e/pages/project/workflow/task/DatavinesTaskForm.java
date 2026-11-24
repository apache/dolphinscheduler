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

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DatavinesTaskForm extends TaskNodeForm {

    private final WebDriver driver;

    public DatavinesTaskForm(WorkflowForm parent) {
        super(parent);

        this.driver = parent.driver();
    }

    public DatavinesTaskForm address(String address) {
        setInput("Datavines Address", address);
        return this;
    }

    public DatavinesTaskForm jobId(String jobId) {
        setInput("Job ID", jobId);
        return this;
    }

    public DatavinesTaskForm token(String token) {
        setInput("Token", token);
        return this;
    }

    private void setInput(String label, String value) {
        By locator = By.xpath(String.format(
                "//div[contains(@class,'n-form-item') and .//span[normalize-space(text())='%s']]//input",
                label));
        WebElement input = WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(locator));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(value);
    }
}
