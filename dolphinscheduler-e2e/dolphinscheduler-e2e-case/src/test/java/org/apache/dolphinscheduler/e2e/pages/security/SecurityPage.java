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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.Getter;

@Getter
public class SecurityPage extends NavBarPage implements NavBarItem {
    @FindBy(className = "tab-tenant-manage")
    private WebElement menuTenantManage;

    @FindBy(className = "tab-user-manage")
    private WebElement menUserManage;

    @FindBy(className = "tab-worker-group-manage")
    private WebElement menWorkerGroupManage;

    @FindBy(className = "tab-queue-manage")
    private WebElement menuQueueManage;

    @FindBy(className = "tab-environment-manage")
    private WebElement menuEnvironmentManage;

    @FindBy(className = "tab-token-manage")
    private WebElement menuTokenManage;

    public SecurityPage(RemoteWebDriver driver) {
        super(driver);
    }

    public <T extends SecurityPage.Tab> T goToTab(Class<T> tab) {
        if (tab == TenantPage.class) {
            WebElement menuTenantManageElement = new WebDriverWait(driver, 60)
                    .until(ExpectedConditions.elementToBeClickable(menuTenantManage));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuTenantManageElement);
            return tab.cast(new TenantPage(driver));
        }
        if (tab == UserPage.class) {
            WebElement menUserManageElement = new WebDriverWait(driver, 60)
                    .until(ExpectedConditions.elementToBeClickable(menUserManage));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menUserManageElement);
            new WebDriverWait(driver, 25).until(ExpectedConditions.urlContains("/#/security/users"));
            return tab.cast(new UserPage(driver));
        }
        if (tab == WorkerGroupPage.class) {
            WebElement menWorkerGroupManageElement = new WebDriverWait(driver, 60)
                    .until(ExpectedConditions.elementToBeClickable(menWorkerGroupManage));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menWorkerGroupManageElement);
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
        throw new UnsupportedOperationException("Unknown tab: " + tab.getName());
    }

    public interface Tab {
    }
}
