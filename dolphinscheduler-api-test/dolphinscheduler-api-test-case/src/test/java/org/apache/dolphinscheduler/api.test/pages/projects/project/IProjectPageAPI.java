package org.apache.dolphinscheduler.api.test.pages.projects.project;

import org.apache.dolphinscheduler.api.test.base.IPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

public interface IProjectPageAPI extends IPageAPI {
    RestResponse<Result> createProject(ProjectRequestEntity projectRequestEntity);
}
