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


package org.apache.dolphinscheduler.test.pages.security.common;

import org.apache.dolphinscheduler.test.core.Page;
import org.apache.dolphinscheduler.test.pages.navBar.NavBarPage;
import org.apache.dolphinscheduler.test.pages.security.tenantManage.TenantManagePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SecurityPage extends NavBarPage {

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(1) > .n-menu-item-content")
    private WebElement menuTenantManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(2) > .n-menu-item-content")
    private WebElement menUserManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(5) > .n-menu-item-content")
    private WebElement menWorkerGroupManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(6) > .n-menu-item-content")
    private WebElement menuQueueManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(7) > .n-menu-item-content")
    private WebElement menuEnvironmentManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(8) > .n-menu-item-content")
    private WebElement menuClusterManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(9) > .n-menu-item-content")
    private WebElement menuNamespaceManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(10) > .n-menu-item-content")
    private WebElement menuTokenManage;

    @FindBy(css = ".tab-horizontal .n-menu-item:nth-child(7) > .n-menu-item-content")
    private WebElement securityTab;

    public SecurityPage() {}

    public TenantManagePage toTenantManage() throws Exception {
        this.waitFor(10).until(ExpectedConditions.urlContains("/security"));
        menuTenantManage.click();
        return this.to(new TenantManagePage());
    }

    @Override
    protected void onUnload(Page nextPage) {
        nextPage.waitFor(10).until(ExpectedConditions.elementToBeClickable(menuTenantManage));
        nextPage.waitFor(10).until(ExpectedConditions.elementToBeClickable(menUserManage));
        nextPage.waitFor(10).until(ExpectedConditions.elementToBeClickable(menWorkerGroupManage));
        nextPage.waitFor(10).until(ExpectedConditions.elementToBeClickable(menuEnvironmentManage));
        nextPage.waitFor(10).until(ExpectedConditions.elementToBeClickable(menuNamespaceManage));
        nextPage.waitFor(10).until(ExpectedConditions.elementToBeClickable(menuTokenManage));
        nextPage.waitFor(10).until(ExpectedConditions.elementToBeClickable(securityTab));
    }
}
