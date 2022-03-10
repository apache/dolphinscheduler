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

package org.apache.dolphinscheduler.e2e.pages.security;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage.NavBarItem;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.Getter;

@Getter
public class SecurityPage extends NavBarPage implements NavBarItem {
//    @FindBys({
//        @FindBy(className = "tab-vertical"),
//        @FindBy(xpath = "//div[contains(@class, 'n-menu-item')][1]"),
//        @FindBy(className = "n-menu-item-content"),
//    })
    @FindBy(xpath = "//div[contains(@class, 'tab-vertical')]//div[contains(@class, 'n-menu-item')" +
        "][1]//div[contains(@class, 'n-menu-item-content')]")
    private WebElement menuTenantManage;


//    @FindBy(xpath = "//div[contains(@class, 'tab-vertical')]//div[contains(@class, 'n-menu-item')" +
//            "][2]//div[contains(@class, 'n-menu-item-content')]")
    @FindBys({
            @FindBy(className = "tab-vertical"),
            @FindBy(xpath = "/*/div[contains(@class, 'n-menu-item')][2]//div[contains(@class, 'n-menu-item-content')]")
    })
    private WebElement menUserManage;

    @FindBys({
        @FindBy(className = "tab-vertical"),
        @FindBy(xpath = "//div[contains(@class, 'n-menu-item')][5]"),
        @FindBy(className = "n-menu-item-content"),
    })
    private WebElement menWorkerGroupManage;

    @FindBys({
        @FindBy(className = "tab-vertical"),
        @FindBy(xpath = "//div[contains(@class, 'n-menu-item')][6]"),
        @FindBy(className = "n-menu-item-content"),
    })
    private WebElement menuQueueManage;

    @FindBys({
        @FindBy(className = "tab-vertical"),
        @FindBy(xpath = "//div[contains(@class, 'n-menu-item')][7]"),
        @FindBy(className = "n-menu-item-content"),
    })
    private WebElement menuEnvironmentManage;

    @FindBys({
        @FindBy(className = "tab-vertical"),
        @FindBy(xpath = "//div[contains(@class, 'n-menu-item')][8]"),
        @FindBy(className = "n-menu-item-content"),
    })
    private WebElement menuNamespaceManage;

    @FindBys({
        @FindBy(className = "tab-vertical"),
        @FindBy(xpath = "//div[contains(@class, 'n-menu-item')][9]"),
        @FindBy(className = "n-menu-item-content"),
    })
    private WebElement menuTokenManage;

    public SecurityPage(RemoteWebDriver driver) {
        super(driver);
    }

    public <T extends SecurityPage.Tab> T goToTab(Class<T> tab) {
        if (tab == TenantPage.class) {
            new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(menuTenantManage));
            menuTenantManage.click();
            return tab.cast(new TenantPage(driver));
        }

        if (tab == UserPage.class) {
            new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(menUserManage));
            menUserManage.click();
            return tab.cast(new UserPage(driver));
        }

        if (tab == WorkerGroupPage.class) {
            new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(menWorkerGroupManage));
            menWorkerGroupManage.click();
            return tab.cast(new WorkerGroupPage(driver));
        }

        if (tab == QueuePage.class) {
            menuQueueManage().click();
            return tab.cast(new QueuePage(driver));
        }

        if (tab == EnvironmentPage.class) {
            menuEnvironmentManage().click();
            return tab.cast(new EnvironmentPage(driver));
        }

        if (tab == TokenPage.class) {
            menuTokenManage().click();
            return tab.cast(new TokenPage(driver));
        }

        if (tab == NamespacePage.class) {
            menuNamespaceManage().click();
            return tab.cast(new NamespacePage(driver));
        }

        throw new UnsupportedOperationException("Unknown tab: " + tab.getName());
    }

    public interface Tab {
    }
}
