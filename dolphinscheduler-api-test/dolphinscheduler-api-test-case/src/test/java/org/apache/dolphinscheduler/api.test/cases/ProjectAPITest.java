package org.apache.dolphinscheduler.api.test.cases;

import org.apache.dolphinscheduler.api.test.base.AbstractAPITest;
import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.extensions.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.pages.projects.project.ProjectPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectResponseEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.devskiller.jfairy.Fairy;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisplayName("Project Page API test")
public class ProjectAPITest extends AbstractAPITest {
    private final Fairy fairy = Fairy.create();
    private ProjectPageAPI projectPageAPI;
    ProjectRequestEntity projectRequestEntity = null;
    ProjectResponseEntity projectResponseEntity = null;

    @BeforeAll
    public void initProjectAPIFactory() {
        projectPageAPI = pageAPIFactory.createProjectPageAPI();
        projectRequestEntity = new ProjectRequestEntity();
        projectRequestEntity.setProjectName(fairy.person().getCompany().getName() + "1");
        projectRequestEntity.setDescription(fairy.person().getFullName());
    }

    @Test
    @Order(1)
    public void testCreateProject() {
        RestResponse<Result> response = projectPageAPI.createProject(projectRequestEntity);
        response.isResponseSuccessful();
        projectResponseEntity = response.getResponse().jsonPath().getObject(Constants.DATA_KEY, ProjectResponseEntity.class);
    }

}
