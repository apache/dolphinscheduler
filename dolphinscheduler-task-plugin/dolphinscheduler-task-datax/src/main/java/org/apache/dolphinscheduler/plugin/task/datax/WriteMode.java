package org.apache.dolphinscheduler.plugin.task.datax;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.sql.JDBCType;

public enum WriteMode {
    INSERT(0,"insert"),
    REPLACE(1, "replace"),
    UPDATE(2, "update");

    WriteMode(int code, String descp) {
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

    public static WriteMode valueOf(int code) {
        for (WriteMode writeMode : WriteMode.class.getEnumConstants()) {
            if (code == writeMode.code) {
                return writeMode;
            }
        }
        throw new IllegalArgumentException("Code:" + code + " is not a valid "
                + "DataX writeMode value.");
    }
}
