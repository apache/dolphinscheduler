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

package org.apache.dolphinscheduler.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.security.impl.oidc.OidcAuthenticator;
import org.apache.dolphinscheduler.api.security.impl.oidc.OidcConfigProperties;
import org.apache.dolphinscheduler.api.security.impl.oidc.OidcProviderConfig;
import org.apache.dolphinscheduler.api.security.impl.pwd.PasswordAuthenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Unit tests for {@link LoginController} with OIDC authentication enabled.
 */
@TestPropertySource(properties = {
        "security.authentication.type=OIDC",
        "security.authentication.oidc.enable=true",
        "api.ui-url=http://ds.ui"
})
public class LoginControllerOidcTest extends AbstractControllerTest {

    private final String providerId = "keycloak";
    private final String uiUrl = "http://ds.ui";

    @MockBean
    private OidcAuthenticator oidcAuthenticator;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private OidcConfigProperties oidcConfigProperties;

    @Autowired
    private LoginController loginController;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(loginController, "authenticator", oidcAuthenticator);
    }

    @Test
    public void testSsoLogin_whenOidcEnabled_returnsSuccessWithNull() throws Exception {
        mockMvc.perform(get("/login/sso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Status.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testGetOidcProviders_whenEnabled_returnsProviderList() throws Exception {
        Map<String, OidcProviderConfig> providers = new HashMap<>();
        OidcProviderConfig providerConfig = new OidcProviderConfig();
        providerConfig.setDisplayName("Login with Keycloak");
        providers.put(providerId, providerConfig);

        when(oidcConfigProperties.isEnable()).thenReturn(true);
        when(oidcConfigProperties.getProviders()).thenReturn(providers);

        mockMvc.perform(get("/oidc-providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Status.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data[0].id").value(providerId))
                .andExpect(jsonPath("$.data[0].displayName").value("Login with Keycloak"));
    }

    @Test
    public void testGetOidcProviders_whenDisabled_returnsEmptyList() throws Exception {
        when(oidcConfigProperties.isEnable()).thenReturn(false);

        mockMvc.perform(get("/oidc-providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Status.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testRedirectToOidc() throws Exception {
        String state = providerId + ":" + UUID.randomUUID();
        String authUrl = "http://oidc-provider.com/auth?state=" + state;

        when(oidcAuthenticator.getSignInUrl(anyString())).thenReturn(authUrl);

        mockMvc.perform(get("/oauth2/authorization/{providerId}", providerId))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(authUrl));
    }

    @Test
    public void testRedirectToOidc_withAuthenticatorError() throws Exception {
        when(oidcAuthenticator.getSignInUrl(anyString())).thenReturn(null);

        mockMvc.perform(get("/oauth2/authorization/{providerId}", providerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Status.INTERNAL_SERVER_ERROR_ARGS.getCode()))
                .andExpect(jsonPath("$.msg", containsString("Redirect URL must not be null")));
    }

    @Test
    public void testHandleOidcCallback_withSuccess() throws Exception {
        String code = "testCode";
        String state = providerId + ":" + UUID.randomUUID();
        String sessionId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(1);
        user.setUserName("oidc_user");

        Session session = new Session();
        session.setId(sessionId);
        session.setUserId(user.getId());

        when(oidcAuthenticator.login(state, code)).thenReturn(user);
        when(sessionService.createSessionIfAbsent(user)).thenReturn(session);

        String expectedRedirectUrl = String.format("%s/login?sessionId=%s&authType=%s", uiUrl, sessionId, "oidc");

        performOidcCallback(code, null, state)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    public void testHandleOidcCallback_withLoginErrorFromProvider() throws Exception {
        String error = "access_denied";
        String state = providerId + ":" + UUID.randomUUID();
        String expectedRedirectUrl = "/dolphinscheduler/ui/#/login?error=oidc_login_failed";

        performOidcCallback(null, error, state)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    public void testHandleOidcCallback_withMissingCode() throws Exception {
        String state = providerId + ":" + UUID.randomUUID();
        String expectedRedirectUrl = "/dolphinscheduler/ui/#/login?error=oidc_missing_code";

        performOidcCallback(null, null, state)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    public void testHandleOidcCallback_whenAuthenticatorFails() throws Exception {
        String code = "testCode";
        String state = providerId + ":" + UUID.randomUUID();
        String expectedRedirectUrl = "/dolphinscheduler/ui/#/login?error=oidc_authentication_failed";

        when(oidcAuthenticator.login(state, code)).thenReturn(null);

        performOidcCallback(code, null, state)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    public void testHandleOidcCallback_withCriticalException() throws Exception {
        String code = "testCode";
        String state = providerId + ":" + UUID.randomUUID();
        String expectedRedirectUrl = "/dolphinscheduler/ui/#/login?error=oidc_critical_error";

        when(oidcAuthenticator.login(state, code)).thenThrow(new RuntimeException("Critical DB error"));

        performOidcCallback(code, null, state)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    public void testHandleOidcCallback_withNonOidcAuthenticator() throws Exception {
        Authenticator passwordAuthenticator = new PasswordAuthenticator();
        ReflectionTestUtils.setField(loginController, "authenticator", passwordAuthenticator);

        String code = "testCode";
        String state = providerId + ":" + UUID.randomUUID();
        String expectedRedirectUrl = "/dolphinscheduler/ui/#/login?error=oidc_not_enabled";

        performOidcCallback(code, null, state)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        ReflectionTestUtils.setField(loginController, "authenticator", oidcAuthenticator);
    }

    private ResultActions performOidcCallback(String code, String error, String state) throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                get("/login/oauth2/code/{providerId}", providerId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("state", state)
                        .sessionAttr(Constants.SSO_LOGIN_USER_STATE, state);

        if (code != null) {
            requestBuilder.param("code", code);
        }
        if (error != null) {
            requestBuilder.param("error", error);
        }

        return mockMvc.perform(requestBuilder);
    }
}
