package org.apache.dolphinscheduler.testcase;

import org.apache.dolphinscheduler.page.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.apache.dolphinscheduler.base.BaseTest.driver;

@Test(groups={"functionTests","login"})
public class LoginTest {
    private LoginPage loginPage;

    @Test(description = "LoginTest", priority = 1)
    public void testLogin() throws InterruptedException {
        loginPage = new LoginPage(driver);
        System.out.println("===================================");
        System.out.println("jump to Chinese login page");
        loginPage.jumpPageChinese();

        System.out.println("start login");
        assert  loginPage.login();
        System.out.println("end login");
        System.out.println("===================================");

    }
}
