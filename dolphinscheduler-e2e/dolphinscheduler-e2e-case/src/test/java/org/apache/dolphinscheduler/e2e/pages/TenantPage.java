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

package org.apache.dolphinscheduler.e2e.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import lombok.Getter;

@Getter
public final class TenantPage {
    private final WebDriver driver;

    @FindBy(id = "button-create-tenant")
    private WebElement buttonCreateTenant;

    @FindBy(className = "rows-tenant")
    private List<WebElement> tenantList;

    @FindBys({
            @FindBy(className = "el-popconfirm"),
            @FindBy(className = "el-button--primary"),
    })
    private WebElement buttonConfirm;

    private final CreateTenantForm createTenantForm;

    public TenantPage(WebDriver driver) {
        this.driver = driver;
        this.createTenantForm = new CreateTenantForm();

        PageFactory.initElements(driver, this);
    }

    @Getter
    public class CreateTenantForm {
        CreateTenantForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "input-tenant-code")
        private WebElement inputTenantCode;

        @FindBy(id = "select-queue")
        private WebElement selectQueue;

        @FindBy(id = "input-description")
        private WebElement inputDescription;

        @FindBy(id = "button-submit")
        private WebElement buttonSubmit;

        @FindBy(id = "button-cancel")
        private WebElement buttonCancel;
    }
}
