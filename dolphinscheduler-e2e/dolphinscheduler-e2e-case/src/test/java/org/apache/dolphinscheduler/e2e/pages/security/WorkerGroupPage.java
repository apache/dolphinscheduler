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

import lombok.Getter;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import java.util.List;


@Getter
public final class WorkerGroupPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(className = "btn-create-worker-group")
    private WebElement buttonCreateWorkerGroup;

    @FindBy(className = "items")
    private List<WebElement> workerGroupList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    private final WorkerGroupForm createWorkerForm = new WorkerGroupForm();
    private final WorkerGroupForm editWorkerForm = new WorkerGroupForm();



    public WorkerGroupPage(RemoteWebDriver driver) {
        super(driver);
    }

    public WorkerGroupPage create(String workerGroupName) {
        buttonCreateWorkerGroup().click();

        createWorkerForm().inputWorkerGroupName().sendKeys(workerGroupName);
        createWorkerForm().btnSelectWorkerAddress().click();
        createWorkerForm().workerAddressList().click();

        createWorkerForm().buttonSubmit().click();

        return this;
    }

    public WorkerGroupPage update(String workerGroupName, String editWorkerGroupName) {
        workerGroupList()
                .stream()
                .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(workerGroupName))
                .flatMap(it -> it.findElements(By.className("edit")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No edit button in workerGroup list"))
                .click();

        editWorkerForm().inputWorkerGroupName().sendKeys(Keys.CONTROL + "a");
        editWorkerForm().inputWorkerGroupName().sendKeys(Keys.BACK_SPACE);
        editWorkerForm().inputWorkerGroupName().sendKeys(editWorkerGroupName);

        editWorkerForm().buttonSubmit().click();

        return this;
    }


    public WorkerGroupPage delete(String Worker) {
        workerGroupList()
            .stream()
            .filter(it -> it.findElement(By.className("name")).getAttribute("innerHTML").contains(Worker))
            .flatMap(it -> it.findElements(By.className("delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in workerGroup list"))
            .click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class WorkerGroupForm {
        WorkerGroupForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
            @FindBy(className = "input-worker-group-name"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputWorkerGroupName;

        @FindBys({
            @FindBy(className = "select-worker-address"),
            @FindBy(className = "n-base-selection"),
        })
        private WebElement btnSelectWorkerAddress;

        @FindBy(className = "n-base-select-option__content")
        private WebElement workerAddressList;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }
}
