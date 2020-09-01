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

import org.apache.dolphinscheduler.api.ApiApplicationServer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("api")
@SpringBootTest(classes = ApiApplicationServer.class)
public class LdapServiceTest {
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    private LdapService ldapService;

    @Before
    public void setUp() {
        ldapService = new LdapService();
        beanFactory.autowireBean(ldapService);
    }

    @Test
    public void ldapLogin() {
        String email = ldapService.ldapLogin("tesla", "password");
        Assert.assertEquals(email, "tesla@ldap.forumsys.com");

        String email2 = ldapService.ldapLogin("tesla", "error password");
        Assert.assertNull(email2);
    }
}