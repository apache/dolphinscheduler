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
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.pages.OidcLoginPage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@DolphinScheduler(composeFiles = "docker/oidc-login/docker-compose.yaml")
@Slf4j
public class OidcLoginAPITest {

    private static final String PROVIDER_ID = "keycloak";

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
        // In OIDC mode, /login endpoint should not allow password login
        Assertions.assertEquals(401, response.getStatusCode());
    }
}
