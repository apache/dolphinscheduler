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

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import lombok.Getter;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Getter
public final class EnvironmentPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(className = "btn-create-environment")
    private WebElement buttonCreateEnvironment;

    @FindBy(className = "items")
    private List<WebElement> environmentList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

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

        editEnvironmentForm().btnSelectWorkerGroupDropdown().click();
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.visibilityOfElementLocated(new By.ByClassName(
                "n-base-select-option__content")));
        editEnvironmentForm().selectWorkerGroupList()
                .stream()
                .filter(it -> it.getText().contains(workerGroup))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("No %s in worker group dropdown list",
                        workerGroup)))
                .click();

        createEnvironmentForm().buttonSubmit().click();
        return this;
    }

    public EnvironmentPage update(String oldName, String name, String config, String desc, String workerGroup) {
        environmentList()
                .stream()
                .filter(it -> it.findElement(By.className("environment-name")).getAttribute("innerHTML").contains(oldName))
                .flatMap(it -> it.findElements(By.className("edit")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No edit button in environment list"))
                .click();


        editEnvironmentForm().inputEnvironmentName().sendKeys(Keys.CONTROL + "a");
        editEnvironmentForm().inputEnvironmentName().sendKeys(Keys.BACK_SPACE);
        editEnvironmentForm().inputEnvironmentName().sendKeys(name);

        editEnvironmentForm().inputEnvironmentConfig().sendKeys(Keys.CONTROL + "a");
        editEnvironmentForm().inputEnvironmentConfig().sendKeys(Keys.BACK_SPACE);
        editEnvironmentForm().inputEnvironmentConfig().sendKeys(config);

        editEnvironmentForm().inputEnvironmentDesc().sendKeys(Keys.CONTROL + "a");
        editEnvironmentForm().inputEnvironmentDesc().sendKeys(Keys.BACK_SPACE);
        editEnvironmentForm().inputEnvironmentDesc().sendKeys(desc);

        if (editEnvironmentForm().selectedWorkerGroup().getAttribute("innerHTML").equals(workerGroup)) {
            editEnvironmentForm().btnSelectWorkerGroupDropdown().click();
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.visibilityOfElementLocated(new By.ByClassName(
                    "n-base-select-option__content")));
            editEnvironmentForm().selectWorkerGroupList()
                    .stream()
                    .filter(it -> it.getText().contains(workerGroup))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format("No %s in worker group dropdown list",
                            workerGroup)))
                    .click();
        }

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

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class EnvironmentForm {
        EnvironmentForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
            @FindBy(className = "input-environment-name"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputEnvironmentName;

        @FindBys({
            @FindBy(className = "input-environment-config"),
            @FindBy(tagName = "textarea"),
        })
        private WebElement inputEnvironmentConfig;

        @FindBys({
            @FindBy(className = "input-environment-desc"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputEnvironmentDesc;

        @FindBys({
                @FindBy(className = "input-environment-worker-group"),
                @FindBy(className = "n-base-selection"),
        })
        private WebElement btnSelectWorkerGroupDropdown;

        @FindBy(className = "n-base-select-option__content")
        private List<WebElement> selectWorkerGroupList;

        @FindBys({
            @FindBy(className = "n-base-selection-tags"),
            @FindBy(className = "n-tag__content"),
        })
        private WebElement selectedWorkerGroup;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }
}
