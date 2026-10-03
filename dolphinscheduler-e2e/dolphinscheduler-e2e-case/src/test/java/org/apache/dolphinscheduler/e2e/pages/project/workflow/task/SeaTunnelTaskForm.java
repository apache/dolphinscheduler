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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SeaTunnelTaskForm extends TaskNodeForm {

    public SeaTunnelTaskForm(WorkflowForm parent) {
        super(parent);
    }

    public SeaTunnelTaskForm executionType(String executionType) {
        WebElement input = executionTypeInput(executionType);
        WebElement label = input.findElement(By.xpath(".."));
        ((JavascriptExecutor) parent().driver()).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                label);
        label.click();
        WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.elementToBeSelected(input));
        return this;
    }

    public boolean isExecutionType(String executionType) {
        return executionTypeInput(executionType).isSelected();
    }

    private WebElement executionTypeInput(String executionType) {
        return WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
                        ".seatunnel-execute-type input[value='" + executionType + "']")));
    }
}
