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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OidcUserConfigTest {

    @Test
    public void testGettersAndSetters() {
        OidcUserConfig config = new OidcUserConfig();

        Assertions.assertFalse(config.isAutoCreate());
        Assertions.assertEquals("default", config.getDefaultTenantCode());
        Assertions.assertEquals("default", config.getDefaultQueue());
        Assertions.assertNull(config.getAdminGroupMapping());

        config.setAutoCreate(true);
        Assertions.assertTrue(config.isAutoCreate());

        config.setDefaultTenantCode("custom-tenant");
        Assertions.assertEquals("custom-tenant", config.getDefaultTenantCode());

        config.setDefaultQueue("custom-queue");
        Assertions.assertEquals("custom-queue", config.getDefaultQueue());

        List<String> adminGroups = Arrays.asList("admin-group-1", "admin-group-2");
        config.setAdminGroupMapping(adminGroups);
        Assertions.assertEquals(adminGroups, config.getAdminGroupMapping());
    }

    @Test
    public void testEqualsAndHashCode() {
        OidcUserConfig config1 = new OidcUserConfig();
        config1.setAutoCreate(true);
        config1.setDefaultTenantCode("custom-tenant");
        config1.setDefaultQueue("custom-queue");
        config1.setAdminGroupMapping(Collections.singletonList("admin-group"));

        OidcUserConfig config2 = new OidcUserConfig();
        config2.setAutoCreate(true);
        config2.setDefaultTenantCode("custom-tenant");
        config2.setDefaultQueue("custom-queue");
        config2.setAdminGroupMapping(Collections.singletonList("admin-group"));

        OidcUserConfig config3 = new OidcUserConfig();
        config3.setAutoCreate(false);
        config3.setDefaultTenantCode("different-tenant");
        config3.setDefaultQueue("different-queue");
        config3.setAdminGroupMapping(Collections.singletonList("different-group"));

        Assertions.assertEquals(config1, config2);
        Assertions.assertNotEquals(config1, config3);

        Assertions.assertEquals(config1.hashCode(), config2.hashCode());
        Assertions.assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    public void testToString() {
        OidcUserConfig config = new OidcUserConfig();
        config.setAutoCreate(true);
        config.setDefaultTenantCode("custom-tenant");
        config.setDefaultQueue("custom-queue");
        config.setAdminGroupMapping(Collections.singletonList("admin-group"));

        String toString = config.toString();

        Assertions.assertNotNull(toString);
        Assertions.assertTrue(toString.contains("autoCreate=true"));
        Assertions.assertTrue(toString.contains("defaultTenantCode=custom-tenant"));
        Assertions.assertTrue(toString.contains("defaultQueue=custom-queue"));
        Assertions.assertTrue(toString.contains("adminGroupMapping=[admin-group]"));
    }
}
