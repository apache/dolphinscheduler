package org.apache.dolphinscheduler.api.test.utils.enums;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Functions;

/**
 * types for whether to send warning when process ends;
 */
public enum WarningType {
    /**
     * 0 do not send warning;
     * 1 send if process success;
     * 2 send if process failed;
     * 3 send if process ends, whatever the result;
     */
    NONE(0, "none"),
    SUCCESS(1, "success"),
    FAILURE(2, "failure"),
    ALL(3, "all");

    WarningType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    private static final Map<String, WarningType> WARNING_TYPE_MAP =
        Arrays.stream(WarningType.values()).collect(toMap(WarningType::getDesc, Functions.identity()));

    public static WarningType of(String descp) {
        if (WARNING_TYPE_MAP.containsKey(descp)) {
            return WARNING_TYPE_MAP.get(descp);
        }
        return null;
    }
}
