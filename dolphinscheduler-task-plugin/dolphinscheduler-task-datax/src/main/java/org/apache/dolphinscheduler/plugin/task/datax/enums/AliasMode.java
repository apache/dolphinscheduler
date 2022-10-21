package org.apache.dolphinscheduler.plugin.task.datax.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum AliasMode {
    APPEND(0,"append"),
    EXCLUSIVE(1, "exclusive")
    ;

    AliasMode(int code, String descp) {
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

    public static AliasMode valueOf(int code) {
        for (AliasMode aliasMode : AliasMode.class.getEnumConstants()) {
            if (code == aliasMode.code) {
                return aliasMode;
            }
        }
        throw new IllegalArgumentException("Code:" + code + " is not a valid "
                + "DataX elasticsearchwriter aliasMode value.");
    }
}
