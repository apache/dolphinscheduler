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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@ActiveProfiles(ProfileType.H2)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
@TestPropertySource(
        properties = {
                "security.authentication.type=LDAP",
                "security.authentication.ldap.user.admin=read-only-admin",
                "security.authentication.ldap.urls=ldap://ldap.forumsys.com:389/",
                "security.authentication.ldap.base-dn=dc=example,dc=com",
                "security.authentication.ldap.username=cn=read-only-admin,dc=example,dc=com",
                "security.authentication.ldap.password=password",
                "security.authentication.ldap.user.identity-attribute=uid",
                "security.authentication.ldap.user.email-attribute=mail",
                "security.authentication.ldap.user.not-exist-action=CREATE",
        })
public class LdapServiceTest {
    @Autowired
    protected AutowireCapableBeanFactory beanFactory;

    private LdapService ldapService;

    @Before
    public void setUp() {
        ldapService = new LdapService();
        beanFactory.autowireBean(ldapService);
    }

    @Test
    public void getUserType() {
        UserType userType = ldapService.getUserType("read-only-admin");
        Assert.assertEquals(UserType.ADMIN_USER, userType);
    }

    @Test
    public void ldapLogin() {
        String email = ldapService.ldapLogin("tesla", "password");
        Assert.assertEquals("tesla@ldap.forumsys.com", email);

        String email2 = ldapService.ldapLogin("tesla", "error password");
        Assert.assertNull(email2);
    }

    @Test
    public void ldapLoginError() {
        String email = ldapService.ldapLogin("tesla", "password");
        Assert.assertEquals("tesla@ldap.forumsys.com", email);

        String email2 = ldapService.ldapLogin("tesla", "error password");
        Assert.assertNull(email2);
    }
}
