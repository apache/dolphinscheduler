/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.dolphinscheduler.e2e.pages.resource;

import lombok.Getter;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Getter
public class FunctionManagePage extends NavBarPage implements ResourcePage.Tab {
    @FindBy(className = "btn-create-udf-function")
    private WebElement buttonCreateUdfFunction;

    @FindBy(className = "items")
    private List<WebElement> functionList;

    @FindBys({
            @FindBy(className = "n-popconfirm__action"),
            @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    private CreateUdfFunctionBox createUdfFunctionBox;

    private RenameUdfFunctionBox renameUdfFunctionBox;

    public FunctionManagePage(RemoteWebDriver driver) {
        super(driver);

        createUdfFunctionBox = new CreateUdfFunctionBox();

        renameUdfFunctionBox = new RenameUdfFunctionBox();
    }

    public FunctionManagePage createUdfFunction(String udfFunctionName, String className, String udfResourceName, String description) {
        buttonCreateUdfFunction().click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", createUdfFunctionBox().radioFunctionType());

        createUdfFunctionBox().inputFunctionName().sendKeys(udfFunctionName);

        createUdfFunctionBox().inputClassName().sendKeys(className);

        createUdfFunctionBox().inputDescription().sendKeys(description);

        createUdfFunctionBox().buttonUdfResourceDropDown().click();

        createUdfFunctionBox().selectUdfResource()
            .stream()
            .filter(it -> it.getAttribute("innerHTML").contains(udfResourceName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No %s in udf resource list", udfResourceName)))
            .click();

        createUdfFunctionBox().buttonSubmit().click();

        return this;
    }

    public FunctionManagePage renameUdfFunction(String currentName, String afterName) {
        functionList()
            .stream()
            .filter(it -> it.getText().contains(currentName))
            .flatMap(it -> it.findElements(By.className("btn-edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No rename button in function manage list"))
            .click();

        renameUdfFunctionBox().inputFunctionName().sendKeys(Keys.CONTROL + "a");
        renameUdfFunctionBox().inputFunctionName().sendKeys(Keys.BACK_SPACE);
        renameUdfFunctionBox().inputFunctionName().sendKeys(afterName);

        renameUdfFunctionBox.buttonSubmit().click();

        return this;
    }

    public FunctionManagePage deleteUdfFunction(String udfFunctionName) {
        functionList()
            .stream()
            .filter(it -> it.getText().contains(udfFunctionName))
            .flatMap(it -> it.findElements(By.className("btn-delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in udf resource list"))
            .click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class CreateUdfFunctionBox {
        CreateUdfFunctionBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
                @FindBy(className = "radio-function-type"),
                @FindBy(tagName = "input"),
        })
        private WebElement radioFunctionType;

        @FindBys({
                @FindBy(className = "input-function-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputFunctionName;

        @FindBys({
                @FindBy(className = "input-class-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputClassName;

        @FindBys({
                @FindBy(className = "btn-udf-resource-dropdown"),
                @FindBy(className = "n-base-selection"),
        })
        private WebElement buttonUdfResourceDropDown;

        @FindBy(className = "n-tree-node-content__text")
        private List<WebElement> selectUdfResource;

        @FindBys({
                @FindBy(className = "input-description"),
                @FindBy(tagName = "textarea"),
        })
        private WebElement inputDescription;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }

    @Getter
    public class RenameUdfFunctionBox {
        RenameUdfFunctionBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
                @FindBy(className = "input-function-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputFunctionName;

        @FindBys({
                @FindBy(className = "input-class-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputClassName;

        @FindBys({
                @FindBy(className = "input-description"),
                @FindBy(tagName = "textarea"),
        })
        private WebElement inputDescription;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }
}
