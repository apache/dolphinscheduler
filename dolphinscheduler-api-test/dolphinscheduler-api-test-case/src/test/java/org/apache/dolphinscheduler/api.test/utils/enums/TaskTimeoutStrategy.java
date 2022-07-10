package org.apache.dolphinscheduler.api.test.utils.enums;

/**
 * task timeout strategy
 */
public enum TaskTimeoutStrategy {
    /**
     * 0 warn
     * 1 failed
     * 2 warn+failed
     */
    WARN(0, "warn"),
    FAILED(1, "failed"),
    WARN_FAILED(2, "warnfailed");

    TaskTimeoutStrategy(int code, String desc) {
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

    public static TaskTimeoutStrategy of(int status) {
        for (TaskTimeoutStrategy es : values()) {
            if (es.getCode() == status) {
                return es;
            }
        }
        throw new IllegalArgumentException("invalid status : " + status);
    }

}

