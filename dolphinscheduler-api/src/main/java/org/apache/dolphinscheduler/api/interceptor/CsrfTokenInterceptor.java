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

import org.apache.http.HttpStatus;

import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

public class CsrfTokenInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenInterceptor.class);
    private static final String DEFAULT_CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    private static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if (token != null || HttpMethod.GET.matches(request.getMethod())) {
            return true;
        }
        String csrfToken = Optional.ofNullable(request.getHeader(DEFAULT_CSRF_HEADER_NAME))
                .orElse(request.getParameter(DEFAULT_CSRF_PARAMETER_NAME));
        if (csrfToken == null) {
            response.setStatus(HttpStatus.SC_FORBIDDEN);
            logger.info("csrf token is null");
            return false;
        }
        String sessionId = Optional.of(request).map(req -> WebUtils.getCookie(req, "sessionId"))
                .map(Cookie::getValue).orElse(request.getHeader("sessionId"));
        if (sessionId == null) {
            response.setStatus(HttpStatus.SC_FORBIDDEN);
            logger.info("sessionId is null");
            return false;
        }
        String realCsrfToken = new StringBuilder(sessionId).reverse().toString();
        if (!csrfToken.equals(realCsrfToken)) {
            response.setStatus(HttpStatus.SC_FORBIDDEN);
            logger.info("csrf token is error");
            return false;
        }
        return true;
    }
}
