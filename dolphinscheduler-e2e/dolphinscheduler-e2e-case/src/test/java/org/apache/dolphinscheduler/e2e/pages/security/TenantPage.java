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

import lombok.Getter;

@Getter
public final class TenantPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(className = "btn-create-tenant")
    private WebElement buttonCreateTenant;

    @FindBy(className = "items")
    private List<WebElement> tenantList;

    @FindBys({
        @FindBy(className = "n-popconfirm__action"),
        @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    @FindBy(className = "tenant-code")
    private WebElement tenantCode;

    private final TenantForm tenantForm;
    private final TenantForm editTenantForm;

    public TenantPage(RemoteWebDriver driver) {
        super(driver);

        tenantForm = new TenantForm();
        editTenantForm = new TenantForm();
    }

    public TenantPage create(String tenant) {
        return create(tenant, "");
    }

    public TenantPage create(String tenant, String description) {
        buttonCreateTenant().click();
        tenantForm().inputTenantCode().sendKeys(tenant);
        tenantForm().inputDescription().sendKeys(description);
        tenantForm().buttonSubmit().click();

        return this;
    }

    public TenantPage update(String tenant, String description) {
        tenantList().stream()
            .filter(it -> it.findElement(By.className("tenant-code")).getAttribute("innerHTML").contains(tenant))
            .flatMap(it -> it.findElements(By.className("edit")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No edit button in tenant list"))
            .click();

        editTenantForm().inputDescription().sendKeys(Keys.CONTROL + "a");
        editTenantForm().inputDescription().sendKeys(Keys.BACK_SPACE);
        editTenantForm().inputDescription().sendKeys(description);
        editTenantForm().buttonSubmit().click();

        return this;
    }

    public TenantPage delete(String tenant) {
        tenantList()
            .stream()
            .filter(it -> it.getText().contains(tenant))
            .flatMap(it -> it.findElements(By.className("delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in user list"))
            .click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class TenantForm {
        TenantForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
                @FindBy(className = "input-tenant-code"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputTenantCode;

        @FindBy(className = "select-queue")
        private WebElement selectQueue;

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
