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
package org.apache.dolphinscheduler.e2e.pages.resource;

import lombok.Getter;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.security.TenantPage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


@Getter
public class ResourcePage extends NavBarPage implements NavBarPage.NavBarItem {
    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(1) > .n-menu-item-content")
    private WebElement fileManageTab;

    @FindBy(css = ".tab-vertical .n-submenu:nth-of-type(2) .n-menu-item:nth-of-type(1) > .n-menu-item-content")
    private WebElement udfManageTab;

    @FindBy(css = ".tab-vertical .n-submenu:nth-of-type(2) .n-menu-item:nth-of-type(2) > .n-menu-item-content")
    private WebElement functionManageTab;

    public ResourcePage(RemoteWebDriver driver) {
        super(driver);
    }

    public <T extends ResourcePage.Tab> T goToTab(Class<T> tab) {
        if (tab == FileManagePage.class) {
            new WebDriverWait(driver, 10).until(ExpectedConditions.urlContains("/resource"));
            new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(fileManageTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fileManageTab());
            return tab.cast(new FileManagePage(driver));
        }

        if (tab == UdfManagePage.class) {
            new WebDriverWait(driver, 10).until(ExpectedConditions.urlContains("/resource"));
            new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(udfManageTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", udfManageTab());
            return tab.cast(new UdfManagePage(driver));
        }

        if (tab == FunctionManagePage.class) {
            new WebDriverWait(driver, 10).until(ExpectedConditions.urlContains("/resource"));
            new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(functionManageTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", functionManageTab());
            return tab.cast(new FunctionManagePage(driver));
        }

        throw new UnsupportedOperationException("Unknown tab: " + tab.getName());
    }

    public interface Tab {
    }
}
