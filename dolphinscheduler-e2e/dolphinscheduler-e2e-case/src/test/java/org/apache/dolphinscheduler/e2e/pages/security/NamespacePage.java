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
import org.openqa.selenium.support.PageFactory;

import lombok.Getter;

@Getter
public final class NamespacePage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(id = "btnCreateNamespace")
    private WebElement buttonCreateNamespace;

    @FindBy(className = "items")
    private List<WebElement> namespaceList;

    private final NamespaceForm createNamespaceForm;
    private final NamespaceForm editNamespaceForm;

    public NamespacePage(RemoteWebDriver driver) {
        super(driver);
        createNamespaceForm = new NamespaceForm();
        editNamespaceForm = new NamespaceForm();
    }

    public NamespacePage create(String namespaceName, String namespaceValue) {
        buttonCreateNamespace().click();
        createNamespaceForm().inputNamespaceName().sendKeys(namespaceName);
        createNamespaceForm().inputNamespaceValue().sendKeys(namespaceValue);
        createNamespaceForm().buttonSubmit().click();
        return this;
    }

    public NamespacePage update(String namespaceName, String editNamespaceName, String editNamespaceValue) {
        namespaceList()
                .stream()
                .filter(it -> it.findElement(By.className("namespaceName")).getAttribute("innerHTML").contains(namespaceName))
                .flatMap(it -> it.findElements(By.className("edit")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No edit button in namespace list"))
                .click();

        editNamespaceForm().inputNamespaceName().sendKeys(editNamespaceName);
        editNamespaceForm().inputNamespaceValue().sendKeys(editNamespaceValue);
        editNamespaceForm().buttonSubmit().click();

        return this;
    }

    @Getter
    public class NamespaceForm {
        NamespaceForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputNamespaceName")
        private WebElement inputNamespaceName;

        @FindBy(id = "inputNamespaceValue")
        private WebElement inputNamespaceValue;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;
    }
}
