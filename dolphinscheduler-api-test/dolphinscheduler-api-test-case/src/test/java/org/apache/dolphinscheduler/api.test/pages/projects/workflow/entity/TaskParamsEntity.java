package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;

import java.util.List;

public class TaskParamsEntity extends AbstractBaseEntity {
    private List<String> localParams;

    private String rawScript;

    private List<String> resourceList;

    public List<String> getLocalParams() {
        return localParams;
    }

    public void setLocalParams(List<String> localParams) {
        this.localParams = localParams;
    }

    public String getRawScript() {
        return rawScript;
    }

    public void setRawScript(String rawScript) {
        this.rawScript = rawScript;
    }

    public List<String> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<String> resourceList) {
        this.resourceList = resourceList;
    }
}
