package org.apache.dolphinscheduler.listener.enums;

import lombok.Getter;

import com.baomidou.mybatisplus.annotation.EnumValue;

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

    ListenerEventType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

}
