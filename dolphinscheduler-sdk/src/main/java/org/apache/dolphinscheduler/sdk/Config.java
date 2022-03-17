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

package org.apache.dolphinscheduler.sdk;

import org.apache.dolphinscheduler.sdk.tea.NameInMap;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;

import java.util.Map;

public class Config extends TeaModel {
    @NameInMap("token")
    public String token;
    @NameInMap("protocol")
    public String protocol;
    @NameInMap("readTimeout")
    public Integer readTimeout;
    @NameInMap("connectTimeout")
    public Integer connectTimeout;
    @NameInMap("httpProxy")
    public String httpProxy;
    @NameInMap("httpsProxy")
    public String httpsProxy;
    @NameInMap("endpoint")
    public String endpoint;
    @NameInMap("noProxy")
    public String noProxy;
    @NameInMap("maxIdleConns")
    public Integer maxIdleConns;
    @NameInMap("userAgent")
    public String userAgent;

    public Config() {
    }

    public static Config build(Map<String, ?> map) throws Exception {
        Config self = new Config();
        return (Config) TeaModel.build(map, self);
    }

    public Config setToken(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return this.token;
    }

    public Config setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public Config setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public Integer getReadTimeout() {
        return this.readTimeout;
    }

    public Config setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public Integer getConnectTimeout() {
        return this.connectTimeout;
    }

    public Config setHttpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
        return this;
    }

    public String getHttpProxy() {
        return this.httpProxy;
    }

    public Config setHttpsProxy(String httpsProxy) {
        this.httpsProxy = httpsProxy;
        return this;
    }

    public String getHttpsProxy() {
        return this.httpsProxy;
    }

    public Config setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public Config setNoProxy(String noProxy) {
        this.noProxy = noProxy;
        return this;
    }

    public String getNoProxy() {
        return this.noProxy;
    }

    public Config setMaxIdleConns(Integer maxIdleConns) {
        this.maxIdleConns = maxIdleConns;
        return this;
    }

    public Integer getMaxIdleConns() {
        return this.maxIdleConns;
    }

    public Config setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getUserAgent() {
        return this.userAgent;
    }
}
