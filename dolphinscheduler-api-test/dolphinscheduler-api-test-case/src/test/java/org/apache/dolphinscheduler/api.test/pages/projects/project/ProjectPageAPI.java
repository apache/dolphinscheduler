package org.apache.dolphinscheduler.api.test.pages.projects.project;

import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import io.restassured.specification.RequestSpecification;

public class ProjectPageAPI implements IProjectPageAPI {
    private final RequestSpecification reqSpec;
    private final String sessionId;

    public ProjectPageAPI(RequestSpecification reqSpec, String sessionId) {
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public RestResponse<Result> createProject(ProjectRequestEntity projectRequestEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            projectRequestEntity.toMap(), Route.projects(), RequestMethod.POST));
    }
}
