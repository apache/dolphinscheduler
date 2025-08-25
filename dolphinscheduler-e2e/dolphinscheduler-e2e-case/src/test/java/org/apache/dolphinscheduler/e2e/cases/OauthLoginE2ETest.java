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

package org.apache.dolphinscheduler.e2e.cases;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

import io.github.bonigarcia.wdm.WebDriverManager;

@DolphinScheduler(composeFiles = "docker/oauth-login/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class OauthLoginE2ETest {

    public static final String DOLPHINSCHEDULER_API_URL = "http://0.0.0.0:12345/dolphinscheduler";

    public static final String REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";

    public static final String QUESTION_MARK = "?";

    public static final String EQUAL_MARK = "=";

    public static final String AND_MARK = "&";

    @Test
    @Order(10)
    public void testAdminUserLoginSuccess() {

        String realm = "dolphinscheduler";
        String clientId = "dolphinscheduler-ui";
        String redirectUri = "http://localhost:12345/dolphinscheduler/redirect/login/oauth2";
        String username = "test-user";
        String password = "test-password";

        String authUrl = "http://localhost:8080/realms/" + realm + "/protocol/openid-connect/auth?client_id=" + clientId
                + "&response_type=code&scope=openid&redirect_uri="
                + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless=new");

        WebDriverManager.chromedriver().setup();
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
        assertThat(loginByOauth(code)).isEqualTo(302);
    }

    public int loginByOauth(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("provider", "keycloak");
        OkHttpClient requestClient = new OkHttpClient.Builder().followRedirects(false).build();

        Map<String, String> headers = new HashMap<>();

        String requestUrl = String.format("%s%s", DOLPHINSCHEDULER_API_URL, "/rediect/login/oauth2");
        headers.put("Content-Type", REQUEST_CONTENT_TYPE);
        Headers headersBuilder = Headers.of(headers);
        RequestBody requestBody = FormBody.create(getParams(params), MediaType.parse(REQUEST_CONTENT_TYPE));
        log.info("POST request to {}, Headers: {}, Params: {}", requestUrl, headersBuilder, params);
        Request request = new Request.Builder()
                .headers(headersBuilder)
                .url(requestUrl)
                .post(requestBody)
                .build();
        Response response = null;
        try {
            response = requestClient.newCall(request).execute();
            return response.code();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getParams(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(QUESTION_MARK);
        if (!params.isEmpty()) {
            for (Map.Entry<String, Object> item : params.entrySet()) {
                Object value = item.getValue();
                if (Objects.nonNull(value)) {
                    sb.append(AND_MARK);
                    sb.append(item.getKey());
                    sb.append(EQUAL_MARK);
                    sb.append(value);
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }
}
