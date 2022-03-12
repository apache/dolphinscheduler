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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

@Getter
public class UdfManagePage extends NavBarPage implements ResourcePage.Tab {
    @FindBy(className = "btn-create-directory")
    private WebElement buttonCreateDirectory;

    @FindBy(className = "btn-upload-udf")
    private WebElement buttonUploadUdf;

    @FindBy(className = "items")
    private List<WebElement> udfList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    private final UploadFileBox uploadFileBox;

    private final RenameDirectoryBox renameDirectoryBox;

    private final CreateDirectoryBox createDirectoryBox;

    public UdfManagePage(RemoteWebDriver driver) {
        super(driver);

        uploadFileBox = new UploadFileBox();

        renameDirectoryBox = new RenameDirectoryBox();

        createDirectoryBox = new CreateDirectoryBox();
    }

    public UdfManagePage createDirectory(String name, String description) {
        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(name);
        createDirectoryBox().inputDescription().sendKeys(description);
        createDirectoryBox().buttonSubmit().click();

        return this;
    }

    public UdfManagePage uploadFile(String filePath) {
        buttonUploadUdf().click();

        driver.setFileDetector(new LocalFileDetector());

        uploadFileBox().buttonUpload().sendKeys(filePath);
        uploadFileBox().buttonSubmit().click();

        return this;
    }

    public UdfManagePage downloadFile(String fileName) {
        udfList()
            .stream()
            .filter(it -> it.getText().contains(fileName))
            .flatMap(it -> it.findElements(By.className("btn-download")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No download button in udf manage list"))
            .click();

        return this;
    }

    public UdfManagePage rename(String currentName, String AfterName) {
        udfList()
            .stream()
            .filter(it -> it.getText().contains(currentName))
            .flatMap(it -> it.findElements(By.className("btn-edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No rename button in udf manage list"))
            .click();

        renameDirectoryBox().inputName().clear();
        renameDirectoryBox().inputName().sendKeys(AfterName);
        renameDirectoryBox().buttonSubmit().click();

        return this;
    }

    public UdfManagePage delete(String name) {
        udfList()
            .stream()
            .filter(it -> it.getText().contains(name))
            .flatMap(it -> it.findElements(By.className("btn-delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in udf manage list"))
            .click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class RenameDirectoryBox {
        RenameDirectoryBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
                @FindBy(className = "input-directory-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputName;

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
    public class UploadFileBox {
        UploadFileBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
                @FindBy(className = "btn-upload"),
                @FindBy(tagName = "input"),
        })
        private WebElement buttonUpload;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }

    @Getter
    public class CreateDirectoryBox {
        CreateDirectoryBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
                @FindBy(className = "input-directory-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputDirectoryName;

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
