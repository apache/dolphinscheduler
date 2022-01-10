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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import java.util.List;


@Getter
public final class WorkerGroupPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(id = "btnCreateWorkerGroup")
    private WebElement buttonCreateWorkerGroup;

    @FindBy(className = "items")
    private List<WebElement> workerGroupList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    private final WorkerGroupForm createWorkerForm = new WorkerGroupForm();
    private final WorkerGroupForm editWorkerForm = new WorkerGroupForm();



    public WorkerGroupPage(RemoteWebDriver driver) {
        super(driver);
    }

    public WorkerGroupPage create(String workerGroupName) {
        buttonCreateWorkerGroup().click();

        createWorkerForm().inputWorkerGroupName().sendKeys(workerGroupName);
        createWorkerForm().selectWorkerAddress().click();
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

        editWorkerForm().inputWorkerGroupName().clear();
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

        buttonConfirm()
            .stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No confirm button when deleting"))
            .click();

        return this;
    }

    @Getter
    public class WorkerGroupForm {
        WorkerGroupForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputWorkerGroupName")
        private WebElement inputWorkerGroupName;

        @FindBy(id = "selectWorkerAddress")
        private WebElement selectWorkerAddress;

        @FindBy(className = "vue-treeselect__menu")
        private WebElement workerAddressList;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }
}
