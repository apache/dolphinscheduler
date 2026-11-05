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

package org.apache.dolphinscheduler.extract.base.authentication;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.Getter;

public final class RpcAuthenticationConfig {

    @Getter
    private final boolean enabled;

    @Getter
    private final int timeoutMillis;

    private final SecretKeySpec secretKey;

    private RpcAuthenticationConfig(boolean enabled, int timeoutMillis, byte[] secret) {
        this.enabled = enabled;
        this.timeoutMillis = timeoutMillis;
        this.secretKey = secret == null ? null : new SecretKeySpec(secret, "HmacSHA256");
    }

    public static RpcAuthenticationConfig disabled() {
        return new RpcAuthenticationConfig(false, 5000, null);
    }

    public static RpcAuthenticationConfig load() {
        String enabled = PropertyUtils.getString("rpc.authentication.enabled", "false").trim();
        if ("false".equalsIgnoreCase(enabled)) {
            return disabled();
        }
        if (!"true".equalsIgnoreCase(enabled)) {
            throw new IllegalArgumentException("rpc.authentication.enabled must be true or false");
        }
        return fromSecret(PropertyUtils.getString("rpc.authentication.secret"), 5000);
    }

    public static RpcAuthenticationConfig fromSecret(String encodedSecret, int timeoutMillis) {
        if (encodedSecret == null || encodedSecret.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "rpc.authentication.secret is required when authentication is enabled");
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("RPC authentication timeout must be positive");
        }
        final byte[] secret;
        try {
            secret = Base64.getDecoder().decode(encodedSecret.trim());
        } catch (IllegalArgumentException e) {
            // Do not include the secret or decoding errors, which may contain credentials.
            throw new IllegalArgumentException(
                    "rpc.authentication.secret must be a Base64-encoded 32-byte random secret");
        }
        if (secret.length != 32) {
            throw new IllegalArgumentException(
                    "rpc.authentication.secret must be a Base64-encoded 32-byte random secret");
        }
        return new RpcAuthenticationConfig(true, timeoutMillis, secret);
    }

    Mac newMac() throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        return mac;
    }
}
