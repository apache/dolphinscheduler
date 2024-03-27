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

package org.apache.dolphinscheduler.api.v3.security;

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {

    private static AuthenticationService authenticationService;

    @Autowired
    private UserMapper userMapper;

    @PostConstruct
    void init() {
        authenticationService = this;
    }

    public static AuthenticationService getInstance() {
        return authenticationService;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("token");

        if (token == null)
            throw new ServiceException("token not exists");

        User user = userMapper.queryUserByToken(token, new Date());

        if (user == null)
            throw new ServiceException("user not found with token: " + token);

        return new ApiTokenAuthentication(user, AuthorityUtils.createAuthorityList(
                user.getUserType().name()));
    }
}
