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


import org.apache.dolphinscheduler.api.test.cases.TenantAPITest;
import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.TaskDefinitionRequestData;
import org.apache.dolphinscheduler.api.test.entity.TaskParamsMap;
import org.apache.dolphinscheduler.api.test.entity.TaskRelationRequestData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowCreateRequestData;
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


public final class WorkFlowDefinitionPage {
    private static String genNumId = null;
    private static String workFlowCode = null;

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowDefinitionPage.class);

    public void getGenNumId(String sessionId, String projectName) {

        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("genNum", 1);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        RequestClient requestClient = new RequestClient();
        HttpResponse res = requestClient.get("/projects/" + projectCode + "/task-definition/gen-task-codes", headers, params);
        ArrayList list = (ArrayList) res.body().data();
        genNumId = list.get(0).toString();

    }


    public HttpResponse createWorkflow(String sessionId, String projectName, String workFlowName, ArrayList<Object> localParams, ArrayList<Object> resourceList,
                                       String rawScript, String delayTime, String description,
                                       String environmentCode, String failRetryInterval, String failRetryTimes, String flag, String taskDefinitionRequestDataName, String taskPriority,
                                       String taskType, Integer timeout, String timeoutFlag, String timeoutNotifyStrategy, String workerGroup, String taskRelationRequestDataName,
                                       Integer preTaskCode, Integer preTaskVersion, String conditionType, HashMap<String, Object> conditionParams,
                                       String executionType, String globalParams) {
        Map<String, String> headers = new HashMap<>();
        WorkFlowCreateRequestData workFlowCreateRequestData = new WorkFlowCreateRequestData();
        TaskDefinitionRequestData taskDefinitionRequestData = new TaskDefinitionRequestData();
        TaskRelationRequestData taskRelationRequestData = new TaskRelationRequestData();

        TaskParamsMap taskParams = new TaskParamsMap();
        taskParams.localParams(localParams);
        taskParams.resourceList(resourceList);
        taskParams.rawScript(rawScript);

        taskDefinitionRequestData.code(genNumId);
        taskDefinitionRequestData.delayTime(delayTime);
        taskDefinitionRequestData.description(description);
        taskDefinitionRequestData.environmentCode(environmentCode);
        taskDefinitionRequestData.failRetryInterval(failRetryInterval);
        taskDefinitionRequestData.failRetryTimes(failRetryTimes);
        taskDefinitionRequestData.flag(flag);
        taskDefinitionRequestData.name(taskDefinitionRequestDataName);
        taskDefinitionRequestData.taskParams(taskParams);
        taskDefinitionRequestData.taskPriority(taskPriority);
        taskDefinitionRequestData.taskType(taskType);
        taskDefinitionRequestData.timeout(timeout);
        taskDefinitionRequestData.timeoutFlag(timeoutFlag);
        taskDefinitionRequestData.timeoutNotifyStrategy(timeoutNotifyStrategy);
        taskDefinitionRequestData.workerGroup(workerGroup);

        ArrayList<Object> taskDefinitionRequestDataList = new ArrayList<>();
        taskDefinitionRequestDataList.add(taskDefinitionRequestData);

        taskRelationRequestData.name(taskRelationRequestDataName);
        taskRelationRequestData.preTaskCode(preTaskCode);
        taskRelationRequestData.preTaskVersion(preTaskVersion);
        taskRelationRequestData.preTaskCode(preTaskCode);
        taskRelationRequestData.postTaskCode(genNumId);
        taskRelationRequestData.conditionType(conditionType);
        taskRelationRequestData.conditionParams(conditionParams);
        ArrayList<Object> taskRelationRequestDataList = new ArrayList<>();
        taskRelationRequestDataList.add(taskRelationRequestData);

        HashMap<String, Object> locations = new HashMap();
        locations.put("taskCode", genNumId);
        locations.put("x", 33.5);
        locations.put("y", 38.5);
        ArrayList<Object> locationsList = new ArrayList<>();
        locationsList.add(locations);

        workFlowCreateRequestData.locations(JSONUtils.toJsonString(locationsList));
        workFlowCreateRequestData.taskDefinitionJson(JSONUtils.toJsonString(taskDefinitionRequestDataList));
        workFlowCreateRequestData.taskRelationJson(JSONUtils.toJsonString(taskRelationRequestDataList));
        workFlowCreateRequestData.name(workFlowName);
        workFlowCreateRequestData.tenantCode(TenantAPITest.tenantName);
        workFlowCreateRequestData.executionType(executionType);
        workFlowCreateRequestData.description("");
        workFlowCreateRequestData.globalParams(globalParams);
        workFlowCreateRequestData.timeout(0);

        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);

        HttpResponse res = requestClient.post("/projects/" + projectCode + "/process-definition", headers, JSONUtils.convertValue(workFlowCreateRequestData, Map.class));
        return res;
    }

    public HttpResponse queryWorkflow(String sessionId, String projectName, String workFlowName) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("pageSize", 10);
        params.put("pageNo", 1);
        params.put("searchVal", workFlowName);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        HttpResponse res = requestClient.get("/projects/" + projectCode + "/process-definition", headers, params);

        for (WorkFlowResponseTotalList workFlowList : JSONUtils.convertValue(res.body().data(), WorkFlowResponseData.class).totalList()) {
            workFlowCode = workFlowList.code();
        }

        return res;
    }

    public HttpResponse onLineWorkflow(String sessionId, String projectName, String workFlowName) {
        WorkFlowDefinitionPage workflow = new WorkFlowDefinitionPage();
        workflow.queryWorkflow(sessionId, projectName, workFlowName);
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("name", workFlowName);
        params.put("releaseState", "ONLINE");
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        HttpResponse res = requestClient.post("/projects/" + projectCode + "/process-definition/" + workFlowCode + "/release", headers, params);

        return res;

    }

    public HttpResponse runWorkflow(String sessionId, String projectName, String workFlowName, String startEndTime, String scheduleTime, String failureStrategy, String warningType,
                                    String warningGroupId, String execType, String startNodeList, String taskDependType, String dependentMode, String runMode, String processInstancePriority,
                                    String workerGroup, String environmentCode, String startParams, String expectedParallelismNumber, Integer dryRun) {
        WorkFlowDefinitionPage workflow = new WorkFlowDefinitionPage();
        workflow.queryWorkflow(sessionId, projectName, workFlowName);
        workflow.onLineWorkflow(sessionId, projectName, workFlowName);
        Map<String, String> headers = new HashMap<>();

        WorkFlowRunRequestData workFlowRunRequestData = new WorkFlowRunRequestData();

        workFlowRunRequestData.processDefinitionCode(workFlowCode);
        workFlowRunRequestData.startEndTime(startEndTime);
        workFlowRunRequestData.scheduleTime(scheduleTime);
        workFlowRunRequestData.failureStrategy(failureStrategy);
        workFlowRunRequestData.warningType(warningType);
        workFlowRunRequestData.warningGroupId(warningGroupId);
        workFlowRunRequestData.execType(execType);
        workFlowRunRequestData.startNodeList(startNodeList);
        workFlowRunRequestData.taskDependType(taskDependType);
        workFlowRunRequestData.dependentMode(dependentMode);
        workFlowRunRequestData.runMode(runMode);
        workFlowRunRequestData.processInstancePriority(processInstancePriority);
        workFlowRunRequestData.workerGroup(workerGroup);
        workFlowRunRequestData.environmentCode(environmentCode);
        workFlowRunRequestData.startParams(startParams);
        workFlowRunRequestData.expectedParallelismNumber(expectedParallelismNumber);
        workFlowRunRequestData.dryRun(dryRun);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        HttpResponse res = requestClient.post("/projects/" + projectCode + "/executors/start-process-instance", headers, JSONUtils.convertValue(workFlowRunRequestData, Map.class));

        return res;

    }

}
