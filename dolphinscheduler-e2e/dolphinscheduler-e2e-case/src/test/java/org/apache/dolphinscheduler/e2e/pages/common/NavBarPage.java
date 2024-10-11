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

package org.apache.dolphinscheduler.e2e.pages.common;

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.datasource.DataSourcePage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectPage;
import org.apache.dolphinscheduler.e2e.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;

import lombok.Getter;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
public class NavBarPage {

    protected final RemoteWebDriver driver;

    @FindBy(xpath = "//div[contains(@class, 'tab-horizontal')]//div[contains(@role,'menubar')]//span[contains(text(), 'Project')]")
    private WebElement projectTab;

    @FindBy(xpath = "//div[contains(@class, 'tab-horizontal')]//div[contains(@role,'menubar')]//span[contains(text(), 'Resources')]")
    private WebElement resourceTab;

    @FindBy(xpath = "//div[contains(@class, 'tab-horizontal')]//div[contains(@role,'menubar')]//span[contains(text(), 'Data Quality')]")
    private WebElement dataQualityTab;

    @FindBy(xpath = "//div[contains(@class, 'tab-horizontal')]//div[contains(@role,'menubar')]//span[contains(text(), 'Datasource')]")
    private WebElement dataSourceTab;

    @FindBy(xpath = "//div[contains(@class, 'tab-horizontal')]//div[contains(@role,'menubar')]//span[contains(text(), 'Security')]")
    private WebElement securityTab;

    public NavBarPage(RemoteWebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);
    }

    public <T extends NavBarItem> T goToNav(Class<T> nav) {
        if (nav == ProjectPage.class) {
            WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.elementToBeClickable(projectTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", projectTab());
            WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/projects/list"));
            return nav.cast(new ProjectPage(driver));
        }

        if (nav == SecurityPage.class) {
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .until(ExpectedConditions.elementToBeClickable(securityTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", securityTab());
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .until(ExpectedConditions.urlContains("/security/tenant-manage"));
            return nav.cast(new SecurityPage(driver));
        }

        if (nav == ResourcePage.class) {
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .until(ExpectedConditions.elementToBeClickable(resourceTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", resourceTab());
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .until(ExpectedConditions.urlContains("/resource/file-manage"));
            return nav.cast(new ResourcePage(driver));
        }

        if (nav == DataSourcePage.class) {
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .until(ExpectedConditions.elementToBeClickable(dataSourceTab));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dataSourceTab());
            WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/datasource"));
            return nav.cast(new DataSourcePage(driver));
        }

        throw new UnsupportedOperationException("Unknown nav bar");
    }

    public interface NavBarItem {
    }
}
