/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.security.impl.sso;

import org.apache.dolphinscheduler.api.security.impl.AbstractSsoAuthenticator;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import java.security.MessageDigest;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GenericOidcAuthenticator extends AbstractSsoAuthenticator {

    @Autowired
    private UsersService usersService;

    @Value("${security.authentication.oidc.redirect-url}")
    private String redirectUrl;

    @Value("${security.authentication.oidc.user.admin:#{null}}")
    private String adminUserName;

    @Autowired
    private OidcService oidcService;

    @Override
    public User login(@NonNull String userName, String code) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String originalState = (String) request.getSession().getAttribute(Constants.SSO_LOGIN_USER_STATE);
        request.getSession().setAttribute(Constants.SSO_LOGIN_USER_STATE, null);

        if (originalState == null || !MessageDigest.isEqual(originalState.getBytes(), userName.getBytes())) {
            log.warn("CSRF State validation failed");
            return null;
        }

        String token = oidcService.getToken(code, userName);
        OidcUserInfo userInfo = oidcService.getUserInfo(token);
        if (userInfo == null || userInfo.getUsername() == null) {
            log.warn("OIDC login failed, userInfo missing");
            return null;
        }

        User user = usersService.getUserByUserName(userInfo.getUsername());
        if (user == null) {
            user = usersService.createUser(getUserType(userInfo.getUsername()), userInfo.getUsername(), userInfo.getEmail());
        }

        return user;
    }

    public UserType getUserType(String userName) {
        return adminUserName != null && adminUserName.equalsIgnoreCase(userName) ? UserType.ADMIN_USER : UserType.GENERAL_USER;
    }

    @Override
    public String getSignInUrl(String state) {
        return oidcService.getSignInUrl(redirectUrl, state);
    }
}
