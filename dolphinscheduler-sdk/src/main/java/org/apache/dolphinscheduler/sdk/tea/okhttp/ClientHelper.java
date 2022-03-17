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

package org.apache.dolphinscheduler.sdk.tea.okhttp;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;

public class ClientHelper {
    public static final ConcurrentHashMap<String, OkHttpClient> clients = new ConcurrentHashMap<String, OkHttpClient>();

    public static OkHttpClient getOkHttpClient(String host, int port, Map<String, Object> map) throws Exception {
        String key;
        if (null != map.get("httpProxy") || null != map.get("httpsProxy")) {
            Object urlString = null == map.get("httpProxy") ? map.get("httpsProxy") : map.get("httpProxy");
            URL url = new URL(String.valueOf(urlString));
            key = getClientKey(url.getHost(), url.getPort());
        } else {
            key = getClientKey(host, port);
        }
        OkHttpClient client = clients.get(key);
        if (null == client) {
            client = creatClient(map);
            clients.put(key, client);
        }
        return client;
    }

    public static OkHttpClient creatClient(Map<String, Object> map) {
        OkHttpClientBuilder builder = new OkHttpClientBuilder();
        builder = builder.connectTimeout(map).readTimeout(map).connectionPool(map).proxy(map).proxyAuthenticator(map);
        OkHttpClient client = builder.buildOkHttpClient();
        return client;
    }

    public static String getClientKey(String host, int port) {
        return String.format("%s:%d", host, port);
    }
}
