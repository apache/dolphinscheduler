/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.e2e.pages.security;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import lombok.Getter;

@Getter
public final class EnvironmentPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(id = "btnCreateEnvironment")
    private WebElement buttonCreateEnvironment;

    @FindBy(className = "items")
    private List<WebElement> environmentList;

    @FindBys({
            @FindBy(className = "el-popconfirm"),
            @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    private final EnvironmentForm createEnvironmentForm;
    private final EnvironmentForm editEnvironmentForm;

    public EnvironmentPage(RemoteWebDriver driver) {
        super(driver);
        createEnvironmentForm = new EnvironmentForm();
        editEnvironmentForm = new EnvironmentForm();
    }

    public EnvironmentPage create(String name, String config, String desc, String workerGroup) {
        buttonCreateEnvironment().click();
        createEnvironmentForm().inputEnvironmentName().sendKeys(name);
        createEnvironmentForm().inputEnvironmentConfig().sendKeys(config);
        createEnvironmentForm().inputEnvironmentDesc().sendKeys(desc);
        createEnvironmentForm().inputWorkerGroup().sendKeys(workerGroup);
        createEnvironmentForm().buttonSubmit().click();
        return this;
    }

    public EnvironmentPage update(String oldName, String name, String config, String desc, String workerGroup) {
        environmentList()
                .stream()
                .filter(it -> it.findElement(By.className("environmentName")).getAttribute("innerHTML").contains(oldName))
                .flatMap(it -> it.findElements(By.className("edit")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No edit button in environment list"))
                .click();

        editEnvironmentForm().inputEnvironmentName().sendKeys(name);
        editEnvironmentForm().inputEnvironmentConfig().sendKeys(config);
        editEnvironmentForm().inputEnvironmentDesc().sendKeys(desc);
        editEnvironmentForm().inputWorkerGroup().sendKeys(workerGroup);
        editEnvironmentForm().buttonSubmit().click();

        return this;
    }

    public EnvironmentPage delete(String name) {
        environmentList()
                .stream()
                .filter(it -> it.getText().contains(name))
                .flatMap(it -> it.findElements(By.className("delete")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No delete button in environment list"))
                .click();

        buttonConfirm()
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No confirm button when deleting"))
                .click();

        return this;
    }

    @Getter
    public class EnvironmentForm {
        EnvironmentForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputEnvironmentName")
        private WebElement inputEnvironmentName;

        @FindBy(id = "inputEnvironmentConfig")
        private WebElement inputEnvironmentConfig;

        @FindBy(id = "inputEnvironmentDesc")
        private WebElement inputEnvironmentDesc;

        @FindBy(id = "inputEnvironmentWorkerGroup")
        private WebElement inputWorkerGroup;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }
}
