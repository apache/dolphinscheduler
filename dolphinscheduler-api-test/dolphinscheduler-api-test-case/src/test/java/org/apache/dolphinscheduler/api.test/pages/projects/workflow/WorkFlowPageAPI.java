package org.apache.dolphinscheduler.api.test.pages.projects.workflow;

import io.restassured.specification.RequestSpecification;

import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowReleaseRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowRunRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

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
