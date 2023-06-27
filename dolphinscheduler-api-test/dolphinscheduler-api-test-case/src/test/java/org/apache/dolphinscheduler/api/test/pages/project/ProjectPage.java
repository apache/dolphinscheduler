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

package org.apache.dolphinscheduler.api.test.pages.project;

import lombok.AllArgsConstructor;

import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public final class ProjectPage {
    private String sessionId;

    public HttpResponse createProject(User loginUser, String projectName) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("projectName", projectName);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.post("/projects", headers, params);
    }

    public HttpResponse queryAllProjectList(User loginUser) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects/list", headers, params);
    }

    public HttpResponse updateProject(User loginUser, Long code, String projectName, String userName) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("projectName", projectName);
        params.put("userName", userName);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%d", code);
        return requestClient.put(url, headers, params);
    }

    public HttpResponse queryProjectByCode(User loginUser, Long code) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%d", code);
        return requestClient.get(url, headers, params);
    }

    public HttpResponse queryProjectListPaging(User loginUser, Integer pageSize, Integer pageNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("pageSize", pageSize);
        params.put("pageNo", pageNo);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects", headers, params);
    }

    public HttpResponse queryProjectWithAuthorizedLevelListPaging(User loginUser, Integer userId, Integer pageSize, Integer pageNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("userId", userId);
        params.put("pageSize", pageSize);
        params.put("pageNo", pageNo);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects/project-with-authorized-level-list-paging", headers, params);
    }

    public HttpResponse queryUnauthorizedProject(User loginUser, Integer userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("userId", userId);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects/unauth-project", headers, params);
    }

    public HttpResponse queryAuthorizedProject(User loginUser, Integer userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("userId", userId);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects/authed-project", headers, params);
    }

    public HttpResponse queryProjectWithAuthorizedLevel(User loginUser, Integer userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("userId", userId);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects/project-with-authorized-level", headers, params);
    }

    public HttpResponse queryAuthorizedUser(User loginUser, Long projectCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("projectCode", projectCode);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects/authed-user", headers, params);
    }

    public HttpResponse queryProjectCreatedAndAuthorizedByUser(User loginUser) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects/created-and-authed", headers, params);
    }

    public HttpResponse queryAllProjectListForDependent(User loginUser) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        return requestClient.get("/projects/list-dependent", headers, params);
    }

    public HttpResponse deleteProject(User loginUser, Long code) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%d", code);
        return requestClient.delete(url, headers, params);
    }
}
