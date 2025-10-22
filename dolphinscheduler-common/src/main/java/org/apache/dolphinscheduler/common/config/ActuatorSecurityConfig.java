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

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Actuator endpoints.
 * <p>
 * This configuration applies security rules to Actuator paths such as:
 * <ul>
 *   <li>/actuator/</li>
 *   <li>/dolphinscheduler/actuator/</li>
 * </ul>
 * Security can be enabled/disabled via {@code management.security.enabled}.
 * When enabled, HTTP Basic authentication is required with a configured username and password.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ActuatorSecurityConfig.ActuatorSecurityProperties.class)
public class ActuatorSecurityConfig {

    private static final String ACTUATOR_PATH_PATTERN_1 = "/dolphinscheduler/actuator/";
    private static final String ACTUATOR_PATH_PATTERN_2 = "/actuator/";
    private static final String ROLE_ACTUATOR = "ACTUATOR";

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(
                                                           HttpSecurity http,
                                                           ActuatorSecurityProperties properties) throws Exception {

        http.requestMatcher(request -> request.getRequestURI().startsWith(ACTUATOR_PATH_PATTERN_1) ||
                request.getRequestURI().startsWith(ACTUATOR_PATH_PATTERN_2));

        if (properties.isEnabled()) {
            http.authorizeHttpRequests(authz -> authz
                    .anyRequest().hasRole(ROLE_ACTUATOR))
                    .httpBasic();
        } else {
            http.authorizeHttpRequests(authz -> authz
                    .anyRequest().permitAll());
        }

        http.csrf().disable();
        return http.build();
    }

    @Bean
    public UserDetailsService actuatorUserDetailsService(ActuatorSecurityProperties properties) {
        if (!properties.isEnabled()) {
            return new InMemoryUserDetailsManager();
        }

        String username = properties.getUsername();
        String password = properties.getPassword();

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("management.security.username must be configured when enabled.");
        }
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("management.security.password must be configured when enabled.");
        }

        UserDetails user = User.withUsername(username)
                .password("{noop}" + password)
                .roles(ROLE_ACTUATOR)
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Properties for Actuator security settings.
     * Maps: management.security.*
     */
    @Getter
    @Setter
    @ToString(exclude = "password")
    @ConfigurationProperties("management.security")
    public static class ActuatorSecurityProperties {

        /**
         * Whether Actuator endpoints require authentication.
         * Default: false (public access).
         */
        private boolean enabled = false;

        /**
         * Username for accessing secured Actuator endpoints.
         */
        private String username;

        /**
         * Password for authentication.
         */
        private String password;
    }
}
