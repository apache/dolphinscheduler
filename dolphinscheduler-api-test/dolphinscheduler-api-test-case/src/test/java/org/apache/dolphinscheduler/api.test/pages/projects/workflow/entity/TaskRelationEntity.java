package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;

import java.util.Map;

public class TaskRelationEntity extends AbstractBaseEntity {
    public String name;

    public int preTaskCode;

    public int preTaskVersion;

    public String postTaskCode;

    public int postTaskVersion;

    public String conditionType;

    public Map<String, String> conditionParams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPreTaskCode() {
        return preTaskCode;
    }

    public void setPreTaskCode(int preTaskCode) {
        this.preTaskCode = preTaskCode;
    }

    public int getPreTaskVersion() {
        return preTaskVersion;
    }

    public void setPreTaskVersion(int preTaskVersion) {
        this.preTaskVersion = preTaskVersion;
    }

    public String getPostTaskCode() {
        return postTaskCode;
    }

    public void setPostTaskCode(String postTaskCode) {
        this.postTaskCode = postTaskCode;
    }

    public int getPostTaskVersion() {
        return postTaskVersion;
    }

    public void setPostTaskVersion(int postTaskVersion) {
        this.postTaskVersion = postTaskVersion;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public Map<String, String> getConditionParams() {
        return conditionParams;
    }

    public void setConditionParams(Map<String, String> conditionParams) {
        this.conditionParams = conditionParams;
    }
}
