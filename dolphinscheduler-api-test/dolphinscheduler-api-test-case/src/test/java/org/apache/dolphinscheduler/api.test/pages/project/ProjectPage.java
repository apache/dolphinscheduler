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


import com.sun.xml.internal.ws.api.model.ExceptionType;
import org.apache.dolphinscheduler.api.test.cases.ProjectAPITest;
import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.ProjectListResponseData;
import org.apache.dolphinscheduler.api.test.entity.ProjectListResponseTotalList;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class ProjectPage {
    private static final Logger logger = LoggerFactory.getLogger(ProjectPage.class);

    public HttpResponse createProject(String sessionId, String projectName, String description, String userName) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("projectName", projectName);
        params.put("description", description);
        params.put("userName", userName);

        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();

        try {
            HttpResponse res = requestClient.post("/projects", headers, params);
            return res;
        } catch (Exception e) {
            logger.error("create project fail, message:{}", e);
        }
        return null;
    }

    public HttpResponse searchProject(String sessionId, String searchVal) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("pageSize", 10);
        params.put("pageNo", 1);
        params.put("searchVal", searchVal);

        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();

        return requestClient.get("/projects", headers, params);
    }

    public String getProjectCode(String sessionId, String searchVal) {
        String projectCode = null;
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("pageSize", 10);
        params.put("pageNo", 1);
        params.put("searchVal", searchVal);

        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();

        HttpResponse res = requestClient.get("/projects", headers, params);

        logger.info("project %s", res);

        for (ProjectListResponseTotalList ProjectListRes : JSONUtils.convertValue(res.body().data(), ProjectListResponseData.class).totalList()) {
            logger.info(ProjectListRes.code());
            projectCode =  ProjectListRes.code();
        }
        return projectCode;
    }

}
