package org.apache.dolphinscheduler.test.pages.login;

import org.apache.dolphinscheduler.test.core.Page;
import org.apache.dolphinscheduler.test.pages.home.HomePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends Page {
    @FindBys({
            @FindBy(className = "input-user-name"),
            @FindBy(tagName = "input"),
    })
    @CacheLookup
    private WebElement inputUsername;

    @FindBys({
            @FindBy(className = "input-password"),
            @FindBy(tagName = "input"),
    })
    @CacheLookup
    private WebElement inputPassword;

    @FindBy(className = "btn-login")
    @CacheLookup
    private WebElement buttonLogin;

    @FindBy(className = "n-switch__button")
    @CacheLookup
    private WebElement buttonSwitchLanguage;

    public LoginPage() {
    }

    @Override
    protected void isLoaded() throws Error {
        this.waitFor(10).until(ExpectedConditions.elementToBeClickable(buttonSwitchLanguage));
    }

    @Override
    public void onUnload(Page nextPage) {
        nextPage.waitFor(10).until(ExpectedConditions.urlContains("/home"));
    }

    public HomePage loginAs() throws Exception {
        inputUsername.sendKeys("admin");
        inputPassword.sendKeys("dolphinscheduler123");
        buttonLogin.click();
        return this.to(new HomePage());
    }

    public HomePage loginAs(String userName, String passwd) throws Exception {
        inputUsername.sendKeys(userName);
        inputPassword.sendKeys(passwd);
        buttonLogin.click();
        return this.to(new HomePage());
    }

}
