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

package org.apache.dolphinscheduler.api.security;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.apache.dolphinscheduler.api.security.impl.ldap.LdapService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "security.authentication.type=LDAP",
})
public class SecurityConfigLDAPTest extends AbstractControllerTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private LdapService ldapService;

    @Test
    public void testAuthenticator() {
        Authenticator authenticator = securityConfig.authenticator();
        Assertions.assertNotNull(authenticator);
    }

    @Test
    public void testLdapUserNotExistAction() {
        LdapUserNotExistActionType authenticator = ldapService.getLdapUserNotExistAction();
        Assertions.assertEquals(LdapUserNotExistActionType.CREATE, authenticator);

        boolean isCreateAction = ldapService.createIfUserNotExists();
        Assertions.assertEquals(Boolean.TRUE, isCreateAction);
    }
}
