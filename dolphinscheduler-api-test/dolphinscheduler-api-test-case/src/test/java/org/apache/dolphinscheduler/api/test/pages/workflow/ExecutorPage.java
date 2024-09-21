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

package org.apache.dolphinscheduler.api.test.pages.workflow;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ExecutorPage {

    private String sessionId;

    public HttpResponse startWorkflowInstance(User loginUser,
                                              long projectCode,
                                              long workflowDefinitionCode,
                                              String scheduleTime,
                                              FailureStrategy failureStrategy,
                                              WarningType warningType) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("workflowDefinitionCode", workflowDefinitionCode);
        params.put("scheduleTime", scheduleTime);
        params.put("failureStrategy", failureStrategy);
        params.put("warningType", warningType);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/executors/start-workflow-instance", projectCode);
        return requestClient.post(url, headers, params);
    }

    public HttpResponse queryExecutingWorkflow(User loginUser, long projectCode, long workflowInstanceCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("id", workflowInstanceCode);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/executors/query-executing-workflow", projectCode);
        return requestClient.get(url, headers, params);
    }

    public HttpResponse execute(User loginUser, long projectCode, int workflowInstanceId, ExecuteType executeType) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("projectCode", projectCode);
        params.put("workflowInstanceId", workflowInstanceId);
        params.put("executeType", executeType);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/executors/execute", projectCode);
        return requestClient.post(url, headers, params);
    }

    public HttpResponse executeTask(User loginUser, long projectCode, int workflowInstanceId, String startNodeList,
                                    TaskDependType taskDependType) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUser", loginUser);
        params.put("workflowInstanceId", workflowInstanceId);
        params.put("startNodeList", startNodeList);
        params.put("taskDependType", taskDependType);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.SESSION_ID_KEY, sessionId);

        RequestClient requestClient = new RequestClient();
        String url = String.format("/projects/%s/executors/execute-task", projectCode);
        return requestClient.post(url, headers, params);
    }

}
