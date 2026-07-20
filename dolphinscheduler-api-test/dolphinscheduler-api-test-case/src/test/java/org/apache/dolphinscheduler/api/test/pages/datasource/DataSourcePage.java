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

package org.apache.dolphinscheduler.api.test.pages.datasource;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class DataSourcePage {

    private String sessionId;

    public HttpResponse createDataSource(String name, String type, String host, int port,
                                         String userName, String password, String database) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("type", type);
        params.put("host", host);
        params.put("port", port);
        params.put("userName", userName);
        params.put("password", password);
        params.put("database", database);
        params.put("other", new HashMap<>());

        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.postJson("/datasources", headers, JSONUtils.toJsonString(params));
    }

    public HttpResponse updateDataSource(int id, String name, String type, String host, int port,
                                         String userName, String password, String database) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", name);
        params.put("type", type);
        params.put("host", host);
        params.put("port", port);
        params.put("userName", userName);
        params.put("password", password);
        params.put("database", database);
        params.put("other", new HashMap<>());

        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/datasources/%d", id);
        return requestClient.putJson(url, headers, JSONUtils.toJsonString(params));
    }

    public HttpResponse queryDataSource(int id) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/datasources/%d", id);
        return requestClient.get(url, headers, params);
    }

    public HttpResponse deleteDataSource(int id) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/datasources/%d", id);
        return requestClient.delete(url, headers, params);
    }
}
