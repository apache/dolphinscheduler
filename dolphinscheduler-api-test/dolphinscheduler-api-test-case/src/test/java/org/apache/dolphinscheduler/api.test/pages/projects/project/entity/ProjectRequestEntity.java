package org.apache.dolphinscheduler.api.test.pages.projects.project.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;

public class ProjectRequestEntity extends AbstractBaseEntity {
    private String projectName;
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
