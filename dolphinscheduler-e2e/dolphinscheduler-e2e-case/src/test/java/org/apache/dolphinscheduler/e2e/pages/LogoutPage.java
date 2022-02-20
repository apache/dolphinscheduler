package org.apache.dolphinscheduler.e2e.pages;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import lombok.Getter;

@Getter
public final class LogoutPage extends NavBarPage {
    @FindBy(className = "el-dropdown-link")
    private WebElement buttonDropDown;

    @FindBy(className = "logout")
    private WebElement buttonLogout;

    public LogoutPage(RemoteWebDriver driver) {
        super(driver);
    }

    public LogoutPage logout(String user) {
        buttonDropDown().click();
        buttonLogout().click();
        return this;
    }
}
