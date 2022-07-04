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

package org.apache.dolphinscheduler.test.pages.security.tenantManage;

import org.apache.dolphinscheduler.test.core.Page;
import org.apache.dolphinscheduler.test.endpoint.api.security.tenant.entity.TenantRequestEntity;
import org.apache.dolphinscheduler.test.pages.security.SecuritySidePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import java.lang.reflect.InvocationTargetException;
import java.util.List;


public class TenantManagePage extends SecuritySidePage {
    private TenantForm tenantForm;
    @FindBy(className = "btn-create-tenant")
    private WebElement createTenantButton;

    @FindBy(className = "n-input__input")
    private WebElement searchValInput;

    @FindBy(className = "n-button__content")
    private WebElement searchValButton;

    @FindBy(className = "items")
    private List<WebElement> tenantList;

    @FindBys({
            @FindBy(className = "n-popconfirm__action"),
            @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    public TenantManagePage() {
    }

    public TenantManagePage create(String tenant) {
        createTenantButton.click();
        tenantForm.create(tenant);
        return this;
    }

    public TenantManagePage create(String tenant, String description) {
        createTenantButton.click();
        tenantForm.create(tenant, description);
        return this;
    }

    public TenantManagePage create(TenantRequestEntity tenantRequestEntity) {
        createTenantButton.click();
        tenantForm.create(tenantRequestEntity.getTenantCode(), tenantRequestEntity.getDescription());
        return this;
    }

    public TenantManagePage update(String tenant, String description) {
        tenantList.stream()
                .filter(it -> it.findElement(By.className("tenant-code")).getAttribute("innerHTML").contains(tenant))
                .flatMap(it -> it.findElements(By.className("edit")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No edit button in tenant list"))
                .click();

        tenantForm.getInputDescription().sendKeys(Keys.CONTROL + "a");
        tenantForm.getInputDescription().sendKeys(Keys.BACK_SPACE);
        tenantForm.getInputDescription().sendKeys(description);
        tenantForm.getInputDescription().click();

        return this;
    }

    public TenantManagePage delete(String tenant) throws Exception {
        tenantList.stream()
                .filter(it -> it.getText().contains(tenant))
                .flatMap(it -> it.findElements(By.className("delete")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No delete button in user list"))
                .click();

//        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", buttonConfirm);

        buttonConfirm.click();
        return this;
    }

    @Override
    protected void onLoad(Page previousPage) {
        super.onLoad(previousPage);
        try {
            this.tenantForm = this.getBrowser().createPage(TenantForm.class);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public List<WebElement> getTenantList() {
        return tenantList;
    }

    public TenantForm getTenantForm() {
        return tenantForm;
    }
}
