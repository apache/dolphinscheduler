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

import static org.apache.dolphinscheduler.api.enums.Status.IP_IS_EMPTY;
import static org.apache.dolphinscheduler.api.enums.Status.NOT_SUPPORT_SSO;
import static org.apache.dolphinscheduler.api.enums.Status.SIGN_OUT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.USER_LOGIN_FAILURE;

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.api.configuration.OAuth2Configuration;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.security.impl.AbstractSsoAuthenticator;
import org.apache.dolphinscheduler.api.security.impl.oidc.OidcAuthenticator;
import org.apache.dolphinscheduler.api.security.impl.oidc.OidcConfigProperties;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * login controller
 */
@Tag(name = "LOGIN_TAG")
@RestController
@RequestMapping("")
@Slf4j
public class LoginController extends BaseController {

    private final SessionService sessionService;
    private final Authenticator authenticator;
    private final OAuth2Configuration oAuth2Configuration;
    private final OidcConfigProperties oidcConfigProperties;
    private final UsersService usersService;
    private final ApiConfig apiConfig;

    @Autowired
    public LoginController(SessionService sessionService,
                           Authenticator authenticator,
                           UsersService usersService,
                           Optional<OAuth2Configuration> oAuth2Configuration,
                           Optional<OidcConfigProperties> oidcConfigProperties,
                           ApiConfig apiConfig) {
        this.sessionService = sessionService;
        this.authenticator = authenticator;
        this.usersService = usersService;
        this.oAuth2Configuration = oAuth2Configuration.orElse(null);
        this.oidcConfigProperties = oidcConfigProperties.orElse(null);
        this.apiConfig = apiConfig;
    }

    /**
     * login
     *
     * @param userName     user name
     * @param userPassword user password
     * @param request      request
     * @param response     response
     * @return login result
     */
    @Operation(summary = "login", description = "LOGIN_NOTES")
    @Parameters({
            @Parameter(name = "userName", description = "USER_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "userPassword", description = "USER_PASSWORD", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/login")
    @ApiException(USER_LOGIN_FAILURE)
    public Result login(@RequestParam(value = "userName") String userName,
                        @RequestParam(value = "userPassword") String userPassword,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        // user name check
        if (StringUtils.isEmpty(userName)) {
            return error(Status.USER_NAME_NULL.getCode(),
                    Status.USER_NAME_NULL.getMsg());
        }

        // user ip check
        String ip = getClientIpAddress(request);
        if (StringUtils.isEmpty(ip)) {
            return error(IP_IS_EMPTY.getCode(), IP_IS_EMPTY.getMsg());
        }

        // verify username and password
        Result<Map<String, String>> result = authenticator.authenticate(userName, userPassword, ip);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            return result;
        }

        response.setStatus(HttpStatus.SC_OK);
        Map<String, String> cookieMap = result.getData();
        for (Map.Entry<String, String> cookieEntry : cookieMap.entrySet()) {
            Cookie cookie = new Cookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

        return result;
    }

    /**
     * sso login
     *
     * @return sso server url
     */
    @Operation(summary = "sso login", description = "SSO_LOGIN_NOTES")
    @GetMapping(value = "/login/sso")
    @ApiException(NOT_SUPPORT_SSO)
    public Result ssoLogin(HttpServletRequest request) {
        if (authenticator instanceof OidcAuthenticator) {
            return Result.success(null);
        }

        if (authenticator instanceof AbstractSsoAuthenticator) {
            String randomState = UUID.randomUUID().toString();
            HttpSession session = request.getSession();
            if (session.getAttribute(Constants.SSO_LOGIN_USER_STATE) == null) {
                session.setAttribute(Constants.SSO_LOGIN_USER_STATE, randomState);
            }
            return Result.success(((AbstractSsoAuthenticator) authenticator).getSignInUrl(randomState));
        }
        return Result.success();
    }

    @Operation(summary = "signOut", description = "SIGN_OUT_NOTES")
    @PostMapping(value = "/signOut")
    @ApiException(SIGN_OUT_ERROR)
    public Result signOut(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                          HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        sessionService.expireSession(loginUser.getId());
        // clear session
        request.removeAttribute(Constants.SESSION_USER);
        return success();
    }

    @DeleteMapping("cookies")
    public void clearCookieSessionId(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            cookie.setMaxAge(0);
            cookie.setValue(null);
            response.addCookie(cookie);
        }
        response.setStatus(HttpStatus.SC_OK);
    }

    @Operation(summary = "getOauth2Provider", description = "GET_OAUTH2_PROVIDER")
    @GetMapping("oauth2-provider")
    public Result<List<OAuth2Configuration.OAuth2ClientProperties>> oauth2Provider() {
        if (oAuth2Configuration == null) {
            return Result.success(new ArrayList<>());
        }

        Collection<OAuth2Configuration.OAuth2ClientProperties> values = oAuth2Configuration.getProvider().values();
        List<OAuth2Configuration.OAuth2ClientProperties> providers = values.stream().map(e -> {
            OAuth2Configuration.OAuth2ClientProperties oAuth2ClientProperties =
                    new OAuth2Configuration.OAuth2ClientProperties();
            oAuth2ClientProperties.setAuthorizationUri(e.getAuthorizationUri());
            oAuth2ClientProperties.setRedirectUri(e.getRedirectUri());
            oAuth2ClientProperties.setClientId(e.getClientId());
            oAuth2ClientProperties.setProvider(e.getProvider());
            oAuth2ClientProperties.setIconUri(e.getIconUri());
            return oAuth2ClientProperties;
        }).collect(Collectors.toList());
        return Result.success(providers);
    }

    /**
     * Get OIDC providers
     * @return list of OIDC providers
     */
    @Operation(summary = "getOidcProviders", description = "GET_OIDC_PROVIDERS")
    @GetMapping("/oidc-providers")
    public Result<List<Map<String, String>>> oidcProviders() {
        if (oidcConfigProperties == null || !oidcConfigProperties.isEnable()
                || oidcConfigProperties.getProviders() == null) {
            return Result.success(new ArrayList<>());
        }

        List<Map<String, String>> providers = oidcConfigProperties.getProviders().entrySet().stream()
                .map(entry -> {
                    Map<String, String> provider = new HashMap<>();
                    provider.put("id", entry.getKey());
                    provider.put("displayName", entry.getValue().getDisplayName());
                    provider.put("iconUri", entry.getValue().getIconUri());
                    return provider;
                })
                .collect(Collectors.toList());

        return Result.success(providers);
    }

    @SneakyThrows
    @Operation(summary = "redirectToOauth2", description = "REDIRECT_TO_OAUTH2_LOGIN")
    @GetMapping("redirect/login/oauth2")
    public void loginByAuth2(@RequestParam String code, @RequestParam String provider,
                             HttpServletRequest request, HttpServletResponse response) {
        OAuth2Configuration.OAuth2ClientProperties oAuth2ClientProperties =
                oAuth2Configuration.getProvider().get(provider);
        try {
            Map<String, String> tokenRequestHeader = new HashMap<>();
            tokenRequestHeader.put("Accept", "application/json");
            Map<String, Object> requestBody = new HashMap<>(16);
            requestBody.put("client_secret", oAuth2ClientProperties.getClientSecret());
            HashMap<String, Object> requestParamsMap = new HashMap<>();
            requestParamsMap.put("client_id", oAuth2ClientProperties.getClientId());
            requestParamsMap.put("code", code);
            requestParamsMap.put("grant_type", "authorization_code");
            requestParamsMap.put("redirect_uri",
                    String.format("%s?provider=%s", oAuth2ClientProperties.getRedirectUri(), provider));
            OkHttpRequestHeaders okHttpRequestHeadersPost = new OkHttpRequestHeaders();
            okHttpRequestHeadersPost.setHeaders(tokenRequestHeader);
            okHttpRequestHeadersPost.setOkHttpRequestHeaderContentType(OkHttpRequestHeaderContentType.APPLICATION_JSON);

            String tokenJsonStr = OkHttpUtils.post(oAuth2ClientProperties.getTokenUri(), okHttpRequestHeadersPost,
                    requestParamsMap, requestBody, Constants.HTTP_CONNECT_TIMEOUT, Constants.HTTP_CONNECT_TIMEOUT,
                    Constants.HTTP_CONNECT_TIMEOUT).getBody();
            String accessToken = JSONUtils.getNodeString(tokenJsonStr, "access_token");
            Map<String, String> userInfoRequestHeaders = new HashMap<>();
            userInfoRequestHeaders.put("Accept", "application/json");
            Map<String, Object> userInfoQueryMap = new HashMap<>();
            userInfoQueryMap.put("access_token", accessToken);
            userInfoRequestHeaders.put("Authorization", "Bearer " + accessToken);
            OkHttpRequestHeaders okHttpRequestHeadersGet = new OkHttpRequestHeaders();
            okHttpRequestHeadersGet.setHeaders(userInfoRequestHeaders);

            String userInfoJsonStr = OkHttpUtils.get(oAuth2ClientProperties.getUserInfoUri(),
                    okHttpRequestHeadersGet,
                    userInfoQueryMap,
                    Constants.HTTP_CONNECT_TIMEOUT,
                    Constants.HTTP_CONNECT_TIMEOUT,
                    Constants.HTTP_CONNECT_TIMEOUT).getBody();
            String username = JSONUtils.getNodeString(userInfoJsonStr, "login");
            User user = usersService.getUserByUserName(username);
            if (user == null) {
                user = usersService.createUser(UserType.GENERAL_USER, username, null);
            }
            Session session = sessionService.createSessionIfAbsent(user);
            response.setStatus(HttpStatus.SC_MOVED_TEMPORARILY);
            response.sendRedirect(String.format("%s?sessionId=%s&authType=%s", oAuth2ClientProperties.getCallbackUrl(),
                    session.getId(), "oauth2"));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            response.setStatus(HttpStatus.SC_MOVED_TEMPORARILY);
            response.sendRedirect(String.format("%s?authType=%s&error=%s", oAuth2ClientProperties.getCallbackUrl(),
                    "oauth2", "oauth2 auth error"));
        }
    }

    /**
     * Handle OIDC callback
     *
     * @param code       authorization code
     * @param state      state parameter
     * @param response   HTTP response
     */
    @SneakyThrows
    @Operation(summary = "handleOidcCallback", description = "HANDLE_OIDC_CALLBACK")
    @GetMapping("/login/oauth2/code/{providerId}")
    public void handleOidcCallback(@PathVariable String providerId,
                                   @RequestParam(name = "code", required = false) String code,
                                   @RequestParam(name = "error", required = false) String error,
                                   @RequestParam(name = "state") String state,
                                   HttpServletResponse response) throws IOException {

        if (!state.startsWith(providerId + ":")) {
            log.error("OIDC login failed: State parameter does not match the provider ID.");
            response.sendRedirect("/dolphinscheduler/ui/#/login?error=oidc_invalid_state");
            return;
        }

        // Handle login failure from OIDC provider
        if (error != null) {
            String sanitizedError = error.replaceAll("[\n\r\t]", "_");
            log.error("OIDC login failed with error: {}.", sanitizedError);
            response.sendRedirect("/dolphinscheduler/ui/#/login?error=oidc_login_failed");
            return;
        }

        // Handle the case where code is missing without an error
        if (code == null) {
            log.error("OIDC login failed: The authorization code was not provided.");
            response.sendRedirect("/dolphinscheduler/ui/#/login?error=oidc_missing_code");
            return;
        }

        try {
            if (!(authenticator instanceof OidcAuthenticator)) {
                log.error("OIDC authentication is not active or authenticator type is incorrect.");
                response.sendRedirect("/dolphinscheduler/ui/#/login?error=oidc_not_enabled");
                return;
            }

            User user = ((OidcAuthenticator) authenticator).login(state, code);

            if (user == null) {
                log.error("OIDC authentication failed. User could not be authenticated or created.");
                response.sendRedirect("/dolphinscheduler/ui/#/login?error=oidc_authentication_failed");
                return;
            }

            Session session = sessionService.createSessionIfAbsent(user);

            response.setStatus(HttpStatus.SC_MOVED_TEMPORARILY);
            response.sendRedirect(String.format("%s/login?sessionId=%s&authType=%s",
                    apiConfig.getUiUrl(), session.getId(), "oidc"));
        } catch (Exception ex) {
            log.error("A critical error occurred during the OIDC callback process.", ex);
            try {
                response.sendRedirect("/dolphinscheduler/ui/#/login?error=oidc_critical_error");
            } catch (IOException e) {
                log.error("Failed to redirect to login page after a critical error.", e);
            }
        }
    }

    @Operation(summary = "redirectToOidc", description = "REDIRECT_TO_OIDC_LOGIN")
    @GetMapping("/oauth2/authorization/{providerId}")
    public void redirectToOidc(@PathVariable String providerId, HttpServletRequest request,
                               HttpServletResponse response) throws IOException {

        // Validate the providerId before using it
        if (oidcConfigProperties == null || oidcConfigProperties.getProviders() == null
                || !oidcConfigProperties.getProviders().containsKey(providerId)) {
            log.error("Invalid OIDC provider ID requested: {}", providerId);
            response.sendRedirect("/dolphinscheduler/ui/#/login?error=invalid_provider");
            return;
        }

        String state = providerId + ":" + UUID.randomUUID().toString();
        request.getSession().setAttribute(Constants.SSO_LOGIN_USER_STATE, state);
        String authorizationUrl = ((OidcAuthenticator) authenticator).getSignInUrl(state);

        if (authorizationUrl == null) {
            log.error("OIDC authorization URL is null for providerId: {}", providerId);
            response.sendRedirect("/dolphinscheduler/ui/#/login?error=oidc_authorization_url_null");
            return;
        }

        response.sendRedirect(authorizationUrl);
    }
}
