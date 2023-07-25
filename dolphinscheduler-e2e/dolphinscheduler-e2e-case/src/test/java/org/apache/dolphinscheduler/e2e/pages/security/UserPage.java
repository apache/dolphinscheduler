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
public final class UserPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(className = "btn-create-user")
    private WebElement buttonCreateUser;

    @FindBy(className = "items")
    private List<WebElement> userList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    private final UserForm createUserForm = new UserForm();
    private final UserForm editUserForm = new UserForm();


    public UserPage(RemoteWebDriver driver) {
        super(driver);
    }

    public UserPage create(String user, String password, String email, String phone, String tenant) {
        buttonCreateUser().click();

        createUserForm().inputUserName().sendKeys(user);
        createUserForm().inputUserPassword().sendKeys(password);

        createUserForm().btnSelectTenantDropdown().click();

        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.visibilityOfElementLocated(new By.ByClassName(
                "n-base-select-option__content")));

        createUserForm().selectTenant()
            .stream()
            .filter(it -> it.getText().contains(tenant))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No %s in tenant dropdown list", tenant)))
            .click();

        createUserForm().inputEmail().sendKeys(email);
        createUserForm().inputPhone().sendKeys(phone);
        createUserForm().buttonSubmit().click();

        return this;
    }

    public UserPage update(String user, String editUser, String editEmail, String editPhone,
                           String tenant) {
        userList().stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(user))
            .flatMap(it -> it.findElements(By.className("edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No edit button in user list"))
            .click();

        editUserForm().inputUserName().sendKeys(Keys.CONTROL+"a");
        editUserForm().inputUserName().sendKeys(Keys.BACK_SPACE);
        editUserForm().inputUserName().sendKeys(editUser);

        createUserForm().btnSelectTenantDropdown().click();

        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.visibilityOfElementLocated(new By.ByClassName(
                "n-base-select-option__content")));

        createUserForm().selectTenant()
                .stream()
                .filter(it -> it.getText().contains(tenant))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("No %s in tenant dropdown list", tenant)))
                .click();

        editUserForm().inputEmail().sendKeys(Keys.CONTROL+"a");
        editUserForm().inputEmail().sendKeys(Keys.BACK_SPACE);
        editUserForm().inputEmail().sendKeys(editEmail);

        editUserForm().inputPhone().sendKeys(Keys.CONTROL+"a");
        editUserForm().inputPhone().sendKeys(Keys.BACK_SPACE);
        editUserForm().inputPhone().sendKeys(editPhone);

        editUserForm().buttonSubmit().click();

        return this;
    }

    public UserPage delete(String user) {
        userList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(user))
            .flatMap(it -> it.findElements(By.className("delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in user list"))
            .click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class UserForm {
        UserForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
            @FindBy(className = "input-username"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputUserName;

        @FindBys({
            @FindBy(className = "input-password"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputUserPassword;

        @FindBys({
            @FindBy(className = "select-tenant"),
            @FindBy(className = "n-base-selection"),
        })
        private WebElement btnSelectTenantDropdown;

        @FindBy(className = "n-base-select-option__content")
        private List<WebElement> selectTenant;

        @FindBys({
                @FindBy(className = "select-queue"),
                @FindBy(className = "n-base-selection"),
        })
        private WebElement btnSelectQueueDropdown;

        @FindBy(className = "n-base-select-option__content")
        private List<WebElement> selectQueue;

        @FindBys({
                @FindBy(className = "input-email"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputEmail;

        @FindBys({
                @FindBy(className = "input-phone"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputPhone;

        @FindBy(className = "radio-state-enable")
        private WebElement radioStateEnable;

        @FindBy(className = "radio-state-disable")
        private WebElement radioStateDisable;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }
}
