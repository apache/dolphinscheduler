package org.apache.dolphinscheduler.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum DependentResult {

        SUCCESS(1, "success"),
        FAILED(2, "failed");

        @EnumValue
        private final int code;
        private final String desc;

        DependentResult(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static DependentResult of(int code) {
            for (DependentResult result : DependentResult.values()) {
                if (result.getCode() == code) {
                    return result;
                }
            }
            return null;
        }
}
