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

package org.apache.dolphinscheduler.test.pages.security;

import org.apache.dolphinscheduler.test.pages.navBar.NavBarPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SecuritySidePage extends NavBarPage {
    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(1) > .n-menu-item-content")
    private WebElement tenantManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(2) > .n-menu-item-content")
    private WebElement userManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(3) > .n-menu-item-content")
    private WebElement alarmGroupManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(4) > .n-menu-item-content")
    private WebElement alarmInstanceManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(5) > .n-menu-item-content")
    private WebElement workerGroupManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(6) > .n-menu-item-content")
    private WebElement queueManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(7) > .n-menu-item-content")
    private WebElement environmentManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(8) > .n-menu-item-content")
    private WebElement clusterManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(9) > .n-menu-item-content")
    private WebElement namespaceManage;

    @FindBy(css = ".tab-vertical > .n-menu-item:nth-child(10) > .n-menu-item-content")
    private WebElement tokenManage;

    public WebElement toTenantManage() {
        return tenantManage;
    }

    public WebElement toUserManage() {
        return userManage;
    }

    public WebElement toAlarmGroupManage() {
        return alarmGroupManage;
    }

    public WebElement toAlarmInstanceManage() {
        return alarmInstanceManage;
    }

    public WebElement toWorkerGroupManage() {
        return workerGroupManage;
    }

    public WebElement toQueueManage() {
        return queueManage;
    }

    public WebElement toEnvironmentManage() {
        return environmentManage;
    }

    public WebElement toClusterManage() {
        return clusterManage;
    }

    public WebElement toNamespaceManage() {
        return namespaceManage;
    }

    public WebElement toTokenManage() {
        return tokenManage;
    }
}
