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

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.configuration.OAuth2Configuration;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.security.impl.AbstractSsoAuthenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private SessionService sessionService;

    @Autowired
    private Authenticator authenticator;

    @Autowired(required = false)
    private OAuth2Configuration oAuth2Configuration;

    @Autowired
    private UsersService usersService;

    /**
     * login
     *
     * @param userName user name
     * @param userPassword user password
     * @param request request
     * @param response response
     * @return login result
     */
    @Operation(summary = "login", description = "LOGIN_NOTES")
    @Parameters({
            @Parameter(name = "userName", description = "USER_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "userPassword", description = "USER_PASSWORD", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/login")
    @ApiException(USER_LOGIN_FAILURE)
    @AccessLogAnnotation(ignoreRequestArgs = {"userPassword", "request", "response"})
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

    /**
     * sign out
     *
     * @param loginUser login user
     * @param request request
     * @return sign out result
     */
    @Operation(summary = "signOut", description = "SIGNOUT_NOTES")
    @PostMapping(value = "/signOut")
    @ApiException(SIGN_OUT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "request"})
    public Result signOut(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                          HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        sessionService.signOut(ip, loginUser);
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
            String tokenJsonStr = OkHttpUtils.post(oAuth2ClientProperties.getTokenUri(), tokenRequestHeader,
                    requestParamsMap, requestBody);
            String accessToken = JSONUtils.getNodeString(tokenJsonStr, "access_token");
            Map<String, String> userInfoRequestHeaders = new HashMap<>();
            userInfoRequestHeaders.put("Accept", "application/json");
            Map<String, Object> userInfoQueryMap = new HashMap<>();
            userInfoQueryMap.put("access_token", accessToken);
            userInfoRequestHeaders.put("Authorization", "Bearer " + accessToken);
            String userInfoJsonStr =
                    OkHttpUtils.get(oAuth2ClientProperties.getUserInfoUri(), userInfoRequestHeaders, userInfoQueryMap);
            String username = JSONUtils.getNodeString(userInfoJsonStr, "login");
            User user = usersService.getUserByUserName(username);
            if (user == null) {
                user = usersService.createUser(username, null, null, 0, null, null, 1);
            }
            String sessionId = sessionService.createSession(user, null);
            if (sessionId == null) {
                log.error("Failed to create session, userName:{}.", user.getUserName());
            }
            response.setStatus(HttpStatus.SC_MOVED_TEMPORARILY);
            response.sendRedirect(String.format("%s?sessionId=%s&authType=%s", oAuth2ClientProperties.getCallbackUrl(),
                    sessionId, "oauth2"));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            response.setStatus(HttpStatus.SC_MOVED_TEMPORARILY);
            response.sendRedirect(String.format("%s?authType=%s&error=%s", oAuth2ClientProperties.getCallbackUrl(),
                    "oauth2", "oauth2 auth error"));
        }
    }
}
