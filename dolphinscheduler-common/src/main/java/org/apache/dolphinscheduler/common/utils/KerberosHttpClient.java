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
package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * kerberos http client
 */
public class KerberosHttpClient {

    public static final Logger logger = LoggerFactory.getLogger(KerberosHttpClient.class);

    private String principal;
    private String keyTabLocation;

    public KerberosHttpClient() {
    }

    public KerberosHttpClient(String principal, String keyTabLocation) {
        super();
        this.principal = principal;
        this.keyTabLocation = keyTabLocation;
    }

    public KerberosHttpClient(String principal, String keyTabLocation, String krb5Location) {
        this(principal, keyTabLocation);
        System.setProperty("java.security.krb5.conf", krb5Location);
    }

    public KerberosHttpClient(String principal, String keyTabLocation, boolean isDebug) {
        this(principal, keyTabLocation);
        if (isDebug) {
            System.setProperty("sun.security.spnego.debug", "true");
            System.setProperty("sun.security.krb5.debug", "true");
        }
    }

    public KerberosHttpClient(String principal, String keyTabLocation, String krb5Location, boolean isDebug) {
        this(principal, keyTabLocation, isDebug);
        System.setProperty("java.security.krb5.conf", krb5Location);
    }

    private static CloseableHttpClient buildSpengoHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().
                register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
        builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(null, -1, null), new Credentials() {
            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }
        });
        builder.setDefaultCredentialsProvider(credentialsProvider);
        CloseableHttpClient httpClient = builder.build();
        return httpClient;
    }

    public String get(final String url, final String userId) {
        logger.info(String.format("Calling KerberosHttpClient %s %s %s", this.principal, this.keyTabLocation, url));
        Configuration config = new Configuration() {
            @SuppressWarnings("serial")
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return new AppConfigurationEntry[]{new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>() {
                    {
                        put("useTicketCache", "false");
                        put("useKeyTab", "true");
                        put("keyTab", keyTabLocation);
                        put("refreshKrb5Config", "true");
                        put("principal", principal);
                        put("storeKey", "true");
                        put("doNotPrompt", "true");
                        put("isInitiator", "true");
                        put("debug", "true");
                    }
                })};
            }
        };
        Set<Principal> princ = new HashSet<Principal>(1);
        princ.add(new KerberosPrincipal(userId));
        Subject sub = new Subject(false, princ, new HashSet<Object>(), new HashSet<Object>());

        LoginContext lc = null;
        try {
            lc = new LoginContext("", sub, null, config);
            lc.login();
            Subject serviceSubject = lc.getSubject();
            return Subject.doAs(serviceSubject, (PrivilegedAction<String>) () -> {
                CloseableHttpResponse response = null;
                String responseContent = null;
                CloseableHttpClient httpClient = buildSpengoHttpClient();
                HttpGet httpget = null;
                try {
                    httpget = new HttpGet(url);
                    response = httpClient.execute(httpget);
                    //check response status is 200
                    if (response.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            responseContent = EntityUtils.toString(entity, Constants.UTF_8);
                        } else {
                            logger.warn("http entity is null");
                        }
                    } else {
                        logger.error("http get:{} response status code is not 200!", response.getStatusLine().getStatusCode());
                    }
                } catch (IOException ioe) {
                    logger.error(ioe.getMessage(), ioe);
                } finally {
                    try {
                        if (response != null) {
                            EntityUtils.consume(response.getEntity());
                            response.close();
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                    if (!httpget.isAborted()) {
                        httpget.releaseConnection();
                        httpget.abort();
                    }
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                return responseContent;
            });
        } catch (LoginException le) {
            logger.error("Kerberos authentication failed ", le);
        }
        return null;
    }


}
