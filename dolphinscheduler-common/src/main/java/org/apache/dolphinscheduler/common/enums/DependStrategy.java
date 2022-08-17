package org.apache.dolphinscheduler.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * depend strategy
 */
public enum DependStrategy {
    /**
     * 0 close
     * 1 open
     */
    failureContinue(0, "failure-continue"),
    failureWaiting(1, "failure-waiting");

    DependStrategy(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @EnumValue
    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
