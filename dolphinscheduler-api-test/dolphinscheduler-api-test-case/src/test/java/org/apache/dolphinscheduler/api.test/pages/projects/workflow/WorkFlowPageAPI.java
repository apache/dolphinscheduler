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

package org.apache.dolphinscheduler.api.test.pages.projects.workflow;

import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowReleaseRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowRunRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import io.restassured.specification.RequestSpecification;

public class WorkFlowPageAPI implements IWorkFlowPageAPI {
    private final RequestSpecification reqSpec;
    private final String sessionId;

    public WorkFlowPageAPI(RequestSpecification reqSpec, String sessionId) {
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public RestResponse<Result> createWorkFlowDefinition(WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntity, String projectCode) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowDefinitionRequestEntity.toMap(), Route.workFlowDefinition(projectCode), RequestMethod.POST));
    }

    @Override
    public RestResponse<Result> releaseWorkFlowDefinition(WorkFlowReleaseRequestEntity workFlowReleaseRequestEntity, String projectCode, String workFlowDefinitionCode) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowReleaseRequestEntity.toMap(), Route.workFlowDefinition(projectCode, workFlowDefinitionCode), RequestMethod.POST));
    }

    @Override
    public RestResponse<Result> runWorkFlowDefinition(WorkFlowRunRequestEntity workFlowRunRequestEntity, String projectCode) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowRunRequestEntity.toMap(), Route.workFlowRun(projectCode), RequestMethod.POST));
    }
}
