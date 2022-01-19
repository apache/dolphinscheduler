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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import lombok.Getter;

@Getter
public class SecurityPage extends NavBarPage implements NavBarItem {
    @FindBy(className = "tab-tenant-manage")
    private WebElement menuTenantManage;

    @FindBy(className = "tab-user-manage")
    private WebElement menUserManage;

    @FindBy(className = "tab-worker-group-manage")
    private WebElement menWorkerGroupManage;

    @FindBy(className = "tab-warning-group-manage")
    private WebElement menWarningGroupManage;

    @FindBy(className = "tab-warning-instance-manage")
    private WebElement menWarningInstanceManage;


    public SecurityPage(RemoteWebDriver driver) {
        super(driver);
    }

    public <T extends SecurityPage.Tab> T goToTab(Class<T> tab) {
        if (tab == TenantPage.class) {
            menuTenantManage().click();
            return tab.cast(new TenantPage(driver));
        }
        if (tab == UserPage.class) {
            menUserManage().click();
            return tab.cast(new UserPage(driver));
        }
        if (tab == WarningGroupPage.class) {
            menWarningGroupManage().click();
            return tab.cast(new WarningGroupPage(driver));
        }
        if (tab == WarningInstancePage.class) {
            menWarningInstanceManage().click();
            return tab.cast(new WarningInstancePage(driver));
        }
        if (tab == WorkerGroupPage.class) {
            menWorkerGroupManage().click();
            return tab.cast(new WorkerGroupPage(driver));
        }
        throw new UnsupportedOperationException("Unknown tab: " + tab.getName());
    }

    public interface Tab {
    }
}
