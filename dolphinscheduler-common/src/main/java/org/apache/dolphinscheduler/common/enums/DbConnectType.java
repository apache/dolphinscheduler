package org.apache.dolphinscheduler.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum DbConnectType {

    ORACLE_SERVICE_NAME(0, "Oracle Service Name"),
    ORACLE_SID(1, "Oracle SID");

    DbConnectType(int code, String descp) {
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
