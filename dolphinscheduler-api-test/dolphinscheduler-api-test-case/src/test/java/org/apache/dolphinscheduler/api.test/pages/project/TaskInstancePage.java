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
import org.apache.dolphinscheduler.api.test.entity.TaskInstanceResponseData;
import org.apache.dolphinscheduler.api.test.entity.TaskInstanceResponseTotalList;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskInstancePage {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstancePage.class);

    private static String taskState = null;

    private static Integer taskInstanceId = null;

    private static String startEndTime = "2022-06-25T16:00:00.000Z";

    private static String scheduleTime = "2022-06-26 00:00:00,2022-06-26 00:00:00";

    private static String failureStrategy = "CONTINUE";

    private static String warningType = "NONE";

    private static String warningGroupId = "";

    private static String execType = "START_PROCESS";

    private static String startNodeList = "";

    private static String taskDependType = "TASK_POST";

    private static String dependentMode = "OFF_MODE";

    private static String runMode = "RUN_MODE_SERIAL";

    private static String processInstancePriority = "MEDIUM";

    private static String startParams = "";

    private static String expectedParallelismNumber = "";

    private static int dryRun = 0;

    private static String workerGroup = "default";

    private static String environmentCode = "-1";

    public String queryTaskInstance(String sessionId, String projectName, String workFlowName) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("pageSize", 10);
        params.put("pageNo", 1);
        params.put("searchVal", "");
        params.put("processInstanceId", "");
        params.put("host", "");
        params.put("stateType", "");
        params.put("startDate", "");
        params.put("endDate", "");
        params.put("executorName", "");
        params.put("processInstanceName", workFlowName);

        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);

        WorkFlowDefinitionPage workflow = new WorkFlowDefinitionPage();
        workflow.runWorkflow(sessionId, projectName, workFlowName, startEndTime, scheduleTime, failureStrategy, warningType,
            warningGroupId, execType, startNodeList, taskDependType, dependentMode, runMode, processInstancePriority,
            workerGroup, environmentCode, startParams, expectedParallelismNumber, dryRun);

        HttpResponse res = requestClient.get("/projects/" + projectCode + "/task-instances", headers, params);
        for (TaskInstanceResponseTotalList taskInstanceRes : JSONUtils.convertValue(res.body().data(), TaskInstanceResponseData.class).totalList()) {

            taskState = taskInstanceRes.state();
            taskInstanceId = taskInstanceRes.id();
        }
        return taskState;
    }

    public HttpResponse queryTaskInstanceLog(String sessionId, String projectName, String workFlowName) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        TaskInstancePage taskInstance = new TaskInstancePage();
        taskInstance.queryTaskInstance(sessionId, projectName, workFlowName);

        params.put("taskInstanceId", taskInstanceId);
        params.put("limit", 1000);
        params.put("skipLineNum", 0);

        HttpResponse res = requestClient.get("/log/detail", headers, params);
        return res;
    }

}
