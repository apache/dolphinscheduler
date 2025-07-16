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

import org.apache.dolphinscheduler.api.security.impl.AbstractSsoAuthenticator;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.net.URI;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

@Slf4j
@Component("oidcAuthenticator")
public class OidcAuthenticator extends AbstractSsoAuthenticator {

    @Autowired
    private OidcConfigProperties oidcConfig;

    @Autowired
    private UsersService usersService;

    @Value("${api.base-url:http://localhost:12345/dolphinscheduler}")
    private String apiBaseUrl;

    private final Map<String, OIDCProviderMetadata> providerMetadataCache = new ConcurrentHashMap<>();

    @Override
    public User login(@NonNull String state, String code) {

        try {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                log.error("ServletRequestAttributes is null, cannot get session");
                return null;
            }

            HttpServletRequest request = servletRequestAttributes.getRequest();
            String originalState = (String) request.getSession().getAttribute(Constants.SSO_LOGIN_USER_STATE);
            request.getSession().removeAttribute(Constants.SSO_LOGIN_USER_STATE);

            if (originalState == null || !MessageDigest.isEqual(originalState.getBytes(), state.getBytes())) {
                log.error("State validation failed. Expected: {}, Actual: {}", originalState, state);
                return null;
            }

            String[] stateParts = state.split(":", 2);
            if (stateParts.length != 2) {
                log.error("Invalid state format: {}", state);
                return null;
            }

            String providerId = stateParts[0];
            OidcProviderConfig providerConfig = oidcConfig.getProviders().get(providerId);
            if (providerConfig == null) {
                log.error("Provider not found: {}", providerId);
                return null;
            }

            OIDCProviderMetadata providerMetadata = getProviderMetadata(providerId, providerConfig);
            OIDCTokens tokens = exchangeCodeForTokens(providerMetadata, providerConfig, code, providerId);
            IDTokenClaimsSet idTokenClaims = validateIdToken(providerMetadata, providerConfig, tokens.getIDToken());
            UserInfo userInfo = getUserInfo(providerMetadata, tokens.getAccessToken());

            String username = extractUsername(providerConfig, idTokenClaims, userInfo);
            String email = extractEmail(idTokenClaims, userInfo);
            List<String> groups = extractGroups(providerConfig, idTokenClaims, userInfo);

            User user = usersService.getUserByUserName(username);

            if (user == null && oidcConfig.getUser().isAutoCreate()) {
                UserType userType = determineUserType(groups);
                user = usersService.createUser(userType, username, email);
            }

            return user;
        } catch (Exception e) {
            log.error("An error occurred during OIDC authentication.", e);
            return null;
        }
    }

    @Override
    public String getSignInUrl(String state) {

        try {
            String[] stateParts = state.split(":", 2);
            String providerId = stateParts[0];
            OidcProviderConfig providerConfig = oidcConfig.getProviders().get(providerId);

            OIDCProviderMetadata providerMetadata = getProviderMetadata(providerId, providerConfig);
            ClientID clientID = new ClientID(providerConfig.getClientId());
            URI redirectURI = new URI(getCallbackUrl(providerId));
            Scope scope = parseScope(providerConfig.getScope());
            State nimbusState = new State(state);
            Nonce nonce = new Nonce();

            AuthenticationRequest authRequest = new AuthenticationRequest.Builder(
                    new ResponseType(ResponseType.Value.CODE),
                    scope,
                    clientID,
                    redirectURI)
                            .state(nimbusState)
                            .nonce(nonce)
                            .endpointURI(providerMetadata.getAuthorizationEndpointURI())
                            .build();

            return authRequest.toURI().toString();
        } catch (Exception e) {
            log.error("Error generating OIDC sign-in URL", e);
            return null;
        }
    }

    private String getCallbackUrl(String providerId) {
        return String.format("%s/login/oauth2/code/%s", apiBaseUrl, providerId);
    }

    /**
     * Get OIDC provider metadata
     */
    private OIDCProviderMetadata getProviderMetadata(String providerId,
                                                     OidcProviderConfig providerConfig) throws Exception {
        if (providerMetadataCache.containsKey(providerId)) {
            return providerMetadataCache.get(providerId);
        }

        URI wellKnownURI = new URI(providerConfig.getIssuerUri() + "/.well-known/openid-configuration");
        com.nimbusds.oauth2.sdk.http.HTTPRequest httpRequest =
                new com.nimbusds.oauth2.sdk.http.HTTPRequest(com.nimbusds.oauth2.sdk.http.HTTPRequest.Method.GET,
                        wellKnownURI.toURL());

        HTTPResponse httpResponse = httpRequest.send();
        OIDCProviderMetadata metadata = OIDCProviderMetadata.parse(httpResponse.getContent());
        providerMetadataCache.put(providerId, metadata);

        return metadata;
    }

    /**
     * Exchange authorization code for tokens
     */
    private OIDCTokens exchangeCodeForTokens(OIDCProviderMetadata providerMetadata, OidcProviderConfig providerConfig,
                                             // String code) throws Exception {
                                             String code, String providerId) throws Exception {
        ClientID clientID = new ClientID(providerConfig.getClientId());

        Secret clientSecret = new Secret(providerConfig.getClientSecret());
        URI redirectURI = new URI(getCallbackUrl(providerId));

        AuthorizationCode authorizationCode = new AuthorizationCode(code);
        AuthorizationCodeGrant codeGrant = new AuthorizationCodeGrant(authorizationCode, redirectURI);

        ClientAuthentication clientAuth;
        if ("client_secret_post".equalsIgnoreCase(providerConfig.getClientAuthenticationMethod())) {
            clientAuth = new ClientSecretPost(clientID, clientSecret);
        } else {
            clientAuth = new ClientSecretBasic(clientID, clientSecret);
        }

        TokenRequest tokenRequest = new TokenRequest(
                providerMetadata.getTokenEndpointURI(),
                clientAuth,
                codeGrant);

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());

        if (!tokenResponse.indicatesSuccess()) {
            log.error("Token request failed: {}", tokenResponse.toErrorResponse().getErrorObject());
            throw new Exception("Token exchange failed");
        }

        return ((OIDCTokenResponse) tokenResponse).getOIDCTokens();
    }

    /**
     * Validate ID token and extract claims
     */
    private IDTokenClaimsSet validateIdToken(OIDCProviderMetadata providerMetadata,
                                             OidcProviderConfig providerConfig, JWT idToken) throws Exception {
        JWTClaimsSet claimsSet = idToken.getJWTClaimsSet();

        String issuer = claimsSet.getIssuer();
        if (issuer == null || !issuer.equals(providerMetadata.getIssuer().getValue())) {
            throw new RuntimeException("Invalid issuer in ID token");
        }

        List<String> audiences = claimsSet.getAudience();
        if (audiences == null || !audiences.contains(providerConfig.getClientId())) {
            throw new RuntimeException("Invalid audience in ID token");
        }

        Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime == null || expirationTime.before(new Date())) {
            throw new RuntimeException("ID token has expired");
        }

        return new IDTokenClaimsSet(claimsSet);
    }

    /**
     * Get user info from UserInfo endpoint
     */
    private UserInfo getUserInfo(OIDCProviderMetadata providerMetadata, AccessToken accessToken) throws Exception {
        UserInfoRequest userInfoRequest = new UserInfoRequest(
                providerMetadata.getUserInfoEndpointURI(),
                accessToken);

        HTTPResponse httpResponse = userInfoRequest.toHTTPRequest().send();
        UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);

        if (!userInfoResponse.indicatesSuccess()) {
            log.error("User info request failed: {}", userInfoResponse.toErrorResponse().getErrorObject());
            return null;
        }

        return userInfoResponse.toSuccessResponse().getUserInfo();
    }

    private String extractUsername(OidcProviderConfig providerConfig, IDTokenClaimsSet idTokenClaims,
                                   UserInfo userInfo) {
        String userNameAttribute = providerConfig.getUserNameAttribute();

        Object usernameFromIdToken = idTokenClaims.getClaim(userNameAttribute);
        if (usernameFromIdToken != null) {
            return usernameFromIdToken.toString();
        }

        if (userInfo != null) {
            Object usernameFromUserInfo = userInfo.getClaim(userNameAttribute);
            if (usernameFromUserInfo != null) {
                return usernameFromUserInfo.toString();
            }
        }

        return idTokenClaims.getSubject().getValue();
    }

    private String extractEmail(IDTokenClaimsSet idTokenClaims, UserInfo userInfo) {
        Object emailFromIdToken = idTokenClaims.getClaim("email");
        if (emailFromIdToken != null) {
            return emailFromIdToken.toString();
        }

        if (userInfo != null) {
            Object emailFromUserInfo = userInfo.getClaim("email");
            if (emailFromUserInfo != null) {
                return emailFromUserInfo.toString();
            }
        }

        return null;
    }

    private List<String> extractGroups(OidcProviderConfig providerConfig, IDTokenClaimsSet idTokenClaims,
                                       UserInfo userInfo) {
        String groupsClaim = providerConfig.getGroupsClaim();
        if (groupsClaim == null || groupsClaim.isEmpty()) {
            return Collections.emptyList();
        }

        Object groupsFromIdToken = idTokenClaims.getClaim(groupsClaim);
        if (groupsFromIdToken instanceof List) {
            return (List<String>) groupsFromIdToken;
        }

        if (userInfo != null) {
            Object groupsFromUserInfo = userInfo.getClaim(groupsClaim);
            if (groupsFromUserInfo instanceof List) {
                return (List<String>) groupsFromUserInfo;
            }
        }

        return Collections.emptyList();
    }

    private UserType determineUserType(List<String> groups) {
        List<String> adminGroups = oidcConfig.getUser().getAdminGroupMapping();
        if (adminGroups != null && !adminGroups.isEmpty() && groups != null) {
            for (String group : groups) {
                if (adminGroups.contains(group)) {
                    return UserType.ADMIN_USER;
                }
            }
        }

        return UserType.GENERAL_USER;
    }

    private Scope parseScope(String scopeString) {
        if (scopeString == null || scopeString.isEmpty()) {
            return new Scope("openid", "profile", "email");
        }
        return Scope.parse(scopeString);
    }
}
