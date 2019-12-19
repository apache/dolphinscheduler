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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.JsonWebTokenService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

/**
 * secret-key-based jwt authentication
 */
public class JsonWebTokenSecretKeyAuthenticator implements Authenticator, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(JsonWebTokenSecretKeyAuthenticator.class);
    @Autowired
    private UsersService userService;
    @Autowired
    private JsonWebTokenService jwtService;

    @Value("${security.authentication.jwt.secret.key}")
    private String secretKeyPlainText;
    @Value("${security.authentication.jwt.expire.minutes:1440}")
    private int expireTimeOfMinutes;

    private SecretKey secretKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(secretKeyPlainText)) {
            throw new Exception("Secret key can't empty. check #security.authentication.jwt.secret.key");
        }
        secretKey = Keys.hmacShaKeyFor(secretKeyPlainText.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Result<Map<String, String>> authenticate(String username, String password, String extra) {
        Result<Map<String, String>> result = new Result<>();

        User user = userService.queryUser(username, password);
        if (user == null) {
            result.setCode(Status.USER_NAME_PASSWD_ERROR.getCode());
            result.setMsg(Status.USER_NAME_PASSWD_ERROR.getMsg());
            return result;
        }

        // create token
        Instant now = Instant.now();
        String token = Jwts.builder()
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expireTimeOfMinutes, ChronoUnit.MINUTES)))
                .setIssuer(user.getUserName())
                .setSubject(user.getUserName())
                .setId(UUID.randomUUID().toString())
                .signWith(secretKey)
                .compact();
        if (StringUtils.isBlank(token)) {
            result.setCode(Status.GET_USER_TOKEN_ERROR.getCode());
            result.setMsg(Status.GET_USER_TOKEN_ERROR.getMsg());
            return result;
        }

        result.setData(Collections.singletonMap(Constants.USER_AUTH, token));
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.LOGIN_SUCCESS.getMsg());
        return result;
    }

    @Override
    public User getAuthUser(HttpServletRequest request) {
        String token = jwtService.getToken(request);
        if (StringUtils.isBlank(token)) {
            logger.error("user token is empty");
            return null;
        }
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            String name = claimsJws.getBody().getSubject();
            if (StringUtils.isNotBlank(name)) {
                return userService.queryUser(name);
            }
        } catch (Exception e) {
            logger.error("token verification failed, e={}", e.toString());
        }
        return null;
    }
}
