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

package org.apache.dolphinscheduler.e2e.pages.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.common.CodeEditor;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import java.util.List;

import lombok.Getter;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
public class FileManagePage extends NavBarPage implements ResourcePage.Tab {

    @FindBy(className = "btn-create-directory")
    private WebElement buttonCreateDirectory;

    @FindBy(className = "btn-create-file")
    private WebElement buttonCreateFile;

    @FindBy(className = "btn-upload-resource")
    private WebElement buttonUploadFile;

    private final CreateDirectoryBox createDirectoryBox;

    private final RenameBox renameBox;

    private final UploadFileBox uploadFileBox;

    private final EditFileBox editFileBox;

    @FindBy(className = "items")
    private List<WebElement> fileList;

    @FindBys({
            @FindBy(className = "n-popconfirm__action"),
            @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    @FindBys({
            @FindBy(className = "monaco-editor"),
            @FindBy(className = "view-line"),
    })
    private WebElement editor;

    public FileManagePage(RemoteWebDriver driver) {
        super(driver);

        createDirectoryBox = new CreateDirectoryBox();

        renameBox = new RenameBox();

        uploadFileBox = new UploadFileBox();

        editFileBox = new EditFileBox();

    }

    public FileManagePage createDirectory(String name) {
        waitForPageLoading();
        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(name);
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(createDirectoryBox().buttonSubmit()));
        createDirectoryBox().buttonSubmit().click();
        return this;
    }

    public FileManagePage cancelCreateDirectory(String name) {
        waitForPageLoading();
        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(name);
        createDirectoryBox().buttonCancel().click();

        return this;
    }

    public FileManagePage rename(String currentName, String AfterName) {
        waitForPageLoading();
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

    public FileManagePage createSubDirectory(String directoryName, String subDirectoryName) {
        fileList()
                .stream()
                .filter(it -> it.getText().contains(directoryName))
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("No %s in file manage list", directoryName)))
                .click();

        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(subDirectoryName);
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(createDirectoryBox().buttonSubmit()));
        createDirectoryBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage delete(String name) {
        waitForPageLoading();
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

    // todo: add file type
    public FileManagePage createFile(String fileName, String scripts) {
        waitForPageLoading();
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(buttonCreateFile()));

        buttonCreateFile().click();

        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/resource/file/create"));

        CreateFileBox createFileBox = new CreateFileBox();
        createFileBox.inputFileName().sendKeys(fileName);
        createFileBox.codeEditor().content(scripts);
        createFileBox.buttonSubmit().click();
        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/resource/file-manage"));
        return this;
    }

    public FileManagePage createFileUntilSuccess(String fileName, String scripts) {

        createFile(fileName, scripts);

        await()
                .untilAsserted(() -> assertThat(fileList())
                        .as("File list should contain newly-created file: " + fileName)
                        .extracting(WebElement::getText)
                        .anyMatch(it -> it.contains(fileName)));
        return this;
    }

    public FileManagePage editFile(String fileName, String scripts) {
        waitForPageLoading();
        fileList()
                .stream()
                .filter(it -> it.getText().contains(fileName))
                .flatMap(it -> it.findElements(By.className("btn-edit")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No edit button in file manage list"))
                .click();

        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/edit"));

        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.textToBePresentInElement(driver.findElement(By.tagName("body")), fileName));

        editFileBox().codeEditor().content(scripts);
        editFileBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage uploadFile(String filePath) {
        waitForPageLoading();
        buttonUploadFile().click();

        driver.setFileDetector(new LocalFileDetector());

        uploadFileBox().buttonUpload().sendKeys(filePath);
        uploadFileBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage downloadFile(String fileName) {
        waitForPageLoading();
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

    private void waitForPageLoading() {
        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/resource/file-manage"));
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
    public class EditFileBox {

        EditFileBox() {
            PageFactory.initElements(driver, this);
        }

        CodeEditor codeEditor = new CodeEditor(driver);

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
}
