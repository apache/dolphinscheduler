package org.apache.dolphinscheduler.api.audit;

import org.apache.dolphinscheduler.common.enums.AuditModuleType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;

public class AuditMessage {
    private User user;

    private Date auditDate;

    private AuditModuleType module;

    private AuditOperationType operation;

    private String projectName;

    private String processName;

    public AuditMessage(User user, Date auditDate, AuditModuleType module, AuditOperationType operation, String projectName, String processName) {
        this.user = user;
        this.auditDate = auditDate;
        this.module = module;
        this.operation = operation;
        this.processName = processName;
        this.projectName = projectName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    public AuditModuleType getModule() {
        return module;
    }

    public void setModule(AuditModuleType module) {
        this.module = module;
    }

    public AuditOperationType getOperation() {
        return operation;
    }

    public void setOperation(AuditOperationType operation) {
        this.operation = operation;
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

    @Override
    public String toString() {
        return "AuditMessage{" +
                "user=" + user +
                ", Date=" + auditDate +
                ", module=" + module +
                ", operation=" + operation +
                ", projectName='" + projectName + '\'' +
                ", processName='" + processName + '\'';
    }
}
