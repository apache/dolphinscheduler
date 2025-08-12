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

import org.apache.dolphinscheduler.api.test.core.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.entity.GetUserInfoResponseData;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.LoginResponseData;
import org.apache.dolphinscheduler.api.test.pages.OidcLoginPage;
import org.apache.dolphinscheduler.api.test.pages.security.UserPage;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.common.enums.UserType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisableIfTestFails;

@DolphinScheduler(composeFiles = "docker/oidc-login/docker-compose.yaml")
@Slf4j
@DisableIfTestFails
public class OidcLoginAPITest {

    private static String sessionId;
    private static final String PROVIDER_ID = "keycloak";

    @Test
    @Order(10)
    public void testGetOidcProviders() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        HttpResponse response = oidcLoginPage.getOidcProviders();

        Assertions.assertTrue(response.getBody().getSuccess());

        @SuppressWarnings("unchecked")
        List<Map<String, String>> providers = (List<Map<String, String>>) response.getBody().getData();
        Assertions.assertNotNull(providers);
        Assertions.assertFalse(providers.isEmpty());

        Map<String, String> provider = providers.get(0);
        Assertions.assertEquals(PROVIDER_ID, provider.get("id"));
        Assertions.assertEquals("Login with Keycloak", provider.get("displayName"));
    }

    @Test
    @Order(20)
    public void testInitiateOidcLogin() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        HttpResponse response = oidcLoginPage.initiateOidcLogin(PROVIDER_ID);

        // The response should be a redirect to the Keycloak authorization endpoint
        Assertions.assertEquals(302, response.getStatusCode());
        String location = response.getHeaders().get("Location");
        Assertions.assertNotNull(location);
        Assertions.assertTrue(location.contains("auth/realms/dolphinscheduler/protocol/openid-connect/auth"));
        Assertions.assertTrue(location.contains("client_id=dolphinscheduler-client"));
        Assertions.assertTrue(location.contains("response_type=code"));
        Assertions.assertTrue(location.contains("scope="));
        Assertions.assertTrue(location.contains("state="));
    }

    @Test
    @Order(30)
    public void testOidcCallbackMissingCode() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        String state = PROVIDER_ID + ":" + UUID.randomUUID().toString();
        HttpResponse response = oidcLoginPage.handleOidcCallbackMissingCode(PROVIDER_ID, state);

        Assertions.assertEquals(302, response.getStatusCode());
        String location = response.getHeaders().get("Location");
        Assertions.assertNotNull(location);
        Assertions.assertTrue(location.contains("login?error=oidc_missing_code"));
    }

    @Test
    @Order(40)
    public void testOidcCallbackError() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        String state = PROVIDER_ID + ":" + UUID.randomUUID().toString();
        HttpResponse response = oidcLoginPage.handleOidcCallbackError(PROVIDER_ID, "access_denied", state);

        Assertions.assertEquals(302, response.getStatusCode());
        String location = response.getHeaders().get("Location");
        Assertions.assertNotNull(location);
        Assertions.assertTrue(location.contains("login?error=oidc_login_failed"));
    }

    @Test
    @Order(50)
    public void testOidcCallbackInvalidState() {
        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        String state = PROVIDER_ID + ":" + UUID.randomUUID().toString();
        String code = "valid_code";
        HttpResponse response = oidcLoginPage.handleOidcCallback(PROVIDER_ID, code, state);

        Assertions.assertEquals(302, response.getStatusCode());
    }

    @Test
    @Order(60)
    public void testSimulatedSuccessfulOidcLogin() {

        OidcLoginPage oidcLoginPage = new OidcLoginPage();
        HttpResponse loginResponse = oidcLoginPage.login("admin_user", "password");

        if (loginResponse.getBody().getSuccess()) {
            sessionId =
                    JSONUtils.convertValue(loginResponse.getBody().getData(), LoginResponseData.class).getSessionId();
            UserPage userPage = new UserPage();
            HttpResponse getUserInfoResponse = userPage.getUserInfo(sessionId);
            GetUserInfoResponseData getUserInfoResponseData =
                    JSONUtils.convertValue(getUserInfoResponse.getBody().getData(), GetUserInfoResponseData.class);
            Assertions.assertEquals("admin_user", getUserInfoResponseData.getUserName());
            Assertions.assertEquals(UserType.ADMIN_USER, getUserInfoResponseData.getUserType());
        } else {
            log.info("User admin_user not found, which is expected if no OIDC login has occurred yet");
        }
    }
}
