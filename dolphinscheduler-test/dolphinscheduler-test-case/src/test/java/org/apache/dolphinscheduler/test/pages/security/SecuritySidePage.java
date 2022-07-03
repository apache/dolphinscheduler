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
