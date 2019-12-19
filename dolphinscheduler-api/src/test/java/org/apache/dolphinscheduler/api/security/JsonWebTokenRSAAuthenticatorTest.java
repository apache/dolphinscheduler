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
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.Tenant;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "security.authentication.jwt.public-key-file=/tmp/rsa_public_key.pem",
        "security.authentication.jwt.private-key-file=/tmp/rsa_private_key.pem",
        "security.authentication.jwt.generate-token-mode=local",
})
public class JsonWebTokenRSAAuthenticatorTest {
    private static Logger logger = LoggerFactory.getLogger(JsonWebTokenRSAAuthenticatorTest.class);
    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private JsonWebTokenRSAAuthenticator authenticator;

    private String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuv1nvSBOqH61sl2RnOOp\n" +
            "RZN4NOIvZ0TBnQp4vXDEwBB4u9+NS1VA8VWSeVIL1s/TL6zPobneLrsfEO4LnGAr\n" +
            "rn9/FBZjqwznwtzXp6HLy9b6VCbprMYVaWW4Wrlqu+YgNvd6781v9RbzxN1Zn0cm\n" +
            "fQ+LDYUzbSP3gpn6hRKqBu+RMQJ07u9rqlfyCy6c/qXT38YJh8+R5YdKeIdhAjWg\n" +
            "SzAoj7G13QI4nWhcsigyG372UMjTVet4LXddDQYNnQ01UadbxR+IHy2TkcZLvUsn\n" +
            "M1hkYU7arp9y9wiQaOsXtXZ0lLbtEJzO+vMMQ7beheXL3PNxxAvydwu1ytJK2xOr\n" +
            "fQIDAQAB\n" +
            "-----END PUBLIC KEY-----";
    private String PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpAIBAAKCAQEAuv1nvSBOqH61sl2RnOOpRZN4NOIvZ0TBnQp4vXDEwBB4u9+N\n" +
            "S1VA8VWSeVIL1s/TL6zPobneLrsfEO4LnGArrn9/FBZjqwznwtzXp6HLy9b6VCbp\n" +
            "rMYVaWW4Wrlqu+YgNvd6781v9RbzxN1Zn0cmfQ+LDYUzbSP3gpn6hRKqBu+RMQJ0\n" +
            "7u9rqlfyCy6c/qXT38YJh8+R5YdKeIdhAjWgSzAoj7G13QI4nWhcsigyG372UMjT\n" +
            "Vet4LXddDQYNnQ01UadbxR+IHy2TkcZLvUsnM1hkYU7arp9y9wiQaOsXtXZ0lLbt\n" +
            "EJzO+vMMQ7beheXL3PNxxAvydwu1ytJK2xOrfQIDAQABAoIBAFu7q2Pd8Ji6qLqb\n" +
            "Kq9UBJvMb+FJwUWhNnarR3ZwVyV+LyzDzBZT+asrdtMrzAETLaCq36WQgI5eYCw3\n" +
            "C90oVOWjSJ17KmQkzaIMI4PqXQdgLKiR+ns3JhPxwhJH+jO5/r3YhSgVmcIcA3Tl\n" +
            "vmzkQ96AfJAic4ssZOVeUsdr05vYnq6xyWkBHJlBrCO85lTwTuijYJWxbx9Sxnuz\n" +
            "jkkdNiOpjXvzI0JxkSEpJo8DkcT8+fAYdhrmBuGFM0W/9nt23XAD1UiDV5kfY83w\n" +
            "TdV6xNWFGGeBro4y7my71yQJQj8qxsgy4hSD919BxXy2ye91CkmOsZD5ICP/Vavl\n" +
            "GSGm0YECgYEA269D96sm+h81dNEaZOhOmUetGQDHDMF/GhRfHPy52HBuofGuNTsR\n" +
            "EWD0x0/8pQwLXMIW+EXiz2ugvy20gkWfzXMypXVsKncEjGR3P2/x151XJFU8h10d\n" +
            "HuKfC3DiiQd6vS02v978xfFy5cNCa0Hw/vr0c8rncK7lrs7kbXSnbJ0CgYEA2eaL\n" +
            "cP0GUp1Pj7JJlS1jCdMe4LSgQOmi6JMGGVp4c0tnIpz1Kx6USnbOT0Xi9ZQlvmo3\n" +
            "N4DofBuEvZ5yXx1i75jzIRWwe4lCGCZ323hlnD7zxTjP0JebPvExN9RyN2yILI5B\n" +
            "qUsGYJUMh6/y1StkcYyLHzHy8qoH5myFDBgAVGECgYA0tGC/C43HVjNx+jS13tH0\n" +
            "jjCmHfyjP8zpidej0bIYjijMq3nbBXe7zqILK1J2mmQjdeEi4Ulf7/dZxnJXCrMj\n" +
            "DC8raPdxXKp3sCa3znib/QCKE/T4mMtCvKXhjcybiXLV7gIDmFDWgG3LV8QYjXJv\n" +
            "CfAZ06Ug8KTkAnjlFaaRNQKBgQCt0rO51p6MmOE3CEqarjIrTDd9mZmdLsO+NErR\n" +
            "PtKHZsdHXV5Wn3Y8ULoTkvSSYzQYyjJyHGtZVbp2aUdjiNW7vI92/Q5j/gKzsTw7\n" +
            "37dvBOkBk2h0RfyaIV3Z46eZfwLCSFko66iSs1PjigB0/MCLtEnnALmFyw2ySy93\n" +
            "jk31AQKBgQCuyb9Fe0VPtqP2v2yOQBmQVMDH+jK7szLILQlkyDMRYrkNAVfiushD\n" +
            "vU06A8RX8ohIZtcRmY/a4du5jxFUYPcpCLV1x24T3sdbrl5WciIX4hnJPtfHWae1\n" +
            "YOw8+MCDq/pjrF3if4bmo7tQ+ad+HrYyPxoeyENorME57HjA1Rvn9w==\n" +
            "-----END RSA PRIVATE KEY-----";

    @Bean
    public TenantService tenantService() {
        Tenant tenant = new Tenant();
        tenant.setId(0);
        tenant.setTenantName("default");
        tenant.setTenantCode("default");
        Map<String, Object> tenantMap = new HashMap<>();
        tenantMap.put(Constants.STATUS, Status.SUCCESS);
        tenantMap.put(Constants.DATA_LIST, Collections.singletonList(tenant));
        TenantService tenantService = Mockito.mock(TenantService.class);
        when(tenantService.queryTenantList("default")).thenReturn(tenantMap);
        return tenantService;
    }

    @Before
    public void setUp() throws Exception {
        writeKeyToTempDir("/tmp/rsa_public_key.pem", PUBLIC_KEY);
        writeKeyToTempDir("/tmp/rsa_private_key.pem", PRIVATE_KEY);

        authenticator = new JsonWebTokenRSAAuthenticator();
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
    public void getAuthUser() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        String token = getMockToken();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(Constants.USER_AUTH, token)});

        // get auth user
        User user = authenticator.getAuthUser(request);
        Assert.assertNotNull(user);
        logger.info(user.toString());
    }

    @Test
    public void getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        PublicKey publicKey = authenticator.getPublicKey(PUBLIC_KEY);
        Assert.assertNotNull(publicKey);
        logger.info(publicKey.toString());
    }

    @Test
    public void getPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        PrivateKey privateKey = authenticator.getPrivateKey(PRIVATE_KEY);
        Assert.assertNotNull(privateKey);
        logger.info(privateKey.toString());
    }

    private String getMockToken() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        // get token
        PrivateKey privateKey = authenticator.getPrivateKey(PRIVATE_KEY);
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "admin");
        claims.put("email", "test@xxx.com");
        return Jwts.builder()
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(5, ChronoUnit.MINUTES)))
                .addClaims(claims)
                .setId(UUID.randomUUID().toString())
                .signWith(privateKey)
                .compact();
    }

    private void writeKeyToTempDir(String filename, String contents) throws IOException {
        File file = new File(filename);
        file.delete();
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(contents);
        writer.flush();
        writer.close();
    }
}
