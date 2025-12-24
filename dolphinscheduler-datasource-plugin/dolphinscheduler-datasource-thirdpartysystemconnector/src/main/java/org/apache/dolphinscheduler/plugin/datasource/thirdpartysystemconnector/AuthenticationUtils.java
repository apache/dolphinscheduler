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

package org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector;

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.AuthConfig;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.AuthMapping;
import org.apache.dolphinscheduler.plugin.datasource.thirdpartysystemconnector.param.ThirdPartySystemConnectorConnectionParam;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import com.fasterxml.jackson.databind.JsonNode;

@Slf4j
public class AuthenticationUtils {

    /**
     * Authenticate and get Token
     *
     * @param  thirdPartySystemConnectorConnectionParam configuration
     * @return Authenticated Token
     * @throws Exception
     */
    public static String authenticateAndGetToken(ThirdPartySystemConnectorConnectionParam thirdPartySystemConnectorConnectionParam) throws Exception {
        AuthConfig authConfig = thirdPartySystemConnectorConnectionParam.getAuthConfig();
        if (authConfig == null) {
            throw new RuntimeException("AuthConfig is not provided");
        }

        switch (authConfig.getAuthType()) {
            case BASIC_AUTH:
                // Basic authentication
                String auth = authConfig.getBasicUsername() + ":" + authConfig.getBasicPassword();
                String encoding = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
                return encoding;
            case JWT:
                // JWT authentication
                return authConfig.getJwtToken();
            case OAUTH2:
                // OAuth2 authentication
                return getOAuth2Token(thirdPartySystemConnectorConnectionParam);
            default:
                throw new RuntimeException("Unsupported auth type: " + authConfig.getAuthType());
        }
    }

    /**
     * Get OAuth2 Token
     *
     * @param thirdPartySystemConnectorConnectionParam Authentication configuration
     * @return OAuth2 Token
     * @throws Exception
     */
    private static String getOAuth2Token(ThirdPartySystemConnectorConnectionParam thirdPartySystemConnectorConnectionParam) throws Exception {
        AuthConfig authConfig = thirdPartySystemConnectorConnectionParam.getAuthConfig();
        try {
            OkHttpRequestHeaders headers = new OkHttpRequestHeaders();
            headers.setHeaders(new HashMap<>());
            headers.setOkHttpRequestHeaderContentType(OkHttpRequestHeaderContentType.APPLICATION_FORM_URLENCODED);

            FormBody.Builder formBodyBuilder = new FormBody.Builder()
                    .add("client_id", authConfig.getOauth2ClientId())
                    .add("client_secret", authConfig.getOauth2ClientSecret())
                    .add("username", authConfig.getOauth2Username())
                    .add("password", authConfig.getOauth2Password())
                    .add("grant_type", authConfig.getOauth2GrantType());

            // Add parameters from authMappings
            if (authConfig.getAuthMappings() != null) {
                for (AuthMapping authMapping : authConfig.getAuthMappings()) {
                    formBodyBuilder.add(authMapping.getKey(), authMapping.getValue());
                }
            }

            RequestBody formBody = formBodyBuilder.build();

            OkHttpResponse response = OkHttpUtils.postFormBody(
                    thirdPartySystemConnectorConnectionParam.getCompleteUrl(authConfig.getOauth2TokenUrl()),
                    headers,
                    null,
                    formBody,
                    30000, 30000, 30000);

            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Authentication failed: " + response.getBody());
            }

            JsonNode authResult = JSONUtils.parseObject(response.getBody(), JsonNode.class);
            if (authResult.has("access_token")) {
                log.info("Authentication successful, token obtained");
                return authResult.get("access_token").asText();
            } else {
                throw new RuntimeException("Failed to get access token from response");
            }

        } catch (Exception e) {
            log.error("Authentication failed", e);
            throw new RuntimeException("Authentication failed", e);
        }
    }

}
