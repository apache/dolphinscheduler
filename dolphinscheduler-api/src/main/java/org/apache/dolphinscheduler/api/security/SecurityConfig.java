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

package org.apache.dolphinscheduler.api.security;

import org.apache.dolphinscheduler.api.security.plugins.ldap.LdapAuthenticator;
import org.apache.dolphinscheduler.api.security.plugins.ldap.LdapLoginCredentials;
import org.apache.dolphinscheduler.api.security.plugins.oauth2.OAuth2Authenticator;
import org.apache.dolphinscheduler.api.security.plugins.oauth2.OAuth2LoginCredentials;
import org.apache.dolphinscheduler.api.security.plugins.pwd.PasswordAuthenticator;
import org.apache.dolphinscheduler.api.security.plugins.pwd.PasswordLoginCredentials;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// public class SecurityConfig extends WebSecurityConfigurerAdapter {
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${security.authentication.type:PASSWORD}")
    private String type;

    private AutowireCapableBeanFactory beanFactory;

    private AuthenticationType authenticationType;

    @Autowired
    public SecurityConfig(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    private void setAuthenticationType(String type) {
        if (StringUtils.isBlank(type)) {
            logger.info("security.authentication.type configuration is empty, the default value 'PASSWORD'");
            this.authenticationType = AuthenticationType.PASSWORD;
            return;
        }

        this.authenticationType = AuthenticationType.valueOf(type);
    }

    @Bean(name = "authenticator")
    public Authenticator authenticator() {
        setAuthenticationType(type);
        Authenticator authenticator;
        switch (authenticationType) {
            case PASSWORD:
                authenticator = new PasswordAuthenticator();
                break;
            case LDAP:
                authenticator = new LdapAuthenticator();
                break;
            case OAUTH2:
                authenticator = new OAuth2Authenticator();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + authenticationType);
        }
        beanFactory.autowireBean(authenticator);
        return authenticator;
    }

    @Bean
    public AbstractLoginCredentials loginCredentials() {
        setAuthenticationType(type);
        AbstractLoginCredentials credentials;
        switch (authenticationType) {
            case PASSWORD:
                credentials = new PasswordLoginCredentials();
                break;
            case LDAP:
                credentials = new LdapLoginCredentials();
                break;
            case OAUTH2:
                credentials = new OAuth2LoginCredentials();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + authenticationType);
        }
        beanFactory.autowireBean(credentials);
        return credentials;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .and().logout().invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true);
        return http.build();
    }

    public String getType() {
        return type;
    }
}
