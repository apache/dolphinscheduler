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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import lombok.Getter;

@Getter
public final class TenantPage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(id = "btnCreateTenant")
    private WebElement buttonCreateTenant;

    @FindBy(className = "items")
    private List<WebElement> tenantList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private WebElement buttonConfirm;

    private final CreateTenantForm createTenantForm;

    public TenantPage(RemoteWebDriver driver) {
        super(driver);

        createTenantForm = new CreateTenantForm();
    }

    public TenantPage create(String tenant) {
        return create(tenant, "");
    }

    public TenantPage create(String tenant, String description) {
        buttonCreateTenant().click();
        createTenantForm().inputTenantCode().sendKeys(tenant);
        createTenantForm().inputDescription().sendKeys(description);
        createTenantForm().buttonSubmit().click();

        return this;
    }

    public TenantPage delete(String tenant) {
        tenantList()
            .stream()
            .filter(it -> it.getText().contains(tenant))
            .flatMap(it -> it.findElements(By.className("delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in tenant list"))
            .click();

        buttonConfirm().click();

        return this;
    }

    @Getter
    public class CreateTenantForm {
        CreateTenantForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputTenantCode")
        private WebElement inputTenantCode;

        @FindBy(id = "selectQueue")
        private WebElement selectQueue;

        @FindBy(id = "inputDescription")
        private WebElement inputDescription;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }
}
