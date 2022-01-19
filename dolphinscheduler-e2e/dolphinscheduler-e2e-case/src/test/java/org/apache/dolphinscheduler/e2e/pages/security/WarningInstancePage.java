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

import lombok.Getter;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

@Getter
public final class WarningInstancePage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(id = "btnCreateAlarmInstance")
    private WebElement buttonCreateWarningInstance;

    @FindBy(className = "items")
    private List<WebElement> warningInstanceList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    private final WarningInstanceForm createWarningInstanceForm = new WarningInstanceForm();
    private final WarningInstanceForm editWarningInstanceForm = new WarningInstanceForm();
    private final WarningInstanceForm alertPluginForm = new WarningInstanceForm();


    public WarningInstancePage(RemoteWebDriver driver) {
        super(driver);
    }

    public WarningInstancePage create(String alarmInstanceName, String alarmPluginName, String inputPluginContent, String keyword) {
        buttonCreateWarningInstance().click();

        createWarningInstanceForm().inputAlarmInstanceName().sendKeys(alarmInstanceName);
        createWarningInstanceForm().selectAlarmPlugin().click();
        SelectAlarmPlugin(alarmPluginName);
        createWarningInstanceForm().inputWebhook().sendKeys(inputPluginContent);
        createWarningInstanceForm().inputKeyword().sendKeys(keyword);
        createWarningInstanceForm().radioNoEnableProxy().click();
        createWarningInstanceForm().buttonSubmit().click();
        return this;
    }


    public WarningInstancePage update(String warningInstanceName, String editWarningInstanceName) {
        warningInstanceList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(warningInstanceName))
            .flatMap(it -> it.findElements(By.className("edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No edit button in alarmInstance list"))
            .click();

        editWarningInstanceForm().inputAlarmInstanceName().clear();
        editWarningInstanceForm().inputAlarmInstanceName().sendKeys(editWarningInstanceName);
        editWarningInstanceForm().buttonSubmit().click();

        return this;
    }

    public WarningInstancePage delete(String WarningInstance) {
        warningInstanceList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(WarningInstance))
            .flatMap(it -> it.findElements(By.className("delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in alarmInstance list"))
            .click();

        buttonConfirm()
            .stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No confirm button when deleting"))
            .click();

        return this;
    }

    public WarningInstancePage selectAlarmPlugin(String alarmPluginName) {

        final By optionsLocator = By.className("option-alarmPluginName");

        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.visibilityOfElementLocated(optionsLocator));

        driver().findElements(optionsLocator)
                .stream()
                .filter(it -> it.getText().contains(alarmPluginName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such tenant: " + alarmPluginName))
                .click();
        return this;
    }

    @Getter
    public class WarningInstanceForm {
        WarningInstanceForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputAlarmInstanceName")
        private WebElement inputAlarmInstanceName;

        @FindBy(id = "selectAlarmPlugin")
        private WebElement selectAlarmPlugin;

        @FindBy(id = "alarmPluginName")
        private WebElement alarmPluginName;

        @FindBy(xpath = "//form/div/div/div/div/div/input")
        private WebElement  inputWebhook;

        @FindBy(xpath = "//form/div/div[2]/div/div/div/input")
        private WebElement inputKeyword;

        @FindBy(xpath = "//label[2]/span/span")
        private WebElement radioNoEnableProxy;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }
}
