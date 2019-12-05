package org.apache.dolphinscheduler.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @Author chongzi
 * @Date 2019/11/27 20:06
 **/
@Getter
public enum BasicType {

    INT(0,"INT"),
    VARCHAR(1,"VARCHAR"),
    INTEGER(2,"INTEGER"),
    BIGINT(3,"BIGINT"),
    LONG(4,"LONG"),
    DOUBLE(5,"DOUBLE"),
    FLOAT(6,"FLOAT"),
    CHAR(7,"CHAR"),
    DECIMAL(8,"DECIMAL"),
    TIMESTAMP(9,"TIMESTAMP"),
    DATETIME(10,"DATETIME");

    BasicType(int code, String descp){
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

}