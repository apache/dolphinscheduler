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

package org.apache.dolphinscheduler.api.security.impl.sso;

import org.apache.dolphinscheduler.api.security.impl.AbstractSsoAuthenticator;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;

import org.casbin.casdoor.entity.CasdoorUser;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CasdoorAuthenticator extends AbstractSsoAuthenticator {

    @Autowired
    private UsersService usersService;
    @Autowired
    private CasdoorAuthService casdoorAuthService;
    @Value("${casdoor.redirect-url}")
    private String redirectUrl;

    @Override
    public User login(String state, String code, String extra) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            return null;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String originalState = (String) request.getSession().getAttribute(Constants.SSO_LOGIN_USER_STATE);
        // Invalid state
        request.getSession().setAttribute(Constants.SSO_LOGIN_USER_STATE, null);
        // Check state to protect from CSRF attack
        if (originalState == null || !MessageDigest.isEqual(originalState.getBytes(), state.getBytes())) {
            return null;
        }

        String token = casdoorAuthService.getOAuthToken(code, state);
        CasdoorUser casdoorUser = casdoorAuthService.parseJwtToken(token);
        User user = null;
        if (casdoorUser.getName() != null) {
            // check if user exist
            user = usersService.getUserByUserName(casdoorUser.getName());
            if (user == null) {
                user = usersService.createUser(UserType.GENERAL_USER, casdoorUser.getName(), casdoorUser.getEmail());
            }
        }
        return user;
    }

    @Override
    public String getSignInUrl(String state) {
        return casdoorAuthService.getSigninUrl(redirectUrl, state);
    }

}
