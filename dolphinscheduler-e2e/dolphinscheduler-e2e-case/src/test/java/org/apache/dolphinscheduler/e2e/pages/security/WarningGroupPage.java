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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

@Getter
public final class WarningGroupPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(id = "btnCreateAlarmGroup")
    private WebElement buttonCreateAlarmGroup;

    @FindBy(className = "items")
    private List<WebElement> alarmGroupList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    private final AlarmGroupForm createWarningGroupForm = new AlarmGroupForm();
    private final AlarmGroupForm editWarningGroupForm = new AlarmGroupForm();


    public WarningGroupPage(RemoteWebDriver driver) {
        super(driver);
    }

    public WarningGroupPage create(String alarmGroupName, String alarmDescription) {
        buttonCreateAlarmGroup().click();

        createWarningGroupForm().inputAlarmGroupName().sendKeys(alarmGroupName);
        createWarningGroupForm().selectAlarmInstance().click();
        createWarningGroupForm().optionAlarmInstance().click();
        createWarningGroupForm().inputAlarmDescription().sendKeys(alarmDescription);
        createWarningGroupForm().buttonSubmit().click();

        return this;
    }

    public WarningGroupPage update(String alarmGroupName, String editAlarmGroupName, String editAlarmDescription) {
        alarmGroupList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(alarmGroupName))
            .flatMap(it -> it.findElements(By.className("edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No edit button in alarmGroup list"))
            .click();
        editWarningGroupForm().inputAlarmGroupName().clear();
        editWarningGroupForm().inputAlarmGroupName().sendKeys(editAlarmGroupName);
        editWarningGroupForm().inputAlarmDescription().clear();
        editWarningGroupForm().inputAlarmDescription().sendKeys(editAlarmDescription);
        editWarningGroupForm().buttonSubmit().click();

        return this;
    }

    public WarningGroupPage delete(String alarmGroupName) {
        alarmGroupList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(alarmGroupName))
            .flatMap(it -> it.findElements(By.className("delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in alarmGroup list"))
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
    public class AlarmGroupForm {
        AlarmGroupForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputAlarmGroupName")
        private WebElement inputAlarmGroupName;

        @FindBy(id = "selectAlarmInstance")
        private WebElement selectAlarmInstance;

        @FindBy(id = "optionAlarmInstance")
        private WebElement optionAlarmInstance;

        @FindBy(id = "inputAlarmDescription")
        private WebElement inputAlarmDescription;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }

}
