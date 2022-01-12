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
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import java.util.List;


@Getter
public class FileManagePage extends NavBarPage implements ResourcePage.Tab {
    @FindBy(id = "btnCreateDirectory")
    private WebElement buttonCreateDirectory;

    private final CreateDirectoryBox createDirectoryBox;

    @FindBy(className = "items")
    private List<WebElement> fileList;

    @FindBy(id = "delete")
    private WebElement buttonDelete;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    public FileManagePage(RemoteWebDriver driver) {
        super(driver);

        createDirectoryBox = new CreateDirectoryBox();
    }

    public FileManagePage createDirectory(String name, String description) {
        buttonCreateDirectory().click();

        createDirectoryBox().inputDirectoryName().sendKeys(name);
        createDirectoryBox().inputDescription().sendKeys(description);
        createDirectoryBox().buttonSubmit().click();

        return this;
    }

    public FileManagePage delete(String name) {
        fileList()
            .stream()
            .filter(it -> it.getText().contains(name))
            .flatMap(it -> it.findElements(By.id("delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in file manage list"))
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
    public class CreateDirectoryBox {
        CreateDirectoryBox() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputDirectoryName")
        private WebElement inputDirectoryName;

        @FindBy(id = "inputDescription")
        private WebElement inputDescription;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }
}
