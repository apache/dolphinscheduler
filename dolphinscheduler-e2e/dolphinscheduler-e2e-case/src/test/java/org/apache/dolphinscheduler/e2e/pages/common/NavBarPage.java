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

package org.apache.dolphinscheduler.e2e.pages.common;

import org.apache.dolphinscheduler.e2e.pages.datasource.DataSourcePage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.Getter;

import java.time.Duration;

@Getter
public class NavBarPage {
    protected final RemoteWebDriver driver;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(2) > .n-menu-item-content")
    private WebElement projectTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(3) > .n-menu-item-content")
    private WebElement resourceTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(4) > .n-menu-item-content")
    private WebElement dataQualityTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(5) > .n-menu-item-content")
    private WebElement dataSourceTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(7) > .n-menu-item-content")
    private WebElement securityTab;

    public NavBarPage(RemoteWebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);
    }

    public <T extends NavBarItem> T goToNav(Class<T> nav) {
        if (nav == ProjectPage.class) {
            new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.elementToBeClickable(projectTab));
            projectTab.click();
            return nav.cast(new ProjectPage(driver));
        }

        if (nav == SecurityPage.class) {
            new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.elementToBeClickable(securityTab));
            securityTab.click();
            return nav.cast(new SecurityPage(driver));
        }

        if (nav == ResourcePage.class) {
            new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.elementToBeClickable(resourceTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", resourceTab());
            return nav.cast(new ResourcePage(driver));
        }

        if (nav == DataSourcePage.class) {
            new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.elementToBeClickable(dataSourceTab));
            dataSourceTab.click();
            return nav.cast(new DataSourcePage(driver));
        }

        throw new UnsupportedOperationException("Unknown nav bar");
    }

    public interface NavBarItem {
    }
}
