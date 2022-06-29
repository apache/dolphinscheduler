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
import org.apache.dolphinscheduler.api.test.entity.WorkFlowResponseData;
import org.apache.dolphinscheduler.api.test.entity.WorkFlowResponseTotalList;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class WorkFlowDefinitionPage {
    private static String genNumId = null;
    private static String workFlowCode = null;

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
        String taskDefinitionJson = "[{\"code\":" + genNumId + ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"echo_123\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo 123\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]";
        String taskRelationJson = "[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":"+ genNumId + ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]";
        String locations = "[{\"taskCode\":" + genNumId +",\"x\":33.5,\"y\":38.5}]";
        params.put("taskDefinitionJson", taskDefinitionJson);
        params.put("taskRelationJson", taskRelationJson);
        params.put("locations", locations);
        params.put("name", workFlowName);
        params.put("tenantCode", "admin");
        params.put("executionType", "PARALLEL");
        params.put("description", "");
        params.put("globalParams", "[]");
        params.put("timeout", 0);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        System.out.printf("创建工作流 param是：%s",params);
        System.out.printf("创建工作流header头是：%s", "/projects/"+projectCode+"/process-definition");
        HttpResponse res = requestClient.post("/projects/"+projectCode+"/process-definition", headers, params);

        System.out.printf("创建工作流结果：%s", res);
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
        System.out.printf("查询工作流结果：%s\n", res);
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

        System.out.printf("上线工作流：%s", res);
        return res;

    }

    public HttpResponse runWorkflow(String sessionId, String projectName, String workFlowName){
        WorkFlowDefinitionPage workflow = new WorkFlowDefinitionPage();
        workflow.queryWorkflow(sessionId, projectName, workFlowName);
        workflow.onLineWorkflow(sessionId, projectName, workFlowName);
        Map<String, Object> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("processDefinitionCode", workFlowCode);
        params.put("startEndTime", "2022-06-25T16:00:00.000Z");
        params.put("startEndTime", "2022-06-25T16:00:00.000Z");
        params.put("scheduleTime", "2022-06-26 00:00:00,2022-06-26 00:00:00");
        params.put("failureStrategy", "CONTINUE");
        params.put("warningType", "NONE");
        params.put("warningGroupId", "");
        params.put("execType", "START_PROCESS");
        params.put("startNodeList", "");
        params.put("taskDependType", "TASK_POST");
        params.put("dependentMode", "OFF_MODE");
        params.put("runMode", "RUN_MODE_SERIAL");
        params.put("processInstancePriority", "MEDIUM");
        params.put("workerGroup", "default");
        params.put("environmentCode", "");
        params.put("startParams", "");
        params.put("expectedParallelismNumber", "");
        params.put("dryRun", 0);
        headers.put(Constants.SESSION_ID_KEY, sessionId);
        RequestClient requestClient = new RequestClient();
        ProjectPage project = new ProjectPage();
        String projectCode = project.getProjectCode(sessionId, projectName);
        HttpResponse res = requestClient.post("/projects/"+projectCode+"/executors/start-process-instance", headers, params);

        System.out.printf("运行工作流：%s", res);
        return res;

    }



}
