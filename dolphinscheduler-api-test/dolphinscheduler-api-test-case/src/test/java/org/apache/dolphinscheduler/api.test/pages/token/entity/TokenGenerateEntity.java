package org.apache.dolphinscheduler.api.test.pages.token.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;

public class TokenGenerateEntity extends AbstractBaseEntity {
    private int userId;
    private String expireTime;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }
}
