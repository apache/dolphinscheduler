package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;
import org.apache.dolphinscheduler.api.test.utils.enums.ProcessExecutionTypeEnum;

public class WorkFlowDefinitionRequestEntity extends AbstractBaseEntity {
    private String taskDefinitionJson;

    private String taskRelationJson;

    private String locations;

    private String name;

    private String tenantCode;

    private ProcessExecutionTypeEnum executionType;

    private String description;

    private String globalParams;

    private int timeout;

    public String getTaskDefinitionJson() {
        return taskDefinitionJson;
    }

    public void setTaskDefinitionJson(String taskDefinitionJson) {
        this.taskDefinitionJson = taskDefinitionJson;
    }

    public String getTaskRelationJson() {
        return taskRelationJson;
    }

    public void setTaskRelationJson(String taskRelationJson) {
        this.taskRelationJson = taskRelationJson;
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public ProcessExecutionTypeEnum getExecutionType() {
        return executionType;
    }

    public void setExecutionType(ProcessExecutionTypeEnum executionType) {
        this.executionType = executionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGlobalParams() {
        return globalParams;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParams = globalParams;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

