package org.apache.dolphinscheduler.test.apis.common;

public enum RequestMethod {
    POST("Post"),
    PUT("put");

    RequestMethod(String method) {
        this.method = method;
    }

    private final String method;

    public String getMethod() {
        return method;
    }
}
