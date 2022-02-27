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
import org.openqa.selenium.By.ByClassName;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.Getter;

@Getter
public final class UserPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(id = "btnCreateUser")
    private WebElement buttonCreateUser;

    @FindBy(className = "items")
    private List<WebElement> userList;

    @FindBy(id = "select-list")
    private List<WebElement> selectList;

    @FindBy(id = "selected-list")
    private List<WebElement> selectedList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    private final UserForm createUserForm = new UserForm();
    private final UserForm editUserForm = new UserForm();
    private final UserAuthorizeForm authorizeUserForm = new UserAuthorizeForm();

    public UserPage(RemoteWebDriver driver) {
        super(driver);
    }

    public UserPage create(String user, String password, String email, String phone) {
        buttonCreateUser().click();

        createUserForm().inputUserName().sendKeys(user);
        createUserForm().inputUserPassword().sendKeys(password);
        createUserForm().inputEmail().sendKeys(email);
        createUserForm().inputPhone().sendKeys(phone);
        createUserForm().buttonSubmit().click();

        return this;
    }

    public UserPage update(String user, String editUser, String editPassword, String editEmail, String editPhone) {
        List<WebElement> userList = driver.findElementsByClassName("items");
        userList.stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(user))
            .flatMap(it -> it.findElements(By.className("edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No edit button in user list"))
            .click();

        UserForm editUserForm = new UserForm();

        editUserForm.inputUserName().clear();
        editUserForm.inputUserName().sendKeys(editUser);
        editUserForm.inputUserPassword().clear();
        editUserForm.inputUserPassword().sendKeys(editPassword);
        editUserForm.inputEmail().clear();
        editUserForm.inputEmail().sendKeys(editEmail);
        editUserForm.inputPhone().clear();
        editUserForm.inputPhone().sendKeys(editPhone);
        editUserForm.buttonSubmit().click();

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

        buttonConfirm()
            .stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No confirm button when deleting"))
            .click();

        return this;
    }

    public UserPage clickAuthorize(String user) {
        userList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(user))
            .flatMap(it -> it.findElements(By.className("authorize")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No authorize button in user list"))
            .click();

        return this;
    }

    public UserPage closeAuthorize() {
        UserAuthorizeForm userAuthorizeForm = new UserAuthorizeForm();
        userAuthorizeForm.buttonCancel().click();

        return this;
    }

    public UserPage authorizeProject(String user, String projectName) {
        clickAuthorize(user);

        new WebDriverWait(driver, 5).until(ExpectedConditions.invisibilityOfElementLocated(new ByClassName("dialogAuthProject")));

        UserAuthorizeForm userAuthorizeForm = new UserAuthorizeForm();
        userAuthorizeForm.buttonAuthProject().click();

        selectList()
            .stream()
            .filter(it -> it.findElement(By.className("selectName")).getAttribute("innerHTML").contains(projectName))
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No project in project list"))
            .click();

        userAuthorizeForm.buttonSubmit().click();

        return this;
    }

    public UserPage authorizeDataSource(String user, String dataSourceName) {
        clickAuthorize(user);

        new WebDriverWait(driver, 5).until(ExpectedConditions.invisibilityOfElementLocated(By.className("dialogAuthProject")));

        UserAuthorizeForm userAuthorizeForm = new UserAuthorizeForm();
        userAuthorizeForm.buttonAuthDataSource().click();

        selectList()
            .stream()
            .filter(it -> it.findElement(By.className("selectName")).getAttribute("innerHTML").contains(dataSourceName))
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No dataSource in dataSource list"))
            .click();

        userAuthorizeForm.buttonSubmit().click();

        return this;
    }

    @Getter
    public class UserAuthorizeForm {
        UserAuthorizeForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(className = "authorize")
        private WebElement buttonAuthorize;

        @FindBy(className = "authProject")
        private WebElement buttonAuthProject;

        @FindBy(className = "authFile")
        private WebElement buttonAuthFile;

        @FindBy(className = "authDataSource")
        private WebElement buttonAuthDataSource;

        @FindBy(className = "authUdfFunc")
        private WebElement buttonAuthUdfFunc;

        @FindBy(className = "selectName")
        private WebElement selectName;

        @FindBy(className = "selectedName")
        private WebElement selectedName;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }

    @Getter
    public class UserForm {
        UserForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputUserName")
        private WebElement inputUserName;

        @FindBy(id = "inputUserPassword")
        private WebElement inputUserPassword;

        @FindBy(id = "selectTenant")
        private WebElement selectTenant;

        @FindBy(id = "selectQueue")
        private WebElement selectQueue;

        @FindBy(id = "inputEmail")
        private WebElement inputEmail;

        @FindBy(id = "inputPhone")
        private WebElement inputPhone;

        @FindBy(id = "radioStateEnable")
        private WebElement radioStateEnable;

        @FindBy(id = "radioStateDisable")
        private WebElement radioStateDisable;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }
}
