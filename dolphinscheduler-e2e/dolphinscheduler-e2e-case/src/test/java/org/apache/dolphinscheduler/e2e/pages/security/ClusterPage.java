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

import lombok.Getter;

@Getter
public final class ClusterPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(className = "btn-create-cluster")
    private WebElement buttonCreateCluster;

    @FindBy(className = "items")
    private List<WebElement> clusterList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    private final ClusterForm createClusterForm;
    private final ClusterForm editClusterForm;

    public ClusterPage(RemoteWebDriver driver) {
        super(driver);
        createClusterForm = new ClusterForm();
        editClusterForm = new ClusterForm();
    }

    public ClusterPage create(String name, String config, String desc) {
        buttonCreateCluster().click();
        createClusterForm().inputClusterName().sendKeys(name);
        createClusterForm().inputClusterConfig().sendKeys(config);
        createClusterForm().inputClusterDesc().sendKeys(desc);

        createClusterForm().buttonSubmit().click();
        return this;
    }

    public ClusterPage update(String oldName, String name, String config, String desc) {
        clusterList()
                .stream()
                .filter(it -> it.findElement(By.className("cluster-name")).getAttribute("innerHTML").contains(oldName))
                .flatMap(it -> it.findElements(By.className("edit")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No edit button in cluster list"))
                .click();


        editClusterForm().inputClusterName().sendKeys(Keys.CONTROL + "a");
        editClusterForm().inputClusterName().sendKeys(Keys.BACK_SPACE);
        editClusterForm().inputClusterName().sendKeys(name);

        editClusterForm().inputClusterConfig().sendKeys(Keys.CONTROL + "a");
        editClusterForm().inputClusterConfig().sendKeys(Keys.BACK_SPACE);
        editClusterForm().inputClusterConfig().sendKeys(config);

        editClusterForm().inputClusterDesc().sendKeys(Keys.CONTROL + "a");
        editClusterForm().inputClusterDesc().sendKeys(Keys.BACK_SPACE);
        editClusterForm().inputClusterDesc().sendKeys(desc);

        editClusterForm().buttonSubmit().click();

        return this;
    }

    public ClusterPage delete(String name) {
        clusterList()
                .stream()
                .filter(it -> it.getText().contains(name))
                .flatMap(it -> it.findElements(By.className("delete")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No delete button in cluster list"))
                .click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class ClusterForm {
        ClusterForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
            @FindBy(className = "input-cluster-name"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputClusterName;

        @FindBys({
            @FindBy(className = "input-cluster-config"),
            @FindBy(tagName = "textarea"),
        })
        private WebElement inputClusterConfig;

        @FindBys({
            @FindBy(className = "input-cluster-desc"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputClusterDesc;

        @FindBys({
            @FindBy(className = "n-base-selection-tags"),
            @FindBy(className = "n-tag__content"),
        })
        private WebElement selectedWorkerGroup;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }
}
