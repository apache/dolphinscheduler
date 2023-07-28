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

package org.apache.dolphinscheduler.e2e.pages;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.Getter;
import lombok.SneakyThrows;

import java.time.Duration;

@Getter
public final class LoginPage extends NavBarPage {
    @FindBys({
        @FindBy(className = "input-user-name"),
        @FindBy(tagName = "input"),
    })
    private WebElement inputUsername;

    @FindBys( {
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
    public NavBarPage login(String username, String password) {
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(buttonSwitchLanguage));

        buttonSwitchLanguage().click();

        inputUsername().sendKeys(username);
        inputPassword().sendKeys(password);
        buttonLogin().click();

        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.urlContains("/home"));

        return new NavBarPage(driver);
    }
}
