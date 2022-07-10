package org.apache.dolphinscheduler.api.test.utils.enums;

/**
 * task node depend type
 */
public enum ComplementDependentMode {
    /**
     * 0 off mode
     * 1 run complement data with all dependent process
     */
    OFF_MODE(0, "off mode"),
    ALL_DEPENDENT(1, "all dependent");

    ComplementDependentMode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

