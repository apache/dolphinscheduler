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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.extract.base.config.NettyClientConfig;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class RpcAuthenticationConfigTest {

    @Test
    void rejectsMissingSecret() {
        assertThrows(IllegalArgumentException.class, () -> RpcAuthenticationConfig.fromSecret(null, 5000));
        assertThrows(IllegalArgumentException.class, () -> RpcAuthenticationConfig.fromSecret("", 5000));
    }

    @Test
    void rejectsEmptyShortAndMalformedSecretsWithoutDisclosingThem() throws Exception {
        for (String secret : new String[]{"", "c2hvcnQ=", "sensitive-invalid-secret!"}) {
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                    () -> RpcAuthenticationConfig.fromSecret(secret, 5000));
            if (!secret.isEmpty()) {
                assertFalse(failure.getMessage().contains(secret));
            }
            assertNull(failure.getCause());
        }
    }

    @Test
    void acceptsGeneratedBase64SecretWithTrailingNewline() throws Exception {
        String encoded = Base64.getEncoder().encodeToString(new byte[32]);
        RpcAuthenticationConfig config = RpcAuthenticationConfig.fromSecret(encoded + "\n", 5000);
        assertTrue(config.isEnabled());
        assertEquals(5000, config.getTimeoutMillis());
        assertFalse(config.toString().contains(encoded));
    }

    @Test
    void rejectsNonPositiveTimeout() {
        assertThrows(IllegalArgumentException.class,
                () -> RpcAuthenticationConfig.fromSecret("secret", 0));
        assertThrows(IllegalArgumentException.class,
                () -> RpcAuthenticationConfig.fromSecret("secret", -1));
    }
    @Test
    void clientAndServerDefaultsReadSharedProperties() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        try (MockedStatic<PropertyUtils> properties = mockStatic(PropertyUtils.class)) {
            properties.when(() -> PropertyUtils.getString("rpc.authentication.enabled", "false")).thenReturn("true");
            properties.when(() -> PropertyUtils.getString("rpc.authentication.secret")).thenReturn(secret);
            assertTrue(new NettyClientConfig().getAuthentication().isEnabled());
            assertTrue(NettyClientConfig.builder().build().getAuthentication().isEnabled());
            assertTrue(new NettyServerConfig().getAuthentication().isEnabled());
            assertTrue(NettyServerConfig.builder().build().getAuthentication().isEnabled());
        }
    }

    @Test
    void disabledAuthenticationDoesNotRequireASecret() {
        try (MockedStatic<PropertyUtils> properties = mockStatic(PropertyUtils.class)) {
            properties.when(() -> PropertyUtils.getString("rpc.authentication.enabled", "false")).thenReturn("false");
            assertFalse(RpcAuthenticationConfig.load().isEnabled());
            properties.verify(() -> PropertyUtils.getString("rpc.authentication.secret"), org.mockito.Mockito.never());
        }
    }

    @Test
    void clientAndServerDefaultsRejectMissingSecret() {
        try (MockedStatic<PropertyUtils> properties = mockStatic(PropertyUtils.class)) {
            properties.when(() -> PropertyUtils.getString("rpc.authentication.enabled", "false")).thenReturn("true");
            assertThrows(IllegalArgumentException.class, NettyClientConfig::new);
            assertThrows(IllegalArgumentException.class, () -> NettyClientConfig.builder().build());
            assertThrows(IllegalArgumentException.class, NettyServerConfig::new);
            assertThrows(IllegalArgumentException.class, () -> NettyServerConfig.builder().build());
        }
    }

    @Test
    void invalidEnabledFlagDoesNotSilentlyDisableAuthentication() {
        try (MockedStatic<PropertyUtils> properties = mockStatic(PropertyUtils.class)) {
            properties.when(() -> PropertyUtils.getString("rpc.authentication.enabled", "false")).thenReturn("typo");
            assertThrows(IllegalArgumentException.class, NettyClientConfig::new);
            assertThrows(IllegalArgumentException.class, NettyServerConfig::new);
        }
    }

}
