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

package org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param;

import java.util.List;

import lombok.Data;

@Data
public class AuthConfig {

    private String authType; // authType：BASIC, JWT, OAUTH2
    private String headerPrefix; // headerPrefix：BASIC, JWT, OAUTH2

    // === （Basic Auth） ===
    private String basicUsername;
    private String basicPassword;

    // === JWT ===
    private String jwtToken;

    // === OAuth2 ===
    private String oauth2TokenUrl;
    private String oauth2ClientId;
    private String oauth2ClientSecret;
    private String oauth2GrantType; // e.g., "client_credentials", "password"
    private String oauth2Username; // only password mode
    private String oauth2Password; // only password mode

    // === other authMappings ===
    private List<AuthMapping> authMappings;
}
