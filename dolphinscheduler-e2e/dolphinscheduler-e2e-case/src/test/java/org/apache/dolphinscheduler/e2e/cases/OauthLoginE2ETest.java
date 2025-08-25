package org.apache.dolphinscheduler.e2e.cases;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DolphinScheduler(composeFiles = "docker/oauth/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class OauthLoginE2ETest {


    @Test
    @Order(10)
    public void testAdminUserLoginSuccess() {


        String realm = "dolphinscheduler";
        String clientId = "dolphinscheduler-ui";
        String redirectUri = "http://localhost:12345/dolphinscheduler/redirect/login/oauth2";
        String username = "test-user";
        String password = "test-password";


        String authUrl = null;
        try {
            authUrl = "http://localhost:8080/realms/" + realm +
                    "/protocol/openid-connect/auth?client_id=" + clientId +
                    "&response_type=code&scope=openid&redirect_uri="
                    + URLEncoder.encode(redirectUri, String.valueOf(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        driver.get(authUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement userInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        userInput.sendKeys(username);

        WebElement passInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passInput.sendKeys(password);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("kc-login")));
        loginButton.click();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String currentUrl = driver.getCurrentUrl();
        driver.quit();

        if (!currentUrl.contains("code=")) {
            throw new RuntimeException("the URL: " + currentUrl);
        }

        String code = currentUrl.split("code=")[1].split("&")[0];
        HttpResponse response = loginPage.loginByOauth(code, "keycloak");
        assertThat(response.getStatusCode()).isEqualTo(302);
    }

    public HttpResponse loginByOauth(String code, String provider) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("provider", provider);
        OkHttpClient requestClient = new OkHttpClient.Builder().followRedirects(false).build();
        return requestClient.post("/rediect/login/oauth2", null, params);
    }
}
