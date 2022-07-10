package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;
import org.apache.dolphinscheduler.api.test.utils.enums.ReleaseState;

public class WorkFlowReleaseRequestEntity extends AbstractBaseEntity {
    private String name;
    private ReleaseState releaseState;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReleaseState getReleaseState() {
        return releaseState;
    }

    public void setReleaseState(ReleaseState releaseState) {
        this.releaseState = releaseState;
    }
}
