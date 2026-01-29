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

package org.apache.dolphinscheduler.api.security.impl.oidc;

import lombok.Data;

/**
 * OIDC provider configuration
 */
@Data
public class OidcProviderConfig {

    /**
     * Display the name for the provider (to show on the login button)
     */
    private String displayName;

    private String issuerUri;

    /**
     * URI for the provider's icon (logo) to be displayed on the login button.
     */
    private String iconUri;

    private String clientId;

    private String clientSecret;

    /**
     * OIDC client authentication method (e.g., client_secret_basic, client_secret_post)
     */
    private String clientAuthenticationMethod = "client_secret_basic";

    /**
     * OIDC scopes (comma-separated)
     */
    private String scope = "openid,profile,email";

    /**
     * OIDC username attribute (claim to use as username)
     */
    private String userNameAttribute = "preferred_username";

    /**
     * OIDC groups claim (claim containing user groups/roles)
     */
    private String groupsClaim;
}
