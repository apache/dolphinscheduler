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

package org.apache.dolphinscheduler.api.test.pages.workflow;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.methods.CloseableHttpResponse;


@Slf4j
@AllArgsConstructor
public class ProcessDefinitionPage {

    private String sessionId;

    public CloseableHttpResponse importProcessDefinition(User loginUser, long projectCode, File file) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/process-definition/import", projectCode);
        return requestClient.postWithFile(url, headers, params, file);
    }

    public HttpResponse queryAllProcessDefinitionByProjectCode(User loginUser, long projectCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/process-definition/all", projectCode);
        return requestClient.get(url, headers, params);
    }

    public HttpResponse queryProcessDefinitionByCode(User loginUser, long projectCode, long processDefinitionCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/process-definition/%s", projectCode, processDefinitionCode);
        return requestClient.get(url, headers, params);
    }

    public HttpResponse getProcessListByProjectCode(User loginUser, long projectCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/process-definition/query-process-definition-list", projectCode);
        return requestClient.get(url, headers, params);
    }

    public HttpResponse queryProcessDefinitionByName(User loginUser, long projectCode, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("name", name);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/process-definition/query-by-name", projectCode);
        return requestClient.get(url, headers, params);
    }

    public HttpResponse queryProcessDefinitionList(User loginUser, long projectCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/process-definition/list", projectCode);
        return requestClient.get(url, headers, params);
    }

    public HttpResponse releaseProcessDefinition(User loginUser, long projectCode, long code, ReleaseState releaseState) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("code", code);
        params.put("releaseState", releaseState);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/process-definition/%s/release", projectCode, code);
        return requestClient.post(url, headers, params);
    }

    public HttpResponse deleteProcessDefinitionByCode(User loginUser, long projectCode, long code) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("code", code);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/process-definition/%s", projectCode, code);
        return requestClient.delete(url, headers, params);
    }
}
