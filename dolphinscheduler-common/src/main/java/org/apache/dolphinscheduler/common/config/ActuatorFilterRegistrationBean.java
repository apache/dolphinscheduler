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

package org.apache.dolphinscheduler.common.config;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(1)
@Component
@Configuration
public class ActuatorFilterRegistrationBean extends FilterRegistrationBean<Filter> {

    @Value("${management.security.password:#{null}}")
    private String password;

    private final boolean enable = PropertyUtils.getBoolean("management.security.enable", false);
    private final String username = PropertyUtils.getString("management.security.username", "");

    @PostConstruct
    public void init() {
        log.info("ActuatorFilterRegistrationBean enable:{}, username:{}, password:{}", enable, username, "*****");

        if (enable) {
            setFilter(new ActuatorFilter());

            Map<String, String> initParams = new HashMap<>(4);
            initParams.put("enable", Boolean.toString(true));
            initParams.put("username", this.username);
            initParams.put("password", this.password);
            setInitParameters(initParams);
        } else {
            setFilter(new ActuatorFilter());
        }
    }

    static class ActuatorFilter implements Filter {

        private final static String ACTUATOR_PATH_PATTERN_1 = "/dolphinscheduler/actuator/";
        private final static String ACTUATOR_PATH_PATTERN_2 = "/actuator/";
        private final static String HEADER_ACTUATOR_AUTH = "Authorization";

        private boolean enable;
        private String username;
        private String password;

        @Override
        public void init(FilterConfig filterConfig) {
            this.enable = Boolean.parseBoolean(filterConfig.getInitParameter("enable"));
            this.username = filterConfig.getInitParameter("username");
            this.password = filterConfig.getInitParameter("password");

            log.info("ActuatorFilter init, enable:{}, username:{}, password:{}", enable, username, "*****");
        }

        /**
         * Implements the doFilter method of the Filter interface to intercept and process requests.
         * Main functionalities include:
         * 1. Determine whether to execute the filtering logic based on the `enable` flag.
         * 2. Perform special authentication for Actuator interface requests.
         * 3. Allow other non-Actuator interface requests to pass through.
         *
         * @param servletRequest The Servlet request object, used to obtain request information.
         * @param servletResponse The Servlet response object, used to return results to the client.
         * @param filterChain The filter chain object, used to pass the request to the next filter or target resource.
         * @throws IOException If an I/O error occurs during execution.
         * @throws ServletException If the Servlet encounters an error.
         */
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                             FilterChain filterChain) throws IOException, ServletException {
            // Determine if the filtering logic should be executed
            if (!enable) {
                // If not enabled, allow the request to pass through
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                // Cast the ServletRequest to HttpServletRequest to get the request URL
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                String url = request.getRequestURI();

                // Check if the request URL starts with the Actuator path patterns
                if (url.startsWith(ACTUATOR_PATH_PATTERN_1) || url.startsWith(ACTUATOR_PATH_PATTERN_2)) {
                    // Get the Authorization header value from the request
                    String authRequest = request.getHeader(HEADER_ACTUATOR_AUTH);
                    // If the Authorization header is blank, log an error and throw an exception
                    if (StringUtils.isBlank(authRequest)) {
                        log.error("Actuator interface authentication failed, the Authorization header cannot be empty");
                        throw new ServletException(
                                "Actuator interface authentication failed, the Authorization header cannot be empty");
                    }
                    // Generate the correct Authorization header value
                    String authReal = getBaseAuth(this.username, this.password);
                    // Compare the Authorization header value from the request with the correct value
                    if (authReal.equalsIgnoreCase(authRequest)) {
                        // If they match, allow the request to pass through
                        filterChain.doFilter(servletRequest, servletResponse);
                    } else {
                        // If they do not match, log an error and throw an exception
                        log.error(
                                "Actuator interface authentication failed, the Authorization header does not match, authReal:{}, authRequest:{}",
                                authReal,
                                authRequest);
                        throw new ServletException(
                                "Actuator interface authentication failed, the Authorization header does not match");
                    }
                } else {
                    // For non-Actuator interface requests, allow them to pass through
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            }
        }

        @Override
        public void destroy() {
            log.info("ActuatorFilter destroy");
        }

        /**
         * Generates the HTTP Basic Authentication header value.
         * This method combines the username and password into a Base64 encoded string, which is used in the Authorization header of HTTP requests.
         *
         * @param userName The username for authentication.
         * @param password The password, used in conjunction with the username for authentication.
         * @return The Authorization header value formatted as "Basic " followed by the Base64 encoded username and password.
         */
        private String getBaseAuth(String userName, String password) {
            // Concatenate the username and password into a single string, separated by a colon
            String auth = userName + ":" + password;
            // Convert the concatenated string to a byte array and encode it using Base64, generating a URL-safe string
            String base64Auth = Base64.encodeBase64URLSafeString(auth.getBytes());
            // Concatenate the "Basic " prefix with the Base64 encoded string to form the final Authorization header
            // value
            return "Basic " + base64Auth;
        }

    }
}
