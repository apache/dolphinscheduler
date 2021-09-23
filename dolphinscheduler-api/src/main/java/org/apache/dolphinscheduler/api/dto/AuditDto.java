package org.apache.dolphinscheduler.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class AuditDto {

    /**
     * operator
     */
    private String userName;

    /**
     * operation module
     */
    private String module;

    /**
     * operation
     */
    private String operation;

    /**
     * operation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date time;

    /**
     * project name
     */
    private String projectName;

    /**
     * process name
     */
    private String processName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
}
