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

package org.apache.dolphinscheduler.api.interceptor;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.thread.ThreadLocalContext;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * login interceptor, must log in first
 */
public class LoginHandlerInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandlerInterceptor.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Authenticator authenticator;

    /**
     * Intercept the execution of a handler. Called after HandlerMapping determined
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return boolean true or false
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // get token
        String token = request.getHeader("token");
        User user;
        if (StringUtils.isEmpty(token)) {
            user = authenticator.getAuthUser(request);
            // if user is null
            if (user == null) {
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                logger.info("user does not exist");
                return false;
            }
        } else {
            user = userMapper.queryUserByToken(token, new Date());
            if (user == null) {
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                logger.info("user token has expired");
                return false;
            }
        }

        // check user state
        if (user.getState() == Flag.NO.ordinal()) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            logger.info(Status.USER_DISABLED.getMsg());
            return false;
        }
        request.setAttribute(Constants.SESSION_USER, user);
        ThreadLocalContext.getTimezoneThreadLocal().set(user.getTimeZone());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        ThreadLocalContext.getTimezoneThreadLocal().remove();
    }
}
