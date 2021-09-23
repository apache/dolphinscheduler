package org.apache.dolphinscheduler.api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfiguration {
    @Value("${audit.control.global.switch:true}")
    private boolean auditGlobalControlSwitch;

    public boolean isAuditGlobalControlSwitch() {
        return auditGlobalControlSwitch;
    }

    public void setAuditGlobalControlSwitch(boolean auditGlobalControlSwitch) {
        this.auditGlobalControlSwitch = auditGlobalControlSwitch;
    }
}
