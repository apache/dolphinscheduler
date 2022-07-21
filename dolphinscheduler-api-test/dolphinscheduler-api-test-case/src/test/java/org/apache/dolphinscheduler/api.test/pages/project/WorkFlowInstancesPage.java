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


import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.TaskDefinitionRequestData;
import org.apache.dolphinscheduler.api.test.entity.TaskParamsMap;
import org.apache.dolphinscheduler.api.test.entity.TaskRelationRequestData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowCreateRequestData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowInstancesResponseData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowInstancesResponseTotalListData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowResponseData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowResponseTotalList;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowRunRequestData;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class WorkFlowInstancesPage {
    private static String workFlowInstanceState = null;
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowInstancesPage.class);


    public String queryWorkflowInstanceState(String sessionId, String projectName, String workFlowName) {

        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("pageSize",10);
        params.put("pageNo",1);
        params.put("searchVal",workFlowName);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        HttpResponse res = requestClient.get("/projects/"+ projectCode + "/process-instances", headers, params);

        for (WorkFlowInstancesResponseTotalListData workFlowInstanceList : JSONUtils.convertValue(res.body().data(), WorkFlowInstancesResponseData.class).totalList()) {
            workFlowInstanceState =  workFlowInstanceList.state();
        }

        return workFlowInstanceState;
    }


}
