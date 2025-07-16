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

package org.apache.dolphinscheduler.api.security.impl.oidc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class OidcAuthenticatorTest {

    @InjectMocks
    private OidcAuthenticator oidcAuthenticator;

    @Mock
    private OidcConfigProperties oidcConfigProperties;

    @Mock
    private UsersService usersService;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private ServletRequestAttributes attributes;

    private final String providerId = "keycloak";
    private final String username = "oidc_user";
    private final String email = "oidc_user@example.com";
    private final String code = "test_auth_code";

    @BeforeEach
    public void setUp() {
        when(attributes.getRequest()).thenReturn(request);
        when(request.getSession()).thenReturn(session);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    public void testLogin_Success_NewAdminUser() throws Exception {
        String state = providerId + ":" + UUID.randomUUID();
        when(session.getAttribute(Constants.SSO_LOGIN_USER_STATE)).thenReturn(state);
        when(usersService.getUserByUserName(username)).thenReturn(null);

        mockOidcConfiguration(providerId, true, Collections.singletonList("admin-group"));

        User newUser = new User();
        newUser.setUserName(username);
        newUser.setUserType(UserType.ADMIN_USER);
        when(usersService.createUser(UserType.ADMIN_USER, username, email)).thenReturn(newUser);

        executeLoginWithMocks(() -> {
            User resultUser = oidcAuthenticator.login(state, code);

            Assertions.assertNotNull(resultUser);
            Assertions.assertEquals(username, resultUser.getUserName());
            Assertions.assertEquals(UserType.ADMIN_USER, resultUser.getUserType());
            verify(usersService).createUser(UserType.ADMIN_USER, username, email);
        }, Collections.singletonList("admin-group"));
    }

    @Test
    public void testLogin_Success_ExistingUser() throws Exception {
        String state = providerId + ":" + UUID.randomUUID();
        User existingUser = new User();
        existingUser.setUserName(username);

        when(session.getAttribute(Constants.SSO_LOGIN_USER_STATE)).thenReturn(state);
        when(usersService.getUserByUserName(username)).thenReturn(existingUser);
        mockOidcConfiguration(providerId, true, Collections.emptyList());

        executeLoginWithMocks(() -> {
            User resultUser = oidcAuthenticator.login(state, code);
            Assertions.assertNotNull(resultUser);
            Assertions.assertEquals(username, resultUser.getUserName());
            verify(usersService, never()).createUser(any(), anyString(), anyString());
        }, Collections.emptyList());
    }

    @Test
    public void testLogin_Failure_AutoCreateOff() throws Exception {
        String state = providerId + ":" + UUID.randomUUID();
        when(session.getAttribute(Constants.SSO_LOGIN_USER_STATE)).thenReturn(state);
        when(usersService.getUserByUserName(username)).thenReturn(null);

        mockOidcConfiguration(providerId, false, Collections.emptyList());

        executeLoginWithMocks(() -> {
            User resultUser = oidcAuthenticator.login(state, code);

            Assertions.assertNull(resultUser);
            verify(usersService, never()).createUser(any(), anyString(), anyString());
        }, Collections.emptyList());
    }

    @Test
    public void testLogin_Failure_StateMismatch() {
        String providerState = providerId + ":" + UUID.randomUUID();
        String sessionState = providerId + ":" + UUID.randomUUID();

        when(session.getAttribute(Constants.SSO_LOGIN_USER_STATE)).thenReturn(sessionState);

        User resultUser = oidcAuthenticator.login(providerState, code);

        Assertions.assertNull(resultUser);
    }

    @Test
    public void testLogin_Failure_InvalidProviderId() {
        String invalidProviderId = "unknown-provider";
        String state = invalidProviderId + ":" + UUID.randomUUID();

        when(session.getAttribute(Constants.SSO_LOGIN_USER_STATE)).thenReturn(state);
        mockOidcConfiguration(providerId, true, Collections.emptyList());

        User resultUser = oidcAuthenticator.login(state, code);

        Assertions.assertNull(resultUser);
    }

    private void executeLoginWithMocks(Runnable assertions, List<String> groups) throws Exception {
        injectMockMetadataIntoCache();

        try (
                MockedStatic<OIDCTokenResponseParser> tokenParserMock =
                        Mockito.mockStatic(OIDCTokenResponseParser.class);
                MockedStatic<UserInfoResponse> userInfoResponseMock = Mockito.mockStatic(UserInfoResponse.class);
                MockedConstruction<TokenRequest> tokenRequestConstruction = Mockito.mockConstruction(TokenRequest.class,
                        (mock, context) -> {
                            HTTPRequest httpRequest = mock(HTTPRequest.class);
                            HTTPResponse httpResponse = mock(HTTPResponse.class);
                            when(mock.toHTTPRequest()).thenReturn(httpRequest);
                            when(httpRequest.send()).thenReturn(httpResponse);
                        });
                MockedConstruction<UserInfoRequest> userInfoRequestConstruction =
                        Mockito.mockConstruction(UserInfoRequest.class,
                                (mock, context) -> {
                                    HTTPRequest httpRequest = mock(HTTPRequest.class);
                                    HTTPResponse httpResponse = mock(HTTPResponse.class);
                                    when(mock.toHTTPRequest()).thenReturn(httpRequest);
                                    when(httpRequest.send()).thenReturn(httpResponse);
                                })) {

            mockTokenAndUserInfoFlow(tokenParserMock, userInfoResponseMock, groups);
            assertions.run();
        }
    }

    private void injectMockMetadataIntoCache() throws Exception {
        OIDCProviderMetadata metadata = mock(OIDCProviderMetadata.class);
        when(metadata.getTokenEndpointURI()).thenReturn(new java.net.URI("http://fake/token"));
        when(metadata.getUserInfoEndpointURI()).thenReturn(new java.net.URI("http://fake/userinfo"));
        when(metadata.getIssuer()).thenReturn(new Issuer("http://fake-issuer.com"));

        Field cacheField = OidcAuthenticator.class.getDeclaredField("providerMetadataCache");
        ReflectionUtils.makeAccessible(cacheField);
        @SuppressWarnings("unchecked")
        Map<String, OIDCProviderMetadata> cache =
                (Map<String, OIDCProviderMetadata>) ReflectionUtils.getField(cacheField, oidcAuthenticator);
        cache.put(providerId, metadata);
    }

    /**
     * Helper method to mock the OIDC provider configuration.
     */
    private void mockOidcConfiguration(String providerId, boolean autoCreate, List<String> adminGroups) {
        OidcProviderConfig providerConfig = new OidcProviderConfig();
        providerConfig.setIssuerUri("http://fake-issuer.com");
        providerConfig.setClientId("test-client");
        providerConfig.setClientSecret("test-secret");
        providerConfig.setUserNameAttribute("preferred_username");
        providerConfig.setGroupsClaim("groups");

        OidcUserConfig userConfig = new OidcUserConfig();
        userConfig.setAutoCreate(autoCreate);
        userConfig.setAdminGroupMapping(adminGroups);

        Map<String, OidcProviderConfig> providers = new HashMap<>();
        providers.put(providerId, providerConfig);

        when(oidcConfigProperties.getProviders()).thenReturn(providers);
        when(oidcConfigProperties.getUser()).thenReturn(userConfig);
    }

    /**
     * Mocks the token and user info parts of the OIDC flow, assuming metadata is already cached.
     */
    private void mockTokenAndUserInfoFlow(
                                          MockedStatic<OIDCTokenResponseParser> tokenParserMock,
                                          MockedStatic<UserInfoResponse> userInfoResponseMock,
                                          List<String> groups) throws Exception {

        OIDCTokenResponse tokenResponse = mock(OIDCTokenResponse.class);
        when(tokenResponse.indicatesSuccess()).thenReturn(true);
        OIDCTokens oidcTokens = mock(OIDCTokens.class);
        when(oidcTokens.getAccessToken()).thenReturn(new BearerAccessToken());

        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .issuer("http://fake-issuer.com")
                        .audience("test-client")
                        .subject(username)
                        .claim("preferred_username", username)
                        .claim("email", email)
                        .claim("groups", groups)
                        .expirationTime(new Date(System.currentTimeMillis() + 60000))
                        .issueTime(new Date())
                        .build();
        PlainJWT idToken = new PlainJWT(claimsSet);
        when(oidcTokens.getIDToken()).thenReturn(idToken);
        when(tokenResponse.getOIDCTokens()).thenReturn(oidcTokens);
        tokenParserMock
                .when(() -> OIDCTokenResponseParser.parse(any(HTTPResponse.class)))
                .thenReturn(tokenResponse);

        UserInfoResponse userInfoResponse = mock(UserInfoResponse.class);
        when(userInfoResponse.indicatesSuccess()).thenReturn(true);
        UserInfoSuccessResponse successResponse = mock(UserInfoSuccessResponse.class);
        when(userInfoResponse.toSuccessResponse()).thenReturn(successResponse);

        UserInfo nimbusUserInfo = new UserInfo(new Subject(username));
        nimbusUserInfo.setClaim("preferred_username", username);
        nimbusUserInfo.setClaim("email", email);
        nimbusUserInfo.setClaim("groups", groups);
        when(successResponse.getUserInfo()).thenReturn(nimbusUserInfo);

        userInfoResponseMock
                .when(() -> UserInfoResponse.parse(any(HTTPResponse.class)))
                .thenReturn(userInfoResponse);
    }
}
