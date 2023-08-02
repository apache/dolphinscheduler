package org.apache.dolphinscheduler.listener.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author wxn
 * @date 2023/7/29
 */
public enum ListenerEventPostServiceStatus {

    RUN(0, "run"),
    PAUSE(1, "pause"),
    STOP(1, "stop"),
    ;

    ListenerEventPostServiceStatus(int code, String descp) {
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
