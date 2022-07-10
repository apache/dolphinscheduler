package org.apache.dolphinscheduler.api.test.utils.enums;

/**
 * failure policy when some task node failed.
 */
public enum FailureStrategy {

    /**
     * 0 ending process when some tasks failed.
     * 1 continue running when some tasks failed.
     **/
    END(0, "end"),
    CONTINUE(1, "continue");

    FailureStrategy(int code, String desc) {
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
