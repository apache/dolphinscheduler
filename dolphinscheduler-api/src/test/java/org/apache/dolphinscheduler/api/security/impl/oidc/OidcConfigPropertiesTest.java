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

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "security.authentication.type=OIDC",
        "security.authentication.oidc.enable=true",
        "security.authentication.oidc.providers.keycloak.display-name=Login with Keycloak",
        "security.authentication.oidc.providers.keycloak.issuer-uri=http://keycloak:8080/realms/dolphinscheduler",
        "security.authentication.oidc.providers.keycloak.client-id=dolphinscheduler-client",
        "security.authentication.oidc.providers.keycloak.client-secret=test-secret",
        "security.authentication.oidc.providers.keycloak.icon-uri=/icons/keycloak.png",
        "security.authentication.oidc.providers.keycloak.user-name-attribute=preferred_username",
        "security.authentication.oidc.providers.keycloak.groups-claim=groups",
        "security.authentication.oidc.user.auto-create=true",
        "security.authentication.oidc.user.default-tenant-code=default",
        "security.authentication.oidc.user.admin-group-mapping[0]=dolphinscheduler-admins"
})
public class OidcConfigPropertiesTest extends AbstractControllerTest {

    @Autowired
    private OidcConfigProperties oidcConfigProperties;

    @Test
    public void testOidcConfigPropertiesLoading() {
        Assertions.assertTrue(oidcConfigProperties.isEnable());

        OidcProviderConfig keycloakConfig = oidcConfigProperties.getProviders().get("keycloak");
        Assertions.assertNotNull(keycloakConfig);
        Assertions.assertEquals("Login with Keycloak", keycloakConfig.getDisplayName());
        Assertions.assertEquals("http://keycloak:8080/realms/dolphinscheduler", keycloakConfig.getIssuerUri());
        Assertions.assertEquals("dolphinscheduler-client", keycloakConfig.getClientId());
        Assertions.assertEquals("test-secret", keycloakConfig.getClientSecret());
        Assertions.assertEquals("/icons/keycloak.png", keycloakConfig.getIconUri());
        Assertions.assertEquals("preferred_username", keycloakConfig.getUserNameAttribute());
        Assertions.assertEquals("groups", keycloakConfig.getGroupsClaim());

        OidcUserConfig userConfig = oidcConfigProperties.getUser();
        Assertions.assertNotNull(userConfig);
        Assertions.assertTrue(userConfig.isAutoCreate());
        Assertions.assertEquals("default", userConfig.getDefaultTenantCode());
        Assertions.assertEquals(Collections.singletonList("dolphinscheduler-admins"),
                userConfig.getAdminGroupMapping());
    }
}
