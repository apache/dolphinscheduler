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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
@TestPropertySource(properties = {
        "security.authentication.jwt.secret.key=eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ",
        "security.authentication.jwt.expire.minutes=60"
})
public class JsonWebTokenSecretKeyAuthenticatorTest {
    private static Logger logger = LoggerFactory.getLogger(JsonWebTokenSecretKeyAuthenticatorTest.class);

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private JsonWebTokenSecretKeyAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        authenticator = new JsonWebTokenSecretKeyAuthenticator();
        beanFactory.autowireBean(authenticator);
        authenticator.afterPropertiesSet();
    }

    @Test
    public void authenticate() {
        Result result = authenticator.authenticate("admin", "dolphinscheduler123", "127.0.0.1");
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        logger.info(result.toString());
    }

    @Test
    public void getAuthUser() {
        // get token
        String plainText = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
        SecretKey secretKey = Keys.hmacShaKeyFor(plainText.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String token = Jwts.builder()
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(5, ChronoUnit.MINUTES)))
                .setIssuer("admin")
                .setSubject("admin")
                .setId(UUID.randomUUID().toString())
                .signWith(secretKey)
                .compact();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(Constants.USER_AUTH, token)});

        // get auth user
        User user = authenticator.getAuthUser(request);
        Assert.assertNotNull(user);
        logger.info(user.toString());
    }
}
