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
import io.jsonwebtoken.Jwts;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.JsonWebTokenService;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import javax.servlet.http.HttpServletRequest;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * rsa-based jwt authentication (only support rsa format key)
 */
public class JsonWebTokenRSAAuthenticator implements Authenticator, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(JsonWebTokenRSAAuthenticator.class);

    @Value("${security.authentication.jwt.public-key-file}")
    private String publicKeyFile;

    @Value("${security.authentication.jwt.private-key-file:}")
    private String privateKeyFile;

    @Value("${security.authentication.jwt.generate-token-mode:local}")
    private String generateTokenMode;

    @Value("${security.authentication.jwt.expire.minutes:1440}")
    private int expireTimeOfMinutes;

    @Value("${security.authentication.jwt.remote-token-url:}")
    private String remoteTokenUrl;

    @Value("${security.authentication.jwt.remote-token-regex:}")
    private String remoteTokenRegex;

    @Value("${security.authentication.jwt.remote.default.tenant.code:}")
    private String defaultRemoteTenantCode;
    private int defaultRemoteTenantId;

    @Value("${security.authentication.jwt.remote.default.queue:}")
    private String defaultRemoteQueue;

    @Value("${security.authentication.jwt.required-name-claim:name}")
    private String requiredNameClaim;

    @Value("${security.authentication.jwt.required-email-claim:email}")
    private String requiredEmailClaim;

    @Autowired
    private UsersService userService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private JsonWebTokenService jwtService;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(publicKeyFile)) {
            throw new FileNotFoundException("Pem file can't empty. path=" + publicKeyFile);
        }

        String publicKeyContent = readPemKeyContent(publicKeyFile);
        publicKey = getPublicKey(publicKeyContent);

        if (StringUtils.isNotBlank(privateKeyFile)) {
            String privateKeyContent = readPemKeyContent(privateKeyFile);
            privateKey = getPrivateKey(privateKeyContent);
        }

        if (StringUtils.isNotBlank(defaultRemoteTenantCode)) {
            Map<String, Object> result = tenantService.queryTenantList(defaultRemoteTenantCode);
            Status status = (Status) result.get(Constants.STATUS);
            if (status == Status.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<Tenant> datalist = (List<Tenant>) result.get(Constants.DATA_LIST);
                defaultRemoteTenantId = datalist.get(0).getId();
            } else {
                throw new Exception(String.format("tenant %s don't exist. please check #security.authentication.default.tenant.code"
                        , defaultRemoteTenantCode));
            }
        }
    }

    @Override
    public Result<Map<String, String>> authenticate(String username, String password, String extra) {
        Result<Map<String, String>> result = new Result<>();
        try  {
            Result<String> tokenResult;
            switch (generateTokenMode) {
                case "local":
                    tokenResult = getTokenFromLocal(username, password);
                    break;
                case "remote":
                    tokenResult = getTokenFromRemote(username, password);
                    break;
                default:
                    throw new Exception("token mode don't exist. check #security.authentication.jwt.generate-token-mode");
            }
            if (tokenResult.getCode() != Status.SUCCESS.getCode()) {
                result.setCode(tokenResult.getCode());
                result.setMsg(tokenResult.getMsg());
                return result;
            }

            User user = getUserByToken(tokenResult.getData(), true);
            if (user == null) {
                result.setCode(Status.CREATE_USER_ERROR.getCode());
                result.setMsg(Status.CREATE_USER_ERROR.getMsg());
                return result;
            }
            result.setData(Collections.singletonMap(Constants.USER_AUTH, tokenResult.getData()));
            result.setCode(Status.SUCCESS.getCode());
            result.setMsg(Status.LOGIN_SUCCESS.getMsg());
        } catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
            result.setCode(Status.USER_LOGIN_FAILURE.getCode());
            result.setMsg(Status.USER_LOGIN_FAILURE.getMsg());
        }

        return result;
    }

    /**
     * get token from local
     * @param username user name
     * @param password pwd
     * @return result
     */
    private Result<String> getTokenFromLocal(String username, String password) {
        Result<String> result = new Result<>();
        User user = userService.queryUser(username, password);
        if (user == null) {
            result.setCode(Status.USER_NAME_PASSWD_ERROR.getCode());
            result.setMsg(Status.USER_NAME_PASSWD_ERROR.getMsg());
            return result;
        }

        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put(requiredNameClaim, user.getUserName());
        claims.put(requiredEmailClaim, user.getEmail());
        String token = Jwts.builder()
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expireTimeOfMinutes, ChronoUnit.MINUTES)))
                .addClaims(claims)
                .setId(UUID.randomUUID().toString())
                .signWith(privateKey)
                .compact();
        if (logger.isDebugEnabled()) {
            logger.debug("token = {}", token);
        }
        result.setData(token);
        result.setCode(Status.SUCCESS.getCode());
        return result;
    }

    /**
     * get token from remote server endpoint
     * @param username user name
     * @param password pwd
     * @return result
     */
    private Result<String> getTokenFromRemote(String username, String password) throws IOException {
        Result<String> result = new Result<>();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(remoteTokenUrl);
        String params = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        StringEntity entity = new StringEntity(params);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = client.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            result.setCode(Status.USER_NAME_PASSWD_ERROR.getCode());
            result.setMsg(Status.USER_NAME_PASSWD_ERROR.getMsg());
            return result;
        }
        String jsonString = EntityUtils.toString(response.getEntity());
        if (logger.isDebugEnabled()) {
            logger.debug(jsonString);
        }

        Matcher matcher = Pattern.compile(remoteTokenRegex).matcher(jsonString);
        String token = null;
        if (matcher.find()) {
            token = matcher.group(1);
        }
        if (StringUtils.isBlank(token)) {
            result.setCode(Status.GET_USER_TOKEN_ERROR.getCode());
            result.setMsg(Status.GET_USER_TOKEN_ERROR.getMsg());
            return result;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("token = {}", token);
        }
        result.setData(token);
        result.setCode(Status.SUCCESS.getCode());
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
            return getUserByToken(token, false);
        } catch (Exception e) {
            logger.error("token verification failed, e={}", e.toString());
        }
        return null;
    }

    /**
     * get user by jwt token, create user if the user don't exist.
     * @param token token
     * @return user
     */
    private User getUserByToken(String token, boolean createIfNotExist) throws Exception {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
        if (logger.isDebugEnabled()) {
            logger.debug("claimsJws = {}", claimsJws);
        }
        Claims body = claimsJws.getBody();
        String name = body.get(requiredNameClaim, String.class);
        String email = body.get(requiredEmailClaim, String.class);
        User user = userService.queryUser(name);
        if (user == null && createIfNotExist) {
            user = userService.createUser(name, UUID.randomUUID().toString(), email, defaultRemoteTenantId, null, defaultRemoteQueue);
        }
        return user;
    }

    /**
     * Get public key object from string. only support rsa key
     * @param pemKeyContent key contents
     * @return public key
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws InvalidKeySpecException InvalidKeySpecException
     */
    PublicKey getPublicKey(String pemKeyContent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String stripped = pemKeyContent
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)----", "")
                .replaceAll("\r\n", "")
                .replaceAll("\n", "")
                .trim();
        byte[] encodedKey = Base64.getDecoder().decode(stripped);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    /**
     * Get private key object from string. only support rsa key
     * @param pemKeyContent
     * @return key contents
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws InvalidKeySpecException InvalidKeySpecException
     * @throws IOException IOException
     */
    PrivateKey getPrivateKey(String pemKeyContent) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        String stripped = pemKeyContent
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)----", "")
                .replaceAll("\r\n", "")
                .replaceAll("\n", "")
                .trim();
        byte[] encodedKey = Base64.getDecoder().decode(stripped);
        DerInputStream derInputStream = new DerInputStream(encodedKey);
        DerValue[] seq = derInputStream.getSequence(0);
        BigInteger modulus = seq[1].getBigInteger();
        BigInteger publicExp = seq[2].getBigInteger();
        BigInteger privateExp = seq[3].getBigInteger();
        BigInteger prime1 = seq[4].getBigInteger();
        BigInteger prime2 = seq[5].getBigInteger();
        BigInteger exp1 = seq[6].getBigInteger();
        BigInteger exp2 = seq[7].getBigInteger();
        BigInteger crtCoef = seq[8].getBigInteger();
        RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec(modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    /**
     * Read pem key contents
     * @param filename file name
     * @return key contents
     * @throws IOException io exception
     */
    private String readPemKeyContent(String filename) throws IOException {
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();
        return new String(keyBytes);
    }
}
