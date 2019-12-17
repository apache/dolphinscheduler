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
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.service.UsersService;
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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonWebTokenAuthenticator implements Authenticator, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(JsonWebTokenAuthenticator.class);

    @Value("${security.authentication.default.tenant.code}")
    private String defaultTenantCode;
    private int defaultTenantId;
    @Value("${security.authentication.default.queue}")
    private String defaultQueue;
    @Value("${security.authentication.jwt.pem-key-file}")
    private String pemKeyFile;
    @Value("${security.authentication.jwt.required-token-url}")
    private String requiredTokenUrl;
    @Value("${security.authentication.jwt.required-token-regex}")
    private String requiredTokenRegex;
    @Value("${security.authentication.jwt.required-name-claim}")
    private String requiredNameClaim;
    @Value("${security.authentication.jwt.required-email-claim}")
    private String requiredEmailClaim;

    @Autowired
    private UsersService userService;
    @Autowired
    private TenantService tenantService;

    private PublicKey publicKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(pemKeyFile)) {
            throw new FileNotFoundException("Pem file can't empty. path=" + pemKeyFile);
        }
        publicKey = getPublicKey(pemKeyFile);
        Map<String, Object> result = tenantService.queryTenantList(defaultTenantCode);
        Status status = (Status) result.get(Constants.STATUS);
        if (status == Status.SUCCESS) {
            List<Tenant> datalist = (List<Tenant>) result.get(Constants.DATA_LIST);
            defaultTenantId = datalist.get(0).getId();
        } else {
            throw new Exception(String.format("tenant %s don't exist. please check #security.authentication.default.tenant.code"
                            , defaultTenantCode));
        }
    }

    @Override
    public User authenticate(String username, String password) {
        User user = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(requiredTokenUrl);
            String params = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            StringEntity entity = new StringEntity(params);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                String jsonString = EntityUtils.toString(response.getEntity());
                logger.info(jsonString);
                Matcher matcher = Pattern.compile(requiredTokenRegex).matcher(jsonString);
                String token = null;
                if (matcher.find()) {
                    token = matcher.group(1);
                }
                if (StringUtils.isNotBlank(token)) {
                    if (logger.isDebugEnabled()) {
                        logger.info("token = {}", token);
                    }
                    Jws<Claims> claimsJws = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
                    if (logger.isDebugEnabled()) {
                        logger.info("claimsJws = {}", claimsJws);
                    }
                    Claims body = claimsJws.getBody();
                    String name = body.get(requiredNameClaim, String.class);
                    String email = body.get(requiredEmailClaim, String.class);
                    user = userService.queryUser(name);
                    if (user == null) {
                        user = userService.createUser(name, password, email, defaultTenantId, null, defaultQueue);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Get public key object from local rsa file. only support rsa key
     * @param filename key file name
     * @return public key
     * @throws IOException read file io exception
     * @throws NoSuchAlgorithmException no rsa algorithm exception
     * @throws InvalidKeySpecException invalid key spec exception
     */
    public PublicKey getPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pemKeyContent = readPemKeyContent(filename);
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
