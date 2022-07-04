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

package org.apache.dolphinscheduler.test.pages.navBar;

import org.apache.dolphinscheduler.test.core.Page;
import org.apache.dolphinscheduler.test.core.exception.PageInstanceNotInitializedException;
import org.apache.dolphinscheduler.test.pages.dataQuality.DataQuality;
import org.apache.dolphinscheduler.test.pages.dataSource.DataSourcePage;
import org.apache.dolphinscheduler.test.pages.monitor.MonitorPage;
import org.apache.dolphinscheduler.test.pages.project.ProjectPage;
import org.apache.dolphinscheduler.test.pages.resource.ResourcePage;
import org.apache.dolphinscheduler.test.pages.security.common.SecurityPage;
import org.apache.dolphinscheduler.test.pages.home.HomePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class NavBarPage extends Page {

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(1) > .n-menu-item-content")
    @CacheLookup
    private WebElement homeTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(2) > .n-menu-item-content")
    @CacheLookup
    private WebElement projectTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(3) > .n-menu-item-content")
    @CacheLookup
    private WebElement resourceTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(4) > .n-menu-item-content")
    @CacheLookup
    private WebElement dataQualityTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(5) > .n-menu-item-content")
    @CacheLookup
    private WebElement dataSourceTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(6) > .n-menu-item-content")
    @CacheLookup
    private WebElement monitorTab;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(7) > .n-menu-item-content")
    private WebElement securityTab;

    public NavBarPage() {
    }

    public void toNav() throws InterruptedException {
        dataQualityTab.click();
        Thread.sleep(3000);
        homeTab.click();
        Thread.sleep(3000);
    }

    public HomePage toHomeTab() throws PageInstanceNotInitializedException {
        homeTab.click();
        return this.to(new HomePage());
    }

    public ProjectPage toProjectTab() throws PageInstanceNotInitializedException {
        projectTab.click();
        return this.to(new ProjectPage());
    }

    public ResourcePage toResourceTab() throws PageInstanceNotInitializedException {
        resourceTab.click();
        return this.to(new ResourcePage());
    }

    public DataQuality toDataQualityTab() throws PageInstanceNotInitializedException {
        dataQualityTab.click();
        return this.to(new DataQuality());
    }

    public DataSourcePage toDataSourceTab() throws PageInstanceNotInitializedException {
        dataSourceTab.click();
        return this.to(new DataSourcePage());
    }

    public MonitorPage toMonitorTab() throws PageInstanceNotInitializedException {
        monitorTab.click();
        return this.to(new MonitorPage());
    }


    public SecurityPage toSecurityTab() throws PageInstanceNotInitializedException {
        securityTab.click();
        return this.to(new SecurityPage());
    }

    @Override
    protected void onUnload(Page nextPage) {
        nextPage.waitFor().until(ExpectedConditions.elementToBeClickable(homeTab));
        nextPage.waitFor().until(ExpectedConditions.elementToBeClickable(projectTab));
        nextPage.waitFor().until(ExpectedConditions.elementToBeClickable(resourceTab));
        nextPage.waitFor().until(ExpectedConditions.elementToBeClickable(dataQualityTab));
        nextPage.waitFor().until(ExpectedConditions.elementToBeClickable(dataSourceTab));
        nextPage.waitFor().until(ExpectedConditions.elementToBeClickable(monitorTab));
        nextPage.waitFor().until(ExpectedConditions.elementToBeClickable(securityTab));
    }
}
