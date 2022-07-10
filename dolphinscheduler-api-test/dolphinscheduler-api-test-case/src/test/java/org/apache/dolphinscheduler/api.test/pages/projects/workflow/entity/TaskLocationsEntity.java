package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;

public class TaskLocationsEntity extends AbstractBaseEntity {
    private String taskCode;

    private String x;

    private String y;

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
