package org.apache.dolphinscheduler.api.audit;


import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.mapper.AuditLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditSubscriberImpl implements AuditSubscriber {

    @Autowired
    private AuditLogMapper logMapper;

    @Override
    public void execute(AuditMessage message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserName(message.getUser().getUserName());
        auditLog.setModule(message.getModule().getCode());
        auditLog.setOperation(message.getOperation().getCode());
        auditLog.setTime(message.getAuditDate());
        auditLog.setProcessName(message.getProcessName());
        auditLog.setProjectName(message.getProjectName());
        logMapper.insert(auditLog);
    }
}
