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

package org.apache.dolphinscheduler.e2e.pages;

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.models.users.IUser;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import lombok.Getter;
import lombok.SneakyThrows;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
public final class LoginPage extends NavBarPage {

    @FindBys({
            @FindBy(className = "input-user-name"),
            @FindBy(tagName = "input"),
    })
    private WebElement inputUsername;

    @FindBys({
            @FindBy(className = "input-password"),
            @FindBy(tagName = "input"),
    })
    private WebElement inputPassword;

    @FindBy(className = "btn-login")
    private WebElement buttonLogin;

    @FindBy(className = "n-switch__button")
    private WebElement buttonSwitchLanguage;

    public LoginPage(RemoteWebDriver driver) {
        super(driver);
    }

    @SneakyThrows
    public NavBarPage login(IUser user) {
        return login(user.getUserName(), user.getPassword());
    }

    @SneakyThrows
    public NavBarPage login(String username, String password) {
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(buttonSwitchLanguage));
        buttonSwitchLanguage().click();

        inputUsername().sendKeys(username);
        inputPassword().sendKeys(password);
        buttonLogin().click();

        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/home"));

        return new NavBarPage(driver);
    }
}
