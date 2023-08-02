package org.apache.dolphinscheduler.listener.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author wxn
 * @date 2023/7/10
 */
public enum ListenerEventPostStatus {

    /**
     * 0 waiting executed; 1 execute failed
     * when EXECUTION_SUCCESS, the data will be deleted, no need for EXECUTION_SUCCESS
     */
    WAIT_EXECUTION(0, "waiting"),
    EXECUTION_FAILURE(1, "failure");

    ListenerEventPostStatus(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }
}
