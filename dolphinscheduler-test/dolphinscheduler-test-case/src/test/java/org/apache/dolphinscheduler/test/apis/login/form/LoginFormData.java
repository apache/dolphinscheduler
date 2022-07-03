package org.apache.dolphinscheduler.test.apis.login.form;

public enum LoginFormData {
    USR_NAME("userName", "admin"),
    USER_PASSWD("userPassword", "dolphinscheduler123"),
    END_POINT("login", "");

    LoginFormData(String param, String data) {
        this.data = data;
        this.param = param;

    }

    private final String param;
    private final String data;

    public String getParam() {
        return param;
    }

    public String getData() {
        return data;
    }
}
