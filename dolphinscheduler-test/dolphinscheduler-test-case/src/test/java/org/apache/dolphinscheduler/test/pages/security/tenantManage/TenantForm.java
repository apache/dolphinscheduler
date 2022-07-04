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

import org.apache.dolphinscheduler.test.core.Module;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

public class TenantForm extends Module {

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
    
    public TenantForm() {}

    public void create(String tenant, String description) {
        inputTenantCode.sendKeys(tenant);
        inputDescription.sendKeys(description);
        buttonSubmit.click();
    }

    public void create(String tenant) {
        inputTenantCode.sendKeys(tenant);
        buttonSubmit.click();
    }

    public WebElement getInputTenantCode() {
        return inputTenantCode;
    }

    public WebElement getSelectQueue() {
        return selectQueue;
    }

    public WebElement getInputDescription() {
        return inputDescription;
    }

    public WebElement getButtonSubmit() {
        return buttonSubmit;
    }

    public WebElement getButtonCancel() {
        return buttonCancel;
    }
}
