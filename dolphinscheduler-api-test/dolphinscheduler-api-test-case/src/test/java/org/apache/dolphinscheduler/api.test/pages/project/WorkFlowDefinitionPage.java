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


import org.apache.dolphinscheduler.api.test.cases.ProjectAPITest;
import org.apache.dolphinscheduler.api.test.core.Constants;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowCreateRequestData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowResponseTotalList;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowResponseData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowRunRequestData;
import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
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
    private static final Logger logger = LoggerFactory.getLogger(ProjectAPITest.class);

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
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        WorkFlowCreateRequestData workFlowCreateRequestData = new WorkFlowCreateRequestData();

        String taskDefinitionJson = "[{\"code\":" + genNumId + ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"echo_123\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo 123\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]";
        String taskRelationJson = "[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":"+ genNumId + ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]";
        String locations = "[{\"taskCode\":" + genNumId +",\"x\":33.5,\"y\":38.5}]";
        workFlowCreateRequestData.setTaskDefinitionJson(taskDefinitionJson);
        workFlowCreateRequestData.setTaskRelationJson(taskRelationJson);
        workFlowCreateRequestData.setLocations(locations);
        workFlowCreateRequestData.setName(workFlowName);
        workFlowCreateRequestData.setTenantCode("admin");
        workFlowCreateRequestData.setExecutionType("PARALLEL");
        workFlowCreateRequestData.setDescription("");
        workFlowCreateRequestData.setGlobalParams("[]");
        workFlowCreateRequestData.setTimeout(0);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);

        HttpResponse res = requestClient.post("/projects/"+projectCode+"/process-definition", headers, JSONUtils.convertValue(workFlowCreateRequestData, Map.class));
        logger.info("创建工作流结果：%s", res);
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

        logger.info("查询工作流结果：%s\n", res);
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

        logger.info("上线工作流：%s", res);
        return res;

    }

    public HttpResponse runWorkflow(String sessionId, String projectName, String workFlowName){
        WorkFlowDefinitionPage workflow = new WorkFlowDefinitionPage();
        workflow.queryWorkflow(sessionId, projectName, workFlowName);
        workflow.onLineWorkflow(sessionId, projectName, workFlowName);
        Map<String, String> headers = new HashMap<>();

        WorkFlowRunRequestData workFlowRunRequestData = new WorkFlowRunRequestData();
        workFlowRunRequestData.setProcessDefinitionCode(workFlowCode);
        workFlowRunRequestData.setStartEndTime("2022-06-25T16:00:00.000Z");
        workFlowRunRequestData.setScheduleTime("2022-06-26 00:00:00,2022-06-26 00:00:00");
        workFlowRunRequestData.setFailureStrategy("CONTINUE");
        workFlowRunRequestData.setWarningType("NONE");
        workFlowRunRequestData.setWarningGroupId("");
        workFlowRunRequestData.setExecType("START_PROCESS");
        workFlowRunRequestData.setStartNodeList("");
        workFlowRunRequestData.setTaskDependType("TASK_POST");
        workFlowRunRequestData.setDependentMode("OFF_MODE");
        workFlowRunRequestData.setRunMode("RUN_MODE_SERIAL");
        workFlowRunRequestData.setProcessInstancePriority("MEDIUM");
        workFlowRunRequestData.setWorkerGroup("default");
        workFlowRunRequestData.setEnvironmentCode("");
        workFlowRunRequestData.setStartParams("");
        workFlowRunRequestData.setExpectedParallelismNumber("");
        workFlowRunRequestData.setDryRun(0);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        HttpResponse res = requestClient.post("/projects/"+projectCode+"/executors/start-process-instance", headers, JSONUtils.convertValue(workFlowRunRequestData, Map.class));

        logger.info("运行工作流：%s", res);
        return res;

    }



}
