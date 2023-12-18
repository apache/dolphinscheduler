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

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Getter
public class UdfManagePage extends NavBarPage implements ResourcePage.Tab {
    @FindBy(className = "btn-create-directory")
    private WebElement buttonCreateDirectory;

    @FindBy(className = "btn-upload-resource")
    private WebElement buttonUploadUdf;

    @FindBy(className = "items")
    private List<WebElement> udfList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    private final UploadFileBox uploadFileBox;

    private final RenameBox renameBox;

    private final CreateDirectoryBox createDirectoryBox;

    public UdfManagePage(RemoteWebDriver driver) {
        super(driver);

        uploadFileBox = new UploadFileBox();

        renameBox = new RenameBox();

        createDirectoryBox = new CreateDirectoryBox();
    }

    public UdfManagePage createDirectory(String name) {
        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(name);
        createDirectoryBox().buttonSubmit().click();

        return this;
    }

    public UdfManagePage uploadFile(String filePath) {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(buttonUploadUdf));

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
            .flatMap(it -> it.findElements(By.className("btn-rename")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No rename button in udf manage list"))
            .click();

        renameBox().inputName().clear();
        renameBox().inputName().sendKeys(AfterName);
        renameBox().buttonSubmit().click();

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
    public class RenameBox {
        RenameBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
            @FindBy(className = "input-name"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputName;

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

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }
}
