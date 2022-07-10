package org.apache.dolphinscheduler.api.test.pages.projects.project.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;

public class ProjectResponseEntity extends AbstractBaseEntity {
    private int id;
    private int userId;
    private String userName;
    private String code;
    private String name;
    private String description;
    private String createTime;
    private String updateTime;
    private int perm;
    private int defCount;
    private int instRunningCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPerm() {
        return perm;
    }

    public void setPerm(int perm) {
        this.perm = perm;
    }

    public int getDefCount() {
        return defCount;
    }

    public void setDefCount(int defCount) {
        this.defCount = defCount;
    }

    public int getInstRunningCount() {
        return instRunningCount;
    }

    public void setInstRunningCount(int instRunningCount) {
        this.instRunningCount = instRunningCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
