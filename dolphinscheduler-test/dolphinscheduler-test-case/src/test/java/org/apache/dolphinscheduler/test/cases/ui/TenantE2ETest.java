package org.apache.dolphinscheduler.test.cases.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.dolphinscheduler.test.cases.common.AbstractTenantApiTest;
import org.apache.dolphinscheduler.test.core.Browser;
import org.apache.dolphinscheduler.test.pages.security.tenantManage.TenantManagePage;
import org.apache.dolphinscheduler.test.pages.login.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class TenantE2ETest extends AbstractTenantApiTest {
    private static final String tenant = System.getProperty("user.name");
    private static final Logger log = getLogger(TenantE2ETest.class);
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
        TenantManagePage page = browser.
                toPage(LoginPage.class).
                loginAs().
                toSecurityTab().
                toTenantManage();
        String tenantCode = fairy.person().getUsername();
        page.create(tenantCode, fairy.person().getMobileTelephoneNumber());

        await().untilAsserted(() -> assertThat(page.getTenantList())
                .as("Tenant list should contain newly-created tenant")
                .extracting(WebElement::getText)
                .anyMatch(it -> it.contains(tenantCode)));

    }
}
