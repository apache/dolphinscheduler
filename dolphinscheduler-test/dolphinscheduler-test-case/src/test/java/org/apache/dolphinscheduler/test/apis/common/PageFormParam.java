package org.apache.dolphinscheduler.test.apis.common;

public enum PageFormParam {
    PAGE_SIZE("pageSize"),
    PAGE_NO("pageNo"),
    SEARCH_VAL("searchVal");

    PageFormParam(String param) {
        this.param = param;

    }

    private final String param;

    public String getParam() {
        return param;
    }
}
