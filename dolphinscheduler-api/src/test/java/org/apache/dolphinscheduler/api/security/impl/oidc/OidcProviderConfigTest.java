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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OidcProviderConfigTest {

    @Test
    public void testGettersAndSetters() {
        OidcProviderConfig config = new OidcProviderConfig();

        Assertions.assertEquals("client_secret_basic", config.getClientAuthenticationMethod());
        Assertions.assertEquals("openid,profile,email", config.getScope());
        Assertions.assertEquals("preferred_username", config.getUserNameAttribute());

        config.setDisplayName("Test Provider");
        Assertions.assertEquals("Test Provider", config.getDisplayName());

        config.setIssuerUri("https://test-issuer.com");
        Assertions.assertEquals("https://test-issuer.com", config.getIssuerUri());

        config.setIconUri("/icons/test.png");
        Assertions.assertEquals("/icons/test.png", config.getIconUri());

        config.setClientId("test-client-id");
        Assertions.assertEquals("test-client-id", config.getClientId());

        config.setClientSecret("test-client-secret");
        Assertions.assertEquals("test-client-secret", config.getClientSecret());

        config.setClientAuthenticationMethod("client_secret_post");
        Assertions.assertEquals("client_secret_post", config.getClientAuthenticationMethod());

        config.setScope("openid,email");
        Assertions.assertEquals("openid,email", config.getScope());

        config.setUserNameAttribute("email");
        Assertions.assertEquals("email", config.getUserNameAttribute());

        config.setGroupsClaim("roles");
        Assertions.assertEquals("roles", config.getGroupsClaim());
    }

    @Test
    public void testEqualsAndHashCode() {
        OidcProviderConfig config1 = new OidcProviderConfig();
        config1.setDisplayName("Test Provider");
        config1.setIssuerUri("https://test-issuer.com");
        config1.setClientId("test-client-id");
        config1.setIconUri("/icon.png");

        OidcProviderConfig config2 = new OidcProviderConfig();
        config2.setDisplayName("Test Provider");
        config2.setIssuerUri("https://test-issuer.com");
        config2.setClientId("test-client-id");
        config2.setIconUri("/icon.png");

        OidcProviderConfig config3 = new OidcProviderConfig();
        config3.setDisplayName("Different Provider");
        config3.setIssuerUri("https://different-issuer.com");
        config3.setClientId("different-client-id");
        config3.setIconUri("/different.png");

        Assertions.assertEquals(config1, config2);
        Assertions.assertNotEquals(config1, config3);

        Assertions.assertEquals(config1.hashCode(), config2.hashCode());
        Assertions.assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    public void testToString() {
        OidcProviderConfig config = new OidcProviderConfig();
        config.setDisplayName("Test Provider");
        config.setIssuerUri("https://test-issuer.com");
        config.setClientId("test-client-id");
        config.setIconUri("/icon.png");

        String toString = config.toString();

        Assertions.assertNotNull(toString);
        Assertions.assertTrue(toString.contains("displayName=Test Provider"));
        Assertions.assertTrue(toString.contains("issuerUri=https://test-issuer.com"));
        Assertions.assertTrue(toString.contains("clientId=test-client-id"));
        Assertions.assertTrue(toString.contains("iconUri=/icon.png"));
    }
}
