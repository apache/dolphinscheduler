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

package org.apache.dolphinscheduler.api.security.impl.ldap;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.common.enums.ProfileType;
import org.apache.dolphinscheduler.common.enums.UserType;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Disabled
@ActiveProfiles(ProfileType.H2)
@SpringBootTest(classes = ApiApplicationServer.class)
@TestPropertySource(properties = {
        "security.authentication.type=LDAP",
        "security.authentication.ldap.user.admin=read-only-admin",
        "security.authentication.ldap.urls=ldap://ldap.forumsys.com:389/",
        "security.authentication.ldap.base-dn=dc=example,dc=com",
        "security.authentication.ldap.username=cn=read-only-admin,dc=example,dc=com",
        "security.authentication.ldap.password=password",
        "security.authentication.ldap.user.identity-attribute=uid",
        "security.authentication.ldap.user.email-attribute=mail",
        "security.authentication.ldap.user.not-exist-action=CREATE",
        "security.authentication.ldap.ssl.enable=false",
        "security.authentication.ldap.ssl.trust-store=",
        "security.authentication.ldap.ssl.trust-store-password=",
        "security.authentication.ldap.user.admin-attribute=uid",
        "security.authentication.ldap.user.admin-value=tesla"
})
public class LdapServiceTest {

    @Autowired
    protected AutowireCapableBeanFactory beanFactory;

    private LdapService ldapService;

    private final String username = "tesla";
    private final String correctPassword = "password";

    @BeforeEach
    public void setUp() {
        ldapService = new LdapService();
        beanFactory.autowireBean(ldapService);
    }

    @Test
    public void getUserType() {
        UserType userType =
                ldapService.getUserType(Map.of(LdapService.ATTRIBUTE_USER_TYPE, UserType.ADMIN_USER.toString()));
        Assertions.assertEquals(UserType.ADMIN_USER, userType);

        userType = ldapService.getUserType(Map.of());
        Assertions.assertEquals(UserType.GENERAL_USER, userType);

    }

    @Test
    public void ldapLogin() throws NoSuchFieldException, IllegalAccessException {
        changeSslEnable(false);
        Map<String, String> ldapAttributes = ldapService.ldapLogin(username, correctPassword);
        String email = ldapAttributes.get(LdapService.ATTRIBUTE_EMAIL);
        Assertions.assertEquals("tesla@ldap.forumsys.com", email);
        String userType = ldapAttributes.get(LdapService.ATTRIBUTE_USER_TYPE);
        Assertions.assertEquals(UserType.ADMIN_USER.toString(), userType);
    }

    @Test
    public void ldapLoginError() throws NoSuchFieldException, IllegalAccessException {
        changeSslEnable(false);
        Map<String, String> ldapAttributes = ldapService.ldapLogin(username, "error password");
        Assertions.assertNull(ldapAttributes);
    }

    @Test
    public void ldapLoginSSL() throws NoSuchFieldException, IllegalAccessException {
        changeSslEnable(true);
        Map<String, String> ldapAttributes = ldapService.ldapLogin(username, correctPassword);
        String email = ldapAttributes.get(LdapService.ATTRIBUTE_EMAIL);
        Assertions.assertNull(email);
    }

    private void changeSslEnable(boolean sslEnable) throws NoSuchFieldException, IllegalAccessException {
        Class<LdapService> ldapServiceClass = LdapService.class;
        Field sslEnableField = ldapServiceClass.getDeclaredField("sslEnable");
        sslEnableField.setAccessible(true);
        sslEnableField.set(ldapService, sslEnable);
        if (sslEnable) {
            Field trustStorePasswordField = ldapServiceClass.getDeclaredField("trustStorePassword");
            trustStorePasswordField.setAccessible(true);
            trustStorePasswordField.set(ldapService, "trustStorePassword");
        }
    }
}
