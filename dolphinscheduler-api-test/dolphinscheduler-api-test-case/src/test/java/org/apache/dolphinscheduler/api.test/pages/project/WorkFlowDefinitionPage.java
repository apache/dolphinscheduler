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
import org.apache.dolphinscheduler.api.test.entity.TaskParamsMap;
import org.apache.dolphinscheduler.api.test.entity.TaskDefinitionRequestData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowRunRequestData;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowCreateRequestData;
import org.apache.dolphinscheduler.api.test.entity.TaskRelationRequestData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowResponseData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowResponseTotalList;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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
        HttpResponse res = requestClient.get("/projects/"+projectCode+"/task-definition/gen-task-codes", headers, params);
        ArrayList list = (ArrayList) res.body().data();
        genNumId = list.get(0).toString();

    }


    public HttpResponse createWorkflow(String sessionId, String projectName, String workFlowName) {
        Map<String, String> headers = new HashMap<>();
        WorkFlowCreateRequestData workFlowCreateRequestData = new WorkFlowCreateRequestData();
        TaskDefinitionRequestData taskDefinitionRequestData = new TaskDefinitionRequestData();
        TaskRelationRequestData taskRelationRequestData = new TaskRelationRequestData();

        TaskParamsMap taskParams = new TaskParamsMap();
        ArrayList<Object> localParams = new ArrayList<>();
        ArrayList<Object> resourceList = new ArrayList<>();
        taskParams.localParams(localParams);
        taskParams.resourceList(resourceList);
        taskParams.rawScript("echo 123");

        taskDefinitionRequestData.code(genNumId);
        taskDefinitionRequestData.delayTime("0");
        taskDefinitionRequestData.description("");
        taskDefinitionRequestData.environmentCode("-1");
        taskDefinitionRequestData.failRetryInterval("1");
        taskDefinitionRequestData.failRetryTimes("0");
        taskDefinitionRequestData.flag("YES");
        taskDefinitionRequestData.name("echo_123");
        taskDefinitionRequestData.taskParams(taskParams);
        taskDefinitionRequestData.taskPriority("MEDIUM");
        taskDefinitionRequestData.taskType("SHELL");
        taskDefinitionRequestData.timeout(0);
        taskDefinitionRequestData.timeoutFlag("CLOSE");
        taskDefinitionRequestData.timeoutNotifyStrategy("");
        taskDefinitionRequestData.workerGroup("default");

        ArrayList<Object> taskDefinitionRequestDataList = new ArrayList<>();
        taskDefinitionRequestDataList.add(taskDefinitionRequestData);

        HashMap<String, Object> conditionParams = new HashMap();

        taskRelationRequestData.name("");
        taskRelationRequestData.preTaskCode(0);
        taskRelationRequestData.preTaskVersion(0);
        taskRelationRequestData.preTaskCode(0);
        taskRelationRequestData.postTaskCode(genNumId);
        taskRelationRequestData.conditionType("NONE");
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
        workFlowCreateRequestData.executionType("PARALLEL");
        workFlowCreateRequestData.description("");
        workFlowCreateRequestData.globalParams("[]");
        workFlowCreateRequestData.timeout(0);

        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);

        HttpResponse res = requestClient.post("/projects/"+projectCode+"/process-definition", headers, JSONUtils.convertValue(workFlowCreateRequestData, Map.class));
        return res;
    }

    public HttpResponse queryWorkflow(String sessionId, String projectName, String workFlowName) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("pageSize",10);
        params.put("pageNo",1);
        params.put("searchVal",workFlowName);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        HttpResponse res = requestClient.get("/projects/"+ projectCode + "/process-definition", headers, params);

        for (WorkFlowResponseTotalList workFlowList : JSONUtils.convertValue(res.body().data(), WorkFlowResponseData.class).totalList()) {
            workFlowCode =  workFlowList.code();
        }

        return res;
    }

    public HttpResponse onLineWorkflow(String sessionId, String projectName, String workFlowName){
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
        HttpResponse res = requestClient.post("/projects/"+projectCode+"/process-definition/"+workFlowCode+"/release", headers, params);

        return res;

    }

    public HttpResponse runWorkflow(String sessionId, String projectName, String workFlowName){
        WorkFlowDefinitionPage workflow = new WorkFlowDefinitionPage();
        workflow.queryWorkflow(sessionId, projectName, workFlowName);
        workflow.onLineWorkflow(sessionId, projectName, workFlowName);
        Map<String, String> headers = new HashMap<>();

        WorkFlowRunRequestData workFlowRunRequestData = new WorkFlowRunRequestData();

        workFlowRunRequestData.processDefinitionCode(workFlowCode);
        workFlowRunRequestData.startEndTime("2022-06-25T16:00:00.000Z");
        workFlowRunRequestData.scheduleTime("2022-06-26 00:00:00,2022-06-26 00:00:00");
        workFlowRunRequestData.failureStrategy("CONTINUE");
        workFlowRunRequestData.warningType("NONE");
        workFlowRunRequestData.warningGroupId("");
        workFlowRunRequestData.execType("START_PROCESS");
        workFlowRunRequestData.startNodeList("");
        workFlowRunRequestData.taskDependType("TASK_POST");
        workFlowRunRequestData.dependentMode("OFF_MODE");
        workFlowRunRequestData.runMode("RUN_MODE_SERIAL");
        workFlowRunRequestData.processInstancePriority("MEDIUM");
        workFlowRunRequestData.workerGroup("default");
        workFlowRunRequestData.environmentCode("");
        workFlowRunRequestData.startParams("");
        workFlowRunRequestData.expectedParallelismNumber("");
        workFlowRunRequestData.dryRun(0);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        HttpResponse res = requestClient.post("/projects/"+projectCode+"/executors/start-process-instance", headers, JSONUtils.convertValue(workFlowRunRequestData, Map.class));

        return res;

    }

}
