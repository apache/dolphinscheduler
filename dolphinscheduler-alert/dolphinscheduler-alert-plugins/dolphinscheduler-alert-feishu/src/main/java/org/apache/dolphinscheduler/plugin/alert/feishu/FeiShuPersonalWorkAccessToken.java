package org.apache.dolphinscheduler.plugin.alert.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;

final class FeiShuPersonalWorkAccessToken {

    @JsonProperty("tenant_access_token")
    private String tenantAccessToken;
    private Long start;
    private Long expire;

    public String getTenantAccessToken() {
        return tenantAccessToken;
    }

    public void setTenantAccessToken(String tenantAccessToken) {
        this.tenantAccessToken = tenantAccessToken;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }
}
