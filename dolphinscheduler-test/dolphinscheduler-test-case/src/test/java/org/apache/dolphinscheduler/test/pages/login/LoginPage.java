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

package org.apache.dolphinscheduler.test.pages.login;

import org.apache.dolphinscheduler.test.core.Page;
import org.apache.dolphinscheduler.test.pages.home.HomePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends Page {
    @FindBys({
            @FindBy(className = "input-user-name"),
            @FindBy(tagName = "input"),
    })
    @CacheLookup
    private WebElement inputUsername;

    @FindBys({
            @FindBy(className = "input-password"),
            @FindBy(tagName = "input"),
    })
    @CacheLookup
    private WebElement inputPassword;

    @FindBy(className = "btn-login")
    @CacheLookup
    private WebElement buttonLogin;

    @FindBy(className = "n-switch__button")
    @CacheLookup
    private WebElement buttonSwitchLanguage;

    public LoginPage() {
    }

    @Override
    protected void isLoaded() throws Error {
        this.waitFor(10).until(ExpectedConditions.elementToBeClickable(buttonSwitchLanguage));
    }

    @Override
    public void onUnload(Page nextPage) {
        nextPage.waitFor(10).until(ExpectedConditions.urlContains("/home"));
    }

    public HomePage loginAs() throws Exception {
        buttonSwitchLanguage.click();
        inputUsername.sendKeys("admin");
        inputPassword.sendKeys("dolphinscheduler123");
        buttonLogin.click();
        return this.to(new HomePage());
    }

    public HomePage loginAs(String userName, String passwd) throws Exception {
        inputUsername.sendKeys(userName);
        inputPassword.sendKeys(passwd);
        buttonLogin.click();
        return this.to(new HomePage());
    }

}
