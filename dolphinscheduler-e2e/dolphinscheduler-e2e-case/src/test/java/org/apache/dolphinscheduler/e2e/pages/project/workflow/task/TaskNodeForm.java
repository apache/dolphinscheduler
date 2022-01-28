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
public abstract class TaskNodeForm {
    @FindBy(id = "inputNodeName")
    private WebElement inputNodeName;
    @FindBy(id = "btnSubmit")
    private WebElement buttonSubmit;
    @FindBys({
        @FindBy(className = "input-param-key"),
        @FindBy(tagName = "input"),
    })
    private List<WebElement> inputParamKey;
    @FindBys({
        @FindBy(className = "input-param-val"),
        @FindBy(tagName = "input"),
    })
    private List<WebElement> inputParamVal;

    @FindBy(id = "selectPreTask")
    private WebElement selectPreTasks;

    private final WorkflowForm parent;

    TaskNodeForm(WorkflowForm parent) {
        this.parent = parent;

        final WebDriver driver = parent.driver();

        PageFactory.initElements(driver, this);
    }

    public TaskNodeForm name(String name) {
        inputNodeName().sendKeys(name);

        return this;
    }

    public TaskNodeForm addParam(String key, String val) {
        assert inputParamKey().size() == inputParamVal().size();

        final int len = inputParamKey().size();

        final WebDriver driver = parent().driver();
        Stream.concat(
                  driver.findElements(new ByChained(By.className("user-def-params-model"), By.className("add"))).stream(),
                  driver.findElements(new ByChained(By.className("user-def-params-model"), By.className("add-dp"))).stream())
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Cannot find button to add param"))
              .click();

        inputParamKey().get(len).sendKeys(key);
        inputParamVal().get(len).sendKeys(val);

        return this;
    }

    public TaskNodeForm preTask(String preTaskName) {
        selectPreTasks().click();

        final By optionsLocator = By.className("option-pre-tasks");

        new WebDriverWait(parent.driver(), 10)
                .until(ExpectedConditions.visibilityOfElementLocated(optionsLocator));

        parent.driver().findElements(optionsLocator)
                .stream()
                .filter(it -> it.getText().contains(preTaskName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such task: " + preTaskName))
                .click()
        ;

        return this;
    }

    public WorkflowForm submit() {
        buttonSubmit.click();

        return parent();
    }
}
