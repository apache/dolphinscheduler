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

package org.apache.dolphinscheduler.api.security.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.security.SecurityConfig;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAuthenticator implements Authenticator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthenticator.class);

    @Autowired
    protected UsersService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SecurityConfig securityConfig;

    /**
     * user login and return user in db
     *
     * @param userId user identity field
     * @param password user login password
     * @param extra extra user login field
     * @return user object in databse
     */
    public abstract User login(String userId, String password, String extra);

    @Override
    public Result<Map<String, String>> authenticate(String userId, String password, String extra) {
        Result<Map<String, String>> result = new Result<>();
        User user = login(userId, password, extra);
        if (user == null) {
            logger.error("Username or password entered incorrectly.");
            result.setCode(Status.USER_NAME_PASSWD_ERROR.getCode());
            result.setMsg(Status.USER_NAME_PASSWD_ERROR.getMsg());
            return result;
        }

        // check user state
        if (user.getState() == Flag.NO.ordinal()) {
            logger.error("The current user is deactivated, userName:{}.", user.getUserName());
            result.setCode(Status.USER_DISABLED.getCode());
            result.setMsg(Status.USER_DISABLED.getMsg());
            return result;
        }

        // create session
        String sessionId = sessionService.createSession(user, extra);
        if (sessionId == null) {
            logger.error("Failed to create session, userName:{}.", user.getUserName());
            result.setCode(Status.LOGIN_SESSION_FAILED.getCode());
            result.setMsg(Status.LOGIN_SESSION_FAILED.getMsg());
            return result;
        }

        logger.info("Session is created and sessionId is :{}.", sessionId);

        Map<String, String> data = new HashMap<>();
        data.put(Constants.SESSION_ID, sessionId);
        data.put(Constants.SECURITY_CONFIG_TYPE, securityConfig.getType());

        result.setData(data);
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.LOGIN_SUCCESS.getMsg());
        return result;
    }

    @Override
    public User getAuthUser(HttpServletRequest request) {
        Session session = sessionService.getSession(request);
        if (session == null) {
            logger.info("session info is null ");
            return null;
        }
        //get user object from session
        return userService.queryUser(session.getUserId());
    }

}
