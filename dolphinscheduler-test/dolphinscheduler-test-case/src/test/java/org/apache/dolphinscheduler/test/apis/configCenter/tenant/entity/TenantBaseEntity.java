package org.apache.dolphinscheduler.test.apis.configCenter.tenant.entity;
import org.apache.dolphinscheduler.test.base.AbstractBaseEntity;

public class TenantBaseEntity extends AbstractBaseEntity {

    private String tenantCode;

    private String description;

    private int queueId;

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }
}
