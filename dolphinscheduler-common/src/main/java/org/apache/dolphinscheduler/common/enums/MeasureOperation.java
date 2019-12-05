package org.apache.dolphinscheduler.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @Author chongzi
 * @Date 2019/11/27 20:06
 **/
@Getter
public enum MeasureOperation {

    EQUAL(0, "="),

    NOTEQUAL(1, "!="),
    MORE(2, ">"),
    MOREEQUAL(3, ">="),
    LESS(4, "<"),
    LESSEQUAL(5, "<="),
    CONTAIN(6, "contain"),
    NOTCONTAIN(7, "notContain");

    MeasureOperation(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;
}
