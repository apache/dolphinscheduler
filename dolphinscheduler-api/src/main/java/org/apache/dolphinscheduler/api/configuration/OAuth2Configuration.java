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
package org.apache.dolphinscheduler.api.configuration;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConditionalOnProperty(prefix = "security.authentication.oauth2", name = "enable", havingValue = "true")
@ConfigurationProperties(prefix = "security.authentication.oauth2")
public class OAuth2Configuration {

    private Map<String, OAuth2ClientProperties> provider = new HashMap<>();

    @Getter
    @Setter
    public static class OAuth2ClientProperties {

        private String authorizationUri;
        private String clientId;
        private String redirectUri;
        private String clientSecret;
        private String tokenUri;
        private String userInfoUri;
        private String callbackUrl;
        private String iconUri;
        private String provider;

    }
}
