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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Getter
public class FunctionManagePage extends NavBarPage implements ResourcePage.Tab {
    @FindBy(id = "btnCreateUdfFunction")
    private WebElement buttonCreateUdfFunction;

    @FindBy(className = "udf-function-items")
    private List<WebElement> functionList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    private final CreateUdfFunctionBox createUdfFunctionBox;

    private final RenameUdfFunctionBox renameUdfFunctionBox;

    public FunctionManagePage(RemoteWebDriver driver) {
        super(driver);

        createUdfFunctionBox = new CreateUdfFunctionBox();

        renameUdfFunctionBox = new RenameUdfFunctionBox();
    }

    public FunctionManagePage createUdfFunction(String udfFunctionName, String className, String udfResourceName, String description) {
        buttonCreateUdfFunction().click();

        createUdfFunctionBox().inputFunctionName().sendKeys(udfFunctionName);

        createUdfFunctionBox().inputClassName().sendKeys(className);

        createUdfFunctionBox().inputDescription().sendKeys(description);

        createUdfFunctionBox().buttonUdfResourceDropDown().click();

        createUdfFunctionBox().selectUdfResource()
            .stream()
            .filter(it -> it.getText().contains(udfResourceName))
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
            .flatMap(it -> it.findElements(By.id("btnRename")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No rename button in function manage list"))
            .click();

        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id("createUdfDialog")));

        renameUdfFunctionBox().inputFunctionName().clear();

        renameUdfFunctionBox().inputFunctionName().sendKeys(afterName);

        renameUdfFunctionBox.buttonSubmit().click();

        return this;
    }

    public FunctionManagePage deleteUdfFunction(String udfFunctionName) {
        functionList()
            .stream()
            .filter(it -> it.getText().contains(udfFunctionName))
            .flatMap(it -> it.findElements(By.id("btnDelete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in udf resource list"))
            .click();

        buttonConfirm()
            .stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No confirm button when deleting in udf resource page"))
            .click();

        return this;
    }

    @Getter
    public class CreateUdfFunctionBox {
        CreateUdfFunctionBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputFunctionName")
        private WebElement inputFunctionName;

        @FindBy(id = "inputClassName")
        private WebElement inputClassName;

        @FindBy(id = "btnUdfResourceDropDown")
        private WebElement buttonUdfResourceDropDown;

        @FindBy(className = "vue-treeselect__menu")
        private List<WebElement> selectUdfResource;

        @FindBy(id = "inputDescription")
        private WebElement inputDescription;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }

    @Getter
    public class RenameUdfFunctionBox {
        RenameUdfFunctionBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputFunctionName")
        private WebElement inputFunctionName;

        @FindBy(id = "inputClassName")
        private WebElement inputClassName;

        @FindBy(id = "inputDescription")
        private WebElement inputDescription;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }
}
