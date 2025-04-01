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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWKSet;
/**
 * OIDC Authenticator - Supports OIDC Providers like Keycloak, Dex, OAuthProxy.
 */
@Component
public class OIDCAuthenticator implements Authenticator {

    private JWKSet jwkSet;

    @Autowired
    private UsersService usersService;

    @Autowired
    private SessionService sessionService;

    private final Map<String, JwtDecoder> jwtDecoders = new HashMap<>();

    /**
     * Initializes OIDC provider decoders.
     */
    public OIDCAuthenticator() {
        initializeDecoders();
    }

    private void initializeDecoders() {
        jwtDecoders.put("keycloak", NimbusJwtDecoder
                .withJwkSetUri("https://your-keycloak.com/realms/your-realm/protocol/openid-connect/certs").build());
        jwtDecoders.put("dex", NimbusJwtDecoder.withJwkSetUri("https://your-dex.com/dex/keys").build());
    }

    @Override
    public Result<Map<String, String>> authenticate(String idToken, String provider, String ip) {
        if (!jwtDecoders.containsKey(provider)) {
            return Result.error(Status.OIDC_AUTH_FAILED);
        }

        try {
            Jwt jwt = jwtDecoders.get(provider).decode(idToken);
            String username = jwt.getClaim("preferred_username"); // Adjust claim name as per provider

            User user = usersService.getUserByUserName(username);
            if (user == null) {
                user = usersService.createUser(UserType.GENERAL_USER, username, "oidc@provider.com");
            }

            // Create a session for the user
            sessionService.createSessionIfAbsent(user); // Ensure this method exists

            // Prepare the result
            Map<String, String> response = new HashMap<>();
            response.put("username", user.getUserName());
            response.put("sessionId", "OIDC_SESSION_" + user.getId());

            return Result.success(response);
        } catch (Exception e) {
            return Result.error(Status.OIDC_AUTH_FAILED);
        }
    }

    @Override
    public User getAuthUser(HttpServletRequest request) {
        // Extract token from Authorization header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null; // No valid token found
        }

        String idToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
        for (JwtDecoder decoder : jwtDecoders.values()) {
            try {
                Jwt jwt = decoder.decode(idToken);
                String username = jwt.getClaim("preferred_username");

                return usersService.getUserByUserName(username);
            } catch (Exception ignored) {
                // Ignore decoding errors and try next provider
            }
        }
        return null; // Token is invalid for all configured OIDC providers
    }
}
