package org.apache.dolphinscheduler.test.apis.login.entity;


import org.apache.dolphinscheduler.test.base.AbstractBaseEntity;

public class LoginRequestEntity extends AbstractBaseEntity {
    String userName;
    String userPassword;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
