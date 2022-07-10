package org.apache.dolphinscheduler.api.test.utils.enums;

/**
 * timeout flag
 */
public enum TimeoutFlag {
    /**
     * 0 close
     * 1 open
     */
    CLOSE(0, "close"),
    OPEN(1, "open");

    TimeoutFlag(int code, String desc) {
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