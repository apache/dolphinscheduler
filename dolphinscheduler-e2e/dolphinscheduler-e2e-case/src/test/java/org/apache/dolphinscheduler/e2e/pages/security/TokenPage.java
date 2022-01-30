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
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage.Tab;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.Getter;

import com.google.common.base.Strings;

@Getter
public final class TokenPage extends NavBarPage implements Tab {
    @FindBy(id = "btnCreateToken")
    private WebElement buttonCreateToken;

    @FindBy(className = "items")
    private List<WebElement> tokenList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    @FindBy(className = "userName")
    private List<WebElement> userName;

    @FindBy(className = "token")
    private List<WebElement> token;

    private final TokenForm createTokenForm = new TokenForm();
    private final TokenForm editTokenForm = new TokenForm();

    public TokenPage(RemoteWebDriver driver) {
        super(driver);
    }

    public TokenPage create() {
        buttonCreateToken().click();
        createTokenForm().buttonGenerateToken().click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(createTokenForm.buttonGenerateToken));
        createTokenForm().buttonSubmit().click();
        return this;
    }

    public TokenPage update(String userName) {
        tokenList().stream()
            .filter(it -> it.findElement(By.className("userName")).getAttribute("innerHTML").contains(userName))
            .flatMap(it -> it.findElements(By.className("edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No edit button in token list"))
            .click();

        TokenForm editTokenForm = new TokenForm();

        editTokenForm.buttonGenerateToken().click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(createTokenForm.buttonGenerateToken));
        editTokenForm.buttonSubmit().click();
        return this;
    }

    public String getToken(String userName) {
        return tokenList().stream()
                          .filter(it -> it.findElement(By.className("userName")).getAttribute("innerHTML").contains(userName))
                          .flatMap(it -> it.findElements(By.className("token")).stream())
                          .filter(it -> !Strings.isNullOrEmpty(it.getAttribute("innerHTML")))
                          .map(it -> it.getAttribute("innerHTML"))
                          .findFirst()
                          .orElseThrow(() -> new IllegalArgumentException("No token for such user: " + userName));
    }

    public TokenPage delete(String userName) {
        tokenList()
            .stream()
            .filter(it -> it.getText().contains(userName))
            .flatMap(it -> it.findElements(By.className("delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in token list"))
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
    public class TokenForm {
        TokenForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "btnGenerateToken")
        private WebElement buttonGenerateToken;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;

    }
}
