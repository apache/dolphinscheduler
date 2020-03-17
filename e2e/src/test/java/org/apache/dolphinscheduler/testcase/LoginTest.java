package org.apache.dolphinscheduler.testcase;

import org.apache.dolphinscheduler.page.LoginPage;
import org.testng.annotations.Test;

import static org.apache.dolphinscheduler.base.BaseTest.driver;

@Test(groups={"functionTests"})
public class LoginTest {
    private LoginPage loginPage;

    @Test(description = "LoginTest",priority=1)
    public void testLogin() throws InterruptedException {
        loginPage = new LoginPage(driver);
//        loginPage.jumpPageEnlish();
        loginPage.jumpPageChinese();
        loginPage.login();
    }
}
