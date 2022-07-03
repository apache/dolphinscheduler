package org.apache.dolphinscheduler.test.cases.ui;

import com.devskiller.jfairy.Fairy;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.dolphinscheduler.test.core.Browser;
import org.apache.dolphinscheduler.test.pages.security.common.SecurityPage;
import org.apache.dolphinscheduler.test.pages.security.tenantManage.TenantManagePage;
import org.apache.dolphinscheduler.test.pages.login.LoginPage;
import org.apache.dolphinscheduler.test.pages.navBar.NavBarPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class TenantE2ETest {
    private static final Logger log = getLogger(TenantE2ETest.class);
    private final Fairy fairy = Fairy.create();
    public WebDriver driver;
    public Browser browser;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        driver = new ChromeDriver();
        browser = new Browser(driver, "http://localhost:3000/login");
        browser.go();
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testCreateTenant() throws Exception {
        TenantManagePage tenantManagePage = browser.
                toPage(LoginPage.class).
                loginAs().
                toSecurityTab().
                toTenantManage();
        tenantManagePage.create(fairy.person().getUsername(), "eeee");
    }
}
