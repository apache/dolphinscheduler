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

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.api.security.impl.ldap.LdapAuthenticator;
import org.apache.dolphinscheduler.api.security.impl.oidc.OidcAuthenticator;
import org.apache.dolphinscheduler.api.security.impl.oidc.OidcConfigProperties;
import org.apache.dolphinscheduler.api.security.impl.pwd.PasswordAuthenticator;
import org.apache.dolphinscheduler.api.security.impl.sso.CasdoorAuthenticator;
import org.apache.dolphinscheduler.api.service.UsersService;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class SecurityConfig {

    @Value("${security.authentication.type:PASSWORD}")
    private String type;

    private final AutowireCapableBeanFactory beanFactory;
    private final OidcConfigProperties oidcConfig;
    private final UsersService usersService;
    private AuthenticationType authenticationType;
    private final ApiConfig apiConfig;

    @Autowired
    public SecurityConfig(AutowireCapableBeanFactory beanFactory,
                          Optional<OidcConfigProperties> oidcConfig,
                          Optional<UsersService> usersService,
                          ApiConfig apiConfig) {
        this.beanFactory = beanFactory;
        this.oidcConfig = oidcConfig.orElse(null);
        this.usersService = usersService.orElse(null);
        this.apiConfig = apiConfig;
    }

    private void setAuthenticationType(String type) {
        if (StringUtils.isBlank(type)) {
            log.info("security.authentication.type configuration is empty, the default value 'PASSWORD'");
            this.authenticationType = AuthenticationType.PASSWORD;
            return;
        }

        this.authenticationType = AuthenticationType.valueOf(type);
    }

    @Bean(name = "authenticator")
    @Primary
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
            case CASDOOR_SSO:
                authenticator = new CasdoorAuthenticator();
                break;
            case OIDC:
                if (oidcConfig == null || usersService == null) {
                    throw new IllegalStateException(
                            "OIDC authentication is configured, but required beans are not available.");
                }
                authenticator = new OidcAuthenticator(oidcConfig, usersService, apiConfig);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + authenticationType);
        }
        beanFactory.autowireBean(authenticator);
        return authenticator;
    }

    public String getType() {
        return type;
    }
}
