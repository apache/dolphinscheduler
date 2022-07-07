package org.apache.dolphinscheduler.api.dto.project;

import io.swagger.annotations.ApiModelProperty;

/**
 * project create request
 */
public class ProjectCreateRequest {

    @ApiModelProperty(example = "pro123", required = true)
    private String projectName;

    @ApiModelProperty(example = "this is a project")
    private String description;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
