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

package org.apache.dolphinscheduler.api.test.cases;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.GetUserInfoResponseData;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.pages.OidcLoginPage;
import org.apache.dolphinscheduler.api.test.pages.security.UserPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@DolphinScheduler(composeFiles = "docker/oidc-login/docker-compose.yaml")
@Slf4j
public class OidcLoginAPITest {

    private static final String PROVIDER_ID = "keycloak";

    private enum HtmlEntity {

        AMP("&amp;", "&"),
        LT("&lt;", "<"),
        GT("&gt;", ">"),
        QUOT("&quot;", "\""),
        APOS("&#39;", "'");

        private final String encoded;
        private final String decoded;

        HtmlEntity(String encoded, String decoded) {
            this.encoded = encoded;
            this.decoded = decoded;
        }
    }

    private enum Delimiter {

        QUERY_START("?"),
        PARAM_SEP("&"),
        KEY_VALUE_SEP("=");

        private final String value;

        Delimiter(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Test
    @Order(1)
    public void testGetOidcProviders() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        HttpResponse response = oidcLoginPage.getOidcProviders();
        Assertions.assertEquals(200, response.getStatusCode());
        List<Map<String, String>> providers = (List<Map<String, String>>) response.getBody().getData();
        Assertions.assertNotNull(providers);
        Assertions.assertFalse(providers.isEmpty());
        Map<String, String> provider = providers.get(0);
        Assertions.assertEquals(PROVIDER_ID, provider.get("id"));
        Assertions.assertEquals("Login with Keycloak", provider.get("displayName"));
        Assertions.assertTrue(provider.containsKey("iconUri"));
        Assertions.assertTrue(provider.get("iconUri").endsWith("keycloak.png"));
    }

    @Test
    @Order(2)
    public void testInitiateOidcLogin_validProvider() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        HttpResponse response = oidcLoginPage.initiateOidcLogin(PROVIDER_ID);
        Assertions.assertEquals(302, response.getStatusCode());
    }

    @Test
    @Order(3)
    public void testInitiateOidcLogin_invalidProvider() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        HttpResponse response = oidcLoginPage.initiateOidcLoginWithInvalidProvider("invalid-provider");
        Assertions.assertEquals(302, response.getStatusCode());
    }

    @Test
    @Order(4)
    public void testOidcCallbackMissingCode() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        String state = PROVIDER_ID + ":" + UUID.randomUUID().toString();
        HttpResponse response = oidcLoginPage.handleOidcCallbackMissingCode(PROVIDER_ID, state);
        Assertions.assertEquals(302, response.getStatusCode());
    }

    @Test
    @Order(5)
    public void testOidcCallbackError() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        String state = PROVIDER_ID + ":" + UUID.randomUUID().toString();
        HttpResponse response = oidcLoginPage.handleOidcCallbackError(PROVIDER_ID, "access_denied", state);
        Assertions.assertEquals(302, response.getStatusCode());
    }

    @Test
    @Order(6)
    public void testOidcCallbackErrorWithSpecialChars() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        String state = PROVIDER_ID + ":" + UUID.randomUUID().toString();
        HttpResponse response = oidcLoginPage.handleOidcCallbackError(PROVIDER_ID, "err\nor\t", state);
        Assertions.assertEquals(302, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void testOidcCallbackWithInvalidProviderInState() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        String state = "unknownprovider:" + UUID.randomUUID().toString();
        String code = "dummy_code";
        HttpResponse response = oidcLoginPage.handleOidcCallback("unknownprovider", code, state);
        Assertions.assertEquals(302, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void testLoginEndpointDisabledInOidcMode() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        HttpResponse response = oidcLoginPage.loginWithPassword("anyuser", "anypassword");
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(10013, response.getBody().getCode(), "Expected 'user name or password error'");
    }

    @Test
    @Order(10)
    public void testAdminUserLoginSuccess() throws Exception {
        String sessionId = doOidcLogin("admin_user", "password");
        Assertions.assertNotNull(sessionId);

        UserPage userPage = new UserPage();
        HttpResponse getUserInfoHttpResponse = userPage.getUserInfo(sessionId);
        GetUserInfoResponseData userInfo =
                JSONUtils.convertValue(getUserInfoHttpResponse.getBody().getData(), GetUserInfoResponseData.class);
        Assertions.assertEquals("admin_user", userInfo.getUserName());
        Assertions.assertEquals("ADMIN_USER", userInfo.getUserType().name());
    }

    @Test
    @Order(20)
    public void testGeneralUserLoginSuccess() throws Exception {
        String sessionId = doOidcLogin("general_user", "password");
        Assertions.assertNotNull(sessionId);

        UserPage userPage = new UserPage();
        HttpResponse getUserInfoHttpResponse = userPage.getUserInfo(sessionId);
        GetUserInfoResponseData userInfo =
                JSONUtils.convertValue(getUserInfoHttpResponse.getBody().getData(), GetUserInfoResponseData.class);
        Assertions.assertEquals("general_user", userInfo.getUserName());
        Assertions.assertEquals("GENERAL_USER", userInfo.getUserType().name());
    }

    @Test
    @Order(30)
    public void testLoginFailedWrongPassword() throws Exception {
        String sessionId = doOidcLoginExpectingFailure("general_user", "wrong");
        Assertions.assertNull(sessionId);
    }

    @SneakyThrows
    private String doOidcLogin(String username, String password) {
        OkHttpClient http = buildClientWithCookieJar();

        String authorizeUrl = getAuthorizationRedirect(http);

        String loginAction = fetchLoginFormAction(http, authorizeUrl);

        String callbackUrl = submitCredentialsAndGetCallback(http, loginAction, username, password);

        return callDsCallbackAndExtractSessionId(http, callbackUrl);
    }

    @SneakyThrows
    private String doOidcLoginExpectingFailure(String username, String password) {
        OkHttpClient http = buildClientWithCookieJar();
        String authorizeUrl = getAuthorizationRedirect(http);
        String loginAction = fetchLoginFormAction(http, authorizeUrl);
        String callbackUrl = submitCredentialsAndGetCallback(http, loginAction, username, password);
        if (callbackUrl == null) {
            return null;
        }
        String sessionId = callDsCallbackAndExtractSessionId(http, callbackUrl);
        return sessionId;
    }

    private OkHttpClient buildClientWithCookieJar() {
        return new OkHttpClient.Builder()
                .followRedirects(false)
                .dns(hostname -> {
                    if ("keycloak".equalsIgnoreCase(hostname)) {
                        try {
                            return Collections.singletonList(InetAddress.getByName("127.0.0.1"));
                        } catch (UnknownHostException e) {
                            return Dns.SYSTEM.lookup(hostname);
                        }
                    }
                    return Dns.SYSTEM.lookup(hostname);
                })
                .cookieJar(new CookieJar() {

                    private final List<Cookie> cookieStore = new ArrayList<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.addAll(cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        return cookieStore;
                    }
                })
                .build();
    }

    @SneakyThrows
    private String getAuthorizationRedirect(OkHttpClient http) {
        String kickOffUrl = Constants.DOLPHINSCHEDULER_API_URL + "/oauth2/authorization/" + PROVIDER_ID;
        Request req = new Request.Builder().url(kickOffUrl).get().build();
        try (Response resp = http.newCall(req).execute()) {
            Assertions.assertEquals(302, resp.code(), "DS should redirect to OIDC provider");
            String location = resp.header("Location");
            log.info("Authorization redirect location: {}", location);
            return location;
        }
    }

    @SneakyThrows
    private String fetchLoginFormAction(OkHttpClient http, String authorizationUrl) {
        Request req = new Request.Builder().url(authorizationUrl).get().build();
        try (Response resp = http.newCall(req).execute()) {
            Assertions.assertEquals(200, resp.code(), "Should load Keycloak login page");
            String html = resp.body() != null ? resp.body().string() : "";
            String action = extractBetween(html, "action=\"", "\"");
            if (action != null) {
                action = htmlUnescape(action);
            }
            log.info("Keycloak login form action (decoded): {}", action);
            Assertions.assertNotNull(action, "Cannot find Keycloak login form action");
            return action.startsWith("http") ? action : "http://" + HttpUrl.parse(authorizationUrl).host() + action;
        }
    }

    private String htmlUnescape(String input) {
        if (input == null) {
            return null;
        }
        String result = input;
        for (HtmlEntity entity : HtmlEntity.values()) {
            result = result.replace(entity.encoded, entity.decoded);
        }
        return result;
    }

    @SneakyThrows
    private String submitCredentialsAndGetCallback(OkHttpClient http, String loginAction, String username,
                                                   String password) {
        RequestBody form = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("credentialId", "")
                .build();
        Request req = new Request.Builder().url(loginAction).post(form).build();
        try (Response resp = http.newCall(req).execute()) {
            if (resp.code() != 302) {
                log.warn("Login did not redirect, likely wrong password. Code={}", resp.code());
                return null;
            }
            String loc = resp.header("Location");
            log.info("Post-login redirect location: {}", loc);
            return loc;
        }
    }

    private String callDsCallbackAndExtractSessionId(OkHttpClient http, String callbackUrl) throws IOException {
        if (callbackUrl == null) {
            return null;
        }
        Request req = new Request.Builder().url(callbackUrl).get().build();
        try (Response resp = http.newCall(req).execute()) {
            if (resp.code() != 302) {
                log.warn("DS callback did not redirect to UI. Code={}", resp.code());
                return null;
            }
            String finalLocation = resp.header("Location");
            log.info("Final redirect location: {}", finalLocation);
            return parseQueryParam(finalLocation, "sessionId");
        }
    }

    private String extractBetween(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s < 0)
            return null;
        s += start.length();
        int e = text.indexOf(end, s);
        if (e < 0)
            return null;
        return text.substring(s, e);
    }

    private String parseQueryParam(String url, String name) {
        final int KEY_VALUE_PAIR_LENGTH = 2;
        final int KEY_INDEX = 0;
        final int VALUE_INDEX = 1;

        int qIndex = url.indexOf(Delimiter.QUERY_START.toString());
        if (qIndex < 0) {
            return null;
        }
        String query = url.substring(qIndex + 1);
        for (String p : query.split(Delimiter.PARAM_SEP.toString())) {
            String[] kv = p.split(Delimiter.KEY_VALUE_SEP.toString(), KEY_VALUE_PAIR_LENGTH);
            if (kv.length == KEY_VALUE_PAIR_LENGTH && kv[KEY_INDEX].equals(name)) {
                return URLDecoder.decode(kv[VALUE_INDEX], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
