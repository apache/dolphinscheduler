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
import java.util.Arrays;
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

    @Test
    public void testGetSignInUrl_Success() throws Exception {
        String state = providerId + ":" + UUID.randomUUID();

        OidcProviderConfig providerConfig = new OidcProviderConfig();
        providerConfig.setIssuerUri("http://fake-issuer.com");
        providerConfig.setClientId("test-client");
        providerConfig.setClientSecret("test-secret");
        providerConfig.setUserNameAttribute("preferred_username");
        providerConfig.setGroupsClaim("groups");

        Map<String, OidcProviderConfig> providers = new HashMap<>();
        providers.put(providerId, providerConfig);

        when(oidcConfigProperties.getProviders()).thenReturn(providers);

        OIDCProviderMetadata metadata = mock(OIDCProviderMetadata.class);
        when(metadata.getTokenEndpointURI()).thenReturn(new java.net.URI("http://fake/token"));
        when(metadata.getUserInfoEndpointURI()).thenReturn(new java.net.URI("http://fake/userinfo"));
        when(metadata.getIssuer()).thenReturn(new Issuer("http://fake-issuer.com"));
        when(metadata.getAuthorizationEndpointURI()).thenReturn(new java.net.URI("http://fake/authorize"));

        Field cacheField = OidcAuthenticator.class.getDeclaredField("providerMetadataCache");
        ReflectionUtils.makeAccessible(cacheField);
        @SuppressWarnings("unchecked")
        Map<String, OIDCProviderMetadata> cache =
                (Map<String, OIDCProviderMetadata>) ReflectionUtils.getField(cacheField, oidcAuthenticator);
        cache.put(providerId, metadata);

        Field apiBaseUrlField = OidcAuthenticator.class.getDeclaredField("apiBaseUrl");
        ReflectionUtils.makeAccessible(apiBaseUrlField);
        ReflectionUtils.setField(apiBaseUrlField, oidcAuthenticator, "http://test-api-base-url");

        String signInUrl = oidcAuthenticator.getSignInUrl(state);

        System.out.println("[DEBUG_LOG] Sign-in URL: " + signInUrl);

        Assertions.assertNotNull(signInUrl);
        Assertions.assertTrue(signInUrl.contains("response_type=code"), "URL should contain response_type=code");
        Assertions.assertTrue(signInUrl.contains("client_id=test-client"), "URL should contain client_id=test-client");

        String encodedState = state.replace(":", "%3A");
        Assertions.assertTrue(signInUrl.contains("state=" + encodedState), "URL should contain encoded state=" + encodedState);
    }

    @Test
    public void testGetSignInUrl_Failure() {
        String invalidState = "invalid-state-format";

        String signInUrl = oidcAuthenticator.getSignInUrl(invalidState);

        Assertions.assertNull(signInUrl);
    }

    @Test
    public void testParseScope() throws Exception {
        java.lang.reflect.Method parseScopeMethod = OidcAuthenticator.class.getDeclaredMethod("parseScope", String.class);
        ReflectionUtils.makeAccessible(parseScopeMethod);

        com.nimbusds.oauth2.sdk.Scope scope1 = (com.nimbusds.oauth2.sdk.Scope) parseScopeMethod.invoke(oidcAuthenticator, (String) null);
        Assertions.assertTrue(scope1.contains("openid"));
        Assertions.assertTrue(scope1.contains("profile"));
        Assertions.assertTrue(scope1.contains("email"));

        com.nimbusds.oauth2.sdk.Scope scope2 = (com.nimbusds.oauth2.sdk.Scope) parseScopeMethod.invoke(oidcAuthenticator, "");
        Assertions.assertTrue(scope2.contains("openid"));
        Assertions.assertTrue(scope2.contains("profile"));
        Assertions.assertTrue(scope2.contains("email"));

        com.nimbusds.oauth2.sdk.Scope scope3 = (com.nimbusds.oauth2.sdk.Scope) parseScopeMethod.invoke(oidcAuthenticator, "openid,custom");
        Assertions.assertTrue(scope3.contains("openid"));
        Assertions.assertTrue(scope3.contains("custom"));
        Assertions.assertFalse(scope3.contains("profile"));
    }

    @Test
    public void testExtractUsername() throws Exception {
        java.lang.reflect.Method extractUsernameMethod = OidcAuthenticator.class.getDeclaredMethod(
            "extractUsername", OidcProviderConfig.class, com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class, 
            com.nimbusds.openid.connect.sdk.claims.UserInfo.class);
        ReflectionUtils.makeAccessible(extractUsernameMethod);

        OidcProviderConfig providerConfig = new OidcProviderConfig();
        providerConfig.setUserNameAttribute("preferred_username");

        com.nimbusds.oauth2.sdk.id.Subject subject = new com.nimbusds.oauth2.sdk.id.Subject("subject-id");

        com.nimbusds.jwt.JWTClaimsSet claimsSet1 = new com.nimbusds.jwt.JWTClaimsSet.Builder()
            .subject(subject.getValue())
            .issuer("https://issuer.example.com")
            .audience("client-id")
            .expirationTime(new Date(System.currentTimeMillis() + 60000))
            .issueTime(new Date())
            .claim("preferred_username", "test-username")
            .build();

        com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet idTokenClaims1 = mock(com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class);
        when(idTokenClaims1.getSubject()).thenReturn(subject);
        when(idTokenClaims1.getClaim("preferred_username")).thenReturn("test-username");

        com.nimbusds.openid.connect.sdk.claims.UserInfo userInfo1 = new com.nimbusds.openid.connect.sdk.claims.UserInfo(subject);
        userInfo1.setClaim("preferred_username", "user-info-username");

        String username1 = (String) extractUsernameMethod.invoke(oidcAuthenticator, providerConfig, idTokenClaims1, userInfo1);
        Assertions.assertEquals("test-username", username1);

        com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet idTokenClaims2 = mock(com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class);
        when(idTokenClaims2.getSubject()).thenReturn(subject);
        when(idTokenClaims2.getClaim("preferred_username")).thenReturn(null);

        String username2 = (String) extractUsernameMethod.invoke(oidcAuthenticator, providerConfig, idTokenClaims2, userInfo1);
        Assertions.assertEquals("user-info-username", username2);

        com.nimbusds.openid.connect.sdk.claims.UserInfo userInfo2 = new com.nimbusds.openid.connect.sdk.claims.UserInfo(subject);

        String username3 = (String) extractUsernameMethod.invoke(oidcAuthenticator, providerConfig, idTokenClaims2, userInfo2);
        Assertions.assertEquals("subject-id", username3);
    }

    @Test
    public void testExtractEmail() throws Exception {
        java.lang.reflect.Method extractEmailMethod = OidcAuthenticator.class.getDeclaredMethod(
            "extractEmail", com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class, 
            com.nimbusds.openid.connect.sdk.claims.UserInfo.class);
        ReflectionUtils.makeAccessible(extractEmailMethod);

        com.nimbusds.oauth2.sdk.id.Subject subject = new com.nimbusds.oauth2.sdk.id.Subject("subject-id");

        com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet idTokenClaims1 = mock(com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class);
        when(idTokenClaims1.getClaim("email")).thenReturn("test@example.com");

        com.nimbusds.openid.connect.sdk.claims.UserInfo userInfo1 = new com.nimbusds.openid.connect.sdk.claims.UserInfo(subject);
        userInfo1.setClaim("email", "userinfo@example.com");

        String email1 = (String) extractEmailMethod.invoke(oidcAuthenticator, idTokenClaims1, userInfo1);
        Assertions.assertEquals("test@example.com", email1);

        com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet idTokenClaims2 = mock(com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class);
        when(idTokenClaims2.getClaim("email")).thenReturn(null);

        String email2 = (String) extractEmailMethod.invoke(oidcAuthenticator, idTokenClaims2, userInfo1);
        Assertions.assertEquals("userinfo@example.com", email2);

        com.nimbusds.openid.connect.sdk.claims.UserInfo userInfo2 = new com.nimbusds.openid.connect.sdk.claims.UserInfo(subject);

        String email3 = (String) extractEmailMethod.invoke(oidcAuthenticator, idTokenClaims2, userInfo2);
        Assertions.assertNull(email3);
    }

    @Test
    public void testExtractGroups() throws Exception {
        java.lang.reflect.Method extractGroupsMethod = OidcAuthenticator.class.getDeclaredMethod(
            "extractGroups", OidcProviderConfig.class, com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class, 
            com.nimbusds.openid.connect.sdk.claims.UserInfo.class);
        ReflectionUtils.makeAccessible(extractGroupsMethod);

        OidcProviderConfig providerConfig = new OidcProviderConfig();
        providerConfig.setGroupsClaim("groups");

        com.nimbusds.oauth2.sdk.id.Subject subject = new com.nimbusds.oauth2.sdk.id.Subject("subject-id");

        List<String> idTokenGroups = Arrays.asList("group1", "group2");

        com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet idTokenClaims1 = mock(com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class);
        when(idTokenClaims1.getClaim("groups")).thenReturn(idTokenGroups);

        com.nimbusds.openid.connect.sdk.claims.UserInfo userInfo1 = new com.nimbusds.openid.connect.sdk.claims.UserInfo(subject);
        userInfo1.setClaim("groups", Arrays.asList("group3", "group4"));

        @SuppressWarnings("unchecked")
        List<String> groups1 = (List<String>) extractGroupsMethod.invoke(oidcAuthenticator, providerConfig, idTokenClaims1, userInfo1);
        Assertions.assertEquals(idTokenGroups, groups1);

        com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet idTokenClaims2 = mock(com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.class);
        when(idTokenClaims2.getClaim("groups")).thenReturn(null);

        List<String> userInfoGroups = Arrays.asList("group3", "group4");
        com.nimbusds.openid.connect.sdk.claims.UserInfo userInfo2 = new com.nimbusds.openid.connect.sdk.claims.UserInfo(subject);
        userInfo2.setClaim("groups", userInfoGroups);

        @SuppressWarnings("unchecked")
        List<String> groups2 = (List<String>) extractGroupsMethod.invoke(oidcAuthenticator, providerConfig, idTokenClaims2, userInfo2);
        Assertions.assertEquals(userInfoGroups, groups2);

        OidcProviderConfig providerConfig2 = new OidcProviderConfig();
        providerConfig2.setGroupsClaim(null);

        @SuppressWarnings("unchecked")
        List<String> groups3 = (List<String>) extractGroupsMethod.invoke(oidcAuthenticator, providerConfig2, idTokenClaims1, userInfo1);
        Assertions.assertTrue(groups3.isEmpty());

        @SuppressWarnings("unchecked")
        List<String> groups4 = (List<String>) extractGroupsMethod.invoke(oidcAuthenticator, providerConfig, idTokenClaims2, 
            new com.nimbusds.openid.connect.sdk.claims.UserInfo(subject));
        Assertions.assertTrue(groups4.isEmpty());
    }

    @Test
    public void testDetermineUserType() throws Exception {
        java.lang.reflect.Method determineUserTypeMethod = OidcAuthenticator.class.getDeclaredMethod(
            "determineUserType", List.class);
        ReflectionUtils.makeAccessible(determineUserTypeMethod);

        OidcUserConfig userConfig = new OidcUserConfig();
        userConfig.setAdminGroupMapping(Arrays.asList("admin-group", "super-admin"));
        when(oidcConfigProperties.getUser()).thenReturn(userConfig);

        List<String> adminGroups = Arrays.asList("user-group", "admin-group");
        UserType userType1 = (UserType) determineUserTypeMethod.invoke(oidcAuthenticator, new Object[] { adminGroups });
        Assertions.assertEquals(UserType.ADMIN_USER, userType1);

        List<String> regularGroups = Arrays.asList("user-group", "another-group");
        UserType userType2 = (UserType) determineUserTypeMethod.invoke(oidcAuthenticator, new Object[] { regularGroups });
        Assertions.assertEquals(UserType.GENERAL_USER, userType2);

        UserType userType3 = (UserType) determineUserTypeMethod.invoke(oidcAuthenticator, new Object[] { null });
        Assertions.assertEquals(UserType.GENERAL_USER, userType3);

        userConfig.setAdminGroupMapping(Collections.emptyList());
        UserType userType4 = (UserType) determineUserTypeMethod.invoke(oidcAuthenticator, new Object[] { adminGroups });
        Assertions.assertEquals(UserType.GENERAL_USER, userType4);

        userConfig.setAdminGroupMapping(null);
        UserType userType5 = (UserType) determineUserTypeMethod.invoke(oidcAuthenticator, new Object[] { adminGroups });
        Assertions.assertEquals(UserType.GENERAL_USER, userType5);
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
