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

package org.apache.dolphinscheduler.e2e.pages.resource;

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import lombok.Getter;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
public class ResourcePage extends NavBarPage implements NavBarPage.NavBarItem {

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(1) > .n-menu-item-content")
    private WebElement fileManageTab;

    public ResourcePage(RemoteWebDriver driver) {
        super(driver);

        PageFactory.initElements(driver, this);
    }

    public <T extends ResourcePage.Tab> T goToTab(Class<T> tab) {
        if (tab == FileManagePage.class) {
            WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/resource"));
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .until(ExpectedConditions.elementToBeClickable(fileManageTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fileManageTab());
            WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/file-manage"));
            return tab.cast(new FileManagePage(driver));
        }

        throw new UnsupportedOperationException("Unknown tab: " + tab.getName());
    }

    public interface Tab {
    }
}
