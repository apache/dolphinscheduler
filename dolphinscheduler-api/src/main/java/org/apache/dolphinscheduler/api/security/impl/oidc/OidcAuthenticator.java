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

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.security.impl.AbstractSsoAuthenticator;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
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

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
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

    private final OidcConfigProperties oidcConfig;
    private final UsersService usersService;
    private final ApiConfig apiConfig;

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String STATE_DELIMITER = ":";
    private static final int STATE_PARTS_COUNT = 2;
    private static final int PROVIDER_ID_INDEX = 0;

    private final Map<String, OIDCProviderMetadata> providerMetadataCache = new ConcurrentHashMap<>();

    public OidcAuthenticator(OidcConfigProperties oidcConfig, UsersService usersService, ApiConfig apiConfig) {
        this.oidcConfig = oidcConfig;
        this.usersService = usersService;
        this.apiConfig = apiConfig;
    }

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
                log.error("State validation failed. Expected: {}, Actual: {}", originalState,
                        RegexUtils.escapeNRT(state));
                return null;
            }

            String[] stateParts = state.split(STATE_DELIMITER, STATE_PARTS_COUNT);
            if (stateParts.length != STATE_PARTS_COUNT) {
                log.error("Invalid state format: {}", RegexUtils.escapeNRT(state));
                return null;
            }

            String providerId = stateParts[PROVIDER_ID_INDEX];
            OidcProviderConfig providerConfig = oidcConfig.getProviders().get(providerId);
            if (providerConfig == null) {
                log.error("Provider not found: {}", RegexUtils.escapeNRT(providerId));
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
            UserType userType = determineUserType(groups);

            if (user == null) {
                if (oidcConfig.getUser().isAutoCreate()) {
                    user = usersService.createUser(userType, username, email);
                }
            } else {
                if (user.getUserType() != userType) {
                    user.setUserType(userType);
                    user = usersService.updateUser(user);
                }
            }

            return user;
        } catch (Exception e) {
            log.error("An error occurred during OIDC authentication:", e);
            return null;
        }
    }

    @Override
    public String getSignInUrl(String state) {

        try {
            String[] stateParts = state.split(STATE_DELIMITER, STATE_PARTS_COUNT);
            String providerId = stateParts[PROVIDER_ID_INDEX];
            OidcProviderConfig providerConfig = oidcConfig.getProviders().get(providerId);

            if (providerConfig == null) {
                log.error("Provider not found: {}", RegexUtils.escapeNRT(providerId));
                return null;
            }

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
            log.error("Error generating OIDC sign-in URL:", e);
            return null;
        }
    }

    private String getCallbackUrl(String providerId) {
        return String.format("%s/login/oauth2/code/%s", apiConfig.getBaseUrl(), providerId);
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
        HTTPRequest httpRequest =
                new HTTPRequest(HTTPRequest.Method.GET,
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
                                             String code, String providerId) {
        try {
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

            TokenResponse tokenResponse;
            try {
                tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());
            } catch (Exception e) {
                log.error("Failed to send token request", e);
                throw new ServiceException(Status.OIDC_TOKEN_EXCHANGE_FAILED);
            }

            if (!tokenResponse.indicatesSuccess()) {
                log.error("Token request failed: {}", tokenResponse.toErrorResponse().getErrorObject());
                throw new ServiceException(Status.OIDC_TOKEN_EXCHANGE_FAILED);
            }

            return ((OIDCTokenResponse) tokenResponse).getOIDCTokens();
        } catch (java.net.URISyntaxException e) {
            log.error("Invalid redirect URI configured for OIDC provider: {}", providerId, e);
            throw new ServiceException("Failed to construct OIDC redirect URI", e);
        }
    }

    /**
     * Validate ID token and extract claims
     */
    private IDTokenClaimsSet validateIdToken(OIDCProviderMetadata providerMetadata,
                                             OidcProviderConfig providerConfig, JWT idToken) {
        JWTClaimsSet claimsSet;
        try {
            claimsSet = idToken.getJWTClaimsSet();
        } catch (java.text.ParseException e) {
            throw new ServiceException("Error parsing ID token claims", e);
        }

        String issuer = claimsSet.getIssuer();
        if (issuer == null || !issuer.equals(providerMetadata.getIssuer().getValue())) {
            throw new ServiceException(Status.OIDC_ID_TOKEN_ISSUER_INVALID);
        }

        List<String> audiences = claimsSet.getAudience();
        if (audiences == null || !audiences.contains(providerConfig.getClientId())) {
            throw new ServiceException(Status.OIDC_ID_TOKEN_AUDIENCE_INVALID);
        }

        Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime == null || expirationTime.before(new Date())) {
            throw new ServiceException(Status.OIDC_ID_TOKEN_EXPIRED);
        }

        try {
            return new IDTokenClaimsSet(claimsSet);
        } catch (ParseException e) {
            log.error("Failed to parse ID token claims, required claims may be missing.", e);
            throw new ServiceException("ID token is missing required claims", e);
        }
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
        Object emailFromIdToken = idTokenClaims.getClaim(EMAIL_ATTRIBUTE);
        if (emailFromIdToken != null) {
            return emailFromIdToken.toString();
        }

        if (userInfo != null) {
            Object emailFromUserInfo = userInfo.getClaim(EMAIL_ATTRIBUTE);
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
            @SuppressWarnings("unchecked")
            List<String> groups = (List<String>) groupsFromIdToken;
            return groups;
        }

        if (userInfo != null) {
            Object groupsFromUserInfo = userInfo.getClaim(groupsClaim);
            if (groupsFromUserInfo instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> groups = (List<String>) groupsFromUserInfo;
                return groups;
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
            return new Scope("openid", "profile", EMAIL_ATTRIBUTE);
        }
        return Scope.parse(scopeString);
    }
}
