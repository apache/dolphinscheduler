/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.test.pages.security;

import lombok.AllArgsConstructor;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;


@AllArgsConstructor
public class WorkerGroupPage {

    private String sessionId;

    public HttpResponse saveWorkerGroup(User loginUser, int id, String name, String addrList, String description, String otherParamsJson) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("id", id);
        params.put("name", name);
        params.put("addrList", addrList);
        params.put("description", description);
        params.put("otherParamsJson", otherParamsJson);

        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();

        return requestClient.post("/worker-groups", headers, params);
    }

    public HttpResponse queryAllWorkerGroups(User loginUser) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/worker-groups/all", headers, params);
    }

    public HttpResponse queryAllWorkerGroupsPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("pageNo", pageNo);
        params.put("pageSize", pageSize);
        params.put("searchVal", searchVal);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/worker-groups", headers, params);
    }

    public HttpResponse deleteWorkerGroupById(User loginUser, Integer id) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("id", id);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        final String url = String.format("/worker-groups/%s", id);
        return requestClient.delete(url, headers, params);
    }

    public HttpResponse queryWorkerAddressList(User loginUser) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/worker-groups/worker-address-list", headers, params);
    }


}
