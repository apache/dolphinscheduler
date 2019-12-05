package org.apache.dolphinscheduler.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @Author chongzi
 * @Date 2019/11/27 20:06
 **/
@Getter
public enum MeasureFailState {


    FALSE(0, "false"),

    TRUE(1, "true");

    MeasureFailState(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;
}
