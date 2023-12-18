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

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
    @FindBy(className = "btn-create-token")
    private WebElement buttonCreateToken;

    @FindBy(className = "items")
    private List<WebElement> tokenList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    @FindBy(className = "username")
    private List<WebElement> userName;

    @FindBy(className = "token")
    private List<WebElement> token;

    private final TokenForm createTokenForm = new TokenForm();
    private final TokenForm editTokenForm = new TokenForm();

    public TokenPage(RemoteWebDriver driver) {
        super(driver);
    }

    public TokenPage create(String userName) {
        buttonCreateToken().click();

        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(createTokenForm().selectUserNameDropdown()));
        createTokenForm().selectUserNameDropdown().click();
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.visibilityOfElementLocated(new By.ByClassName(
                "n-base-select-option__content")));
        createTokenForm().selectUserNameList()
                .stream()
                .filter(it -> it.getText().contains(userName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("No %s in token dropdown list",
                        userName)))
                .click();

        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(createTokenForm().buttonGenerateToken()));
        createTokenForm().buttonGenerateToken().click();

        createTokenForm().buttonSubmit().click();

        return this;
    }

    public TokenPage update(String userName) {
        tokenList().stream()
            .filter(it -> it.findElement(By.className("username")).getAttribute("innerHTML").contains(userName))
            .flatMap(it -> it.findElements(By.className("edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No edit button in token list"))
            .click();

        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(editTokenForm().buttonGenerateToken()));
        editTokenForm().buttonGenerateToken().click();
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(editTokenForm().buttonGenerateToken()));

        editTokenForm().buttonSubmit().click();

        return this;
    }

    public String getToken(String userName) {
        return tokenList().stream()
                          .filter(it -> it.findElement(By.className("username")).getAttribute("innerHTML").contains(userName))
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

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class TokenForm {
        TokenForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
            @FindBy(className = "input-username"),
            @FindBy(className = "n-base-selection"),
        })
        private WebElement selectUserNameDropdown;

        @FindBy(className = "n-base-select-option__content")
        private List<WebElement> selectUserNameList;

        @FindBy(className = "btn-generate-token")
        private WebElement buttonGenerateToken;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;

    }
}
