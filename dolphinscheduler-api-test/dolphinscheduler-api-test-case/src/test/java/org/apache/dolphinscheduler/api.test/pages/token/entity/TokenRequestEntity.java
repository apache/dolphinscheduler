package org.apache.dolphinscheduler.api.test.pages.token.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;

public class TokenRequestEntity extends AbstractBaseEntity {
    private String userId;
    private String token;
    private String expireTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }
}
