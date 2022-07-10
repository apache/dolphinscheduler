package org.apache.dolphinscheduler.api.test.utils.enums;

/**
 * complement data run mode
 */
public enum RunMode {
    /**
     * 0 serial run
     * 1 parallel run
     */
    RUN_MODE_SERIAL(0, "serial run"),
    RUN_MODE_PARALLEL(1, "parallel run");

    RunMode(int code, String desc) {
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
