package org.apache.dolphinscheduler.api.test.pages.projects.workflow;

import org.apache.dolphinscheduler.api.test.base.IPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowReleaseRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowRunRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

public interface IWorkFlowPageAPI extends IPageAPI {
    RestResponse<Result> createWorkFlowDefinition(WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntity, String projectCode);

    RestResponse<Result> releaseWorkFlowDefinition(WorkFlowReleaseRequestEntity workFlowReleaseRequestEntity, String projectCode, String workFlowDefinitionCode);

    RestResponse<Result> runWorkFlowDefinition(WorkFlowRunRequestEntity workFlowRunRequestEntity, String projectCode);
}
