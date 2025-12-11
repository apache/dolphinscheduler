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

package org.apache.dolphinscheduler.plugin.task.externalSystem;

import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class BaseExternalSystemParams {

    private Integer id; // System ID

    private String systemName; // System name
    private String serviceAddress; // Service address

    private AuthConfig authConfig; // Authentication configuration

    private InterfaceConfig selectInterface; // Query interface configuration
    private InterfaceConfig submitInterface; // Submit interface configuration
    private PollingInterfaceConfig pollStatusInterface; // Polling status interface configuration
    private InterfaceConfig stopInterface; // Stop interface configuration

    private int interfaceTimeout = 120000; // Interface timeout, default 120000 milliseconds (2 minutes)

    @Data
    public static class AuthConfig {

        private AuthType authType; // Authentication type: BASIC, JWT, OAUTH2
        private String headerPrefix; // Authentication type: BASIC, JWT, OAUTH2

        // === Basic Authentication ===
        private String basicUsername;
        private String basicPassword;

        // === JWT Authentication ===
        private String jwtToken; // JWT token

        // === OAuth2 Authentication ===
        private String oauth2TokenUrl;
        private String oauth2ClientId;
        private String oauth2ClientSecret;
        private String oauth2GrantType; // e.g., "client_credentials", "password"
        private String oauth2Username; // Password mode only
        private String oauth2Password; // Password mode only

        // === Dynamic mapping configuration (e.g. request header/parameter mapping) ===
        private AuthMapping[] authMappings;
    }

    public enum AuthType {
        BASIC_AUTH, JWT, OAUTH2
    }

    @Data
    public static class InterfaceConfig {

        private String url;
        private HttpMethod method; // Request method GET/POST
        private String body;
        private List<RequestParameter> parameters; // Parameter list
        private List<ResponseParameter> responseParameters; // Parameter list

    }

    @Data
    public static class PollingInterfaceConfig extends InterfaceConfig {

        private PollingSuccessConfig pollingSuccessConfig; // Polling success configuration
        private PollingFailureConfig pollingFailureConfig; // Polling failure configuration
    }

    @Data
    public static class RequestParameter {

        private String paramName; // Parameter name
        private String paramValue; // Parameter value (can be a fixed value or placeholder)
        private ParamLocation location; // Parameter location (header,param,body)
    }

    @Data
    public static class ResponseParameter {

        private String key;
        private String jsonPath;
    }

    @Data
    public static class PollingSuccessConfig {

        private String successField; // Success judgment field name
        private String successValue; // Value corresponding to success field
    }

    @Data
    public static class PollingFailureConfig {

        private String failureField; // Failure judgment field name
        private String failureValue; // Value corresponding to failure field
    }

    // Enum: Field types
    public enum FieldType {
        STRING, INTEGER, BOOLEAN, DATE, JSON_OBJECT, CUSTOM
    }

    // Enum: HTTP Methods
    public enum HttpMethod {
        GET, POST, PUT
    }

    // Enum: Parameter locations
    public enum ParamLocation {
        HEADER, PARAM
    }

    @Data
    public static class AuthMapping {

        private String key;
        private String value;
    }
    public String getTokenPrefix(String headerPrefix) {
        if (null == headerPrefix || headerPrefix.isEmpty()) {
            return "";
        } else {
            return headerPrefix.trim() + " ";
        }
    }
    public String getCompleteUrl(String url) {
        if (url == null || !url.startsWith("http")) {
            if (serviceAddress == null) {
                log.warn("Service address is not set.");
                return url; // 或者抛出异常，根据业务需求决定
            }
            return serviceAddress + url;
        }
        return url;
    }

}
