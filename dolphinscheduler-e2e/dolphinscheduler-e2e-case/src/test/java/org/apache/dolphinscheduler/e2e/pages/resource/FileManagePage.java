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

import org.apache.dolphinscheduler.e2e.pages.common.CodeEditor;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import java.io.File;
import java.util.List;


@Getter
public class FileManagePage extends NavBarPage implements ResourcePage.Tab {
    @FindBy(className = "btn-create-directory")
    private WebElement buttonCreateDirectory;

    @FindBy(className = "btn-create-file")
    private WebElement buttonCreateFile;

    @FindBy(className = "btn-upload-file")
    private WebElement buttonUploadFile;

    private final CreateDirectoryBox createDirectoryBox;

    private final RenameBox renameBox;

    private final CreateFileBox createFileBox;

    private final UploadFileBox uploadFileBox;

    @FindBy(className = "items")
    private List<WebElement> fileList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    public FileManagePage(RemoteWebDriver driver) {
        super(driver);

        createDirectoryBox = new CreateDirectoryBox();

        renameBox = new RenameBox();

        createFileBox = new CreateFileBox();

        uploadFileBox = new UploadFileBox();
    }

    public FileManagePage createDirectory(String name, String description) {
        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(name);
        createDirectoryBox().inputDescription().sendKeys(description);
        createDirectoryBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage cancelCreateDirectory(String name, String description) {
        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(name);
        createDirectoryBox().inputDescription().sendKeys(description);
        createDirectoryBox().buttonCancel().click();

        return this;
    }

    public FileManagePage rename(String currentName, String AfterName) {
        fileList()
            .stream()
            .filter(it -> it.getText().contains(currentName))
            .flatMap(it -> it.findElements(By.className("btn-rename")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No rename button in file manage list"))
            .click();

        renameBox().inputName().sendKeys(Keys.CONTROL + "a");
        renameBox().inputName().sendKeys(Keys.BACK_SPACE);
        renameBox().inputName().sendKeys(AfterName);
        renameBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage createSubDirectory(String directoryName, String subDirectoryName, String description) {
        fileList()
            .stream()
            .filter(it -> it.getText().contains(directoryName))
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No %s in file manage list", directoryName)))
            .click();

        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(subDirectoryName);
        createDirectoryBox().inputDescription().sendKeys(description);
        createDirectoryBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage delete(String name) {
        fileList()
            .stream()
            .filter(it -> it.getText().contains(name))
            .flatMap(it -> it.findElements(By.className("btn-delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in file manage list"))
            .click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    public FileManagePage createFile(String fileName, String scripts) {
        buttonCreateFile().click();

        createFileBox().inputFileName().sendKeys(fileName);
        createFileBox().codeEditor().content(scripts);
        createFileBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage editFile(String fileName, String scripts) {
        fileList()
            .stream()
            .filter(it -> it.getText().contains(fileName))
            .flatMap(it -> it.findElements(By.id("btnEdit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No edit button in file manage list"))
            .click();

        createFileBox().codeEditor().content(scripts);
        createFileBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage uploadFile(String filePath) {
        buttonUploadFile().click();

        driver.setFileDetector(new LocalFileDetector());

        uploadFileBox().buttonUpload().sendKeys(filePath);
        uploadFileBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage downloadFile(String fileName) {
        fileList()
            .stream()
            .filter(it -> it.getText().contains(fileName))
            .flatMap(it -> it.findElements(By.className("btn-download")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No download button in file manage list"))
            .click();

        return this;
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
                @FindBy(tagName = "input"),
        })
        private WebElement inputDescription;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
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

        @FindBys({
                @FindBy(className = "input-description"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputDescription;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }

    @Getter
    public class CreateFileBox {
        CreateFileBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
                @FindBy(className = "input-file-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputFileName;

        private final CodeEditor codeEditor = new CodeEditor(driver);

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

        @FindBy(className = "btn-upload")
        private WebElement buttonUpload;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }
}
