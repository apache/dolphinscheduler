package org.apache.dolphinscheduler.test.apis.common;

public enum FormParam {
    SESSION_ID("sessionId"),
    CODE("code"),
    MSG("msg"),
    DATA("data"),
    SUCCESS("success"),
    FAILED("failed");


    FormParam(String param) {
        this.param = param;
    }

    private final String param;

    public String getParam() {
        return param;
    }
}