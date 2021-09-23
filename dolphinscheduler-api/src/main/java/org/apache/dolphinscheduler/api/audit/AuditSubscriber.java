package org.apache.dolphinscheduler.api.audit;

public interface AuditSubscriber {

    /**
     * process the audit message
     *
     * @param message
     */
    void execute(AuditMessage message);
}
