package org.apache.dolphinscheduler.listener.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wxn
 * @date 2023/7/28
 */
@Getter
public enum ListenerEventType {

    /**
     * 0 normal, 1 updating
     */
    MASTER_DOWN(0, "MASTER_DOWN"),
    MASTER_TIMEOUT(1, "MASTER_TIMEOUT"),
    WORKFLOW_ADDED(2, "WORKFLOW_ADDED"),
    WORKFLOW_UPDATE(3, "WORKFLOW_UPDATE"),
    WORKFLOW_REMOVED(4, "WORKFLOW_REMOVED"),
    WORKFLOW_START(5, "WORKFLOW_START"),
    WORKFLOW_END(6, "WORKFLOW_END"),
    WORKFLOW_FAIL(7, "WORKFLOW_FAIL"),
    TASK_ADDED(8, "TASK_ADDED"),
    TASK_UPDATE(9, "TASK_UPDATE"),
    TASK_REMOVED(10, "TASK_REMOVED"),
    TASK_START(11, "TASK_START"),
    TASK_END(12, "TASK_END"),
    TASK_FAIL(13, "TASK_FAIL");

    private static final Map<Integer, ListenerEventType> CODE_MAP = new HashMap<>();

    static {
        for (ListenerEventType listenerEventType : ListenerEventType.values()) {
            CODE_MAP.put(listenerEventType.getCode(), listenerEventType);
        }
    }

    @EnumValue
    private final int code;
    private final String descp;

    ListenerEventType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    public static ListenerEventType of(int code) {
        ListenerEventType listenerEventType = CODE_MAP.get(code);
        if (listenerEventType == null) {
            throw new IllegalArgumentException(String.format("The task execution status code: %s is invalidated",
                    code));
        }
        return listenerEventType;
    }

}
